class Type<T> private constructor(validator: Validator) {
    private val validator: Validator = validator

    fun validate(token: Int): Boolean {
        return validator.validate(token)
    }

    companion object {
        val STRING: Type<String> = Type<String>(StringValidator())
        val NUMBER: Type<Long> = Type(TypeValidator(Bencode.NUMBER))
        val LIST: Type<List<Any>> = Type(TypeValidator(Bencode.LIST))
        val DICTIONARY: Type<Map<String, Any>> = Type(TypeValidator(Bencode.DICTIONARY))
        val UNKNOWN: Type<Void> = Type<Void>(object : Validator {
            override fun validate(token: Int): Boolean {
                return false
            }
        })

        fun values(): Array<Type<*>> {
            return arrayOf(STRING, NUMBER, LIST, DICTIONARY, UNKNOWN)
        }
    }
}