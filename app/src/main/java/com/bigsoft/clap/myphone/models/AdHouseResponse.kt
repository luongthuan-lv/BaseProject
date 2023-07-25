package com.bigsoft.clap.myphone.models

import com.bigsoft.clap.myphone.models.AdHouse
import com.google.gson.annotations.SerializedName

class AdHouseResponse {
    @SerializedName("ad_items")
    val data: List<AdHouse>? = null
}