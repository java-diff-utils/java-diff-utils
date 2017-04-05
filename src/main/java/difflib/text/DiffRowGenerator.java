/*-
 * #%L
 * java-diff-utils
 * %%
 * Copyright (C) 2009 - 2017 java-diff-utils
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */
package difflib.text;

import difflib.DiffUtils;
import difflib.algorithm.DiffException;
import difflib.patch.ChangeDelta;
import difflib.patch.Chunk;
import difflib.patch.DeleteDelta;
import difflib.patch.Delta;
import difflib.patch.Equalizer;
import difflib.patch.InsertDelta;
import difflib.patch.Patch;
import difflib.text.DiffRow.Tag;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class for generating DiffRows for side-by-sidy view. You can customize the way of
 * generating. For example, show inline diffs on not, ignoring white spaces or/and blank lines and
 * so on. All parameters for generating are optional. If you do not specify them, the class will use
 * the default values.
 *
 * These values are: showInlineDiffs = false; ignoreWhiteSpaces = true; ignoreBlankLines = true; ...
 *
 * For instantiating the DiffRowGenerator you should use the its builder. Like in example  <code>
 *    DiffRowGenerator generator = new DiffRowGenerator.Builder().showInlineDiffs(true).
 *    	ignoreWhiteSpaces(true).columnWidth(100).build();
 * </code>
 *
 * @author <a href="dm.naumenko@gmail.com">Dmitry Naumenko</a>
 */
public class DiffRowGenerator {

    private final boolean showInlineDiffs;
    private final boolean ignoreWhiteSpaces;
    private final String inlineOldTag;
    private final String inlineNewTag;
    private final String inlineOldCssClass;
    private final String inlineNewCssClass;
    private final boolean inlineDiffByWord;
    private final int columnWidth;
    private final Equalizer<String> equalizer;
    private final boolean mergeOriginalRevised;

    /**
     * This class used for building the DiffRowGenerator.
     *
     * @author dmitry
     *
     */
    public static class Builder {

        private boolean showInlineDiffs = false;
        private boolean ignoreWhiteSpaces = false;
        private String inlineOldTag = "span";
        private String inlineNewTag = "span";
        private String inlineOldCssClass = "editOldInline";
        private String inlineNewCssClass = "editNewInline";
        private int columnWidth = 80;
        private boolean mergeOriginalRevised = false;
        private boolean inlineDiffByWord = false;

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
         * Set the tag used for displaying changes in the original text.
         *
         * @param tag the tag to set. Without angle brackets. Default: span.
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder inlineOldTag(String tag) {
            inlineOldTag = tag;
            return this;
        }

        /**
         * Set the tag used for displaying changes in the revised text.
         *
         * @param tag the tag to set. Without angle brackets. Default: span.
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder inlineNewTag(String tag) {
            inlineNewTag = tag;
            return this;
        }

        /**
         * Set the css class used for displaying changes in the original text.
         *
         * @param cssClass the tag to set. Without any quotes, just word. Default: editOldInline.
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder inlineOldCssClass(String cssClass) {
            inlineOldCssClass = cssClass;
            return this;
        }

        /**
         * Set the css class used for displaying changes in the revised text.
         *
         * @param cssClass the tag to set. Without any quotes, just word. Default: editNewInline.
         * @return builder with configured ignoreBlankLines parameter
         */
        public Builder inlineNewCssClass(String cssClass) {
            inlineNewCssClass = cssClass;
            return this;
        }

        /**
         * Set the column with of generated lines of original and revised texts.
         *
         * @param width the width to set. Making it < 0 doesn't have any sense. Default 80. @return
         * builder with config ured ignoreBlankLines parameter
         */
        public Builder columnWidth(int width) {
            if (width > 0) {
                columnWidth = width;
            }
            return this;
        }

        /**
         * Build the DiffRowGenerator. If some parameters is not set, the default values are used.
         *
         * @return the customized DiffRowGenerator
         */
        public DiffRowGenerator build() {
            return new DiffRowGenerator(this);
        }

        /**
         * Merge the complete result within the original text. This makes sense for one line
         * display.
         *
         * @param mergeOriginalRevised
         * @return
         */
        public Builder mergeOriginalRevised(boolean mergeOriginalRevised) {
            this.mergeOriginalRevised = mergeOriginalRevised;
            return this;
        }

