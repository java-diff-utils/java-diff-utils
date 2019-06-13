/*
 * Copyright 2009-2017 java-diff-utils.
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
package com.github.difflib.text

import com.github.difflib.BiPredicate
import com.github.difflib.DiffUtils
import com.github.difflib.Function
import com.github.difflib.algorithm.DiffException
import com.github.difflib.patch.AbstractDelta
import com.github.difflib.patch.ChangeDelta
import com.github.difflib.patch.Chunk
import com.github.difflib.patch.DeleteDelta
import com.github.difflib.patch.InsertDelta
import com.github.difflib.patch.Patch
import com.github.difflib.text.DiffRow.Tag
import kotlin.math.max

/**
 * This class for generating DiffRows for side-by-sidy view. You can customize the way of generating. For example, show
 * inline diffs on not, ignoring white spaces or/and blank lines and so on. All parameters for generating are optional.
 * If you do not specify them, the class will use the default values.
 *
 * These values are: showInlineDiffs = false; ignoreWhiteSpaces = true; ignoreBlankLines = true; ...
 *
 * For instantiating the DiffRowGenerator you should use the its builder. Like in example  `
 * DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 * ignoreWhiteSpaces(true).columnWidth(100).build();
` *
 */
class DiffRowGenerator private constructor(builder: Builder) {
    private val columnWidth: Int
    private val equalizer: BiPredicate<String, String>
    private val ignoreWhiteSpaces: Boolean
    private val inlineDiffSplitter: Function<String, MutableList<String>>
    private val mergeOriginalRevised: Boolean
    private val newTag: Function<Boolean, String>
    private val oldTag: Function<Boolean, String>
    private val reportLinesUnchanged: Boolean
    private val lineNormalizer: Function<String, String>

    private val showInlineDiffs: Boolean

    init {
        showInlineDiffs = builder.showInlineDiffs
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces
        oldTag = builder.oldTag
        newTag = builder.newTag
        columnWidth = builder.columnWidth
        mergeOriginalRevised = builder.mergeOriginalRevised
        inlineDiffSplitter = builder.inlineDiffSplitter
        equalizer = if (ignoreWhiteSpaces) IGNORE_WHITESPACE_EQUALIZER else DEFAULT_EQUALIZER
        reportLinesUnchanged = builder.reportLinesUnchanged
        lineNormalizer = builder.lineNormalizer
    }

    /**
     * Get the DiffRows describing the difference between original and revised texts using the given patch. Useful for
     * displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
//    @Throws(DiffException::class)
    fun generateDiffRows(original: List<String>, revised: List<String>): List<DiffRow> {
        return generateDiffRows(original, DiffUtils.diff(original, revised, equalizer))
    }

    /**
     * Generates the DiffRows describing the difference between original and revised texts using the given patch. Useful
     * for displaying side-by-side diff.
     *
     * @param original the original text
     * @param patch the given patch
     * @return the DiffRows between original and revised texts
     */
//    @Throws(DiffException::class)
    fun generateDiffRows(original: List<String>, patch: Patch<String>): List<DiffRow> {
        val diffRows = ArrayList<DiffRow>()
        var endPos = 0
        val deltaList = patch.deltas
        for (delta in deltaList) {
            val orig = delta.source
            val rev = delta.target

            for (line in original.subList(endPos, orig.position)) {
                diffRows.add(buildDiffRow(Tag.EQUAL, line, line))
            }

            // Inserted DiffRow
            if (delta is InsertDelta<*>) {
                endPos = orig.last() + 1
                for (line in rev.lines!!) {
                    diffRows.add(buildDiffRow(Tag.INSERT, "", line))
                }
                continue
            }

            // Deleted DiffRow
            if (delta is DeleteDelta<*>) {
                endPos = orig.last() + 1
                for (line in orig.lines!!) {
                    diffRows.add(buildDiffRow(Tag.DELETE, line, ""))
                }
                continue
            }

            if (showInlineDiffs) {
                diffRows.addAll(generateInlineDiffs(delta))
            } else {
                for (j in 0 until max(orig.size(), rev.size())) {
                    diffRows.add(buildDiffRow(Tag.CHANGE,
                            if (orig.lines!!.size > j) orig.lines!![j] else "",
                            if (rev.lines!!.size > j) rev.lines!![j] else ""))
                }
            }
            endPos = orig.last() + 1
        }

        // Copy the final matching chunk if any.
        for (line in original.subList(endPos, original.size)) {
            diffRows.add(buildDiffRow(Tag.EQUAL, line, line))
        }
        return diffRows
    }

