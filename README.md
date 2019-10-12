# Publish Android application to Google Play Developer Console

## Usage
### 1. [Google Play Developer API](https://developers.google.com/android-publisher/getting_started)
Create key, download secret.p12 and secret.json
### 2. Create config file with properties:
``` properties
trackReleaseName=1.0.3
releaseNotesLanguage=en-US
releaseNotesText=Bug fixes

buildFolder=/project/app/build/outputs/apk/release
outputApkFolder=/path/to/store/release/apk/files
srcResourcesKeyP12=/file/key.p12 (google API console)[https://play.google.com/apps/publish/#ApiAccessPlace]
resourcesClientSecretsJson=/secret/secret.json (google API console)[https://play.google.com/apps/publish/#ApiAccessPlace]

applicationName=MyApplication
packageName=my.package.app
serviceAccountEmail=email from (google API console)[https://play.google.com/apps/publish/#ApiAccessPlace]
# 'alpha', beta', 'production', 'rollout'.
track=alpha

# formatter for apk file, %s will be replaced by `trackReleaseName`
apkFormatString=v%s-appname-release.apk

usListingTitle=Listing title
usListingShortDescription=Listing short description
usListingFullDescription=Listing full description
listingsPromoVideo=link to YouTube
```
### 3. Launch `fatJar` gradle task
### 4. `java -jar applicatio/google-play-client/build/libs/google-play-client-1.0.0.jar path/to/config.properties`