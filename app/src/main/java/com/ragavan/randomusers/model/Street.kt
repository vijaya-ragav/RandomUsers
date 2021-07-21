package com.ragavan.randomusers.model

import com.google.gson.annotations.SerializedName

data class Street(
    @SerializedName("name")
    val streetName: String,
    val number: Int
)