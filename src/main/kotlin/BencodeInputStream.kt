import java.io.*
import java.math.BigDecimal


class BencodeInputStream(`in`: InputStream) :
    FilterInputStream(PushbackInputStream(`in`)) {

    private val pis: PushbackInputStream = (super.`in` as PushbackInputStream)

    private fun peek(): Int {
        val b = pis.read()
        pis.unread(b)
        return b
    }

    fun nextType(): Type<*> {
        val token = peek()
        checkEOF(token)

        return typeForToken(token)
    }

    private fun typeForToken(token: Int): Type<*> {
        for (type in Type.values()) {
            if (type.validate(token)) return type
        }

        return Type.UNKNOWN
    }

    fun readString(): String {
        return String(readStringBytesInternal())
    }

    private fun readStringBytesInternal(): ByteArray {
        var token = pis.read()
        validateToken(token, Type.STRING)

        val buffer = StringBuilder()
        buffer.append(token.toChar())
        while ((pis.read().also { token = it }) != Bencode.SEPARATOR.code) {
            validateToken(token, Type.STRING)

            buffer.append(token.toChar())
        }

        val length = buffer.toString().toInt()
        val bytes = ByteArray(length)
        read(bytes)
        return bytes
    }

    fun readNumber(): Long {
        var token = pis.read()
        validateToken(token, Type.NUMBER)

        val buffer = StringBuilder()
        while ((pis.read().also { token = it }) != Bencode.TERMINATOR.code) {
            checkEOF(token)

            buffer.append(token.toChar())
        }

        return BigDecimal(buffer.toString()).toLong()
    }

    fun readList(): List<Any> {
        var token = pis.read()
        validateToken(token, Type.LIST)

        val list: MutableList<Any> = ArrayList()
        while ((pis.read().also { token = it }) != Bencode.TERMINATOR.code) {
            checkEOF(token)

            list.add(readObject(token))
        }

        return list
    }

    fun readDictionary(): Map<String, Any> {
        var token = pis.read()
        validateToken(token, Type.DICTIONARY)

        val map: MutableMap<String, Any> = LinkedHashMap()
        while ((pis.read().also { token = it }) != Bencode.TERMINATOR.code) {
            checkEOF(token)

            pis.unread(token)
            map[readString()] = readObject(pis.read())
        }

        return map
    }

    private fun readObject(token: Int): Any {
        pis.unread(token)

        val type = typeForToken(token)

        if (type === Type.STRING) return readString()
        if (type === Type.NUMBER) return readNumber()
        if (type === Type.LIST) return readList()
        if (type === Type.DICTIONARY) return readDictionary()

        throw InvalidObjectException("Unexpected token '" + String(Character.toChars(token)) + "'")
    }

    private fun validateToken(token: Int, type: Type<*>) {
        checkEOF(token)

        if (!type.validate(token)) {
            pis.unread(token)
            throw InvalidObjectException("Unexpected token '" + String(Character.toChars(token)) + "'")
        }
    }

    @Throws(EOFException::class)
    private fun checkEOF(b: Int) {
        if (b == EOF) throw EOFException()
    }

    companion object {
        // EOF Constant
        private const val EOF = -1
    }
}