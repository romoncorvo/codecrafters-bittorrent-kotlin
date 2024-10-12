internal class TypeValidator(private val type: Char) : Validator {
    override fun validate(token: Int): Boolean {
        return token == type.code
    }
}