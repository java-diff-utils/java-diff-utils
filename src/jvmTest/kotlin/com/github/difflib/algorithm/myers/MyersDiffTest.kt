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
package com.github.difflib.algorithm.myers

import com.github.difflib.algorithm.DiffAlgorithmListener
import com.github.difflib.algorithm.DiffException
import com.github.difflib.patch.Patch
import java.util.ArrayList
import java.util.Arrays
import org.junit.Assert.*
import org.junit.Test

/**
 *
 * @author tw
 */
class MyersDiffTest {

    @Test
    @Throws(DiffException::class)
    fun testDiffMyersExample1Forward() {
        val original = Arrays.asList("A", "B", "C", "A", "B", "B", "A")
        val revised = Arrays.asList("C", "B", "A", "B", "A", "C")
        val patch = Patch.generate(original, revised, MyersDiff<String>().computeDiff(original, revised, null))
        assertNotNull(patch)
        assertEquals(4, patch.deltas.size.toLong())
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString())
    }

    @Test
    @Throws(DiffException::class)
    fun testDiffMyersExample1ForwardWithListener() {
        val original = Arrays.asList("A", "B", "C", "A", "B", "B", "A")
        val revised = Arrays.asList("C", "B", "A", "B", "A", "C")

        val logdata = ArrayList<String>()
        val patch = Patch.generate(original, revised,
                MyersDiff<String>().computeDiff(original, revised, object : DiffAlgorithmListener {
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
        assertNotNull(patch)
        assertEquals(4, patch.deltas.size.toLong())
        assertEquals("Patch{deltas=[[DeleteDelta, position: 0, lines: [A, B]], [InsertDelta, position: 3, lines: [B]], [DeleteDelta, position: 5, lines: [B]], [InsertDelta, position: 7, lines: [C]]]}", patch.toString())
        println(logdata)
        assertEquals(8, logdata.size.toLong())
    }

}
