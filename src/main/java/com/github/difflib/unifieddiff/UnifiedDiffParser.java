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
public final class UnifiedDiffParser {

    static final Pattern UNIFIED_DIFF_CHUNK_REGEXP = Pattern.compile("^@@\\s+-(?:(\\d+)(?:,(\\d+))?)\\s+\\+(?:(\\d+)(?:,(\\d+))?)\\s+@@");

    private final UnifiedDiffReader READER;
    private final UnifiedDiff data = new UnifiedDiff();
    private final UnifiedDiffLine[] MAIN_PARSER_RULES = new UnifiedDiffLine[]{
        new UnifiedDiffLine(true, "^diff\\s", this::processDiff),
        new UnifiedDiffLine(true, "^index\\s[\\da-zA-Z]+\\.\\.[\\da-zA-Z]+(\\s(\\d+))?$", this::processIndex),
        new UnifiedDiffLine(true, "^---\\s", this::processFromFile),
        new UnifiedDiffLine(true, "^\\+\\+\\+\\s", this::processToFile),
        new UnifiedDiffLine(false, UNIFIED_DIFF_CHUNK_REGEXP, this::processChunk),
        new UnifiedDiffLine("^\\s+", this::processNormalLine),
        new UnifiedDiffLine("^-", this::processDelLine),
        new UnifiedDiffLine("^+", this::processAddLine)
    };

    private UnifiedDiffFile actualFile;

    UnifiedDiffParser(Reader reader) {
        this.READER = new UnifiedDiffReader(reader);
    }

    // schema = [[/^\s+/, normal], [/^diff\s/, start], [/^new file mode \d+$/, new_file], 
    // [/^deleted file mode \d+$/, deleted_file], [/^index\s[\da-zA-Z]+\.\.[\da-zA-Z]+(\s(\d+))?$/, index], 
    // [/^---\s/, from_file], [/^\+\+\+\s/, to_file], [/^@@\s+\-(\d+),?(\d+)?\s+\+(\d+),?(\d+)?\s@@/, chunk], 
    // [/^-/, del], [/^\+/, add], [/^\\ No newline at end of file$/, eof]];
    private UnifiedDiff parse() throws IOException, UnifiedDiffParserException {
        boolean header = true;
        String headerTxt = "";
        String tailTxt = "";
        while (READER.ready()) {
            String line = READER.readLine();
            if (line.matches("^\\-\\-\\s+")) {
                break;
            } else {
                LOG.log(Level.INFO, "parsing line {0}", line);
                if (processLine(header, line) == false) {
                    if (header) {
                        headerTxt += line + "\n";
                    } else {
                        break;
                    }
                } else {
                    header = false;
                    data.setHeader(headerTxt);
                }
            }
        }

        finalizeChunk();

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

    private static final Logger LOG = Logger.getLogger(UnifiedDiffParser.class.getName());

    public static UnifiedDiff parseUnifiedDiff(InputStream stream) throws IOException, UnifiedDiffParserException {
        UnifiedDiffParser parser = new UnifiedDiffParser(new BufferedReader(new InputStreamReader(stream)));
        return parser.parse();
    }

    private boolean processLine(boolean header, String line) throws UnifiedDiffParserException {
        for (UnifiedDiffLine rule : MAIN_PARSER_RULES) {
            if (header && rule.isStopsHeaderParsing() || !header) {
                if (rule.processLine(line)) {
                    return true;
                }
            }
        }
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

    public void processDiff(MatchResult match, String line) {
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

    public void processNormalLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
        revisedTxt.add(cline);
    }

    public void processAddLine(MatchResult match, String line) {
        String cline = line.substring(1);
        revisedTxt.add(cline);
    }

    public void processDelLine(MatchResult match, String line) {
        String cline = line.substring(1);
        originalTxt.add(cline);
    }

    public void processChunk(MatchResult match, String chunkStart) {
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

    public void processIndex(MatchResult match, String line) {
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

    class UnifiedDiffLine {

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
    }
}

class UnifiedDiffReader extends BufferedReader {

    private String lastLine;

    public UnifiedDiffReader(Reader reader) {
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
