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
package com.github.difflib.text;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import com.github.difflib.patch.DeleteDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.patch.InsertDelta;
import com.github.difflib.patch.Patch;
import com.github.difflib.text.DiffRow.Tag;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;

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
 * in example  <code>
 *    DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 *      ignoreWhiteSpaces(true).columnWidth(100).build();
 * </code>
 */
public final class DiffRowGenerator {

    public static final BiPredicate<String, String> DEFAULT_EQUALIZER = Object::equals;

    public static final BiPredicate<String, String> IGNORE_WHITESPACE_EQUALIZER = (original, revised)
            -> adjustWhitespace(original).equals(adjustWhitespace(revised));

    public static final Function<String, String> LINE_NORMALIZER_FOR_HTML = StringUtils::normalize;

    /**
     * Splitting lines by character to achieve char by char diff checking.
     */
    public static final Function<String, List<String>> SPLITTER_BY_CHARACTER = line -> {
        List<String> list = new ArrayList<>(line.length());
        for (Character character : line.toCharArray()) {
            list.add(character.toString());
        }
        return list;
    };

    public static final Pattern SPLIT_BY_WORD_PATTERN = Pattern.compile("\\s+|[,.\\[\\](){}/\\\\*+\\-#]");

    /**
     * Splitting lines by word to achieve word by word diff checking.
     */
    public static final Function<String, List<String>> SPLITTER_BY_WORD = line -> splitStringPreserveDelimiter(line, SPLIT_BY_WORD_PATTERN);
    public static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    public static Builder create() {
        return new Builder();
    }

    private static String adjustWhitespace(String raw) {
        return WHITESPACE_PATTERN.matcher(raw.trim()).replaceAll(" ");
    }

    protected final static List<String> splitStringPreserveDelimiter(String str, Pattern SPLIT_PATTERN) {
        List<String> list = new ArrayList<>();
        if (str != null) {
            Matcher matcher = SPLIT_PATTERN.matcher(str);
            int pos = 0;
            while (matcher.find()) {
                if (pos < matcher.start()) {
                    list.add(str.substring(pos, matcher.start()));
                }
                list.add(matcher.group());
                pos = matcher.end();
            }
            if (pos < str.length()) {
                list.add(str.substring(pos));
            }
        }
        return list;
    }

    /**
     * Wrap the elements in the sequence with the given tag
     *
     * @param startPosition the position from which tag should start. The
     * counting start from a zero.
     * @param endPosition the position before which tag should should be closed.
     * @param tagGenerator the tag generator
     */
    static void wrapInTag(List<String> sequence, int startPosition,
            int endPosition, Tag tag, BiFunction<Tag, Boolean, String> tagGenerator,
            Function<String, String> processDiffs, boolean replaceLinefeedWithSpace) {
        int endPos = endPosition;

        while (endPos >= startPosition) {

            //search position for end tag
            while (endPos > startPosition) {
                if (!"\n".equals(sequence.get(endPos - 1))) {
                    break;
                } else if (replaceLinefeedWithSpace) {
                    sequence.set(endPos - 1, " ");
                    break;
                }
                endPos--;
            }

            if (endPos == startPosition) {
                break;
            }

            sequence.add(endPos, tagGenerator.apply(tag, false));
            if (processDiffs != null) {
                sequence.set(endPos - 1,
                        processDiffs.apply(sequence.get(endPos - 1)));
            }
            endPos--;

            //search position for end tag
            while (endPos > startPosition) {
                if ("\n".equals(sequence.get(endPos - 1))) {
                    if (replaceLinefeedWithSpace) {
                        sequence.set(endPos - 1, " ");
                    } else {
                        break;
                    }
                }
                if (processDiffs != null) {
                    sequence.set(endPos - 1,
                            processDiffs.apply(sequence.get(endPos - 1)));
                }
                endPos--;
            }

            sequence.add(endPos, tagGenerator.apply(tag, true));
            endPos--;
        }
    }

