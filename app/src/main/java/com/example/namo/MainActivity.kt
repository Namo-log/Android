package com.example.namo

import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.View
import android.view.animation.AnticipateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.example.namo.data.NamoDatabase
import com.example.namo.data.entity.home.Event
import com.example.namo.data.remote.event.DeleteEventResponse
import com.example.namo.data.remote.event.DeleteEventView
import com.example.namo.data.remote.event.EditEventResponse
import com.example.namo.data.remote.event.EventService
import com.example.namo.data.remote.event.EventView
import com.example.namo.data.remote.event.PostEventResponse
import com.example.namo.databinding.ActivityMainBinding
import com.example.namo.ui.bottom.home.schedule.ScheduleDialogBasicFragment.Companion.eventToEventForUpload
import com.example.namo.utils.NetworkManager
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException



private const val PERMISSION_REQUEST_CODE = 1001
private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 777
class MainActivity : AppCompatActivity(), EventView, DeleteEventView {

    private lateinit var binding : ActivityMainBinding
    lateinit var db : NamoDatabase
    lateinit var unUploaded : List<Event>


    companion object {
        const val PLACE_NAME_INTENT_KEY : String = "place_name"
        const val PLACE_X_INTENT_KEY : String = "place_x"
        const val PLACE_Y_INTENT_KEY : String = "place_y"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        db = NamoDatabase.getInstance(this)
        initNavigation()

        checkPermissions()
        checkNetworkUpload()
    }

    private fun checkNetworkUpload() {
        if (!NetworkManager.checkNetworkState(this)) {
            Log.d("MainActivity", "WIFI ERROR : Fail to upload")
            return
        }

        val thread = Thread {
            unUploaded = db.eventDao.getNotUploadedEvent()
        }
        thread.start()
        try {
            thread.join()
        } catch (e : InterruptedException) {
            e.printStackTrace()
        }

        // serverId가 없는데 delete인 것들은 안 올림, ID가 0이 아닌데 delete인 건 delete로 올림
        // serverId = 0 인데 state가 delete가 아닌 애들은 POST로 올림, serverId !=0 인 건 PATCH로 올림

        val eventService = EventService()
        eventService.setEventView(this)
        eventService.setDeleteEventView(this)

        for (i in unUploaded) {
            if (i.serverIdx == 0) {
                if (i.state == R.string.event_current_deleted.toString()) {
                    return
                }
                else {
                    //POST
                    eventService.postEvent(eventToEventForUpload(i))
                }
            }
            else {
                if (i.state == R.string.event_current_deleted.toString()) {
                    eventService.deleteEvent(i.serverIdx, i.eventId)
                }
                else {
                    eventService.editEvent(i.serverIdx, eventToEventForUpload(i))
                }
            }
        }
    }

    private fun checkPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            return
        }

        checkNotificationPermission(this)
    }

    private fun checkNotificationPermission(activity: Activity) {
        if (!NotificationManagerCompat.from(activity).areNotificationsEnabled()) {
            Toast.makeText(this, "일정 알림을 위해 설정에서 알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", activity.packageName, null)
            intent.data = uri
            activity.startActivity(intent)
        } else {
            return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                Toast.makeText(this, "알림 권한이 허용되었습니다.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "일정 알림을 위해 설정에서 알림 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                return
            } else {
                Toast.makeText(this, "일정 관리를 위해 설정에서 권한을 허용해주세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun initNavigation() {
        NavigationUI.setupWithNavController(binding.navBar, findNavController(R.id.nav_host))
        //바텀내비 백스택 수정 필요
    }

    override fun onPostEventSuccess(response: PostEventResponse, eventId: Long) {
        Log.d("MainActivity", "onPostEventSuccess")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread {
            db.eventDao.updateEventAfterUpload(eventId, 1, result.eventIdx, R.string.event_current_default.toString())
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onPostEventFailure(message: String, eventId: Long) {
        Log.d("MainActivity", "onPostEventFailure")
    }

    override fun onEditEventSuccess(response: EditEventResponse, eventId: Long) {
        Log.d("MainActivity", "onEditEventSuccess")

        val result = response.result

        //룸디비에 isUpload, serverId, state 업데이트하기
        var thread = Thread {
            db.eventDao.updateEventAfterUpload(eventId, 1, result.eventIdx, R.string.event_current_default.toString())
        }
        thread.start()
        try {
            thread.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onEditEventFailure(message: String, eventId: Long, serverId: Int) {
        Log.d("MainActivity", "onEditEventFailure")
    }

    override fun onDeleteEventSuccess(response: DeleteEventResponse, eventId: Long) {
        Log.d("MainActivity", "onDeleteEventSuccess")

        val result = response.result
        Toast.makeText(this, "$eventId 번 일정의 $result", Toast.LENGTH_SHORT).show()


        var deleteDB : Thread = Thread {
            db.eventDao.deleteEventById(eventId)
        }
        deleteDB.start()
        try {
            deleteDB.join()
        } catch ( e: InterruptedException) {
            e.printStackTrace()
        }
    }

    override fun onDeleteEventFailure(message: String) {
        Log.d("MainActivity", "onDeleteEventFailure")
    }
}