import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.namo.bottom.diary.GalleryDummy
import com.example.namo.databinding.ItemDiaryGalleryBinding

class DiaryGalleryRVAdapter(private val imgList:MutableList<GalleryDummy>):
    RecyclerView.Adapter<DiaryGalleryRVAdapter.ViewHolder>(){


    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val binding: ItemDiaryGalleryBinding = ItemDiaryGalleryBinding.inflate(LayoutInflater.from(viewGroup.context),viewGroup,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(imgList[position])
    }

    override fun getItemCount(): Int = imgList.size

    inner class ViewHolder(val binding: ItemDiaryGalleryBinding): RecyclerView.ViewHolder(binding.root){

        fun bind(item: GalleryDummy) {

            binding.galleryImgIv.setImageResource(item.img)

        }

    }
}