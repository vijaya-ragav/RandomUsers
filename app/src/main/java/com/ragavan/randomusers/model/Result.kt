package com.ragavan.randomusers.model

import androidx.room.Embedded
import androidx.room.Entity

import androidx.room.PrimaryKey

@Entity
data class Results(
    @PrimaryKey(autoGenerate = true)
    var serial: Int,
    val cell: String,
    @Embedded
    val dob: Dob,
    val email: String,
    val gender: String,
    @Embedded
    val location: Location,
    @Embedded
    val name: Name,
    val nat: String,
    val phone: String,
    @Embedded
    val picture: Picture,
)