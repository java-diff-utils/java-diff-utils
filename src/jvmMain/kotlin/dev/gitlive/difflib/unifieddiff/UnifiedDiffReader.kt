package dev.gitlive.difflib.unifieddiff

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

suspend fun UnifiedDiffReader.Companion.parseUnifiedDiff(stream: InputStream): UnifiedDiff {
    val reader = BufferedReader(InputStreamReader(stream))
    return parseUnifiedDiff { reader.readLine() }
}

