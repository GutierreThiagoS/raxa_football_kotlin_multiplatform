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
 * Point holds two integer coordinates
 */

@Serializable
class Point  {
    var x = 0
    var y = 0

    constructor()
    constructor(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    constructor(src: Point) {
        set(src)
    }

    /**
     * Set the point's x and y coordinates
     */
    operator fun set(x: Int, y: Int) {
        this.x = x
        this.y = y
    }

    /**
     * Sets the point's from `src`'s coordinates
     * @hide
     */
    fun set(src: Point) {
        x = src.x
        y = src.y
    }

    /**
     * Negate the point's coordinates
     */
    fun negate() {
        x = -x
        y = -y
    }

    /**
     * Offset the point's coordinates by dx, dy
     */
    fun offset(dx: Int, dy: Int) {
        x += dx
        y += dy
    }

    /**
     * Returns true if the point's coordinates equal (x,y)
     */
    fun equals(x: Int, y: Int): Boolean {
        return this.x == x && this.y == y
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null) return false
        val point: Point = o as Point
        if (x != point.x) return false
        return if (y != point.y) false else true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }

    override fun toString(): String {
        return "Point($x, $y)"
    }

    /**
     * @return Returns a [String] that represents this point which can be parsed with
     * [.unflattenFromString].
     * @hide
     */
    fun flattenToString(): String {
        return x.toString() + "x" + y
    }

}
