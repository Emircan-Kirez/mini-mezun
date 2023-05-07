package com.emircankirez.mobilehomework.model


data class Post(val postId : String, val caption : String, val photoUrl : String, val userId : String, val date : String) {
    constructor() : this("", "", "", "", "")
}