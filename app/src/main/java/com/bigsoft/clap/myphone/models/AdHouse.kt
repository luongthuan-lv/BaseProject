package com.bigsoft.clap.myphone.models

import com.google.gson.annotations.SerializedName

data class AdHouse(
    @SerializedName("app_icon_url")
    var appIconUrl: String = "",
    @SerializedName("app_name")
    var appName: String = "",
    @SerializedName("app_package")
    var appPackage: String = ""
)