package com.emircankirez.mobilehomework.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


// önce build.gradel (app) -> id 'kotlin-parcelize' ekle
@Parcelize
data class Announcement(val title: String, val imageUrl : String, val content: String, val lastDate : String, val userId : String) : Parcelable {
    constructor() : this("", "", "", "","")
}