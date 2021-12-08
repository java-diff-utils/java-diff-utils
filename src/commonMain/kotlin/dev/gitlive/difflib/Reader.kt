package dev.gitlive.difflib


expect abstract class Reader {
    open fun ready(): Boolean
}

expect open class BufferedReader(reader: Reader) : Reader {
    open fun readLine(): String?
}
expect class InputStreamReader(stream: InputStream) : Reader

expect abstract class InputStream