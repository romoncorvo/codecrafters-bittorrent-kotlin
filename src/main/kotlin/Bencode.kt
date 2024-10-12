import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream


class Bencode {
    fun type(bytes: ByteArray?): Type<*> {
        if (bytes == null) throw java.lang.NullPointerException("bytes cannot be null")

        val `in` = BencodeInputStream(ByteArrayInputStream(bytes))

        try {
            return `in`.nextType()
        } catch (t: Throwable) {
            throw BencodeException("Exception thrown during type detection", t)
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> decode(bytes: ByteArray?, type: Type<T>?): T {
        if (bytes == null) throw NullPointerException("bytes cannot be null")
        if (type == null) throw NullPointerException("type cannot be null")
        require(type != Type.UNKNOWN) { "type cannot be UNKNOWN" }

        val `in` = BencodeInputStream(ByteArrayInputStream(bytes))

        try {
            if (type == Type.NUMBER) return `in`.readNumber() as T
            if (type == Type.LIST) return `in`.readList() as T
            if (type == Type.DICTIONARY) return `in`.readDictionary() as T
            return `in`.readStringBytes() as T
        } catch (t: Throwable) {
            throw BencodeException("Exception thrown during decoding", t)
        }
    }

    fun encode(s: String?): ByteArray {
        if (s == null) throw java.lang.NullPointerException("s cannot be null")

        return encode(s, Type.STRING)
    }

    fun encode(n: Number?): ByteArray {
        if (n == null) throw java.lang.NullPointerException("n cannot be null")

        return encode(n, Type.NUMBER)
    }

    fun encode(l: Iterable<*>?): ByteArray {
        if (l == null) throw java.lang.NullPointerException("l cannot be null")

        return encode(l, Type.LIST)
    }

    fun encode(m: Map<*, *>?): ByteArray {
        if (m == null) throw java.lang.NullPointerException("m cannot be null")

        return encode(m, Type.DICTIONARY)
    }

    private fun encode(o: Any, type: Type<*>): ByteArray {
        val out = ByteArrayOutputStream()
        val bencode = BencodeOutputStream(out)

        try {
            if (type == Type.STRING) bencode.writeString(o as String)
            else if (type == Type.NUMBER) bencode.writeNumber(o as Number)
            else if (type == Type.LIST) bencode.writeList(o as Iterable<*>)
            else if (type == Type.DICTIONARY) bencode.writeDictionary(o as Map<*, *>)
        } catch (t: Throwable) {
            throw BencodeException("Exception thrown during encoding", t)
        }

        return out.toByteArray()
    }

    companion object {
        const val NUMBER: Char = 'i'
        const val LIST: Char = 'l'
        const val DICTIONARY: Char = 'd'
        const val TERMINATOR: Char = 'e'
        const val SEPARATOR: Char = ':'
    }
}

