package dev.gitlive.difflib.unifieddiff

import java.io.BufferedReader
import java.io.InputStreamReader

actual typealias InputStream = java.io.InputStream

actual suspend fun UnifiedDiffReader.Companion.readLine(stream: InputStream): UnifiedDiff {
    val reader = BufferedReader(InputStreamReader(stream))
    return parseUnifiedDiff { reader.readLine() }
}

