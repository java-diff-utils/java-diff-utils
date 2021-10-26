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
package dev.gitlive.difflib.text

import dev.gitlive.difflib.BiFunction
import dev.gitlive.difflib.BiPredicate
import dev.gitlive.difflib.DiffUtils
import dev.gitlive.difflib.Function
import dev.gitlive.difflib.patch.*
import kotlin.math.max

/**
 * This class for generating DiffRows for side-by-sidy view. You can customize
 * the way of generating. For example, show inline diffs on not, ignoring white
 * spaces or/and blank lines and so on. All parameters for generating are
 * optional. If you do not specify them, the class will use the default values.
 *
 * These values are: showInlineDiffs = false; ignoreWhiteSpaces = true;
 * ignoreBlankLines = true; ...
 *
 * For instantiating the DiffRowGenerator you should use the its builder. Like
 * in example  `
 * DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 * ignoreWhiteSpaces(true).columnWidth(100).build();
` *
 */
class DiffRowGenerator private constructor(builder: Builder) {
    private val columnWidth: Int
    private var equalizer: BiPredicate<String, String>? = null
    private val ignoreWhiteSpaces: Boolean
    private val inlineDiffSplitter: Function<String, MutableList<String>>
    private val mergeOriginalRevised: Boolean
    private val newTag: BiFunction<DiffRow.Tag, Boolean, String>
    private val oldTag: BiFunction<DiffRow.Tag, Boolean, String>
    private val reportLinesUnchanged: Boolean
    private val lineNormalizer: Function<String, String>
    private val processDiffs: Function<String, String>?
    private val showInlineDiffs: Boolean
    private val replaceOriginalLinefeedInChangesWithSpaces: Boolean
    private val decompressDeltas: Boolean

    /**
     * Get the DiffRows describing the difference between original and revised
     * texts using the given patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
    fun generateDiffRows(original: List<String>, revised: List<String>): List<DiffRow> {
        return generateDiffRows(original, DiffUtils.diff(original, revised, equalizer))
    }

    /**
     * Generates the DiffRows describing the difference between original and
     * revised texts using the given patch. Useful for displaying side-by-side
     * diff.
     *
     * @param original the original text
     * @param patch the given patch
     * @return the DiffRows between original and revised texts
     */
    fun generateDiffRows(original: List<String>, patch: Patch<String>): List<DiffRow> {
        val diffRows: MutableList<DiffRow> = ArrayList()
        var endPos = 0
        val deltaList: List<AbstractDelta<String>> = patch.getDeltas()
        if (decompressDeltas) {
            for (originalDelta in deltaList) {
                for (delta in decompressDeltas(originalDelta)) {
                    endPos = transformDeltaIntoDiffRow(original, endPos, diffRows, delta)
                }
            }
        } else {
            for (delta in deltaList) {
                endPos = transformDeltaIntoDiffRow(original, endPos, diffRows, delta)
            }
        }

        // Copy the final matching chunk if any.
        for (line in original.subList(endPos, original.size)) {
            diffRows.add(buildDiffRow(DiffRow.Tag.EQUAL, line, line))
        }
        return diffRows
    }

    /**
     * Transforms one patch delta into a DiffRow object.
     */
    private fun transformDeltaIntoDiffRow(
        original: List<String>,
        endPos: Int,
        diffRows: MutableList<DiffRow>,
        delta: AbstractDelta<String>
    ): Int {
        val orig: Chunk<String> = delta.source
        val rev: Chunk<String> = delta.target
        for (line in original.subList(endPos, orig.position)) {
            diffRows.add(buildDiffRow(DiffRow.Tag.EQUAL, line, line))
        }
        when (delta.type) {
            DeltaType.INSERT -> for (line in rev.lines) {
                diffRows.add(buildDiffRow(DiffRow.Tag.INSERT, "", line))
            }
            DeltaType.DELETE -> for (line in orig.lines) {
                diffRows.add(buildDiffRow(DiffRow.Tag.DELETE, line, ""))
            }
            else -> if (showInlineDiffs) {
                diffRows.addAll(generateInlineDiffs(delta))
            } else {
                var j = 0
                while (j < max(orig.size(), rev.size())) {
                    diffRows.add(
                        buildDiffRow(
                            DiffRow.Tag.CHANGE,
                            if (orig.lines.size > j) orig.lines[j] else "",
                            if (rev.lines.size > j) rev.lines[j] else ""
                        )
                    )
                    j++
                }
            }
        }
        return orig.last() + 1
    }

