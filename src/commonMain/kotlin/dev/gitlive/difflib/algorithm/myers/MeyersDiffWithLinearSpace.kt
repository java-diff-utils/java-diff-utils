/*
 * Copyright 2021 java-diff-utils.
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
package dev.gitlive.difflib.algorithm.myers

import dev.gitlive.difflib.BiPredicate
import dev.gitlive.difflib.Consumer
import dev.gitlive.difflib.algorithm.Change
import dev.gitlive.difflib.algorithm.DiffAlgorithmFactory
import dev.gitlive.difflib.algorithm.DiffAlgorithmI
import dev.gitlive.difflib.algorithm.DiffAlgorithmListener
import dev.gitlive.difflib.patch.DeltaType

/**
 *
 * @author tw
 */
class MeyersDiffWithLinearSpace<T> : DiffAlgorithmI<T> {
    private val equalizer: BiPredicate<T, T>

    constructor() {
        equalizer = { a: T, b: T -> a!! == b }
    }

    constructor(equalizer: BiPredicate<T, T>) {
        this.equalizer = equalizer
    }

    override fun computeDiff(source: List<T>, target: List<T>, progress: DiffAlgorithmListener?): List<Change> {
        progress?.diffStart()
        val data = DiffData(source, target)
        val maxIdx = source.size + target.size
        buildScript(data, 0, source.size, 0, target.size) { idx: Int -> progress?.diffStep(idx, maxIdx) }
        progress?.diffEnd()
        return data.script
    }

    private fun buildScript(data: DiffData, start1: Int, end1: Int, start2: Int, end2: Int, progress: Consumer<Int>?) {
        progress?.invoke((end1 - start1) / 2 + (end2 - start2) / 2)
        val middle: Snake? = getMiddleSnake(data, start1, end1, start2, end2)
        if (middle == null || middle.start == end1 && middle.diag == end1 - end2 || middle.end == start1 && middle.diag == start1 - start2) {
            var i = start1
            var j = start2
            while (i < end1 || j < end2) {
                if (i < end1 && j < end2 && equalizer(data.source[i], data.target[j])) {
                    //script.append(new KeepCommand<>(left.charAt(i)));
                    ++i
                    ++j
                } else {
                    //TODO: compress these commands.
                    if (end1 - start1 > end2 - start2) {
                        //script.append(new DeleteCommand<>(left.charAt(i)));
                        if (data.script.isEmpty()
                            || data.script[data.script.size - 1].endOriginal != i || data.script[data.script.size - 1].deltaType != DeltaType.DELETE
                        ) {
                            data.script.add(Change(DeltaType.DELETE, i, i + 1, j, j))
                        } else {
                            data.script[data.script.size - 1] = data.script[data.script.size - 1].withEndOriginal(i + 1)
                        }
                        ++i
                    } else {
                        if (data.script.isEmpty()
                            || data.script[data.script.size - 1].endRevised != j || data.script[data.script.size - 1].deltaType != DeltaType.INSERT
                        ) {
                            data.script.add(Change(DeltaType.INSERT, i, i, j, j + 1))
                        } else {
                            data.script[data.script.size - 1] = data.script[data.script.size - 1].withEndRevised(j + 1)
                        }
                        ++j
                    }
                }
            }
        } else {
            buildScript(data, start1, middle.start, start2, middle.start - middle.diag, progress)
            buildScript(data, middle.end, end1, middle.end - middle.diag, end2, progress)
        }
    }

    private fun getMiddleSnake(data: DiffData, start1: Int, end1: Int, start2: Int, end2: Int): Snake? {
        val m = end1 - start1
        val n = end2 - start2
        if (m == 0 || n == 0) {
            return null
        }
        val delta = m - n
        val sum = n + m
        val offset = (if (sum % 2 == 0) sum else sum + 1) / 2
        data.vDown[1 + offset] = start1
        data.vUp[1 + offset] = end1 + 1
        for (d in 0..offset) {
            // Down
            run {
                var k = -d
                while (k <= d) {

                    // First step
                    val i = k + offset
                    if (k == -d || k != d && data.vDown[i - 1] < data.vDown[i + 1]) {
                        data.vDown[i] = data.vDown[i + 1]
                    } else {
                        data.vDown[i] = data.vDown[i - 1] + 1
                    }
                    var x = data.vDown[i]
                    var y = x - start1 + start2 - k
                    while (x < end1 && y < end2 && equalizer(data.source[x], data.target[y])) {
                        data.vDown[i] = ++x
                        ++y
                    }
                    // Second step
                    if (delta % 2 != 0 && delta - d <= k && k <= delta + d) {
                        if (data.vUp[i - delta] <= data.vDown[i]) {
                            return buildSnake(data, data.vUp[i - delta], k + start1 - start2, end1, end2)
                        }
                    }
                    k += 2
                }
            }

            // Up
            var k = delta - d
            while (k <= delta + d) {

                // First step
                val i = k + offset - delta
                if (k == delta - d
                    || k != delta + d && data.vUp[i + 1] <= data.vUp[i - 1]
                ) {
                    data.vUp[i] = data.vUp[i + 1] - 1
                } else {
                    data.vUp[i] = data.vUp[i - 1]
                }
                var x = data.vUp[i] - 1
                var y = x - start1 + start2 - k
                while (x >= start1 && y >= start2 && equalizer(data.source[x], data.target[y])) {
                    data.vUp[i] = x--
                    y--
                }
                // Second step
                if (delta % 2 == 0 && -d <= k && k <= d) {
                    if (data.vUp[i] <= data.vDown[i + delta]) {
                        return buildSnake(data, data.vUp[i], k + start1 - start2, end1, end2)
                    }
                }
                k += 2
            }
        }
        throw IllegalStateException("could not find a diff path")
    }

    private fun buildSnake(data: DiffData, start: Int, diag: Int, end1: Int, end2: Int): Snake {
        var end = start
        while (end - diag < end2 && end < end1 && equalizer(data.source[end], data.target[end - diag])) {
            ++end
        }
        return Snake(start, end, diag)
    }

    private inner class DiffData(val source: List<T>, val target: List<T>) {
        val size: Int
        val vDown: IntArray
        val vUp: IntArray
        val script: MutableList<Change>

        init {
            size = source.size + target.size + 2
            vDown = IntArray(size)
            vUp = IntArray(size)
            script = ArrayList()
        }
    }

    private inner class Snake(val start: Int, val end: Int, val diag: Int)
    companion object {
        /**
         * Factory to create instances of this specific diff algorithm.
         */
        @kotlin.jvm.JvmStatic
        fun factory(): DiffAlgorithmFactory {
            return object : DiffAlgorithmFactory {
                override fun <T> create(): DiffAlgorithmI<T> {
                    return MeyersDiffWithLinearSpace()
                }

                override fun <T> create(equalizer: BiPredicate<T, T>): DiffAlgorithmI<T> {
                    return MeyersDiffWithLinearSpace(equalizer)
                }
            }
        }
    }
}