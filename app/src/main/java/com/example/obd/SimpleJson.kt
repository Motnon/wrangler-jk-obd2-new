package com.example.obd

/**
 * Lightweight, zero-dependency pure-Kotlin JSON parser.
 * Works identically on Android runtime and pure-JVM unit tests without org.json stubs.
 */
object SimpleJson {

    fun parseObject(json: String): Map<String, Any?> {
        val parser = Parser(json)
        val result = parser.parseValue()
        @Suppress("UNCHECKED_CAST")
        return result as? Map<String, Any?> ?: emptyMap()
    }

    fun Map<String, Any?>.obj(key: String): Map<String, Any?>? {
        @Suppress("UNCHECKED_CAST")
        return this[key] as? Map<String, Any?>
    }

    fun Map<String, Any?>.arr(key: String): List<Any?> {
        @Suppress("UNCHECKED_CAST")
        return this[key] as? List<Any?> ?: emptyList()
    }

    fun Map<String, Any?>.str(key: String, default: String = ""): String {
        return (this[key] as? String) ?: default
    }

    fun Map<String, Any?>.bool(key: String, default: Boolean = false): Boolean {
        return (this[key] as? Boolean) ?: default
    }

    fun Map<String, Any?>.num(key: String, default: Double = 0.0): Double {
        return (this[key] as? Number)?.toDouble() ?: default
    }

    private class Parser(private val src: String) {
        private var pos = 0

        fun parseValue(): Any? {
            skipWhitespace()
            if (pos >= src.length) return null
            return when (src[pos]) {
                '{' -> parseObject()
                '[' -> parseArray()
                '"' -> parseString()
                't', 'f' -> parseBoolean()
                'n' -> parseNull()
                else -> parseNumber()
            }
        }

        private fun parseObject(): Map<String, Any?> {
            val map = linkedMapOf<String, Any?>()
            pos++ // '{'
            skipWhitespace()
            if (pos < src.length && src[pos] == '}') {
                pos++
                return map
            }
            while (pos < src.length) {
                skipWhitespace()
                val key = parseString()
                skipWhitespace()
                if (pos < src.length && src[pos] == ':') pos++
                val value = parseValue()
                map[key] = value
                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                } else {
                    if (pos < src.length && src[pos] == '}') pos++
                    break
                }
            }
            return map
        }

        private fun parseArray(): List<Any?> {
            val list = mutableListOf<Any?>()
            pos++ // '['
            skipWhitespace()
            if (pos < src.length && src[pos] == ']') {
                pos++
                return list
            }
            while (pos < src.length) {
                list.add(parseValue())
                skipWhitespace()
                if (pos < src.length && src[pos] == ',') {
                    pos++
                } else {
                    if (pos < src.length && src[pos] == ']') pos++
                    break
                }
            }
            return list
        }

        private fun parseString(): String {
            if (pos >= src.length || src[pos] != '"') return ""
            pos++ // '"'
            val sb = StringBuilder()
            while (pos < src.length) {
                val ch = src[pos++]
                when (ch) {
                    '"' -> return sb.toString()
                    '\\' -> if (pos < src.length) {
                        when (val esc = src[pos++]) {
                            '"', '\\', '/' -> sb.append(esc)
                            'b' -> sb.append('\b')
                            'f' -> sb.append('\u000C')
                            'n' -> sb.append('\n')
                            'r' -> sb.append('\r')
                            't' -> sb.append('\t')
                            'u' -> if (pos + 4 <= src.length) {
                                val hex = src.substring(pos, pos + 4)
                                pos += 4
                                hex.toIntOrNull(16)?.let { sb.append(it.toChar()) }
                            }
                            else -> sb.append(esc)
                        }
                    }
                    else -> sb.append(ch)
                }
            }
            return sb.toString()
        }

        private fun parseBoolean(): Boolean {
            return if (src.startsWith("true", pos)) {
                pos += 4
                true
            } else {
                pos += 5
                false
            }
        }

        private fun parseNull(): Any? {
            pos += 4
            return null
        }

        private fun parseNumber(): Number {
            val start = pos
            while (pos < src.length && src[pos] in "-0123456789.eE+") {
                pos++
            }
            val token = src.substring(start, pos)
            return if (token.contains('.') || token.contains('e', ignoreCase = true)) {
                token.toDoubleOrNull() ?: 0.0
            } else {
                token.toLongOrNull() ?: 0L
            }
        }

        private fun skipWhitespace() {
            while (pos < src.length && src[pos].isWhitespace()) {
                pos++
            }
        }
    }
}
