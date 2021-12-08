package dev.gitlive.difflib

val fs by lazy { js("require")("fs") }
val readline by lazy { js("require")("readline/promises") }

actual abstract class Reader internal constructor(internal val buffer: Any) {
    internal val byteArray = js("Int8Array").from(buffer).unsafeCast<ByteArray>()
    actual open fun ready(): Boolean = byteArray.isNotEmpty()
}

actual open class BufferedReader actual constructor(reader: Reader) : Reader(reader.buffer) {
    private var index = 0
    private var currentBuffer: ByteArray = byteArrayOf()
    
    override fun ready(): Boolean {
        currentBuffer = byteArray
        return super.ready()
    }

    actual open fun readLine(): String? {
        val nextNewLine = currentBuffer.indexOfFirst { it.toInt() == 0x0A }
        if (nextNewLine == -1) return null
        index = nextNewLine
        currentBuffer = currentBuffer.slice(nextNewLine + 1 until currentBuffer.size).toByteArray()
        return currentBuffer.slice(index until nextNewLine).toString().trimEnd()
    }
}

actual class InputStreamReader actual constructor(private val stream: InputStream) : Reader(stream.buffer) {
    override fun ready(): Boolean {
//        stream.buffer
        return false
    }
}

actual abstract class InputStream constructor(internal val buffer: Any)

public class BufferInputStream(internal val inputBuffer: Any) : InputStream(inputBuffer)
