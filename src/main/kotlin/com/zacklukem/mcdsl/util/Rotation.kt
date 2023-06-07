package com.zacklukem.mcdsl.util;

/**
 * Represents a rotation in minecraft
 */
class Rotation(val yaw: Float, val pitch: Float) {
    override fun toString(): String {
        return "$yaw $pitch"
    }
}
