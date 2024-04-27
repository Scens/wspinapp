package com.example.wspinapp.model

import android.os.Parcelable
import kotlinx.serialization.Serializable
import kotlinx.parcelize.Parcelize


@Serializable
@Parcelize
data class Hold(
//    Assume every image is in ratio 3:4
//    X should be a Float between 0 - 100
//    Y as well
//
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
) : Parcelable

enum class HoldType {
    WALL_HOLD, HOLD, START_HOLD, TOP_HOLD
}

enum class HoldShape(val str: String) {
    CIRCLE("circle"), UNKNOWN("unknown")
}


@Serializable
@Parcelize
data class Wall(
    val Holds: Array<Hold>,
    var ImageUrl: String? = null,
    var ImagePreviewUrl: String? = null,

    val ID: UInt? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val DeletedAt: String? = null,
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Wall

        if (!Holds.contentEquals(other.Holds)) return false
        if (ImageUrl != other.ImageUrl) return false
        if (ImagePreviewUrl != other.ImagePreviewUrl) return false
        if (ID != other.ID) return false
        if (CreatedAt != other.CreatedAt) return false
        if (UpdatedAt != other.UpdatedAt) return false
        if (DeletedAt != other.DeletedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Holds.contentHashCode()
        result = 31 * result + (ImageUrl?.hashCode() ?: 0)
        result = 31 * result + (ImagePreviewUrl?.hashCode() ?: 0)
        result = 31 * result + (ID?.hashCode() ?: 0)
        result = 31 * result + (CreatedAt?.hashCode() ?: 0)
        result = 31 * result + (UpdatedAt?.hashCode() ?: 0)
        result = 31 * result + (DeletedAt?.hashCode() ?: 0)
        return result
    }
}

@Serializable
data class Route(
    val Holds: Array<Hold>,
    val StartHolds: Array<Hold> = emptyArray(),
    val TopHold: Array<Hold> = emptyArray(),

    val WallID: UInt? = null,

    val ID: UInt? = null,
    val CreatedAt: String? = null,
    val UpdatedAt: String? = null,
    val DeletedAt: String? = null,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Route

        if (!Holds.contentEquals(other.Holds)) return false
        if (!StartHolds.contentEquals(other.StartHolds)) return false
        if (!TopHold.contentEquals(other.TopHold)) return false
        if (WallID != other.WallID) return false
        if (ID != other.ID) return false
        if (CreatedAt != other.CreatedAt) return false
        if (UpdatedAt != other.UpdatedAt) return false
        if (DeletedAt != other.DeletedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = Holds.contentHashCode()
        result = 31 * result + StartHolds.contentHashCode()
        result = 31 * result + TopHold.contentHashCode()
        result = 31 * result + (WallID?.hashCode() ?: 0)
        result = 31 * result + (ID?.hashCode() ?: 0)
        result = 31 * result + (CreatedAt?.hashCode() ?: 0)
        result = 31 * result + (UpdatedAt?.hashCode() ?: 0)
        result = 31 * result + (DeletedAt?.hashCode() ?: 0)
        return result
    }
}