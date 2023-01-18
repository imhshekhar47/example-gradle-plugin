package org.hshekhar.plugin

/**
 * @created 8/13/2022'T'10:00 AM
 * @author Himanshu Shekhar
 **/
open class DynamoExtension(
    var isDryMode: Boolean = false,
    var isVerbose: Boolean = false,
    var inputDir: String = "",
    var packageName: String = "",
    var documentSuffix: String = "Document",
    var outputDir: String = "",
    var dynamoTablePrefix: String = ""
)