    private fun buildDiffRow(type: Tag, orgline: String, newline: String): DiffRow {
        if (reportLinesUnchanged) {
            return DiffRow(type, orgline, newline)
        } else {
            var wrapOrg = preprocessLine(orgline)
            if (Tag.DELETE == type) {
                if (mergeOriginalRevised || showInlineDiffs) {
                    wrapOrg = oldTag(true) + wrapOrg + oldTag(false)
                }
            }
            var wrapNew = preprocessLine(newline)
            if (Tag.INSERT == type) {
                if (mergeOriginalRevised) {
                    wrapOrg = newTag(true) + wrapNew + newTag(false)
                } else if (showInlineDiffs) {
                    wrapNew = newTag(true) + wrapNew + newTag(false)
                }
            }
            return DiffRow(type, wrapOrg, wrapNew)
        }
    }

    private fun buildDiffRowWithoutNormalizing(type: Tag, orgline: String, newline: String): DiffRow {
        return DiffRow(type,
                StringUtils.wrapText(orgline, columnWidth),
                StringUtils.wrapText(newline, columnWidth))
    }

    internal fun normalizeLines(list: List<String>): List<String> {
        return list.map { lineNormalizer(it) }.toList()
    }

    /**
     * Add the inline diffs for given delta
     *
     * @param delta the given delta
     */
//    @Throws(DiffException::class)
    private fun generateInlineDiffs(delta: AbstractDelta<String>): List<DiffRow> {
        val orig = normalizeLines(delta.source.lines!!)
        val rev = normalizeLines(delta.target.lines!!)
        val origList: MutableList<String>
        val revList: MutableList<String>
        val joinedOrig = orig.joinToString("\n")
        val joinedRev = rev.joinToString("\n")

        origList = inlineDiffSplitter(joinedOrig)
        revList = inlineDiffSplitter(joinedRev)

        val inlineDeltas = DiffUtils.diff(origList, revList).deltas

        inlineDeltas.reverse()
        for (inlineDelta in inlineDeltas) {
            val inlineOrig = inlineDelta.source
            val inlineRev = inlineDelta.target
            if (inlineDelta is DeleteDelta<*>) {
                wrapInTag(origList, inlineOrig.position, inlineOrig
                        .position + inlineOrig.size(), oldTag)
            } else if (inlineDelta is InsertDelta<*>) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.position,
                            revList.subList(inlineRev.position, inlineRev.position + inlineRev.size()))
                    wrapInTag(origList, inlineOrig.position, inlineOrig.position + inlineRev.size(), newTag)
                } else {
                    wrapInTag(revList, inlineRev.position, inlineRev.position + inlineRev.size(), newTag)
                }
            } else if (inlineDelta is ChangeDelta<*>) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.position + inlineOrig.size(),
                            revList.subList(inlineRev.position, inlineRev.position + inlineRev.size()))
                    wrapInTag(origList, inlineOrig.position + inlineOrig.size(), inlineOrig.position + inlineOrig.size()
                            + inlineRev.size(), newTag)
                } else {
                    wrapInTag(revList, inlineRev.position, inlineRev.position + inlineRev.size(), newTag)
                }
                wrapInTag(origList, inlineOrig.position, inlineOrig
                        .position + inlineOrig.size(), oldTag)
            }
        }
        val origResult = StringBuilder()
        val revResult = StringBuilder()
        for (character in origList) {
            origResult.append(character)
        }
        for (character in revList) {
            revResult.append(character)
        }

        val original = origResult.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        val revised = revResult.toString().split("\n".toRegex()).dropLastWhile { it.isEmpty() }
        val diffRows = ArrayList<DiffRow>()
        for (j in 0 until max(original.size, revised.size)) {
            diffRows.add(buildDiffRowWithoutNormalizing(Tag.CHANGE,
                    if (original.size > j) original[j] else "",
                    if (revised.size > j) revised[j] else ""))
        }
        return diffRows
    }

    private fun preprocessLine(line: String): String {
        return if (columnWidth == 0) {
            lineNormalizer(line)
        } else {
            StringUtils.wrapText(lineNormalizer(line), columnWidth)
        }
    }

    /**
     * This class used for building the DiffRowGenerator.
     *
     * @author dmitry
     */
    class Builder internal constructor() {

        internal var showInlineDiffs = false
        internal var ignoreWhiteSpaces = false

        internal var oldTag = { f: Boolean -> if (f) "<span class=\"editOldInline\">" else "</span>" }
        internal var newTag = { f: Boolean -> if (f) "<span class=\"editNewInline\">" else "</span>" }

        internal var columnWidth = 0
        internal var mergeOriginalRevised = false
        internal var reportLinesUnchanged = false
        internal var inlineDiffSplitter = SPLITTER_BY_CHARACTER
        internal var lineNormalizer = LINE_NORMALIZER_FOR_HTML

        /**
         * Show inline diffs in generating diff rows or not.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured showInlineDiff parameter
         */
        fun showInlineDiffs(`val`: Boolean): Builder {
            showInlineDiffs = `val`
            return this
        }

        /**
         * Ignore white spaces in generating diff rows or not.
         *
         * @param val the value to set. Default: true.
         * @return builder with configured ignoreWhiteSpaces parameter
         */
        fun ignoreWhiteSpaces(`val`: Boolean): Builder {
            ignoreWhiteSpaces = `val`
            return this
        }

        /**
         * Give the originial old and new text lines to Diffrow without any additional processing and without any tags to
         * highlight the change.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured reportLinesUnWrapped parameter
         */
        fun reportLinesUnchanged(`val`: Boolean): Builder {
            reportLinesUnchanged = `val`
            return this
        }

        /**
         * Generator for Old-Text-Tags.
         *
         * @param generator the tag generator
         * @return builder with configured ignoreBlankLines parameter
         */
        fun oldTag(generator: Function<Boolean, String>): Builder {
            this.oldTag = generator
            return this
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        fun newTag(generator: Function<Boolean, String>): Builder {
            this.newTag = generator
            return this
        }

        /**
         * Set the column width of generated lines of original and revised texts.
         *
         * @param width the width to set. Making it < 0 doesn't have any sense. Default 80. @return builder with config
         * ured ignoreBlankLines parameter
         */
        fun columnWidth(width: Int): Builder {
            if (width >= 0) {
                columnWidth = width
            }
            return this
        }

        /**
         * Build the DiffRowGenerator. If some parameters is not set, the default values are used.
         *
         * @return the customized DiffRowGenerator
         */
        fun build(): DiffRowGenerator {
            return DiffRowGenerator(this)
        }

        /**
         * Merge the complete result within the original text. This makes sense for one line display.
         *
         * @param mergeOriginalRevised
         * @return
         */
        fun mergeOriginalRevised(mergeOriginalRevised: Boolean): Builder {
            this.mergeOriginalRevised = mergeOriginalRevised
            return this
        }

        /**
         * Per default each character is separatly processed. This variant introduces processing by word, which does not
         * deliver in word changes. Therefore the whole word will be tagged as changed:
         *
         * <pre>
         * false:    (aBa : aba) --  changed: a(B)a : a(b)a
         * true:     (aBa : aba) --  changed: (aBa) : (aba)
        </pre> *
         */
        fun inlineDiffByWord(inlineDiffByWord: Boolean): Builder {
            inlineDiffSplitter = if (inlineDiffByWord) SPLITTER_BY_WORD else SPLITTER_BY_CHARACTER
            return this
        }

        /**
         * To provide some customized splitting a splitter can be provided. Here someone could think about sentence splitter,
         * comma splitter or stuff like that.
         *
         * @param inlineDiffSplitter
         * @return
         */
        fun inlineDiffBySplitter(inlineDiffSplitter: Function<String, MutableList<String>>): Builder {
            this.inlineDiffSplitter = inlineDiffSplitter
            return this
        }

        /**
         * By default DiffRowGenerator preprocesses lines for HTML output. Tabs and special HTML characters like "&lt;"
         * are replaced with its encoded value. To change this you can provide a customized line normalizer here.
         *
         * @param lineNormalizer
         * @return
         */
        fun lineNormalizer(lineNormalizer: Function<String, String>): Builder {
            this.lineNormalizer = lineNormalizer
            return this
        }
    }

    companion object {

        val DEFAULT_EQUALIZER: BiPredicate<String, String> = { obj1, obj2 -> obj1 == obj2 }

        val IGNORE_WHITESPACE_EQUALIZER = { original: String, revised: String -> adjustWhitespace(original) == adjustWhitespace(revised) }

        val LINE_NORMALIZER_FOR_HTML: Function<String, String> = { StringUtils.normalize(it) }


        /**
         * Splitting lines by character to achieve char by char diff checking.
         */
        val SPLITTER_BY_CHARACTER: Function<String, MutableList<String>> = { line: String ->
            val list = ArrayList<String>(line.length)
            for (character in line) {
                list.add(character.toString())
            }
            list
        }
        val SPLIT_BY_WORD_PATTERN = Regex("\\s+|[,.\\[\\](){}/\\\\*+\\-#]")

        /**
         * Splitting lines by word to achieve word by word diff checking.
         */
        val SPLITTER_BY_WORD = { line: String -> splitStringPreserveDelimiter(line, SPLIT_BY_WORD_PATTERN) }
        val WHITESPACE_PATTERN = Regex("\\s+")

        fun create(): Builder {
            return Builder()
        }

        private fun adjustWhitespace(raw: String): String {
            return WHITESPACE_PATTERN.replace(raw.trim { it <= ' ' }, " ")
        }

        fun splitStringPreserveDelimiter(str: String?, SPLIT_PATTERN: Regex): MutableList<String> {
            val list = ArrayList<String>()
            if (str != null) {
                val results = SPLIT_PATTERN.findAll(str)
                var pos = 0
                for (result in results) {
                    if (pos < result.range.start) {
                        list.add(str.substring(pos, result.range.start))
                    }
                    list.add(result.groupValues.single())
                    pos = result.range.endInclusive
                }
                if (pos < str.length) {
                    list.add(str.substring(pos))
                }
            }
            return list
        }

        /**
         * Wrap the elements in the sequence with the given tag
         *
         * @param startPosition the position from which tag should start. The counting start from a zero.
         * @param endPosition the position before which tag should should be closed.
         * @param tagGenerator the tag generator
         */
        internal fun wrapInTag(sequence: MutableList<String>, startPosition: Int,
                               endPosition: Int, tagGenerator: Function<Boolean, String>) {
            var endPos = endPosition

            while (endPos >= startPosition) {

                //search position for end tag
                while (endPos > startPosition) {
                    if ("\n" != sequence[endPos - 1]) {
                        break
                    }
                    endPos--
                }

                if (endPos == startPosition) {
                    break
                }

                sequence.add(endPos, tagGenerator(false))
                endPos--

                //search position for end tag
                while (endPos > startPosition) {
                    if ("\n" == sequence[endPos - 1]) {
                        break
                    }
                    endPos--
                }

                sequence.add(endPos, tagGenerator(true))
                endPos--
            }

            //        sequence.add(endPosition, tagGenerator.apply(false));
            //        sequence.add(startPosition, tagGenerator.apply(true));
        }
    }
}
