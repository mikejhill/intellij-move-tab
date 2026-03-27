/*
 * MIT License
 *
 * Copyright (c) 2025 Michael Hill
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.mikejhill.intellij.movetab.settings

import com.intellij.testFramework.LightPlatformTestCase
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class MoveTabSettingsTest : LightPlatformTestCase() {

    override fun tearDown() {
        MoveTabSettings.wrapAround = true
        super.tearDown()
    }

    @Test
    fun wrap_around_default_is_true() {
        assertTrue("wrapAround should default to true", MoveTabSettings.wrapAround)
    }

    @Test
    fun wrap_around_can_be_set_to_false() {
        MoveTabSettings.wrapAround = false
        assertFalse("wrapAround should be false after setting to false", MoveTabSettings.wrapAround)
    }

    @Test
    fun wrap_around_can_be_set_to_true() {
        MoveTabSettings.wrapAround = false
        MoveTabSettings.wrapAround = true
        assertTrue("wrapAround should be true after setting back to true", MoveTabSettings.wrapAround)
    }

    @Test
    fun wrap_around_persists_across_reads() {
        MoveTabSettings.wrapAround = false
        assertFalse(MoveTabSettings.wrapAround)
        assertFalse(MoveTabSettings.wrapAround)
        MoveTabSettings.wrapAround = true
        assertTrue(MoveTabSettings.wrapAround)
        assertTrue(MoveTabSettings.wrapAround)
    }
}