        /**
         * Per default each character is separatly processed. This variant introduces processing by
         * word, which should deliver no in word changes.
         */
        public Builder inlineDiffByWord(boolean inlineDiffByWord) {
            this.inlineDiffByWord = inlineDiffByWord;
            return this;
        }
    }

    public static Builder create() {
        return new Builder();
    }

    private DiffRowGenerator(Builder builder) {
        showInlineDiffs = builder.showInlineDiffs;
        ignoreWhiteSpaces = builder.ignoreWhiteSpaces;
        inlineOldTag = builder.inlineOldTag;
        inlineNewTag = builder.inlineNewTag;
        inlineOldCssClass = builder.inlineOldCssClass;
        inlineNewCssClass = builder.inlineNewCssClass;
        columnWidth = builder.columnWidth;
        mergeOriginalRevised = builder.mergeOriginalRevised;
        inlineDiffByWord = builder.inlineDiffByWord;
        equalizer = new Equalizer<String>() {
            @Override
            public boolean equals(String original, String revised) {
                if (ignoreWhiteSpaces) {
                    original = original.trim().replaceAll("\\s+", " ");
                    revised = revised.trim().replaceAll("\\s+", " ");
                }
                return original.equals(revised);
            }
        };
    }

    /**
     * Get the DiffRows describing the difference between original and revised texts using the given
     * patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(List<String> original, List<String> revised) throws DiffException {
        return generateDiffRows(original, DiffUtils.diff(original, revised, equalizer));
    }

    private DiffRow buildDiffRow(Tag type, String orgline, String newline) {
        String wrapOrg = StringUtils.wrapText(StringUtils.normalize(orgline), columnWidth);
        if (mergeOriginalRevised && Tag.DELETE == type) {
            wrapOrg = createOpenTag(inlineOldTag, inlineOldCssClass) + wrapOrg + createCloseTag(inlineOldTag);
        }
        String wrapNew = StringUtils.wrapText(StringUtils.normalize(newline), columnWidth);
        if (mergeOriginalRevised && Tag.INSERT == type) {
            wrapOrg = createOpenTag(inlineNewTag, inlineNewCssClass) + wrapNew + createCloseTag(inlineNewTag);
        }
        return new DiffRow(type, wrapOrg, wrapNew);
    }

    private DiffRow buildDiffRowWithoutNormalizing(Tag type, String orgline, String newline) {
        return new DiffRow(type,
                StringUtils.wrapText(orgline, columnWidth),
                StringUtils.wrapText(newline, columnWidth));
    }

    /**
     * Generates the DiffRows describing the difference between original and revised texts using the
     * given patch. Useful for displaying side-by-side diff.
     *
     * @param original the original text
     * @param revised the revised text
     * @param patch the given patch
     * @return the DiffRows between original and revised texts
     */
    public List<DiffRow> generateDiffRows(final List<String> original, Patch<String> patch) throws DiffException {
        List<DiffRow> diffRows = new ArrayList<>();
        int endPos = 0;
        final List<Delta<String>> deltaList = patch.getDeltas();
        for (int i = 0; i < deltaList.size(); i++) {
            Delta<String> delta = deltaList.get(i);
            Chunk<String> orig = delta.getOriginal();
            Chunk<String> rev = delta.getRevised();

            for (String line : original.subList(endPos, orig.getPosition())) {
                diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
            }

            // Inserted DiffRow
            if (delta instanceof InsertDelta) {
                endPos = orig.last() + 1;
                for (String line : (List<String>) rev.getLines()) {
                    diffRows.add(buildDiffRow(Tag.INSERT, "", line));
                }
                continue;
            }

            // Deleted DiffRow
            if (delta instanceof DeleteDelta) {
                endPos = orig.last() + 1;
                for (String line : (List<String>) orig.getLines()) {
                    diffRows.add(buildDiffRow(Tag.DELETE, line, ""));
                }
                continue;
            }

            if (showInlineDiffs) {
                diffRows.addAll(generateInlineDiffs(delta));
            } else {
                for (int j = 0; j < Math.max(orig.size(), rev.size()); j++) {
                    diffRows.add(buildDiffRow(Tag.CHANGE,
                            orig.getLines().size() > j ? orig.getLines().get(j) : "",
                            rev.getLines().size() > j ? rev.getLines().get(j) : ""));
                }
            }
            endPos = orig.last() + 1;
        }

        // Copy the final matching chunk if any.
        for (String line : original.subList(endPos, original.size())) {
            diffRows.add(buildDiffRow(Tag.EQUAL, line, line));
        }
        return diffRows;
    }

