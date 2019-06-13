/*
 * Copyright 2017 java-diff-utils.
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
package com.github.difflib.algorithm.jgit

import com.github.difflib.algorithm.DiffAlgorithmListener
import com.github.difflib.patch.Patch
import com.github.difflib.patch.PatchFailedException
import java.util.ArrayList
import java.util.Arrays
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.*
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test

/**
 *
 * @author toben
 */
class HistogramDiffTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    /**
     * Test of diff method, of class HistogramDiff.
     */
    @Test
    @Throws(PatchFailedException::class)
    fun testDiff() {
        val orgList = Arrays.asList("A", "B", "C", "A", "B", "B", "A")
        val revList = Arrays.asList("C", "B", "A", "B", "A", "C")
        val patch = Patch.generate(orgList, revList, HistogramDiff().computeDiff(orgList, revList, null))
        println(patch)
        assertNotNull(patch)
        assertEquals(3, patch.deltas.size.toLong())
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [DeleteDelta, position: 3, lines: [A, B]], [InsertDelta, position: 7, lines: [B, A, C]]]}", patch.toString())

        val patched = patch.applyTo(orgList)
        assertEquals(revList, patched)
    }

    @Test
    @Throws(PatchFailedException::class)
    fun testDiffWithListener() {
        val orgList = Arrays.asList("A", "B", "C", "A", "B", "B", "A")
        val revList = Arrays.asList("C", "B", "A", "B", "A", "C")

        val logdata = ArrayList<String>()
        val patch = Patch.generate(orgList, revList, HistogramDiff().computeDiff(orgList, revList, object : DiffAlgorithmListener {
            override fun diffStart() {
                logdata.add("start")
            }

            override fun diffStep(value: Int, max: Int) {
                logdata.add("$value - $max")
            }

            override fun diffEnd() {
                logdata.add("end")
            }
        }))
        println(patch)
        assertNotNull(patch)
        assertEquals(3, patch.deltas.size.toLong())
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [DeleteDelta, position: 3, lines: [A, B]], [InsertDelta, position: 7, lines: [B, A, C]]]}", patch.toString())

        val patched = patch.applyTo(orgList)
        assertEquals(revList, patched)

        println(logdata)
        assertEquals(17, logdata.size.toLong())
    }

    companion object {

        @BeforeClass
        fun setUpClass() {
        }

        @AfterClass
        fun tearDownClass() {
        }
    }
}
