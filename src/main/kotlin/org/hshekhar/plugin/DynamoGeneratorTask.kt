package org.hshekhar.plugin

import com.github.mustachejava.DefaultMustacheFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.slf4j.LoggerFactory
import java.io.File
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * @created 8/13/2022'T'9:49 AM
 * @author Himanshu Shekhar
 **/

open class DynamoGeneratorTask: DefaultTask() {

    companion object {
        private val LOG = LoggerFactory.getLogger(DynamoGeneratorTask::class.java)
    }

    @TaskAction
    fun action() {
        LOG.info("entry: action()")
        val dateText = SimpleDateFormat("YYYY-MM-DD HH:SS:mm").format(Date())
        val extension = project.extensions.findByName(TASK_EXTENSION_NAME) as DynamoExtension
        val outputDir = File(extension.outputDir)
        val inputDir = File(extension.inputDir)

        if(LOG.isDebugEnabled) {
            LOG.debug("[isVerbose] = ${extension.isVerbose}")
            LOG.debug("[isDryMode] = ${extension.isDryMode}")
            LOG.debug("[packageName] = ${extension.packageName}")
            LOG.debug("[inputDir] = ${extension.inputDir}")
            LOG.debug("[outputDir] = ${extension.outputDir}")
            LOG.debug("[dynamoTablePrefix] = ${extension.dynamoTablePrefix}")
        }

        val pkgDir = File(outputDir, extension.packageName.split(".").joinToString(File.separator))

        if(!extension.isDryMode) {
            if (!outputDir.exists()) {
                outputDir.mkdirs()
            }
            pkgDir.mkdirs()
        }

        if(inputDir.exists()) {
            val mustacheFactory = DefaultMustacheFactory()
            val mUtilTemplate = mustacheFactory.compile("dynamo-utils.mustache")
            val mEntityTemplate = mustacheFactory.compile("dynamo-entity.mustache")

            // write utils file

            val utilProps = mapOf(
                "packageName" to extension.packageName,
                "createdOn" to dateText,
                "author" to "Himanshu Shekhar<himanshu.kiit@gmail.com>",
            )

            if(!extension.isDryMode) {
                File(pkgDir, "ProtoUtils.kt").printWriter().use { writer ->
                    mUtilTemplate.execute(writer, utilProps)
                }
            } else {
                val utilSW = StringWriter()
                mUtilTemplate.execute(utilSW, utilProps);
                if (extension.isVerbose) {
                    println(utilSW.toString())
                }
                println("Created : ${pkgDir}/ProtoUtils.kt")
            }

            inputDir.walk()
                .filter { it.isFile && it.name.endsWith("proto") }
                //.filter { it.readLines()[0].contains("//dynamo", true) }
                .flatMap {
                    ProtoParser.parseFile(
                        protoFile = it,
                        config = ProtoParser.ParserConfig(
                            suffix = extension.documentSuffix,
                            tablePrefix = extension.dynamoTablePrefix,
                        )
                    )
                }
                .forEach {
                    val message = it.copy(packageName = extension.packageName)
                    val sw = StringWriter()
                    mEntityTemplate.execute(sw, message)

                    if(extension.isVerbose) {
                        println(sw.toString())
                    }

                    if(!extension.isDryMode) {
                        File(pkgDir, "${message.filename}.kt").printWriter().use { writer ->
                            mEntityTemplate.execute(writer, message)
                        }
                    }
                    println("Created : ${pkgDir}/${message.filename}.kt")
                }
        }

        if(!extension.isDryMode) {
            File(outputDir, ".proto2dynamo.log").printWriter().use {
                it.println(
                    """
                    Author: himanshu.kiit@gmail.com
                    Code generated at ${extension.outputDir}
                    by org.hshekhar.baas.plugin.proto2dynamo
                    on $dateText
                """.trimIndent()
                )
            }

            println("Created : ${outputDir}/.proto2dynamo.log")
        }

        LOG.info("exit: action()")
    }
}