package dev.gitlive.difflib.unifieddiff

import kotlinx.coroutines.await
import kotlin.js.Promise
import kotlin.js.json

val readline by lazy { js("require")("readline") }

suspend fun UnifiedDiffReader.Companion.parseUnifiedDiff(buffer: dynamic): UnifiedDiff {
    val rl = readline.createInterface(json("input" to buffer))
    val iterator = rl[js("Symbol").asyncIterator]()
    return parseUnifiedDiff {
        val promise = iterator.next() as Promise<String>
        promise.await()
    }
}
