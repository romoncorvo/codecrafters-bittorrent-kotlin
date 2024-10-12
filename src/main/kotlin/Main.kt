import com.google.gson.Gson


// import com.dampcake.bencode.Bencode; - available if you need it!

val gson = Gson()

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    when (val command = args[0]) {
        "decode" -> {
            // Uncomment this block to pass the first stage
            val bencodedValue = args[1]
            val decoded = decode(bencodedValue)
            println(gson.toJson(decoded))
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
    val result: MutableList<Any> = ArrayList()

    while (input.value.isNotEmpty() && input.value[0] != 'e') {
        val element = decode(input.value)
        result.add(element)
        val encodedEl = encode(element)
        input.value = input.value.substring(encodedEl.length)
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
            "l${input.joinToString { x -> encode(x) }}e"
        }

        else -> throw RuntimeException("Exception thrown during decoding")
    }
}

data class MutableString(var value: String)


