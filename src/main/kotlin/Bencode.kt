import java.io.ByteArrayInputStream


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
            return `in`.readString() as T
        } catch (t: Throwable) {
            throw BencodeException("Exception thrown during decoding", t)
        }
    }

    companion object {
        const val NUMBER: Char = 'i'
        const val LIST: Char = 'l'
        const val DICTIONARY: Char = 'd'
        const val TERMINATOR: Char = 'e'
        const val SEPARATOR: Char = ':'
    }
}

