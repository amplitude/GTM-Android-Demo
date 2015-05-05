# AndroidDemo

Simple tilt maze game with Amplitude integration

A ball sits in a flat tray containing one or more squares (goals). The challenge is to guide the ball around the tray and collect all the squares. Tilt the phone or swipe the ball to start the ball rolling. The ball rolls in a straight line until it hits a wall, you can then tilt again.

The Amplitude integration contains these parts:

* Add Amplitude SDK using Gradle and Maven in ```app/build-remote-dependency.gradle```
* Add Amplitude SDK with local jar in ```app/build-local-dependency.gradle```
* Add Google Play Services for accessing Advertising IDs in ```app/build-remote-dependency.gradle```
* Proguard settings in ```app/proguard-rules.pro```
* Amplitude initialization in ```TiltMazesActivity.onCreate```
* Starting and ending sessions in ```TiltMazesActivity.onPause``` and ```TiltMazesActivity.onResume```
* Setting user properties and logging events with properties in ```GameEngine.mHandler```

Thanks to Balazs Lecz for creating the original game. See https://github.com/masayukig/tiltmazes for the original source and more information.
