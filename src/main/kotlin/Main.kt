import com.google.common.hash.Hashing
import com.google.gson.Gson
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets

val gson = Gson()

@OptIn(ExperimentalStdlibApi::class)
fun main(args: Array<String>) {
    when (val command = args[0]) {
        "decode" -> {
            val bencodedValue = args[1]
            val decoded = decode(bencodedValue)
            println(gson.toJson(decoded))
            return
        }

        "info" -> {
            val fileName = args[1]
//            val file = File(fileName).readText(Charsets.UTF_8)
//            val decoded = decode(file) as Map<*, *>

            val file = File(fileName).inputStream().readBytes()
            val bencode = Bencode()
            val type = bencode.type(file)
            val decoded = bencode.decode(file, type) as Map<*, *>
            val info = decoded["info"] as Map<*, *>

            val bencodedInfo = bencode.encode(info)

            val hashedInfo = Hashing.sha1().hashBytes(bencodedInfo).asBytes().toHexString()
            println("Tracker URL: " + StandardCharsets.UTF_8.decode(decoded["announce"] as ByteBuffer).toString())
            println("Length: " + info["length"])
            println("Info Hash: " + hashedInfo)
            return
        }

        else -> println("Unknown command $command")
    }
}

fun decode(input: String): Any {
    return when {
        Character.isDigit(input[0]) -> {
            decodeString(input)
        }

        input[0] == 'i' -> {
            decodeNumber(input)
        }

        input[0] == 'l' -> {
            decodeList(MutableString(input))
        }

        input[0] == 'd' -> {
            decodeDictionary(MutableString(input))
        }

        else -> throw RuntimeException("Exception thrown during decoding")
    }
}

private fun decodeNumber(input: String): Long {
    val length = input.indexOfFirst { it == 'e' }
    return input.substring(1, length).toLong()
}

private fun decodeString(input: String): String {
    val firstColonIndex = input.indexOfFirst { it == ':' }
    val length = Integer.parseInt(input.substring(0, firstColonIndex))
    return input.substring(firstColonIndex + 1, firstColonIndex + 1 + length)
}

private fun decodeList(input: MutableString): List<Any> {
    input.value = input.value.substring(1)
    val result = mutableListOf<Any>()

    while (input.value.isNotEmpty() && input.value[0] != 'e') {
        val element = decode(input.value)
        result.add(element)
        val encodedEl = encode(element)
        input.value = input.value.substring(encodedEl.length)
    }

    return result
}

private fun decodeDictionary(input: MutableString): Map<String, Any> {
    input.value = input.value.substring(1)
    val result = mutableMapOf<String, Any>()

    while (input.value.isNotEmpty() && input.value[0] != 'e') {
        val key = decode(input.value) as String
        val encodedKeyEl = encode(key)
        input.value = input.value.substring(encodedKeyEl.length)

        val value = decode(input.value)
        val encodedValEl = encode(value)
        input.value = input.value.substring(encodedValEl.length)

        result[key] = value
    }

    return result
}

private fun encode(input: Any?): String {
    return when (input) {
        is Long -> {
            "i${input}e"
        }

        is String -> {
            "${input.length}:${input}"
        }

        is List<*> -> {
            "l${input.joinToString(separator = "") { x -> encode(x) }}e"
        }

        is Map<*, *> -> {
            "d${input.entries.joinToString(separator = "") { x -> encode(x.key) + encode(x.value) }}e"
        }

        else -> throw RuntimeException("Exception thrown during decoding")
    }
}

data class MutableString(var value: String)


