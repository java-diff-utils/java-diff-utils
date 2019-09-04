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

    private final InternalUnifiedDiffReader READER;
    private final UnifiedDiff data = new UnifiedDiff();

    private final UnifiedDiffLine DIFF_COMMAND = new UnifiedDiffLine(true, "^diff\\s", this::processDiff);
    private final UnifiedDiffLine INDEX = new UnifiedDiffLine(true, "^index\\s[\\da-zA-Z]+\\.\\.[\\da-zA-Z]+(\\s(\\d+))?$", this::processIndex);
    private final UnifiedDiffLine FROM_FILE = new UnifiedDiffLine(true, "^---\\s", this::processFromFile);
    private final UnifiedDiffLine TO_FILE = new UnifiedDiffLine(true, "^\\+\\+\\+\\s", this::processToFile);

    private final UnifiedDiffLine CHUNK = new UnifiedDiffLine(false, UNIFIED_DIFF_CHUNK_REGEXP, this::processChunk);
    private final UnifiedDiffLine LINE_NORMAL = new UnifiedDiffLine("^\\s+", this::processNormalLine);
    private final UnifiedDiffLine LINE_DEL = new UnifiedDiffLine("^-", this::processDelLine);
    private final UnifiedDiffLine LINE_ADD = new UnifiedDiffLine("^+", this::processAddLine);

    private UnifiedDiffFile actualFile;

    UnifiedDiffReader(Reader reader) {
        this.READER = new InternalUnifiedDiffReader(reader);
    }

    // schema = [[/^\s+/, normal], [/^diff\s/, start], [/^new file mode \d+$/, new_file], 
    // [/^deleted file mode \d+$/, deleted_file], [/^index\s[\da-zA-Z]+\.\.[\da-zA-Z]+(\s(\d+))?$/, index], 
    // [/^---\s/, from_file], [/^\+\+\+\s/, to_file], [/^@@\s+\-(\d+),?(\d+)?\s+\+(\d+),?(\d+)?\s@@/, chunk], 
    // [/^-/, del], [/^\+/, add], [/^\\ No newline at end of file$/, eof]];
    private UnifiedDiff parse() throws IOException, UnifiedDiffParserException {
        String headerTxt = "";
        LOG.log(Level.INFO, "header parsing");
        String line = null;
        while (READER.ready()) {
            line = READER.readLine();
            LOG.log(Level.INFO, "parsing line {0}", line);
            if (DIFF_COMMAND.validLine(line) || INDEX.validLine(line)
                    || FROM_FILE.validLine(line) || TO_FILE.validLine(line)) {
                break;
            } else {
                headerTxt += line + "\n";
            }
        }
        data.setHeader(headerTxt);

        while (line != null) {
            if (!CHUNK.validLine(line)) {
                if (processLine(line, DIFF_COMMAND, INDEX, FROM_FILE, TO_FILE) == false) {
                    throw new UnifiedDiffParserException("parsing error at line " + line);
                }
            } else {
                if (processLine(line, CHUNK) == false) {
                    throw new UnifiedDiffParserException("parsing error at line " + line);
                }
            }
            line = READER.readLine();
        }

        finalizeChunk();

        String tailTxt = "";
        while (READER.ready()) {
            tailTxt += READER.readLine() + "\n";
        }
        data.setTailTxt(tailTxt);

        return data;
    }

    static String[] parseFileNames(String line) {
        String[] split = line.split(" ");
        return new String[]{
            split[2].replaceAll("^a/", ""),
            split[3].replaceAll("^b/", "")
        };
    }

    private static final Logger LOG = Logger.getLogger(UnifiedDiffReader.class.getName());

    public static UnifiedDiff parseUnifiedDiff(InputStream stream) throws IOException, UnifiedDiffParserException {
        UnifiedDiffReader parser = new UnifiedDiffReader(new BufferedReader(new InputStreamReader(stream)));
        return parser.parse();
    }

    private boolean processLine(String line, UnifiedDiffLine... rules) throws UnifiedDiffParserException {
        for (UnifiedDiffLine rule : rules) {
            if (rule.processLine(line)) {
                LOG.info("  >>> processed rule " + rule.toString());
                return true;
            }
        }
        LOG.info("  >>> no rule matched " + line);
        return false;
    }

    private void initFileIfNecessary() {
        if (!originalTxt.isEmpty() || !revisedTxt.isEmpty()) {
            finalizeChunk();
            actualFile = null;
        }
        if (actualFile == null) {
            actualFile = new UnifiedDiffFile();
            data.addFile(actualFile);
        }
    }

    private void processDiff(MatchResult match, String line) {
        initFileIfNecessary();
        LOG.log(Level.INFO, "start {0}", line);
        String[] fromTo = parseFileNames(READER.lastLine());
        actualFile.setFromFile(fromTo[0]);
        actualFile.setToFile(fromTo[1]);
        actualFile.setDiffCommand(line);
    }

    private List<String> originalTxt = new ArrayList<>();
    private List<String> revisedTxt = new ArrayList<>();
    private int old_ln;
    private int new_ln;

    private void finalizeChunk() {
        if (!originalTxt.isEmpty() || !revisedTxt.isEmpty()) {
            actualFile.getPatch().addDelta(new ChangeDelta<>(new Chunk<>(
                    old_ln - 1, originalTxt), new Chunk<>(
                    new_ln - 1, revisedTxt)));
            old_ln = 0;
            new_ln = 0;
            originalTxt.clear();
            revisedTxt.clear();
        }
    }

    private void processNormalLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
        revisedTxt.add(cline);
    }

    private void processAddLine(MatchResult match, String line) {
        String cline = line.substring(1);
        revisedTxt.add(cline);
    }

    private void processDelLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
    }

    private void processChunk(MatchResult match, String chunkStart) {
        finalizeChunk();
        old_ln = match.group(1) == null ? 1 : Integer.parseInt(match.group(1));
        new_ln = match.group(3) == null ? 1 : Integer.parseInt(match.group(3));
        if (old_ln == 0) {
            old_ln = 1;
        }
        if (new_ln == 0) {
            new_ln = 1;
        }
    }

    private void processIndex(MatchResult match, String line) {
        initFileIfNecessary();
        LOG.log(Level.INFO, "index {0}", line);
        actualFile.setIndex(line.substring(6));
    }

    private void processFromFile(MatchResult match, String line) {
        initFileIfNecessary();
        actualFile.setFromFile(extractFileName(line));
    }

    private void processToFile(MatchResult match, String line) {
        initFileIfNecessary();
        actualFile.setToFile(extractFileName(line));
    }

    private String extractFileName(String line) {
        return line.substring(4).replaceFirst("^(a|b)\\/", "");
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
