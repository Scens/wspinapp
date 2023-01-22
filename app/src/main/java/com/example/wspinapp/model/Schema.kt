package com.example.wspinapp.model

import kotlinx.serialization.Serializable


@Serializable
data class Hold(
    val X: Float,
    val Y: Float,
    val Size: Float,
    val Shape: String,
    val Angle: Float,

    val ID: UInt? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val DeletedAt: String? = null,
    val WallID: UInt? = null
)


@Serializable
data class Wall(
    val Holds: Array<Hold>,
    val ImageUrl: String? = null,

    val ID: UInt? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val DeletedAt: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wall

        if (!Holds.contentEquals(other.Holds)) return false
        if (ImageUrl != other.ImageUrl) return false
        if (ID != other.ID) return false
        if (CreatedAt != other.CreatedAt) return false
        if (UpdatedAt != other.UpdatedAt) return false
        if (DeletedAt != other.DeletedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Holds.contentHashCode()
        result = 31 * result + ImageUrl.hashCode()
        result = 31 * result + ID.hashCode()
        result = 31 * result + CreatedAt.hashCode()
        result = 31 * result + UpdatedAt.hashCode()
        result = 31 * result + DeletedAt.hashCode()
        return result
    }
}


data class AddWall(
    val Holds: Array<Hold>
)
