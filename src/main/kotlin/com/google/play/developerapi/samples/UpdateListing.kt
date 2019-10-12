package com.google.play.developerapi.samples

import com.google.api.client.repackaged.com.google.common.base.Preconditions
import com.google.api.client.repackaged.com.google.common.base.Strings
import com.google.api.services.androidpublisher.model.Listing
import org.apache.commons.logging.LogFactory
import java.util.Locale

/**
 * Updates US and UK listings. Changes title, short-description, full-description and video for
 * en-US and en-GB locales.
 */
object UpdateListing {

    private val log = LogFactory.getLog(UpdateListing::class.java)

    @JvmStatic fun main(args: Array<String>) {
        val config = PublishConfig(propertiesPath(args))

        try {
            Preconditions.checkArgument(!Strings.isNullOrEmpty(config.packageName),
                "packageName cannot be null or empty!")

            // Create the API service.
            val service = AndroidPublisherHelper(config).init(
                config.applicationName, config.serviceAccountEmail
            )
            val edits = service.edits()

            // Create an edit to update listing for application.
            val editRequest = edits
                .insert(config.packageName, null)
            val edit = editRequest.execute()
            val editId = edit.id
            log.info(String.format("Created edit with id: %s", editId))

            // Update listing for US version of the application.
            val newUsListing = Listing()
            newUsListing.setTitle(config.usListingTitle)
                .setFullDescription(config.usListingFullDescription)
                .setShortDescription(config.usListingShortDescription).video = config.listingsPromoVideo

            val updateUSListingsRequest = edits
                .listings()
                .update(
                    config.packageName,
                    editId,
                    Locale.US.toString(),
                    newUsListing
                )
            val updatedUsListing = updateUSListingsRequest.execute()
            log.info(String.format("Created new US app listing with title: %s",
                updatedUsListing.title))

            // Commit changes for edit.
            val commitRequest = edits.commit(config.packageName, editId)
            val appEdit = commitRequest.execute()
            log.info(String.format("App edit with id %s has been comitted", appEdit.id))
        } catch (ex: Exception) {
            log.error("Exception was thrown while updating listing", ex)
        }
    }
}
