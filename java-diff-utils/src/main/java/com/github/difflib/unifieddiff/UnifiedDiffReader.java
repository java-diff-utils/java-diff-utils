/*
 * Copyright 2019 java-diff-utils.
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
package com.github.difflib.unifieddiff;

import com.github.difflib.patch.ChangeDelta;
import com.github.difflib.patch.Chunk;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
public final class UnifiedDiffReader {

    static final Pattern UNIFIED_DIFF_CHUNK_REGEXP = Pattern.compile("^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@");
    static final Pattern TIMESTAMP_REGEXP = Pattern.compile("(\\d{4}-\\d{2}-\\d{2}[T ]\\d{2}:\\d{2}:\\d{2}\\.\\d{3,})(?: [+-]\\d+)?");

    private final InternalUnifiedDiffReader READER;
    private final UnifiedDiff data = new UnifiedDiff();

    private final UnifiedDiffLine DIFF_COMMAND = new UnifiedDiffLine(true, "^diff\\s", this::processDiff);
    private final UnifiedDiffLine SIMILARITY_INDEX = new UnifiedDiffLine(true, "^similarity index (\\d+)%$", this::processSimilarityIndex);
    private final UnifiedDiffLine INDEX = new UnifiedDiffLine(true, "^index\\s[\\da-zA-Z]+\\.\\.[\\da-zA-Z]+(\\s(\\d+))?$", this::processIndex);
    private final UnifiedDiffLine FROM_FILE = new UnifiedDiffLine(true, "^---\\s", this::processFromFile);
    private final UnifiedDiffLine TO_FILE = new UnifiedDiffLine(true, "^\\+\\+\\+\\s", this::processToFile);
    private final UnifiedDiffLine RENAME_FROM = new UnifiedDiffLine(true, "^rename\\sfrom\\s(.+)$", this::processRenameFrom);
    private final UnifiedDiffLine RENAME_TO = new UnifiedDiffLine(true, "^rename\\sto\\s(.+)$", this::processRenameTo);

    private final UnifiedDiffLine NEW_FILE_MODE = new UnifiedDiffLine(true, "^new\\sfile\\smode\\s(\\d+)", this::processNewFileMode);

    private final UnifiedDiffLine DELETED_FILE_MODE = new UnifiedDiffLine(true, "^deleted\\sfile\\smode\\s(\\d+)", this::processDeletedFileMode);

    private final UnifiedDiffLine CHUNK = new UnifiedDiffLine(false, UNIFIED_DIFF_CHUNK_REGEXP, this::processChunk);
    private final UnifiedDiffLine LINE_NORMAL = new UnifiedDiffLine("^\\s", this::processNormalLine);
    private final UnifiedDiffLine LINE_DEL = new UnifiedDiffLine("^-", this::processDelLine);
    private final UnifiedDiffLine LINE_ADD = new UnifiedDiffLine("^\\+", this::processAddLine);

    private UnifiedDiffFile actualFile;

    UnifiedDiffReader(Reader reader) {
        this.READER = new InternalUnifiedDiffReader(reader);
    }

    // schema = [[/^\s+/, normal], [/^diff\s/, start], [/^new file mode \d+$/, new_file], 
    // [/^deleted file mode \d+$/, deleted_file], [/^index\s[\da-zA-Z]+\.\.[\da-zA-Z]+(\s(\d+))?$/, index], 
    // [/^---\s/, from_file], [/^\+\+\+\s/, to_file], [/^@@\s+\-(\d+),?(\d+)?\s+\+(\d+),?(\d+)?\s@@/, chunk], 
    // [/^-/, del], [/^\+/, add], [/^\\ No newline at end of file$/, eof]];
    private UnifiedDiff parse() throws IOException, UnifiedDiffParserException {
//        String headerTxt = "";
//        LOG.log(Level.FINE, "header parsing");
//        String line = null;
//        while (READER.ready()) {
//            line = READER.readLine();
//            LOG.log(Level.FINE, "parsing line {0}", line);
//            if (DIFF_COMMAND.validLine(line) || INDEX.validLine(line)
//                    || FROM_FILE.validLine(line) || TO_FILE.validLine(line)
//                    || NEW_FILE_MODE.validLine(line)) {
//                break;
//            } else {
//                headerTxt += line + "\n";
//            }
//        }
//        if (!"".equals(headerTxt)) {
//            data.setHeader(headerTxt);
//        }

        String line = READER.readLine();
        while (line != null) {
            String headerTxt = "";
            LOG.log(Level.FINE, "header parsing");
            while (line != null) {
                LOG.log(Level.FINE, "parsing line {0}", line);
                if (validLine(line, DIFF_COMMAND, SIMILARITY_INDEX, INDEX,
                            FROM_FILE, TO_FILE,
                            RENAME_FROM, RENAME_TO,
                            NEW_FILE_MODE, DELETED_FILE_MODE, 
                            CHUNK)) {
                    break;
                } else {
                    headerTxt += line + "\n";
                }
                line = READER.readLine();
            }
            if (!"".equals(headerTxt)) {
                data.setHeader(headerTxt);
            }
            if (line != null && !CHUNK.validLine(line)) {
                initFileIfNecessary();
                while (line != null && !CHUNK.validLine(line)) {
                    if (!processLine(line, DIFF_COMMAND, SIMILARITY_INDEX, INDEX,
                            FROM_FILE, TO_FILE,
                            RENAME_FROM, RENAME_TO,
                            NEW_FILE_MODE, DELETED_FILE_MODE)) {
                        throw new UnifiedDiffParserException("expected file start line not found");
                    }
                    line = READER.readLine();
                }
            }
            if (line != null) {
                processLine(line, CHUNK);
                while ((line = READER.readLine()) != null) {
                    line = checkForNoNewLineAtTheEndOfTheFile(line);

                    if (!processLine(line, LINE_NORMAL, LINE_ADD, LINE_DEL)) {
                        throw new UnifiedDiffParserException("expected data line not found");
                    }
                    if ((originalTxt.size() == old_size && revisedTxt.size() == new_size)
                            || (old_size == 0 && new_size == 0 && originalTxt.size() == this.old_ln
                            && revisedTxt.size() == this.new_ln)) {
                        finalizeChunk();
                        break;
                    }
                }
                line = READER.readLine();

                line = checkForNoNewLineAtTheEndOfTheFile(line);
            }
            if (line == null || (line.startsWith("--") && !line.startsWith("---"))) {
                break;
            }
        }

        if (READER.ready()) {
            String tailTxt = "";
            while (READER.ready()) {
                if (tailTxt.length() > 0) {
                    tailTxt += "\n";
                }
                tailTxt += READER.readLine();
            }
            data.setTailTxt(tailTxt);
        }

        return data;
    }

    private String checkForNoNewLineAtTheEndOfTheFile(String line) throws IOException {
        if ("\\ No newline at end of file".equals(line)) {
            actualFile.setNoNewLineAtTheEndOfTheFile(true);
            return READER.readLine();
        }
        return line;
    }

    static String[] parseFileNames(String line) {
        String[] split = line.split(" ");
        return new String[]{
            split[2].replaceAll("^a/", ""),
            split[3].replaceAll("^b/", "")
        };
    }

    private static final Logger LOG = Logger.getLogger(UnifiedDiffReader.class.getName());

    /**
     * To parse a diff file use this method.
     *
     * @param stream This is the diff file data.
     * @return In a UnifiedDiff structure this diff file data is returned.
     * @throws IOException
     * @throws UnifiedDiffParserException
     */
    public static UnifiedDiff parseUnifiedDiff(InputStream stream) throws IOException, UnifiedDiffParserException {
        UnifiedDiffReader parser = new UnifiedDiffReader(new BufferedReader(new InputStreamReader(stream)));
        return parser.parse();
    }

    private boolean processLine(String line, UnifiedDiffLine... rules) throws UnifiedDiffParserException {
        if (line == null) {
            return false;
        }
        for (UnifiedDiffLine rule : rules) {
            if (rule.processLine(line)) {
                LOG.fine("  >>> processed rule " + rule.toString());
                return true;
            }
        }
        LOG.warning("  >>> no rule matched " + line);
        return false;
        //throw new UnifiedDiffParserException("parsing error at line " + line);
    }
    
    private boolean validLine(String line, UnifiedDiffLine ... rules) {
        if (line == null) {
            return false;
        }
        for (UnifiedDiffLine rule : rules) {
            if (rule.validLine(line)) {
                LOG.fine("  >>> accepted rule " + rule.toString());
                return true;
            }
        }
        return false;
    }

    private void initFileIfNecessary() {
        if (!originalTxt.isEmpty() || !revisedTxt.isEmpty()) {
            throw new IllegalStateException();
        }
        actualFile = null;
        if (actualFile == null) {
            actualFile = new UnifiedDiffFile();
            data.addFile(actualFile);
        }
    }

    private void processDiff(MatchResult match, String line) {
        //initFileIfNecessary();
        LOG.log(Level.FINE, "start {0}", line);
        String[] fromTo = parseFileNames(READER.lastLine());
        actualFile.setFromFile(fromTo[0]);
        actualFile.setToFile(fromTo[1]);
        actualFile.setDiffCommand(line);
    }

    private void processSimilarityIndex(MatchResult match, String line) {
        actualFile.setSimilarityIndex(Integer.valueOf(match.group(1)));
    }

    private List<String> originalTxt = new ArrayList<>();
    private List<String> revisedTxt = new ArrayList<>();
    private List<Integer> addLineIdxList = new ArrayList<>();
    private List<Integer> delLineIdxList = new ArrayList<>();
    private int old_ln;
    private int old_size;
    private int new_ln;
    private int new_size;
    private int delLineIdx = 0;
    private int addLineIdx = 0;

    private void finalizeChunk() {
        if (!originalTxt.isEmpty() || !revisedTxt.isEmpty()) {
            actualFile.getPatch().addDelta(new ChangeDelta<>(new Chunk<>(
                    old_ln - 1, originalTxt, delLineIdxList), new Chunk<>(
                    new_ln - 1, revisedTxt, addLineIdxList)));
            old_ln = 0;
            new_ln = 0;
            originalTxt.clear();
            revisedTxt.clear();
            addLineIdxList.clear();
            delLineIdxList.clear();
            delLineIdx = 0;
            addLineIdx = 0;
        }
    }

    private void processNormalLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
        revisedTxt.add(cline);
        delLineIdx++;
        addLineIdx++;
    }

    private void processAddLine(MatchResult match, String line) {
        String cline = line.substring(1);
        revisedTxt.add(cline);
        addLineIdx++;
        addLineIdxList.add(new_ln - 1 + addLineIdx);
    }

    private void processDelLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
        delLineIdx++;
        delLineIdxList.add(old_ln - 1 + delLineIdx);
    }

    private void processChunk(MatchResult match, String chunkStart) {
        // finalizeChunk();
        old_ln = toInteger(match, 1, 1);
        old_size = toInteger(match, 2, 1);
        new_ln = toInteger(match, 3, 1);
        new_size = toInteger(match, 4, 1);
        if (old_ln == 0) {
            old_ln = 1;
        }
        if (new_ln == 0) {
            new_ln = 1;
        }
    }

    private static Integer toInteger(MatchResult match, int group, int defValue) throws NumberFormatException {
        return Integer.valueOf(Objects.toString(match.group(group), "" + defValue));
    }

    private void processIndex(MatchResult match, String line) {
        //initFileIfNecessary();
        LOG.log(Level.FINE, "index {0}", line);
        actualFile.setIndex(line.substring(6));
    }

    private void processFromFile(MatchResult match, String line) {
        //initFileIfNecessary();
        actualFile.setFromFile(extractFileName(line));
        actualFile.setFromTimestamp(extractTimestamp(line));
    }

    private void processToFile(MatchResult match, String line) {
        //initFileIfNecessary();
        actualFile.setToFile(extractFileName(line));
        actualFile.setToTimestamp(extractTimestamp(line));
    }

    private void processRenameFrom(MatchResult match, String line) {
        actualFile.setRenameFrom(match.group(1));
    }

    private void processRenameTo(MatchResult match, String line) {
        actualFile.setRenameTo(match.group(1));
    }

    private void processNewFileMode(MatchResult match, String line) {
        //initFileIfNecessary();
        actualFile.setNewFileMode(match.group(1));
    }

    private void processDeletedFileMode(MatchResult match, String line) {
        //initFileIfNecessary();
        actualFile.setDeletedFileMode(match.group(1));
    }

    private String extractFileName(String _line) {
        Matcher matcher = TIMESTAMP_REGEXP.matcher(_line);
        String line = _line;
        if (matcher.find()) {
            line = line.substring(0, matcher.start());
        }
        line = line.split("\t")[0];
        return line.substring(4).replaceFirst("^(a|b|old|new)(\\/)?", "")
                .trim();
    }

    private String extractTimestamp(String line) {
        Matcher matcher = TIMESTAMP_REGEXP.matcher(line);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    final class UnifiedDiffLine {

        private final Pattern pattern;
        private final BiConsumer<MatchResult, String> command;
        private final boolean stopsHeaderParsing;

        public UnifiedDiffLine(String pattern, BiConsumer<MatchResult, String> command) {
            this(false, pattern, command);
        }

        public UnifiedDiffLine(boolean stopsHeaderParsing, String pattern, BiConsumer<MatchResult, String> command) {
            this.pattern = Pattern.compile(pattern);
            this.command = command;
            this.stopsHeaderParsing = stopsHeaderParsing;
        }

        public UnifiedDiffLine(boolean stopsHeaderParsing, Pattern pattern, BiConsumer<MatchResult, String> command) {
            this.pattern = pattern;
            this.command = command;
            this.stopsHeaderParsing = stopsHeaderParsing;
        }

        public boolean validLine(String line) {
            Matcher m = pattern.matcher(line);
            return m.find();
        }

        public boolean processLine(String line) throws UnifiedDiffParserException {
            Matcher m = pattern.matcher(line);
            if (m.find()) {
                command.accept(m.toMatchResult(), line);
                return true;
            } else {
                return false;
            }
        }

        public boolean isStopsHeaderParsing() {
            return stopsHeaderParsing;
        }

        @Override
        public String toString() {
            return "UnifiedDiffLine{" + "pattern=" + pattern + ", stopsHeaderParsing=" + stopsHeaderParsing + '}';
        }
    }
}

class InternalUnifiedDiffReader extends BufferedReader {

    private String lastLine;

    public InternalUnifiedDiffReader(Reader reader) {
        super(reader);
    }

    @Override
    public String readLine() throws IOException {
        lastLine = super.readLine();
        return lastLine();
    }

    String lastLine() {
        return lastLine;
    }
}
