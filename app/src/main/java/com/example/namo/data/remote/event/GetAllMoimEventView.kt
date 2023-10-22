package com.example.namo.data.remote.event

interface GetAllMoimEventView {
    fun onGetAllMoimEventSuccess (response : GetMonthEventResponse)
    fun onGetAllMoimEventFailure (message : String)
}