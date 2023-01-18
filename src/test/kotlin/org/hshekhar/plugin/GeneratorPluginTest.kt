package org.hshekhar.plugin

import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.createTempDirectory
import kotlin.test.Test

/**
 * @created 8/13/2022'T'10:14 AM
 * @author Himanshu Shekhar
 */


internal class GeneratorPluginTest {

    @Test
    @OptIn(ExperimentalPathApi::class)
    fun `should list generate task`() {
        val tmp = createTempDirectory("tmp").toFile()
        File(tmp, "build.gradle.kts").run {
            writeText("""
                plugins {
                    id ("org.hshekhar.plugin.example")
                }
                
                $TASK_EXTENSION_NAME {
                    isVerbose = true
                }
            """.trimIndent())
        }

        val build  = GradleRunner.create()
            .withProjectDir(tmp)
            .withPluginClasspath()
            .withArguments("tasks", "--all")
            .build()

        println(build.output)
        assert(build.output.contains("Protobuf To Dynamo documents"))
    }

    @Test
    @OptIn(ExperimentalPathApi::class)
    fun `should run proto2dynamo task`() {
        val tmp = createTempDirectory("tmp").toFile()
        File(tmp, "build.gradle.kts").run {
            writeText("""
                plugins {
                    id ("org.hshekhar.plugin.example")
                }
                
                $TASK_EXTENSION_NAME {
                    isVerbose = true
                    packageName = "org.hshekhar.dynamo"
                    inputDir = "${'$'}projectDir/sample" 
                    outputDir = "${'$'}projectDir/build/code"
                }
            """.trimIndent())
        }

        val build  = GradleRunner.create()
            .withProjectDir(tmp)
            .withPluginClasspath()
            .withArguments("proto2dynamo")
            .build()

        println(build.output)
        assert(build.task(":proto2dynamo")!!.outcome == TaskOutcome.SUCCESS)
        assert(File(tmp, "build/code/.proto2dynamo.log").exists())
    }

    @Test
    @OptIn(ExperimentalPathApi::class)
    fun `should generate models`() {
        val tmp = createTempDirectory("tmp").toFile()
        println("tmp: ${tmp.absolutePath}")
        val protoSrcDir = File(tmp, "proto")
        protoSrcDir.mkdirs()

        File(protoSrcDir, "message_entity.proto").printWriter().use {
            it.println("""
                //dynamo
                syntax = "proto3";

                option java_package = "org.hshekhar.baas";
                option java_multiple_files = true;

                package proto;

                message MyFile {
                  message MyFileProps {
                    bool isReadOnly = 1;
                    bool isFile = 2;
                  }
                  string name = 1;
                  repeated string tags = 2;
                }
                
                message MyFolder {
                  string id = 1;
                  repeated MyFile files = 2;
                }
            """.trimIndent())
        }

        File(tmp, "build.gradle.kts").run {
            writeText("""
                plugins {
                    id ("org.hshekhar.plugin.example")
                }
                
                $TASK_EXTENSION_NAME {
                    isDryMode = true    
                    isVerbose = true
                    packageName = "org.hshekhar.baas.dynamo"
                    dynamoTablePrefix = "baas"
                    inputDir = "${'$'}projectDir/proto" 
                    outputDir = "${'$'}projectDir/build/dynamo"
                }
            """.trimIndent())
        }

        val build  = GradleRunner.create()
            .withProjectDir(tmp)
            .withPluginClasspath()
            .withArguments("proto2dynamo")
            .build()

        println(build.output)
    }
}