    private final int columnWidth;
    private final BiPredicate<String, String> equalizer;
    private final boolean ignoreWhiteSpaces;
    private final Function<String, List<String>> inlineDiffSplitter;
    private final boolean mergeOriginalRevised;
    private final BiFunction<Tag, Boolean, String> newTag;
    private final BiFunction<Tag, Boolean, String> oldTag;
    private final boolean reportLinesUnchanged;
    private final Function<String, String> lineNormalizer;
    private final Function<String, String> processDiffs;

    private final boolean showInlineDiffs;
    private final boolean replaceOriginalLinefeedInChangesWithSpaces;

    private DiffRowGenerator(Builder builder) {
        showInlineDiffs = builder.showInlineDiffs;
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces;
        oldTag = builder.oldTag;
        newTag = builder.newTag;
        columnWidth = builder.columnWidth;
        mergeOriginalRevised = builder.mergeOriginalRevised;
        inlineDiffSplitter = builder.inlineDiffSplitter;

        if (builder.equalizer != null) {
            equalizer = builder.equalizer;
        } else {
            equalizer = ignoreWhiteSpaces ? IGNORE_WHITESPACE_EQUALIZER : DEFAULT_EQUALIZER;
        }

        reportLinesUnchanged = builder.reportLinesUnchanged;
        lineNormalizer = builder.lineNormalizer;
        processDiffs = builder.processDiffs;

        replaceOriginalLinefeedInChangesWithSpaces = builder.replaceOriginalLinefeedInChangesWithSpaces;

        Objects.requireNonNull(inlineDiffSplitter);
        Objects.requireNonNull(lineNormalizer);
    }

    /**
     * Get the DiffRows describing the difference between original and revised
     * texts using the given patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(List<String> original, List<String> revised) {
        return generateDiffRows(original, DiffUtils.diff(original, revised, equalizer));
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
    public List<DiffRow> generateDiffRows(final List<String> original, Patch<String> patch) {
        List<DiffRow> diffRows = new ArrayList<>();
        int endPos = 0;
        final List<AbstractDelta<String>> deltaList = patch.getDeltas();

        for (AbstractDelta<String> originalDelta : deltaList) {
            for (AbstractDelta<String> delta : decompressDeltas(originalDelta)) {
                endPos = transformDeltaIntoDiffRow(original, endPos, diffRows, delta);
            }
        }

        // Copy the final matching chunk if any.
        for (String line : original.subList(endPos, original.size())) {
            diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
        }
        return diffRows;
    }

    /**
     * Transforms one patch delta into a DiffRow object.
     */
    private int transformDeltaIntoDiffRow(final List<String> original, int endPos, List<DiffRow> diffRows, AbstractDelta<String> delta) {
        Chunk<String> orig = delta.getSource();
        Chunk<String> rev = delta.getTarget();

        for (String line : original.subList(endPos, orig.getPosition())) {
            diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
        }

        switch (delta.getType()) {
            case INSERT:
                for (String line : rev.getLines()) {
                    diffRows.add(buildDiffRow(Tag.INSERT, "", line));
                }
                break;
            case DELETE:
                for (String line : orig.getLines()) {
                    diffRows.add(buildDiffRow(Tag.DELETE, line, ""));
                }
                break;
            default:
                if (showInlineDiffs) {
                    diffRows.addAll(generateInlineDiffs(delta));
                } else {
                    for (int j = 0; j < Math.max(orig.size(), rev.size()); j++) {
                        diffRows.add(buildDiffRow(Tag.CHANGE,
                                orig.getLines().size() > j ? orig.getLines().get(j) : "",
                                rev.getLines().size() > j ? rev.getLines().get(j) : ""));
                    }
                }
        }

        return orig.last() + 1;
    }