    /**
     * Decompresses ChangeDeltas with different source and target size to a
     * ChangeDelta with same size and a following InsertDelta or DeleteDelta.
     * With this problems of building DiffRows getting smaller.
     *
     * @param delta
     */
    private fun decompressDeltas(delta: AbstractDelta<String>): List<AbstractDelta<String>> {
        if (delta.type == DeltaType.CHANGE && delta.source.size() != delta.target.size()) {
            val deltas: MutableList<AbstractDelta<String>> = ArrayList()
            //System.out.println("decompress this " + delta);
            val minSize = delta.source.size().coerceAtMost(delta.target.size())
            val orig: Chunk<String> = delta.source
            val rev: Chunk<String> = delta.target
            deltas.add(
                ChangeDelta(
                    Chunk(orig.position, orig.lines.subList(0, minSize)),
                    Chunk(rev.position, rev.lines.subList(0, minSize))
                )
            )
            if (orig.lines.size < rev.lines.size) {
                deltas.add(
                    InsertDelta(
                        Chunk(orig.position + minSize, emptyList()),
                        Chunk(rev.position + minSize, rev.lines.subList(minSize, rev.lines.size))
                    )
                )
            } else {
                deltas.add(
                    DeleteDelta(
                        Chunk(orig.position + minSize, orig.lines.subList(minSize, orig.lines.size)),
                        Chunk(rev.position + minSize, emptyList())
                    )
                )
            }
            return deltas
        }
        return listOf(delta)
    }

    private fun buildDiffRow(type: DiffRow.Tag, orgline: String, newline: String): DiffRow {
        return if (reportLinesUnchanged) {
            DiffRow(type, orgline, newline)
        } else {
            var wrapOrg = preprocessLine(orgline)
            if (DiffRow.Tag.DELETE == type) {
                if (mergeOriginalRevised || showInlineDiffs) {
                    wrapOrg = oldTag(type, true) + wrapOrg + oldTag(type, false)
                }
            }
            var wrapNew = preprocessLine(newline)
            if (DiffRow.Tag.INSERT == type) {
                if (mergeOriginalRevised) {
                    wrapOrg = newTag(type, true) + wrapNew + newTag(type, false)
                } else if (showInlineDiffs) {
                    wrapNew = newTag(type, true) + wrapNew + newTag(type, false)
                }
            }
            DiffRow(type, wrapOrg, wrapNew)
        }
    }

    private fun buildDiffRowWithoutNormalizing(type: DiffRow.Tag, orgline: String, newline: String): DiffRow {
        return DiffRow(
            type,
            StringUtils.wrapText(orgline, columnWidth),
            StringUtils.wrapText(newline, columnWidth)
        )
    }

    fun normalizeLines(list: List<String>): List<String> {
        return if (reportLinesUnchanged) list else list.asSequence()
            .map { t: String -> lineNormalizer(t) }
            .toList()
    }

