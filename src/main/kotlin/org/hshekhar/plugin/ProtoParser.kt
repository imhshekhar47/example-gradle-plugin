package org.hshekhar.plugin

import org.hshekhar.plugin.util.Util
import java.io.File
import java.util.*

/**
 * @created 8/13/2022'T'12:01 PM
 * @author Himanshu Shekhar
 **/

data class ProtoField(
    var isRepeated: Boolean = false,
    var pType: String = "String",
    var name: String = "",
) {

    fun isId() = name == "id"

    fun getJavaType() = when (pType) {
        "int", "int32" -> "Int"
        "float" -> "Float"
        "string" -> "String"
        "bool" -> "Boolean"
        "google.protobuf.Timestamp" -> "Date"
        else -> "${pType}Document"
    }

    fun getType(): String {
        val jType = getJavaType()
        return if (isRepeated) "List<${jType}>" else jType
    }

    fun isConverterRequired(): Boolean {
        return getJavaType().endsWith("Document") && !isRepeated
    }

    fun getDefaultValue(): String {
        return if(isRepeated) {
            "listOf()"
        } else "null"
    }
}

data class ProtoMessage(
    var pName: String = "",
    val documentName: String = "",
    var fields: MutableList<ProtoField> = mutableListOf()
) {
    fun hasId() = fields.any { it.name == "id" }
}

data class ProtoDef(
    var file: String = "",
    var packageName: String? = null,
    var messages: MutableList<ProtoMessage> = mutableListOf()
)

data class MessageFile(
    val isEntity: Boolean = false,
    val tableName: String,
    val filename: String,
    val source: String,
    val packageName: String,
    val message: ProtoMessage
)

object ProtoParser {

    data class ParserConfig(
        val suffix: String = "Document",
        val tablePrefix: String = "",
    )

    fun parseFile(protoFile: File, config: ParserConfig = ParserConfig()): List<MessageFile> {
        val protoDef = ProtoDef(file = protoFile.absolutePath)
        var fileData = protoFile.readText()

        val messageStack = Stack<ProtoMessage>()
        fileData.lines()
            .filter { it.trim().isNotEmpty() }
            .forEach { line ->
                val tokens = line.trim().split("\\s+".toRegex())
                val tokenSize = tokens.size

                if (messageStack.isEmpty()) {
                    when (tokens[0]) {
                        "option" -> {
                            if (tokens[1] == "java_package") {
                                protoDef.packageName = Util.between(tokens[tokenSize - 1])
                            }
                        }

                        "message" -> {
                            val message = ProtoMessage(pName = tokens[1])
                            protoDef.messages.add(message)
                            messageStack.push(message)
                        }
                    }
                } else {
                    when (tokens[0]) {
                        "message" -> {
                            val message = ProtoMessage(pName = tokens[1])
                            protoDef.messages.add(message)
                            messageStack.push(message)
                        }

                        "repeated" -> {
                            val field = ProtoField(name = tokens[2], pType = tokens[1], isRepeated = true)
                            messageStack.peek().fields.add(field)
                        }

                        "}" -> {
                            val message = messageStack.pop()
                            println("Message ${message.pName} DONE")
                        }

                        else -> {
                            val field = ProtoField(name = tokens[1], pType = tokens[0])
                            messageStack.peek().fields.add(field)
                        }
                    }
                }
            }

        val packageName = protoDef.packageName
        val source = protoDef.file
        return protoDef.messages.map {
            val messageDocumentName = it.pName + config.suffix
            val tableName = Util.camelToSnakeCase(it.pName)
            val fullTableName = if (config.tablePrefix.isEmpty()) tableName else listOf(config.tablePrefix, tableName).joinToString("_")
            MessageFile(
                isEntity = it.hasId(),
                tableName = fullTableName,
                filename = messageDocumentName,
                source = source,
                packageName = packageName ?: "org.hshekhar",
                message = it.copy(documentName = messageDocumentName),
            )
        }
    }
}