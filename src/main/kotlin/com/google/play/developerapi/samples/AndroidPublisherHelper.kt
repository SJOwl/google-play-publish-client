package com.google.play.developerapi.samples

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.http.HttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.Preconditions
import com.google.api.client.util.Strings
import com.google.api.client.util.store.DataStoreFactory
import com.google.api.client.util.store.FileDataStoreFactory
import com.google.api.services.androidpublisher.AndroidPublisher
import com.google.api.services.androidpublisher.AndroidPublisherScopes
import org.apache.commons.logging.LogFactory
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.security.GeneralSecurityException

/**
 * Helper class to initialize the publisher APIs client library.
 *
 *
 * Before making any calls to the API through the client library you need to
 * call the [AndroidPublisherHelper.init] method. This will run
 * all precondition checks for for client id and secret setup properly in
 * resources/client_secrets.json and authorize this client against the API.
 *
 */
class AndroidPublisherHelper(
    private val config: PublishConfig
) {

    private val log = LogFactory.getLog(AndroidPublisherHelper::class.java)

    companion object {
        const val MIME_TYPE_APK = "application/vnd.android.package-archive"
    }

    /**
     * Directory to store user credentials (only for Installed Application
     * auth).
     */
    private val dataStoreSystemProperty = "user.home"
    private val dataStoreFile = ".store/android_publisher_api"
    private val dataStoreDir = File(System.getProperty(dataStoreSystemProperty), dataStoreFile)

    /** Global instance of the JSON factory.  */
    private val jacksonFactory = JacksonFactory.getDefaultInstance()

    /** Global instance of the HTTP transport.  */
    private var httpTransport: HttpTransport? = null

    /** Installed application user ID.  */
    private val instAppUserId = "user"

    /**
     * Global instance of the [DataStoreFactory]. The best practice is to
     * make it a single globally shared instance across your application.
     */
    private var dataStoreFactory: FileDataStoreFactory? = null

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun authorizeWithServiceAccount(serviceAccountEmail: String): Credential {
        log.info(String.format("Authorizing using Service Account: %s", serviceAccountEmail))

        // Build service account credential.
        return GoogleCredential.Builder()
            .setTransport(httpTransport)
            .setJsonFactory(jacksonFactory)
            .setServiceAccountId(serviceAccountEmail)
            .setServiceAccountScopes(
                setOf(AndroidPublisherScopes.ANDROIDPUBLISHER))
            .setServiceAccountPrivateKeyFromP12File(File(config.srcResourcesKeyP12))
            .build()
    }

    /**
     * Authorizes the installed application to access user's protected data.
     *
     * @throws Exception
     * @throws GeneralSecurityException
     */
    @Throws(Exception::class)
    private fun authorizeWithInstalledApplication(): Credential {
        log.info("Authorizing using installed application")

        // load client secrets
        val clientSecrets = GoogleClientSecrets.load(
            jacksonFactory,
            InputStreamReader(
                AndroidPublisherHelper::class.java
                    .getResourceAsStream(config.resourcesClientSecretsJson)))
        // Ensure file has been filled out.
        checkClientSecretsFile(clientSecrets)

        dataStoreFactory = FileDataStoreFactory(dataStoreDir)

        // set up authorization code flow
        val flow = GoogleAuthorizationCodeFlow.Builder(httpTransport,
            jacksonFactory, clientSecrets,
            setOf(AndroidPublisherScopes.ANDROIDPUBLISHER))
            .setDataStoreFactory(dataStoreFactory!!).build()
        // authorize
        return AuthorizationCodeInstalledApp(
            flow, LocalServerReceiver()).authorize(instAppUserId)
    }

    /**
     * Ensure the client secrets file has been filled out.
     *
     * @param clientSecrets the GoogleClientSecrets containing data from the
     * file
     */
    private fun checkClientSecretsFile(clientSecrets: GoogleClientSecrets) {
        if (clientSecrets.details.clientId.startsWith("[[INSERT") || clientSecrets.details.clientSecret.startsWith("[[INSERT")) {
            log.error("Enter Client ID and Secret from " + "APIs console into resources/client_secrets.json.")
            System.exit(1)
        }
    }

    /**
     * Performs all necessary setup steps for running requests against the API.
     *
     * @param applicationName the name of the application: com.example.app
     * @param serviceAccountEmail the Service Account Email (empty if using
     * installed application)
     * @return the {@Link AndroidPublisher} service
     * @throws GeneralSecurityException
     * @throws IOException
     */
    @Throws(Exception::class)
    @JvmOverloads internal fun init(
        applicationName: String,
        serviceAccountEmail: String? = null
    ): AndroidPublisher {
        Preconditions.checkArgument(!Strings.isNullOrEmpty(applicationName),
            "applicationName cannot be null or empty!")

        // Authorization.
        newTrustedTransport()
        val credential: Credential
        if (serviceAccountEmail == null || serviceAccountEmail.isEmpty()) {
            credential = authorizeWithInstalledApplication()
        } else {
            credential = authorizeWithServiceAccount(serviceAccountEmail)
        }

        // Set up and return API client.
        return AndroidPublisher.Builder(
            httpTransport!!, jacksonFactory, credential).setApplicationName(applicationName)
            .build()
    }

    @Throws(GeneralSecurityException::class, IOException::class)
    private fun newTrustedTransport() {
        if (null == httpTransport) {
            httpTransport = GoogleNetHttpTransport.newTrustedTransport()
        }
    }
}
/**
 * Performs all necessary setup steps for running requests against the API
 * using the Installed Application auth method.
 *
 * @param applicationName the name of the application: com.example.app
 * @return the {@Link AndroidPublisher} service
 */
