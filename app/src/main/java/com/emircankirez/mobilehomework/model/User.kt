package com.emircankirez.mobilehomework.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class User (
    val name : String,
    val surname : String,
    val profilePhotoUrl : String,
    val entryYear : String,
    val graduationYear : String,
    val emailAddress : String,
    val educationLevel : String = "",
    val country : String = "",
    val city : String = "",
    val company : String = "",
    val phoneNumber : String = "") : Parcelable {

    constructor() : this("", "", "", "", "", "")
}