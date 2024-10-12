import java.io.*
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*


class BencodeOutputStream(out: OutputStream?) :
    FilterOutputStream(out) {

    @Throws(IOException::class)
    fun writeString(s: String?) {
        write(encode(s))
    }

    @Throws(IOException::class)
    fun writeString(buff: ByteBuffer) {
        write(encode(buff.array()))
    }

    @Throws(IOException::class)
    fun writeString(array: ByteArray?) {
        write(encode(array))
    }

    @Throws(IOException::class)
    fun writeNumber(n: Number?) {
        write(encode(n))
    }

    @Throws(IOException::class)
    fun writeList(l: Iterable<*>?) {
        write(encode(l))
    }

    @Throws(IOException::class)
    fun writeDictionary(m: Map<*, *>?) {
        write(encode(m)!!)
    }

    @Throws(IOException::class)
    private fun encode(s: String?): ByteArray {
        if (s == null) throw NullPointerException("s cannot be null")

        val buffer = ByteArrayOutputStream()
        val bytes = s.toByteArray(Charset.forName("UTF-8"))
        buffer.write(bytes.size.toString().toByteArray(Charset.forName("UTF-8")))
        buffer.write(Bencode.SEPARATOR.code)
        buffer.write(bytes)

        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun encode(b: ByteArray?): ByteArray {
        if (b == null) throw NullPointerException("b cannot be null")

        val buffer = ByteArrayOutputStream()

        buffer.write(b.size.toString().toByteArray(Charset.forName("UTF-8")))
        buffer.write(Bencode.SEPARATOR.code)
        buffer.write(b)

        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun encode(n: Number?): ByteArray {
        if (n == null) throw NullPointerException("n cannot be null")

        val buffer = ByteArrayOutputStream()
        buffer.write(Bencode.NUMBER.code)
        buffer.write(n.toLong().toString().toByteArray(Charset.forName("UTF-8")))
        buffer.write(Bencode.TERMINATOR.code)

        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun encode(l: Iterable<*>?): ByteArray {
        if (l == null) throw NullPointerException("l cannot be null")

        val buffer = ByteArrayOutputStream()
        buffer.write(Bencode.LIST.code)
        for (o in l) buffer.write(encodeObject(o))
        buffer.write(Bencode.TERMINATOR.code)

        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun encode(m: Map<*, *>?): ByteArray? {
        if (m == null) throw java.lang.NullPointerException("m cannot be null")

        val map: Map<*, *> = if (m !is SortedMap<*, *>) TreeMap<Any, Any>(m)
        else m

        val buffer = ByteArrayOutputStream()
        buffer.write(Bencode.DICTIONARY.code)

        for ((key, value) in map.entries) {
            buffer.write(encode(key.toString()))
            buffer.write(encodeObject(value))
        }
        buffer.write(Bencode.TERMINATOR.code)

        return buffer.toByteArray()
    }

    @Throws(IOException::class)
    private fun encodeObject(o: Any?): ByteArray {
        if (o == null) throw NullPointerException("Cannot write null objects")

        if (o is Number) return encode(o as Number?)
        if (o is Iterable<*>) return encode(o as Iterable<*>?)
        if (o is Map<*, *>) return encode(o as Map<*, *>?)!!
        if (o is ByteBuffer) return encode(o.array())
        if (o is ByteArray) return encode(o as ByteArray?)

        return encode(o.toString())
    }
}