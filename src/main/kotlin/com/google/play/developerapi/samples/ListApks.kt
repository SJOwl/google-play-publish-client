package com.google.play.developerapi.samples

import com.google.api.client.repackaged.com.google.common.base.Preconditions
import com.google.api.client.repackaged.com.google.common.base.Strings
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl.PACKAGE_NAME
import org.apache.commons.logging.LogFactory

/**
 * Lists all the apks for a given app.
 */
object ListApks {

    private val log = LogFactory.getLog(ListApks::class.java)

    @JvmStatic fun main(args: Array<String>) {

        val config = PublishConfig(propertiesPath(args))

        try {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(config.packageName),
                "packageName cannot be null or empty!")

            // Create the API service.
            val service = AndroidPublisherHelper(config).init(
                config.applicationName, config.serviceAccountEmail)
            val edits = service.edits()

            // Create a new edit to make changes.
            val editRequest = edits
                .insert(PACKAGE_NAME,
                    null
                    /** no content  */)
            val appEdit = editRequest.execute()

            // Get a list of apks.
            val apksResponse = edits
                .apks()
                .list(PACKAGE_NAME,
                    appEdit.id).execute()

            // Print the apk info.
            for (apk in apksResponse.apks) {
                println(
                    String.format("Version: %d - Binary sha1: %s", apk.versionCode,
                        apk.binary.sha1))
            }
        } catch (ex: Exception) {
            log.error("Exception was thrown while updating listing", ex)
        }
    }
}

