package com.trendhive.arsample.domain.model

import com.trendhive.arsample.domain.base.BaseModel

data class Quaternion(
    val x: Float = 0f,
    val y: Float = 0f,
    val z: Float = 0f,
    val w: Float = 1f
) : BaseModel {
    fun normalize(): Quaternion {
        val magnitude = kotlin.math.sqrt(x * x + y * y + z * z + w * w)
        return if (magnitude > 0f) {
            Quaternion(x / magnitude, y / magnitude, z / magnitude, w / magnitude)
        } else {
            IDENTITY
        }
    }

    fun conjugate(): Quaternion = Quaternion(-x, -y, -z, w)

    operator fun times(other: Quaternion): Quaternion {
        return Quaternion(
            w * other.x + x * other.w + y * other.z - z * other.y,
            w * other.y - x * other.z + y * other.w + z * other.x,
            w * other.z + x * other.y - y * other.x + z * other.w,
            w * other.w - x * other.x - y * other.y - z * other.z
        )
    }

    companion object {
        val IDENTITY = Quaternion(0f, 0f, 0f, 1f)

        fun fromEulerAngles(pitch: Float, yaw: Float, roll: Float): Quaternion {
            val cy = kotlin.math.cos(yaw * 0.5f)
            val sy = kotlin.math.sin(yaw * 0.5f)
            val cp = kotlin.math.cos(pitch * 0.5f)
            val sp = kotlin.math.sin(pitch * 0.5f)
            val cr = kotlin.math.cos(roll * 0.5f)
            val sr = kotlin.math.sin(roll * 0.5f)

            return Quaternion(
                sr * cp * cy - cr * sp * sy,
                cr * sp * cy + sr * cp * sy,
                cr * cy * sy - sr * sp * cy,
                cr * cp * cy + sr * sp * sy
            )
        }
    }
}
