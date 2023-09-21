# la-android-livestream
Disable / comment the following options while creating a library (.aar library file)

1) In Module level build.gradle
  applicationId "com.learnapp.livestream" in defaultConfig
  id 'com.android.application' in plugins
  
2) In MainApplication class
  Comment HiltAndroidApp annotation
  
3) In Androidmanifest
Comment
Intent filter in activity
Application tag attributes

4) In MainActivity
change mock bundle
