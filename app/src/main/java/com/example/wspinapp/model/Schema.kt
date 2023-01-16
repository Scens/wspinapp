package com.example.wspinapp.model

data class Hold(
    val ID: UInt,
    val CreatedAt: String,
    val UpdatedAt: String,
    val DeletedAt: String,
    val X: Int,
    val Y: Int
)

data class Wall(
    val ID: UInt,
    val CreatedAt: String,
    val UpdatedAt: String,
    val DeletedAt: String,
    val Holds: Array<Hold>,
    val Image: String)


data class AddHold(
    val x: Float,
    val y: Float,
    val radius: Float
)

data class AddWall(
    val Holds: Array<AddHold>
)
