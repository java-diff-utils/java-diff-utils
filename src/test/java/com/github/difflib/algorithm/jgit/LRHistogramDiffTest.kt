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

import com.github.difflib.DiffUtilsTest.readStringListFromInputStream
import com.github.difflib.TestConstants
import com.github.difflib.algorithm.DiffAlgorithmListener
import com.github.difflib.patch.Patch
import com.github.difflib.patch.PatchFailedException
import java.io.IOException
import java.util.ArrayList
import java.util.zip.ZipFile
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
class LRHistogramDiffTest {

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    @Throws(IOException::class, PatchFailedException::class)
    fun testPossibleDiffHangOnLargeDatasetDnaumenkoIssue26() {
        val zip = ZipFile(TestConstants.MOCK_FOLDER + "/large_dataset1.zip")
        val original = readStringListFromInputStream(zip.getInputStream(zip.getEntry("ta")))
        val revised = readStringListFromInputStream(zip.getInputStream(zip.getEntry("tb")))

        val logdata = ArrayList<String>()
        val patch = Patch.generate(original, revised, HistogramDiff().computeDiff(original, revised, object : DiffAlgorithmListener {
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

        assertEquals(34, patch.deltas.size.toLong())

        val created = patch.applyTo(original)
        assertArrayEquals(revised.toTypedArray(), created.toTypedArray())

        assertEquals(50, logdata.size.toLong())
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
