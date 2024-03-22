package framework.animation

import kotlinx.serialization.Serializable

/*
* Copyright (C) 2007 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/**
 * PointF holds two float coordinates
 */
@Serializable
class PointF {
    var x = 0f
    var y = 0f

    constructor()
    constructor(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    constructor(p: Point) {
        x = p.x.toFloat()
        y = p.y.toFloat()
    }

    /**
     * Create a new PointF initialized with the values in the specified
     * PointF (which is left unmodified).
     *
     * @param p The point whose values are copied into the new
     * point.
     */
    constructor(p: PointF) {
        x = p.x
        y = p.y
    }

    /**
     * Set the point's x and y coordinates
     */
    operator fun set(x: Float, y: Float) {
        this.x = x
        this.y = y
    }

    /**
     * Set the point's x and y coordinates to the coordinates of p
     */
    fun set(p: PointF) {
        x = p.x
        y = p.y
    }

    fun negate() {
        x = -x
        y = -y
    }

    fun offset(dx: Float, dy: Float) {
        x += dx
        y += dy
    }

    /**
     * Returns true if the point's coordinates equal (x,y)
     */
    fun equals(x: Float, y: Float): Boolean {
        return this.x == x && this.y == y
    }

    /*override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null ) return false
        val pointF: PointF = o as PointF
        if (Float.compare(pointF.x, x) != 0) return false
        return Float.compare(pointF.y, y) == 0
    }

    override fun hashCode(): Int {
        var result = if (x != +0.0f) Float.floatToIntBits(x) else 0
        result = 31 * result + if (y != +0.0f) Float.floatToIntBits(y) else 0
        return result
    }*/

    override fun toString(): String {
        return "PointF($x, $y)"
    }
}
