import com.google.gson.Gson


// import com.dampcake.bencode.Bencode; - available if you need it!

val gson = Gson()

fun main(args: Array<String>) {
    // You can use print statements as follows for debugging, they'll be visible when running tests.
    when (val command = args[0]) {
        "decode" -> {
            // Uncomment this block to pass the first stage
            val bencodedValue = args[1]
            val bencode = Bencode()
            val type = bencode.type(bencodedValue.toByteArray())
            val decoded = bencode.decode(bencodedValue.toByteArray(), type)
            println(gson.toJson(decoded))
            return
        }

        else -> println("Unknown command $command")
    }
}
