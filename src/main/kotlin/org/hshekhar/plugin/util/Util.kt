package org.hshekhar.plugin.util

/**
 * @created 8/13/2022'T'12:44 PM
 * @author Himanshu Shekhar
 **/

object Util {

    private val snakeRegex = "_[a-zA-Z]".toRegex()
    private val camelRegex = "(?<=[a-zA-Z])[A-Z]".toRegex()

    fun between(text: String, delimiter: String = "\""): String {
        val firstIdx = text.indexOf(string = delimiter, startIndex = 0, ignoreCase = true)
        val lastIdx = text.lastIndexOf(string = delimiter, startIndex = text.length - 1, ignoreCase = true)

        return text.subSequence(firstIdx + 1, lastIdx).toString();
    }

    fun snakeToLowerCamelCase(value: String): String {
        return snakeRegex.replace(value) {
            it.value.replace("_","")
                .lowercase()
        }
    }

    fun camelToSnakeCase(value: String): String {
        return camelRegex.replace(value) {
            "_${it.value}"
        }.lowercase()
    }

}