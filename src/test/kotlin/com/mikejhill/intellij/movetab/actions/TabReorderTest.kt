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

import com.mikejhill.intellij.movetab.actions.TabReorder.Direction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

/**
 * Plain JUnit tests (no IDE required) that exhaustively check the reordering rules against
 * an independent reference model for every tab-bar size, position, direction, and wrap setting.
 */
class TabReorderTest {

    private val maxSize = 8

    /** Independent statement of the expected behavior, written as a lookup rather than branches. */
    private fun expectedIndex(size: Int, index: Int, direction: Direction, wrap: Boolean): Int {
        val last = size - 1
        return when (direction) {
            Direction.START -> 0
            Direction.END -> last
            Direction.LEFT -> if (index == 0) (if (wrap) last else 0) else index - 1
            Direction.RIGHT -> if (index == last) (if (wrap) 0 else last) else index + 1
        }
    }

    private fun forAllCases(block: (size: Int, index: Int, direction: Direction, wrap: Boolean) -> Unit) {
        for (size in 1..maxSize) for (index in 0 until size) for (direction in Direction.entries) for (wrap in listOf(true, false)) {
            block(size, index, direction, wrap)
        }
    }

    @Test
    fun target_index_matches_reference_model_for_all_cases() {
        forAllCases { size, index, direction, wrap ->
            assertEquals(
                "size=$size index=$index direction=$direction wrap=$wrap",
                expectedIndex(size, index, direction, wrap),
                TabReorder.targetIndex(size, index, direction, wrap),
            )
        }
    }

    @Test
    fun move_preserves_all_tabs_and_places_selected_tab_at_target() {
        forAllCases { size, index, direction, wrap ->
            val tabs = (1..size).toList()
            val target = TabReorder.targetIndex(size, index, direction, wrap)
            val result = TabReorder.move(tabs, index, target)
            val context = "size=$size index=$index direction=$direction wrap=$wrap"
            assertEquals(context, tabs.sorted(), result.sorted())
            assertEquals(context, tabs[index], result[target])
            // Every other tab keeps its relative order.
            assertEquals(context, tabs - tabs[index], result - tabs[index])
        }
    }

    @Test
    fun left_then_right_is_identity_when_no_edge_is_crossed() {
        for (size in 2..maxSize) for (index in 1 until size) for (wrap in listOf(true, false)) {
            val left = TabReorder.targetIndex(size, index, Direction.LEFT, wrap)
            assertEquals(index, TabReorder.targetIndex(size, left, Direction.RIGHT, wrap))
        }
    }

    @Test
    fun repeated_moves_with_wrap_cycle_back_to_start() {
        for (size in 1..maxSize) for (direction in listOf(Direction.LEFT, Direction.RIGHT)) {
            var tabs = (1..size).toList()
            var selected = 0
            repeat(size) {
                val target = TabReorder.targetIndex(size, selected, direction, wrapAround = true)
                tabs = TabReorder.move(tabs, selected, target)
                selected = target
            }
            assertEquals("size=$size direction=$direction", (1..size).toList(), tabs)
        }
    }

    @Test
    fun move_returns_copy_and_leaves_input_untouched() {
        val tabs = listOf("a", "b", "c")
        assertEquals(listOf("b", "c", "a"), TabReorder.move(tabs, 0, 2))
        assertEquals(listOf("a", "b", "c"), tabs)
    }

    @Test
    fun rejects_invalid_arguments() {
        assertThrows(IllegalArgumentException::class.java) { TabReorder.targetIndex(0, 0, Direction.LEFT, true) }
        assertThrows(IllegalArgumentException::class.java) { TabReorder.targetIndex(3, -1, Direction.LEFT, true) }
        assertThrows(IllegalArgumentException::class.java) { TabReorder.targetIndex(3, 3, Direction.RIGHT, true) }
    }
}