    /**
     * Add the inline diffs for given delta
     *
     * @param delta the given delta
     */
    private List<DiffRow> generateInlineDiffs(Delta<String> delta) throws DiffException {
        List<String> orig = StringUtils.normalize(delta.getOriginal().getLines());
        List<String> rev = StringUtils.normalize(delta.getRevised().getLines());
        List<String> origList;
        List<String> revList;

        if (inlineDiffByWord) {
            origList = splitStringPreserveDelimiter(String.join("\n", orig));
            revList = splitStringPreserveDelimiter(String.join("\n", rev));
        } else {
            origList = new LinkedList<>();
            revList = new LinkedList<>();
            for (Character character : String.join("\n", orig).toCharArray()) {
                origList.add(character.toString());
            }
            for (Character character : String.join("\n", rev).toCharArray()) {
                revList.add(character.toString());
            }
        }

        List<Delta<String>> inlineDeltas = DiffUtils.diff(origList, revList).getDeltas();

        Collections.reverse(inlineDeltas);
        for (Delta<String> inlineDelta : inlineDeltas) {
            Chunk<String> inlineOrig = inlineDelta.getOriginal();
            Chunk<String> inlineRev = inlineDelta.getRevised();
            if (inlineDelta instanceof DeleteDelta) {
                wrapInTag(origList, inlineOrig.getPosition(), inlineOrig
                        .getPosition()
                        + inlineOrig.size() + 1, this.inlineOldTag, this.inlineOldCssClass);
            } else if (inlineDelta instanceof InsertDelta) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition(),
                            revList.subList(inlineRev.getPosition(), inlineRev.getPosition()
                                    + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition(), inlineOrig.getPosition()
                            + inlineRev.size() + 1, this.inlineNewTag, this.inlineNewCssClass);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition()
                            + inlineRev.size() + 1, this.inlineNewTag, this.inlineNewCssClass);
                }
            } else if (inlineDelta instanceof ChangeDelta) {
                if (mergeOriginalRevised) {
                    origList.addAll(inlineOrig.getPosition() + inlineOrig.size(),
                            revList.subList(inlineRev.getPosition(), inlineRev.getPosition()
                                    + inlineRev.size()));
                    wrapInTag(origList, inlineOrig.getPosition() + inlineOrig.size(), inlineOrig.getPosition() + inlineOrig.size()
                            + inlineRev.size() + 1, this.inlineNewTag, this.inlineNewCssClass);
                } else {
                    wrapInTag(revList, inlineRev.getPosition(), inlineRev.getPosition()
                            + inlineRev.size() + 1, this.inlineNewTag, this.inlineNewCssClass);
                }
                wrapInTag(origList, inlineOrig.getPosition(), inlineOrig
                        .getPosition()
                        + inlineOrig.size() + 1, this.inlineOldTag, this.inlineOldCssClass);
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

    /**
     * Wrap the elements in the sequence with the given tag
     *
     * @param startPosition the position from which tag should start. The counting start from a
     * zero.
     * @param endPosition the position before which tag should should be closed.
     * @param tag the tag name without angle brackets, just a word
     * @param cssClass the optional css class
     */
    public static void wrapInTag(List<String> sequence, int startPosition,
            int endPosition, String tag, String cssClass) {
        sequence.add(startPosition, createOpenTag(tag, cssClass));
        sequence.add(endPosition, createCloseTag(tag));
    }

    private static String createCloseTag(String tag) {
        return "</" + tag + ">";
    }

    private static String createOpenTag(String tag, String cssClass) {
        return "<" + tag + (cssClass != null ? " class=\"" + cssClass + "\"" : "") + ">";
    }

    private static final Pattern SPLIT_PATTERN = Pattern.compile("\\s+|[,.\\[\\](){}/\\\\*+\\-#]");

    static List<String> splitStringPreserveDelimiter(String str) {
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
}
