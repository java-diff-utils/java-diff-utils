package dev.gitlive.difflib.unifieddiff

import dev.gitlive.difflib.*
import dev.gitlive.difflib.patch.ChangeDelta
import dev.gitlive.difflib.patch.Chunk


/**
 *
 * @author Tobias Warneke (t.warneke@gmx.net)
 */
class UnifiedDiffReader internal constructor(lineReader: LineReader) {
    private val nextLine: LineReader
    private val data = UnifiedDiff()
    private val DIFF_COMMAND: UnifiedDiffLine = UnifiedDiffLine(true, "^diff\\s") { match: MatchResult, line: String ->
        processDiff(
            match,
            line
        )
    }
    private val SIMILARITY_INDEX: UnifiedDiffLine = UnifiedDiffLine(true, "^similarity index (\\d+)%$") { match: MatchResult, line: String ->
        processSimilarityIndex(
            match,
            line
        )
    }
    private val INDEX: UnifiedDiffLine = UnifiedDiffLine(true, "^index\\s[\\da-zA-Z]+\\.\\.[\\da-zA-Z]+(\\s(\\d+))?$") { match: MatchResult, line: String ->
        processIndex(
            match,
            line
        )
    }
    private val FROM_FILE: UnifiedDiffLine = UnifiedDiffLine(true, "^---\\s") { match: MatchResult, line: String ->
        processFromFile(
            match,
            line
        )
    }
    private val TO_FILE: UnifiedDiffLine = UnifiedDiffLine(true, "^\\+\\+\\+\\s") { match: MatchResult, line: String ->
        processToFile(
            match,
            line
        )
    }
    private val RENAME_FROM: UnifiedDiffLine = UnifiedDiffLine(true, "^rename\\sfrom\\s(.+)$") { match: MatchResult, line: String ->
        processRenameFrom(
            match,
            line
        )
    }
    private val RENAME_TO: UnifiedDiffLine = UnifiedDiffLine(true, "^rename\\sto\\s(.+)$") { match: MatchResult, line: String ->
        processRenameTo(
            match,
            line
        )
    }
    private val NEW_FILE_MODE: UnifiedDiffLine = UnifiedDiffLine(true, "^new\\sfile\\smode\\s(\\d+)") { match: MatchResult, line: String ->
        processNewFileMode(
            match,
            line
        )
    }
    private val DELETED_FILE_MODE: UnifiedDiffLine = UnifiedDiffLine(true, "^deleted\\sfile\\smode\\s(\\d+)") { match: MatchResult, line: String ->
        processDeletedFileMode(
            match,
            line
        )
    }
    private val CHUNK: UnifiedDiffLine = UnifiedDiffLine(false, UNIFIED_DIFF_CHUNK_REGEXP) { match: MatchResult, chunkStart: String ->
        processChunk(
            match,
            chunkStart
        )
    }
    private val LINE_NORMAL = UnifiedDiffLine("^\\s") { match: MatchResult, line: String ->
        processNormalLine(
            match,
            line
        )
    }
    private val LINE_DEL = UnifiedDiffLine("^-") { match: MatchResult, line: String ->
        processDelLine(
            match,
            line
        )
    }
    private val LINE_ADD = UnifiedDiffLine("^\\+") { match: MatchResult, line: String ->
        processAddLine(
            match,
            line
        )
    }
    private var actualFile: UnifiedDiffFile? = null