    /**
     * Decompresses ChangeDeltas with different source and target size to a
     * ChangeDelta with same size and a following InsertDelta or DeleteDelta.
     * With this problems of building DiffRows getting smaller.
     *
     * @param deltaList
     */
    private List<AbstractDelta<String>> decompressDeltas(AbstractDelta<String> delta) {
        if (delta.getType() == DeltaType.CHANGE && delta.getSource().size() != delta.getTarget().size()) {
            List<AbstractDelta<String>> deltas = new ArrayList<>();
            //System.out.println("decompress this " + delta);

            int minSize = Math.min(delta.getSource().size(), delta.getTarget().size());
            Chunk<String> orig = delta.getSource();
            Chunk<String> rev = delta.getTarget();

            deltas.add(new ChangeDelta<String>(
                    new Chunk<>(orig.getPosition(), orig.getLines().subList(0, minSize)),
                    new Chunk<>(rev.getPosition(), rev.getLines().subList(0, minSize))));

            if (orig.getLines().size() < rev.getLines().size()) {
                deltas.add(new InsertDelta<String>(
                        new Chunk<>(orig.getPosition() + minSize, Collections.emptyList()),
                        new Chunk<>(rev.getPosition() + minSize, rev.getLines().subList(minSize, rev.getLines().size()))));
            } else {
                deltas.add(new DeleteDelta<String>(
                        new Chunk<>(orig.getPosition() + minSize, orig.getLines().subList(minSize, orig.getLines().size())),
                        new Chunk<>(rev.getPosition() + minSize, Collections.emptyList())));
            }
            return deltas;
        }

        return Collections.singletonList(delta);
    }

    private DiffRow buildDiffRow(Tag type, String orgline, String newline) {
        if (reportLinesUnchanged) {
            return new DiffRow(type, orgline, newline);
        } else {
            String wrapOrg = preprocessLine(orgline);
            if (Tag.DELETE == type) {
                if (mergeOriginalRevised || showInlineDiffs) {
                    wrapOrg = oldTag.apply(type, true) + wrapOrg + oldTag.apply(type, false);
                }
            }
            String wrapNew = preprocessLine(newline);
            if (Tag.INSERT == type) {
                if (mergeOriginalRevised) {
                    wrapOrg = newTag.apply(type, true) + wrapNew + newTag.apply(type, false);
                } else if (showInlineDiffs) {
                    wrapNew = newTag.apply(type, true) + wrapNew + newTag.apply(type, false);
                }
            }
            return new DiffRow(type, wrapOrg, wrapNew);
        }
    }

    private DiffRow buildDiffRowWithoutNormalizing(Tag type, String orgline, String newline) {
        return new DiffRow(type,
                StringUtils.wrapText(orgline, columnWidth),
                StringUtils.wrapText(newline, columnWidth));
    }

    List<String> normalizeLines(List<String> list) {
        return reportLinesUnchanged
                ? list
                : list.stream()
                        .map(lineNormalizer::apply)
                        .collect(toList());
    }

