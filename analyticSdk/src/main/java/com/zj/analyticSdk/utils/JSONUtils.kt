package com.zj.analyticSdk.utils

import com.zj.analyticSdk.CALogs.printStackTrace

internal object JSONUtils {

    private fun addIndentBlank(sb: StringBuilder, indent: Int) {
        try {
            for (i in 0 until indent) {
                sb.append('\t')
            }
        } catch (e: Exception) {
            printStackTrace(e)
        }
    }

    fun formatJson(jsonStr: String?): String {
        return try {
            if (null == jsonStr || "" == jsonStr) {
                return ""
            }
            val sb = StringBuilder()
            var last: Char
            var current = '\u0000'
            var indent = 0
            var isInQuotationMarks = false
            for (element in jsonStr) {
                last = current
                current = element
                when (current) {
                    '"' -> {
                        if (last != '\\') {
                            isInQuotationMarks = !isInQuotationMarks
                        }
                        sb.append(current)
                    }
                    '{', '[' -> {
                        sb.append(current)
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            indent++
                            addIndentBlank(sb, indent)
                        }
                    }
                    '}', ']' -> {
                        if (!isInQuotationMarks) {
                            sb.append('\n')
                            indent--
                            addIndentBlank(sb, indent)
                        }
                        sb.append(current)
                    }
                    ',' -> {
                        sb.append(current)
                        if (last != '\\' && !isInQuotationMarks) {
                            sb.append('\n')
                            addIndentBlank(sb, indent)
                        }
                    }
                    '\\' -> {
                    }
                    else -> sb.append(current)
                }
            }
            sb.toString()
        } catch (e: Exception) {
            printStackTrace(e)
            ""
        }
    }
}