    // schema = [[/^\s+/, normal], [/^diff\s/, start], [/^new file mode \d+$/, new_file],
    // [/^deleted file mode \d+$/, deleted_file], [/^index\s[\da-zA-Z]+\.\.[\da-zA-Z]+(\s(\d+))?$/, index],
    // [/^---\s/, from_file], [/^\+\+\+\s/, to_file], [/^@@\s+\-(\d+),?(\d+)?\s+\+(\d+),?(\d+)?\s@@/, chunk],
    // [/^-/, del], [/^\+/, add], [/^\\ No newline at end of file$/, eof]];
    private suspend fun parse(): UnifiedDiff {
        var line: String? = nextLine()
        while (line != null) {
            var headerTxt = ""
            while (line != null) {
                headerTxt += if (validLine(
                        line, DIFF_COMMAND, SIMILARITY_INDEX, INDEX,
                        FROM_FILE, TO_FILE,
                        RENAME_FROM, RENAME_TO,
                        NEW_FILE_MODE, DELETED_FILE_MODE,
                        CHUNK
                    )
                ) {
                    break
                } else {
                    """
                    $line
                    
                    """.trimIndent()
                }
                line = nextLine()
            }
            if ("" != headerTxt) {
                data.header = headerTxt
            }
            if (line != null && !CHUNK.validLine(line)) {
                initFileIfNecessary()
                while (line != null && !CHUNK.validLine(line)) {
                    if (!processLine(
                            line, DIFF_COMMAND, SIMILARITY_INDEX, INDEX,
                            FROM_FILE, TO_FILE,
                            RENAME_FROM, RENAME_TO,
                            NEW_FILE_MODE, DELETED_FILE_MODE
                        )
                    ) {
                        throw UnifiedDiffParserException("expected file start line not found")
                    }
                    line = nextLine()
                }
            }
            if (line != null) {
                processLine(line, CHUNK)
                line = nextLine()
                var tempNewLine: String? = null
                while (line != null) {
                    line = checkForNoNewLineAtTheEndOfTheFile(line)
                    if (!processLine(line, LINE_NORMAL, LINE_ADD, LINE_DEL)) {
                        // To help debugging tests
                        line = nextLine()
                        if (line != null && line.isNotEmpty()) {
                            throw UnifiedDiffParserException("unlikely empty string found in CHUNK before: $line")
                        }
                        throw UnifiedDiffParserException("expected data line not found")
                    }
                    if (originalTxt.size == old_size && revisedTxt.size == new_size
                        || old_size == 0 && new_size == 0 && originalTxt.size == old_ln && revisedTxt.size == new_ln
                    ) {
                        val isNormalLine = line?.let { NORMAL_LINE_TEXT.containsMatchIn(it) } ?: false
                        if (!isNormalLine) {
                            tempNewLine = nextLine()
                            if (NO_NEW_LINE_TEXT != tempNewLine && actualFile!!.isNoNewLineAtTheEndOfTheFile) {
                                revisedTxt.add("")
                                new_size++
                            } else if (NO_NEW_LINE_TEXT == tempNewLine && !actualFile!!.isNoNewLineAtTheEndOfTheFile) {
                                originalTxt.add("")
                                old_size++
                            }
                        }
                        
                        finalizeChunk()
                        break
                    }
                    line = nextLine()
                }
                line = tempNewLine ?: nextLine()
                line = checkForNoNewLineAtTheEndOfTheFile(line)
            }
            if (line == null || line.startsWith("--") && !line.startsWith("---")) {
                break
            }
        }

        var tailTxt = ""
        line = nextLine()
        while (line != null) {
            if (tailTxt.isNotEmpty()) {
                tailTxt += "\n"
            }
            tailTxt += line
            line = nextLine()
        }
        data.setTailTxt(tailTxt)
        return data
    }

    private suspend fun checkForNoNewLineAtTheEndOfTheFile(line: String?): String? {
        if (NO_NEW_LINE_TEXT == line) {
            actualFile!!.isNoNewLineAtTheEndOfTheFile = true
            return nextLine()
        }
        return line
    }

    private suspend fun processLine(line: String?, vararg rules: UnifiedDiffLine): Boolean {
        if (line == null) {
            return false
        }
        for (rule in rules) {
            if (rule.processLine(line)) {
                return true
            }
        }
        return false
    }

    private fun validLine(line: String?, vararg rules: UnifiedDiffLine): Boolean {
        if (line == null) {
            return false
        }
        for (rule in rules) {
            if (rule.validLine(line)) {
                return true
            }
        }
        return false
    }

    private fun initFileIfNecessary() {
        check(!(originalTxt.isNotEmpty() || revisedTxt.isNotEmpty()))
        actualFile = null
        if (actualFile == null) {
            actualFile = UnifiedDiffFile()
            data.addFile(actualFile!!)
        }
    }

    private fun processDiff(match: MatchResult, line: String) {
        val fromTo = parseFileNames(line)
        actualFile!!.fromFile = fromTo[0]
        actualFile!!.toFile = fromTo[1]
        actualFile!!.diffCommand = line
    }

    private fun processSimilarityIndex(match: MatchResult, line: String) {
        actualFile!!.similarityIndex = match.groupValues[1].toInt()
    }

    private val originalTxt: MutableList<String> = mutableListOf()
    private val revisedTxt: MutableList<String> = mutableListOf()
    private val addLineIdxList: MutableList<Int> = mutableListOf()
    private val delLineIdxList: MutableList<Int> = mutableListOf()
    private var old_ln = 0
    private var old_size = 0
    private var new_ln = 0
    private var new_size = 0
    private var delLineIdx = 0
    private var addLineIdx = 0
    private var additions = 0
    private var deletions = 0
    
    private fun finalizeChunk() {
        if (originalTxt.isNotEmpty() || revisedTxt.isNotEmpty()) {
            actualFile!!.patch.addDelta(
                ChangeDelta(
                    Chunk(old_ln - 1, originalTxt, delLineIdxList),
                    Chunk(new_ln - 1, revisedTxt, addLineIdxList)
                )
            )
            old_ln = 0
            new_ln = 0
            originalTxt.clear()
            revisedTxt.clear()
            addLineIdxList.clear()
            delLineIdxList.clear()
            actualFile!!.deletions = deletions
            deletions = 0
            delLineIdx = 0
            actualFile!!.additions = additions
            additions = 0
            addLineIdx = 0
        }
    }

    private fun processNormalLine(match: MatchResult, line: String) {
        val cline = line.substring(1)
        originalTxt.add(cline)
        revisedTxt.add(cline)
        delLineIdx++
        addLineIdx++
    }

    private fun processAddLine(match: MatchResult, line: String) {
        val cline = line.substring(1)
        revisedTxt.add(cline)
        addLineIdx++
        additions++
        addLineIdxList.add(new_ln - 1 + addLineIdx)
    }

