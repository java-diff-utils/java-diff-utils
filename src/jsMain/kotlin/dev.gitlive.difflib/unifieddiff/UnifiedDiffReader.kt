package dev.gitlive.difflib.unifieddiff

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

val readline by lazy { js("require")("readline") }

actual abstract class InputStream (val buffer: dynamic)

actual suspend fun UnifiedDiffReader.Companion.readLine(stream: InputStream): UnifiedDiff {
    val rl = readline.createInterface(json("input" to stream.buffer))
    val iterator = rl[js("Symbol").asyncIterator]()
    return parseUnifiedDiff {
        val promise = iterator.next() as Promise<String>
        promise.await()
    }
}
