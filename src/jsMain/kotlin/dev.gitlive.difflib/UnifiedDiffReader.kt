package dev.gitlive.difflib

import dev.gitlive.difflib.unifieddiff.UnifiedDiff
import dev.gitlive.difflib.unifieddiff.UnifiedDiffReader
import dev.gitlive.difflib.unifieddiff.UnifiedDiffReader.Companion.parseUnifiedDiff
import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

actual typealias InputStream = dynamic

val readline by lazy { js("require")("readline") }

actual suspend fun UnifiedDiffReader.readLine(stream: InputStream): UnifiedDiff {
    val rl = readline.createInterface(json("input" to stream))
    val iterator = rl[js("Symbol").asyncIterator]()
    return parseUnifiedDiff {
        val promise = iterator.next() as Promise<String>
        promise.await()
    }
}