    /**
     * Add the inline diffs for given delta
     *
     * @param delta the given delta
     */
    private List<DiffRow> generateInlineDiffs(AbstractDelta<String> delta) {
        List<String> orig = normalizeLines(delta.getSource().getLines());
        List<String> rev = normalizeLines(delta.getTarget().getLines());
        List<String> origList;
        List<String> revList;
        String joinedOrig = String.join("\n", orig);
        String joinedRev = String.join("\n", rev);

        origList = inlineDiffSplitter.apply(joinedOrig);
        revList = inlineDiffSplitter.apply(joinedRev);

        List<AbstractDelta<String>> inlineDeltas = DiffUtils.diff(origList, revList, equalizer).getDeltas();

        Collections.reverse(inlineDeltas);
        for (AbstractDelta<String> inlineDelta : inlineDeltas) {
            Chunk<String> inlineOrig = inlineDelta.getSource();
            Chunk<String> inlineRev = inlineDelta.getTarget();
            if (inlineDelta.getType() == DeltaType.DELETE) {
                wrapInTag(origList, inlineOrig.getPosition(), inlineOrig
                        .getPosition()
                        + inlineOrig.size(), Tag.DELETE, oldTag, processDiffs, replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised);
            } else if (inlineDelta.getType() == DeltaType.INSERT) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition(),
                            revList.subList(inlineRev.getPosition(),
                                    inlineRev.getPosition() + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition(),
                            inlineOrig.getPosition() + inlineRev.size(),
                            Tag.INSERT, newTag, processDiffs, false);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(),
                            inlineRev.getPosition() + inlineRev.size(),
                            Tag.INSERT, newTag, processDiffs, false);
                }
            } else if (inlineDelta.getType() == DeltaType.CHANGE) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition() + inlineOrig.size(),
                            revList.subList(inlineRev.getPosition(),
                                    inlineRev.getPosition() + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition() + inlineOrig.size(),
                            inlineOrig.getPosition() + inlineOrig.size() + inlineRev.size(),
                            Tag.CHANGE, newTag, processDiffs, false);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(),
                            inlineRev.getPosition() + inlineRev.size(),
                            Tag.CHANGE, newTag, processDiffs, false);
                }
                wrapInTag(origList, inlineOrig.getPosition(),
                        inlineOrig.getPosition() + inlineOrig.size(),
                        Tag.CHANGE, oldTag, processDiffs, replaceOriginalLinefeedInChangesWithSpaces && mergeOriginalRevised);
            }
        }
        StringBuilder origResult = new StringBuilder();
        StringBuilder revResult = new StringBuilder();
        for (String character : origList) {
            origResult.append(character);
        }
        for (String character : revList) {
            revResult.append(character);
        }

        List<String> original = Arrays.asList(origResult.toString().split("\n"));
        List<String> revised = Arrays.asList(revResult.toString().split("\n"));
        List<DiffRow> diffRows = new ArrayList<>();
        for (int j = 0; j < Math.max(original.size(), revised.size()); j++) {
            diffRows.
                    add(buildDiffRowWithoutNormalizing(Tag.CHANGE,
                            original.size() > j ? original.get(j) : "",
                            revised.size() > j ? revised.get(j) : ""));
        }
        return diffRows;
    }

    private String preprocessLine(String line) {
        if (columnWidth == 0) {
            return lineNormalizer.apply(line);
        } else {
            return StringUtils.wrapText(lineNormalizer.apply(line), columnWidth);
        }
    }

    /**
     * This class used for building the DiffRowGenerator.
     *
     * @author dmitry
     *
     */
    public static class Builder {

        private boolean showInlineDiffs = false;
        private boolean ignoreWhiteSpaces = false;

        private BiFunction<Tag, Boolean, String> oldTag
                = (tag, f) -> f ? "<span class=\"editOldInline\">" : "</span>";
        private BiFunction<Tag, Boolean, String> newTag
                = (tag, f) -> f ? "<span class=\"editNewInline\">" : "</span>";

        private int columnWidth = 0;
        private boolean mergeOriginalRevised = false;
        private boolean reportLinesUnchanged = false;
        private Function<String, List<String>> inlineDiffSplitter = SPLITTER_BY_CHARACTER;
        private Function<String, String> lineNormalizer = LINE_NORMALIZER_FOR_HTML;
        private Function<String, String> processDiffs = null;
        private BiPredicate<String, String> equalizer = null;
        private boolean replaceOriginalLinefeedInChangesWithSpaces = false;

        private Builder() {
        }

        /**
         * Show inline diffs in generating diff rows or not.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured showInlineDiff parameter
         */
        public Builder showInlineDiffs(boolean val) {
            showInlineDiffs = val;
            return this;
        }

        /**
         * Ignore white spaces in generating diff rows or not.
         *
         * @param val the value to set. Default: true.
         * @return builder with configured ignoreWhiteSpaces parameter
         */
        public Builder ignoreWhiteSpaces(boolean val) {
            ignoreWhiteSpaces = val;
            return this;
        }

        /**
         * Give the originial old and new text lines to Diffrow without any
         * additional processing and without any tags to highlight the change.
         *
         * @param val the value to set. Default: false.
         * @return builder with configured reportLinesUnWrapped parameter
         */
        public Builder reportLinesUnchanged(final boolean val) {
            reportLinesUnchanged = val;
            return this;
        }

        /**
         * Generator for Old-Text-Tags.
         *
         * @param generator the tag generator
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder oldTag(BiFunction<Tag, Boolean, String> generator) {
            this.oldTag = generator;
            return this;
        }

        /**
         * Generator for Old-Text-Tags.
         *
         * @param generator the tag generator
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder oldTag(Function<Boolean, String> generator) {
            this.oldTag = (tag, f) -> generator.apply(f);
            return this;
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        public Builder newTag(BiFunction<Tag, Boolean, String> generator) {
            this.newTag = generator;
            return this;
        }

        /**
         * Generator for New-Text-Tags.
         *
         * @param generator
         * @return
         */
        public Builder newTag(Function<Boolean, String> generator) {
            this.newTag = (tag, f) -> generator.apply(f);
            return this;
        }

        /**
         * Processor for diffed text parts. Here e.g. whitecharacters could be
         * replaced by something visible.
         *
         * @param processDiffs
         * @return
         */
        public Builder processDiffs(Function<String, String> processDiffs) {
            this.processDiffs = processDiffs;
            return this;
        }

        /**
         * Set the column width of generated lines of original and revised
         * texts.
         *
         * @param width the width to set. Making it &lt; 0 doesn't make any sense. Default 80.
         * @return builder with config of column width
         */
        public Builder columnWidth(int width) {
            if (width >= 0) {
                columnWidth = width;
            }
            return this;
        }

        /**
         * Build the DiffRowGenerator. If some parameters is not set, the
         * default values are used.
         *
         * @return the customized DiffRowGenerator
         */
        public DiffRowGenerator build() {
            return new DiffRowGenerator(this);
        }

        /**
         * Merge the complete result within the original text. This makes sense
         * for one line display.
         *
         * @param mergeOriginalRevised
         * @return
         */
        public Builder mergeOriginalRevised(boolean mergeOriginalRevised) {
            this.mergeOriginalRevised = mergeOriginalRevised;
            return this;
        }

        /**
         * Per default each character is separatly processed. This variant
         * introduces processing by word, which does not deliver in word
         * changes. Therefore the whole word will be tagged as changed:
         *
         * <pre>
         * false:    (aBa : aba) --  changed: a(B)a : a(b)a
         * true:     (aBa : aba) --  changed: (aBa) : (aba)
         * </pre>
         */
        public Builder inlineDiffByWord(boolean inlineDiffByWord) {
            inlineDiffSplitter = inlineDiffByWord ? SPLITTER_BY_WORD : SPLITTER_BY_CHARACTER;
            return this;
        }

        /**
         * To provide some customized splitting a splitter can be provided. Here
         * someone could think about sentence splitter, comma splitter or stuff
         * like that.
         *
         * @param inlineDiffSplitter
         * @return
         */
        public Builder inlineDiffBySplitter(Function<String, List<String>> inlineDiffSplitter) {
            this.inlineDiffSplitter = inlineDiffSplitter;
            return this;
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
        public Builder lineNormalizer(Function<String, String> lineNormalizer) {
            this.lineNormalizer = lineNormalizer;
            return this;
        }

        /**
         * Provide an equalizer for diff processing.
         *
         * @param equalizer equalizer for diff processing.
         * @return builder with configured equalizer parameter
         */
        public Builder equalizer(BiPredicate<String, String> equalizer) {
            this.equalizer = equalizer;
            return this;
        }

        /**
         * Sometimes it happens that a change contains multiple lines. If there
         * is no correspondence in old and new. To keep the merged line more
         * readable the linefeeds could be replaced by spaces.
         *
         * @param replace
         * @return
         */
        public Builder replaceOriginalLinefeedInChangesWithSpaces(boolean replace) {
            this.replaceOriginalLinefeedInChangesWithSpaces = replace;
            return this;
        }
    }
}
