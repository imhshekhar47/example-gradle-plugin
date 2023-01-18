package org.hshekhar.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @created 8/13/2022'T'9:36 AM
 * @author Himanshu Shekhar
 **/

open class GeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.run {
            create(TASK_EXTENSION_NAME, DynamoExtension::class.java)
        }

        with(project.tasks) {
            create(PLUGIN_TASK_NAME, DynamoGeneratorTask::class.java) {
                it.group = "development"
                it.description = "Protobuf To Dynamo documents"
            }.doLast {
                if (extension.isDryMode) {
                    println("Running in dry mode: No changes made")
                }
                if (extension.isVerbose) {
                    println("Dynamo models generated")
                }
            }
        }

        project.afterEvaluate {
            return@afterEvaluate
        }
    }
}