/*
 * MIT License
 *
 * Copyright (c) 2025 Michael Hill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the “Software”), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.mikejhill.intellij.movetab.actions

/**
 * Pure tab-reordering rules, independent of the IntelliJ UI so they can be unit tested exhaustively.
 */
object TabReorder {

    enum class Direction { LEFT, RIGHT, START, END }

    /**
     * Returns the index the tab at [index] should move to within a tab bar of [size] tabs.
     *
     * LEFT/RIGHT move one position and, at the edges, either wrap to the opposite end
     * ([wrapAround] = true) or stay put. START/END always move to the first/last position.
     */
    fun targetIndex(size: Int, index: Int, direction: Direction, wrapAround: Boolean): Int {
        require(size > 0) { "size must be positive, was $size" }
        require(index in 0 until size) { "index $index out of bounds for size $size" }
        val lastIndex = size - 1
        return when (direction) {
            Direction.LEFT -> when {
                index > 0 -> index - 1
                wrapAround -> lastIndex
                else -> index
            }
            Direction.RIGHT -> when {
                index < lastIndex -> index + 1
                wrapAround -> 0
                else -> index
            }
            Direction.START -> 0
            Direction.END -> lastIndex
        }
    }

    /** Returns a copy of [items] with the element at [from] moved to position [to]. */
    fun <T> move(items: List<T>, from: Int, to: Int): List<T> =
        items.toMutableList().apply { add(to, removeAt(from)) }
}