    /**
     * Add the inline diffs for given delta
     *
     * @param delta the given delta
     */
    private fun generateInlineDiffs(delta: AbstractDelta<String>): List<DiffRow> {
        val orig = normalizeLines(delta.source.lines)
        val rev = normalizeLines(delta.target.lines)
        val origList: MutableList<String>
        val revList: MutableList<String>
        val joinedOrig = orig.joinToString("\n")
        val joinedRev = rev.joinToString("\n")
        origList = inlineDiffSplitter(joinedOrig)
        revList = inlineDiffSplitter(joinedRev)
        val inlineDeltas: MutableList<AbstractDelta<String>> = DiffUtils.diff(origList, revList, equalizer).getDeltas()
        inlineDeltas.reverse()
        for (inlineDelta in inlineDeltas) {
            val inlineOrig: Chunk<String> = inlineDelta.source
            val inlineRev: Chunk<String> = inlineDelta.target
            if (inlineDelta.type == DeltaType.DELETE) {
                wrapInTag(
                    origList,
                    inlineOrig.position,
                    inlineOrig
                        .position
                            + inlineOrig.size(),
                    DiffRow.Tag.DELETE,
                    oldTag,
                    processDiffs,
                    replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised
                )
            } else if (inlineDelta.type == DeltaType.INSERT) {
                if (mergeOriginalRevised) {
                    origList.addAll(
                        inlineOrig.position,
                        revList.subList(
                            inlineRev.position,
                            inlineRev.position + inlineRev.size()
                        )
                    )
                    wrapInTag(
                        origList, inlineOrig.position,
                        inlineOrig.position + inlineRev.size(),
                        DiffRow.Tag.INSERT, newTag, processDiffs, false
                    )
                } else {
                    wrapInTag(
                        revList, inlineRev.position,
                        inlineRev.position + inlineRev.size(),
                        DiffRow.Tag.INSERT, newTag, processDiffs, false
                    )
                }
            } else if (inlineDelta.type == DeltaType.CHANGE) {
                if (mergeOriginalRevised) {
                    origList.addAll(
                        inlineOrig.position + inlineOrig.size(),
                        revList.subList(
                            inlineRev.position,
                            inlineRev.position + inlineRev.size()
                        )
                    )
                    wrapInTag(
                        origList, inlineOrig.position + inlineOrig.size(),
                        inlineOrig.position + inlineOrig.size() + inlineRev.size(),
                        DiffRow.Tag.CHANGE, newTag, processDiffs, false
                    )
                } else {
                    wrapInTag(
                        revList, inlineRev.position,
                        inlineRev.position + inlineRev.size(),
                        DiffRow.Tag.CHANGE, newTag, processDiffs, false
                    )
                }
                wrapInTag(
                    origList,
                    inlineOrig.position,
                    inlineOrig.position + inlineOrig.size(),
                    DiffRow.Tag.CHANGE,
                    oldTag,
                    processDiffs,
                    replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised
                )
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
        val original = origResult.toString().trimEnd('\n').lines()
        val revised = revResult.toString().trimEnd('\n').lines()
        val diffRows: MutableList<DiffRow> = ArrayList()
        for (j in 0 until max(original.size, revised.size)) {
            diffRows.add(
                buildDiffRowWithoutNormalizing(
                    DiffRow.Tag.CHANGE,
                    if (original.size > j) original[j] else "",
                    if (revised.size > j) revised[j] else ""
                )
            )
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
    class Builder {
        var showInlineDiffs = false
        var ignoreWhiteSpaces = false
        var decompressDeltas = true
        var oldTag =
            { _: DiffRow.Tag, f: Boolean -> if (f) "<span class=\"editOldInline\">" else "</span>" }
        var newTag =
            { _: DiffRow.Tag, f: Boolean -> if (f) "<span class=\"editNewInline\">" else "</span>" }
        var columnWidth = 0
        var mergeOriginalRevised = false
        var reportLinesUnchanged = false
        var inlineDiffSplitter = SPLITTER_BY_CHARACTER
        var lineNormalizer = LINE_NORMALIZER_FOR_HTML
        var processDiffs: Function<String, String>? = null
        var equalizer: BiPredicate<String, String>? = null
        var replaceOriginalLinefeedInChangesWithSpaces = false

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
         * Give the originial old and new text lines to Diffrow without any
         * additional processing and without any tags to highlight the change.
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
        fun oldTag(generator: BiFunction<DiffRow.Tag, Boolean, String>): Builder {
            oldTag = generator
            return this
        }

        /**
         * Generator for Old-Text-Tags.
         *
         * @param generator the tag generator
         * @return builder with configured ignoreBlankLines parameter
         */
        fun oldTag(generator: Function<Boolean?, String>): Builder {
            oldTag = { _: DiffRow.Tag?, f: Boolean? -> generator(f) }
            return this
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        fun newTag(generator: BiFunction<DiffRow.Tag, Boolean, String>): Builder {
            newTag = generator
            return this
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        fun newTag(generator: Function<Boolean?, String>): Builder {
            newTag = { _: DiffRow.Tag?, f: Boolean? -> generator(f) }
            return this
        }

        /**
         * Processor for diffed text parts. Here e.g. whitecharacters could be
         * replaced by something visible.
         *
         * @param processDiffs
         * @return
         */
        fun processDiffs(processDiffs: Function<String, String>?): Builder {
            this.processDiffs = processDiffs
            return this
        }

        /**
         * Set the column width of generated lines of original and revised
         * texts.
         *
         * @param width the width to set. Making it &lt; 0 doesn't make any
         * sense. Default 80.
         * @return builder with config of column width
         */
        fun columnWidth(width: Int): Builder {
            if (width >= 0) {
                columnWidth = width
            }
            return this
        }

        /**
         * Build the DiffRowGenerator. If some parameters is not set, the
         * default values are used.
         *
         * @return the customized DiffRowGenerator
         */
        fun build(): DiffRowGenerator {
            return DiffRowGenerator(this)
        }

        /**
         * Merge the complete result within the original text. This makes sense
         * for one line display.
         *
         * @param mergeOriginalRevised
         * @return
         */
        fun mergeOriginalRevised(mergeOriginalRevised: Boolean): Builder {
            this.mergeOriginalRevised = mergeOriginalRevised
            return this
        }

        /**
         * Deltas could be in a state, that would produce some unreasonable
         * results within an inline diff. So the deltas are decompressed into
         * smaller parts and rebuild. But this could result in more differences.
         *
         * @param decompressDeltas
         * @return
         */
        fun decompressDeltas(decompressDeltas: Boolean): Builder {
            this.decompressDeltas = decompressDeltas
            return this
        }

        /**
         * Per default each character is separatly processed. This variant
         * introduces processing by word, which does not deliver in word
         * changes. Therefore the whole word will be tagged as changed:
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
         * To provide some customized splitting a splitter can be provided. Here
         * someone could think about sentence splitter, comma splitter or stuff
         * like that.
         *
         * @param inlineDiffSplitter
         * @return
         */
        fun inlineDiffBySplitter(inlineDiffSplitter: Function<String, MutableList<String>>): Builder {
            this.inlineDiffSplitter = inlineDiffSplitter
            return this
        }

        /**
         * By default DiffRowGenerator preprocesses lines for HTML output. Tabs
         * and special HTML characters like "&lt;" are replaced with its encoded
         * value. To change this you can provide a customized line normalizer
         * here.
         *
         * @param lineNormalizer
         * @return
         */
        fun lineNormalizer(lineNormalizer: Function<String, String>): Builder {
            this.lineNormalizer = lineNormalizer
            return this
        }

        /**
         * Provide an equalizer for diff processing.
         *
         * @param equalizer equalizer for diff processing.
         * @return builder with configured equalizer parameter
         */
        fun equalizer(equalizer: BiPredicate<String, String>?): Builder {
            this.equalizer = equalizer
            return this
        }

        /**
         * Sometimes it happens that a change contains multiple lines. If there
         * is no correspondence in old and new. To keep the merged line more
         * readable the linefeeds could be replaced by spaces.
         *
         * @param replace
         * @return
         */
        fun replaceOriginalLinefeedInChangesWithSpaces(replace: Boolean): Builder {
            replaceOriginalLinefeedInChangesWithSpaces = replace
            return this
        }
    }

    companion object {
        val DEFAULT_EQUALIZER = { obj1: String, obj2: String -> obj1 == obj2 }
        val IGNORE_WHITESPACE_EQUALIZER = { original: String, revised: String -> adjustWhitespace(original) == adjustWhitespace(revised) }
        val LINE_NORMALIZER_FOR_HTML = { obj: String -> StringUtils.normalize(obj) }

        /**
         * Splitting lines by character to achieve char by char diff checking.
         */
        val SPLITTER_BY_CHARACTER = { line: String ->
            val list: MutableList<String> = ArrayList<String>(line.length)
            for (character in line) {
                list.add(character.toString())
            }
            list
        }
        @kotlin.jvm.JvmField
        val SPLIT_BY_WORD_PATTERN = Regex("\\s+|[,.\\[\\](){}/\\\\*+\\-#]")

        /**
         * Splitting lines by word to achieve word by word diff checking.
         */
        val SPLITTER_BY_WORD = { line: String ->
            splitStringPreserveDelimiter(
                line,
                SPLIT_BY_WORD_PATTERN
            )
        }
        val WHITESPACE_PATTERN = Regex("\\s+")
        @kotlin.jvm.JvmStatic
        fun create(): Builder {
            return Builder()
        }

        private fun adjustWhitespace(raw: String): String {
            return WHITESPACE_PATTERN.replace(raw.trim { it <= ' ' }, " ")
        }

        @kotlin.jvm.JvmStatic
        fun splitStringPreserveDelimiter(str: String?, SPLIT_PATTERN: Regex): MutableList<String> {
            val list = mutableListOf<String>()
            if (str != null) {
                val results = SPLIT_PATTERN.findAll(str)
                var pos = 0
                for (result in results) {
                    if (pos < result.range.first) {
                        list.add(str.substring(pos, result.range.first))
                    }
                    list.add(result.value)
                    pos = result.range.last + 1
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
         * @param startPosition the position from which tag should start. The
         * counting start from a zero.
         * @param endPosition the position before which tag should should be closed.
         * @param tagGenerator the tag generator
         */
        fun wrapInTag(
            sequence: MutableList<String>, startPosition: Int,
            endPosition: Int, tag: DiffRow.Tag, tagGenerator: BiFunction<DiffRow.Tag, Boolean, String>,
            processDiffs: Function<String, String>?, replaceLinefeedWithSpace: Boolean
        ) {
            var endPos = endPosition
            while (endPos >= startPosition) {

                //search position for end tag
                while (endPos > startPosition) {
                    if ("\n" != sequence[endPos - 1]) {
                        break
                    } else if (replaceLinefeedWithSpace) {
                        sequence[endPos - 1] = " "
                        break
                    }
                    endPos--
                }
                if (endPos == startPosition) {
                    break
                }
                sequence.add(endPos, tagGenerator(tag, false))
                if (processDiffs != null) {
                    sequence[endPos - 1] = processDiffs(sequence[endPos - 1])
                }
                endPos--

                //search position for end tag
                while (endPos > startPosition) {
                    if ("\n" == sequence[endPos - 1]) {
                        if (replaceLinefeedWithSpace) {
                            sequence[endPos - 1] = " "
                        } else {
                            break
                        }
                    }
                    if (processDiffs != null) {
                        sequence[endPos - 1] = processDiffs(sequence[endPos - 1])
                    }
                    endPos--
                }
                sequence.add(endPos, tagGenerator(tag, true))
                endPos--
            }
        }
    }

    init {
        showInlineDiffs = builder.showInlineDiffs
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces
        oldTag = builder.oldTag
        newTag = builder.newTag
        columnWidth = builder.columnWidth
        mergeOriginalRevised = builder.mergeOriginalRevised
        inlineDiffSplitter = builder.inlineDiffSplitter
        decompressDeltas = builder.decompressDeltas
        equalizer = if (builder.equalizer != null) {
            builder.equalizer
        } else {
            if (ignoreWhiteSpaces) IGNORE_WHITESPACE_EQUALIZER else DEFAULT_EQUALIZER
        }
        reportLinesUnchanged = builder.reportLinesUnchanged
        lineNormalizer = builder.lineNormalizer
        processDiffs = builder.processDiffs
        replaceOriginalLinefeedInChangesWithSpaces = builder.replaceOriginalLinefeedInChangesWithSpaces
    }
}