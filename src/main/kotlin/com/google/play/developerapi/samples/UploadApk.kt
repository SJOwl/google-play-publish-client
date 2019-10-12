package com.google.play.developerapi.samples

import com.google.api.client.http.FileContent
import com.google.api.client.repackaged.com.google.common.base.Preconditions
import com.google.api.client.repackaged.com.google.common.base.Strings
import com.google.api.services.androidpublisher.model.LocalizedText
import com.google.api.services.androidpublisher.model.Track
import com.google.api.services.androidpublisher.model.TrackRelease
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.IOException
import java.net.URISyntaxException
import java.security.GeneralSecurityException
import java.util.*

/**
 * Uploads an apk to the alpha track.
 */
object UploadApk {

    private val log = LogFactory.getLog(UploadApk::class.java)

    @JvmStatic
    fun main(args: Array<String>) {

        println("config file = ${propertiesPath(args)}")
        val config = PublishConfig(propertiesPath(args))

        ApkManager(config).copyFiles()

        try {
            Preconditions.checkArgument(
                !Strings.isNullOrEmpty(config.packageName),
                "packageName cannot be null or empty!"
            )

            // Create the API service.
            val service = AndroidPublisherHelper(config).init(
                config.applicationName, config.serviceAccountEmail
            )
            val edits = service.edits()

            // Create a new edit to make changes to your listing.
            val editRequest = edits
                .insert(
                    config.packageName,
                    null
                    /** no content  */
                )
            val edit = editRequest.execute()
            val editId = edit.id
            log.info(String.format("Created edit with id: %s", editId))

            // Upload new apk to developer console
            val apkPath = File(config.apkFilePath).toURI().path
            val apkFile = FileContent(AndroidPublisherHelper.MIME_TYPE_APK, File(apkPath))
            val uploadRequest = edits
                .apks()
                .upload(
                    config.packageName,
                    editId,
                    apkFile
                )
            val apk = uploadRequest.execute()
            log.info(
                String.format(
                    "Version code %d has been uploaded",
                    apk.versionCode
                )
            )

            // Assign apk to alpha track.
            val apkVersionCodes = ArrayList<Long>()
            apkVersionCodes.add(java.lang.Long.valueOf(apk.versionCode!!.toLong()))
            val updateTrackRequest = edits
                .tracks()
                .update(
                    config.packageName,
                    editId,
                    config.track,
                    Track().setReleases(
                        listOf(
                            TrackRelease()
                                .setName(config.trackReleaseName)
                                .setVersionCodes(apkVersionCodes)
                                .setStatus("completed")
                                .setReleaseNotes(
                                    listOf(
                                        LocalizedText()
                                            .setLanguage(config.releaseNotesLanguage)
                                            .setText(config.releaseNotesText)
                                    )
                                )
                        )
                    )
                )
            val updatedTrack = updateTrackRequest.execute()
            log.info(String.format("Track %s has been updated.", updatedTrack.track))

            // Commit changes for edit.
            val commitRequest = edits.commit(config.packageName, editId)
            val appEdit = commitRequest.execute()
            log.info(String.format("App edit with id %s has been comitted", appEdit.id))
        } catch (ex: IOException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (ex: URISyntaxException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (ex: GeneralSecurityException) {
            log.error("Excpetion was thrown while uploading apk to alpha track", ex)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}