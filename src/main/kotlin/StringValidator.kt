internal class StringValidator : Validator {
    override fun validate(token: Int): Boolean {
        return Character.isDigit(token)
    }
}