package com.google.play.developerapi.samples

import java.io.FileInputStream
import java.util.Properties

class PublishConfig(propertiesPath: String) {

    private val properties = Properties().apply {
        load(FileInputStream(propertiesPath))
    }

    val buildFolder: String = properties.getProperty("buildFolder")
    val outputApkFolder: String = properties.getProperty("outputApkFolder")

    val srcResourcesKeyP12: String = properties.getProperty("srcResourcesKeyP12")
    val resourcesClientSecretsJson: String = properties.getProperty("resourcesClientSecretsJson")
    val trackReleaseName: String = properties.getProperty("trackReleaseName")

    val apkFilePath: String = "${properties.getProperty("outputApkFolder")}/${properties.getProperty("apkFormatString").format(trackReleaseName)}"
    val applicationName: String = properties.getProperty("applicationName") + trackReleaseName
    val packageName: String = properties.getProperty("packageName")
    val serviceAccountEmail: String = properties.getProperty("serviceAccountEmail")
    val track: String = properties.getProperty("track")
    val releaseNotesLanguage: String = properties.getProperty("releaseNotesLanguage")
    val releaseNotesText: String = properties.getProperty("releaseNotesText")
    val usListingTitle: String = properties.getProperty("usListingTitle")
    val usListingShortDescription: String = properties.getProperty("usListingShortDescription")
    val usListingFullDescription: String = properties.getProperty("usListingFullDescription")
    val listingsPromoVideo: String = properties.getProperty("listingsPromoVideo")
}