    private fun processDelLine(match: MatchResult, line: String) {
        val cline = line.substring(1)
        originalTxt.add(cline)
        delLineIdx++
        deletions++
        delLineIdxList.add(old_ln - 1 + delLineIdx)
    }

    private fun processChunk(match: MatchResult, chunkStart: String) {
        // finalizeChunk();
        old_ln = toInteger(match, 1, 1)
        old_size = toInteger(match, 2, 1)
        new_ln = toInteger(match, 3, 1)
        new_size = toInteger(match, 4, 1)
        if (old_ln == 0) {
            old_ln = 1
        }
        if (new_ln == 0) {
            new_ln = 1
        }
    }

    private fun processIndex(match: MatchResult, line: String) {
        actualFile!!.index = line.substring(6)
    }

    private fun processFromFile(match: MatchResult, line: String) {
        actualFile!!.fromFile = extractFileName(line)
        actualFile!!.fromTimestamp = extractTimestamp(line)
    }

    private fun processToFile(match: MatchResult, line: String) {
        actualFile!!.toFile = extractFileName(line)
        actualFile!!.toTimestamp = extractTimestamp(line)
    }

    private fun processRenameFrom(match: MatchResult, line: String) {
        actualFile!!.renameFrom = match.groupValues[1]
    }

    private fun processRenameTo(match: MatchResult, line: String) {
        actualFile!!.renameTo = match.groupValues[1]
    }

    private fun processNewFileMode(match: MatchResult, line: String) { 
        actualFile!!.newFileMode = match.groupValues[1]
    }

    private fun processDeletedFileMode(match: MatchResult, line: String) {
        actualFile!!.deletedFileMode = match.groupValues[1]
    }

    private fun extractFileName(_line: String): String {
        var line = _line
        if (TIMESTAMP_REGEXP.containsMatchIn(_line)) {
            line = line.substring(0, TIMESTAMP_REGEXP.find(_line)!!.range.first)
        }
        line = line.split("\t").toTypedArray()[0]
        return line.substring(4).replaceFirst("^(a|b|old|new)(\\/)?".toRegex(), "")
            .trim { it <= ' ' }
    }

    private fun extractTimestamp(line: String): String? {
        return if (TIMESTAMP_REGEXP.containsMatchIn(line)) {
            TIMESTAMP_REGEXP.find(line)?.groupValues?.firstOrNull()
        } else null
    }

    internal inner class UnifiedDiffLine {
        private val pattern: Regex
        private val command: BiConsumer<MatchResult, String>
        val isStopsHeaderParsing: Boolean

        constructor(pattern: String, command: BiConsumer<MatchResult, String>) : this(false, pattern, command) {}
        constructor(stopsHeaderParsing: Boolean, pattern: String, command: BiConsumer<MatchResult, String>) {
            this.pattern = Regex(pattern)
            this.command = command
            isStopsHeaderParsing = stopsHeaderParsing
        }

        constructor(stopsHeaderParsing: Boolean, pattern: Regex, command: BiConsumer<MatchResult, String>) {
            this.pattern = pattern
            this.command = command
            isStopsHeaderParsing = stopsHeaderParsing
        }

        fun validLine(line: String): Boolean {
            return pattern.containsMatchIn(line)
        }

        suspend fun processLine(line: String): Boolean {
            return if (pattern.containsMatchIn(line)) {
                command(pattern.find(line)!!, line)
                true
            } else {
                false
            }
        }
        
        override fun toString(): String {
            return "UnifiedDiffLine{pattern=$pattern, stopsHeaderParsing=$isStopsHeaderParsing}"
        }
    }

    companion object {
        val UNIFIED_DIFF_CHUNK_REGEXP = """^@@\s+-(?:(\d+)(?:,(\d+))?)\s+\+(?:(\d+)(?:,(\d+))?)\s+@@""".toRegex()
        val TIMESTAMP_REGEXP = """(\d{4}-\d{2}-\d{2}[T ]\d{2}:\d{2}:\d{2}\.\d{3,})(?: [+-]\d+)?""".toRegex()
        val NORMAL_LINE_TEXT = """^\s""".toRegex()
        const val NO_NEW_LINE_TEXT = """\ No newline at end of file"""

        fun parseFileNames(line: String?): Array<String> {
            val split = line!!.split(" ").toTypedArray()
            return arrayOf(
                split[2].replace("""^a/""".toRegex(), ""),
                split[3].replace("""^b/""".toRegex(), "")
            )
        }

        /**
         * To parse a diff file use this method.
         *
         * @param stream This is the diff file data.
         * @return In a UnifiedDiff structure this diff file data is returned.
         * @throws IOException
         * @throws UnifiedDiffParserException
         */
        internal suspend fun parseUnifiedDiff(readLine: LineReader): UnifiedDiff {
            val parser = UnifiedDiffReader(readLine)
            return parser.parse()
        }

        private fun toInteger(match: MatchResult, group: Int, defValue: Int): Int {
            return match.groups[group]?.value?.toIntOrNull() ?: defValue
        }
    }

    init {
        nextLine = lineReader
    }
}
