package com.example.wspinapp.model

data class Hold(
    val X: Float,
    val Y: Float,
    val Size: Float,
    val Shape: String,
    val Angle: Float,

    val ID: UInt = 0u,
    val CreatedAt: String = "",
    val UpdatedAt: String = "",
    val DeletedAt: String = "",
    val WallId: UInt = 0u
)



data class Wall(
    val Holds: Array<Hold>,
    val ImageUrl: String,

    val ID: UInt = 0u,
    val CreatedAt: String = "",
    val UpdatedAt: String = "",
    val DeletedAt: String = "",
)
data class AddWall(
    val Holds: Array<Hold>
)
