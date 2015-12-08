![The Eye Tribe](tet_logo.png)

Java SDK for the EyeTribe Dev Kit
====

This is the Java library implementation for the [EyeTribe Dev Kit](https://theeyetribe.com/products/). This reference implementation provides a Java interface for communicating with the EyeTribe Server through the open [EyeTribe API](http://dev.theeyetribe.com/api/). The library allow developers to get started quickly and focus their efforts on creating truly immersive and innovative apps using our eye tracking technology. 

Download
----
Download the [latest JAR](https://github.com/EyeTribe/tet-java-client/releases) or grab via Gradle through [JCenter](https://bintray.com/eyetribe/maven/eyetribe-java):

    buildscript {
	    repositories {
	    	jcenter()
	    }
    }

    dependencies {
    	compile 'com.theeyetribe:eyetribe-java:0.9.60'
    }


Documentation
----
Find documentation of this library at [EyeTribe Java SDK Doc](http://eyetribe.github.io/tet-java-client). The EyeTribe API reference is found at [Eye Tribe Developer Website](http://dev.theeyetribe.com/api/).


Samples
----
An essential part of using the [EyeTribe Dev Kit](https://theeyetribe.com/products/) is 'calibrating the system'. Doing so involves creating a UI that  supports this library and guides the user through a series of mandatory steps.

![Steps of the JavaFX calibration samples](jfx_sample.png)

This library holds a sample implementation of a Calibration UI using [JavaFX](docs.oracle.com/javase/8/javafx/get-started-tutorial/jfx-overview.htm). Find source code for this under [./javafx-sample](https://github.com/EyeTribe/tet-java-client/tree/master/javafx-sample) or download the prebuilt [EyeTribeJavaFx](https://github.com/EyeTribe/tet-java-client/releases). This sample runs on all platforms supported by the [EyeTribe Dev Kit](https://theeyetribe.com/products/) that have Java 8 version 65 or higher installed.


Building (optional)
----
A prebuilt version of the library is available under [releases](https://github.com/EyeTribe/tet-java-client/releases). The EyeTribe Java SDK is backwards compatible to Java 6. Should you wish to build it yourself, please follow the below instructions:

Prerequisites:

- Download & install [Java JDK 6](http://www.oracle.com/technetwork/java/javase/downloads). Set environment variable 'JAVA6_HOME' to '${your.jdk6.path}'.
- Download & install [Java JDK 8](http://www.oracle.com/technetwork/java/javase/downloads). Set environment variable 'JAVA\_HOME' and 'JAVA8\_HOME' to '${your.jdk8.path}'.
- Download & install [InjelliJ IDEA](https://www.jetbrains.com/idea/) or [Gradle](http://gradle.org/).

To build:

- Import Gradle project to [InjelliJ IDEA](https://www.jetbrains.com/idea/).
- Set project language level to 8 in 'Module Setting -> Project'
- Run gradle task 'sdk:jar'
- Alternatively run task 'sdk:jar' using [Gradle](http://gradle.org/) from the commandline.


Proguard
----
If you choose to build yourself and are using [Proguard](http://proguard.sourceforge.net/) for obfuscation, be sure to add the following options. 

    -keepattributes Signature, *Annotation*
    -keep class sun.misc.Unsafe { *; }
    -keep class com.theeyetribe.clientsdk.response.* { *; }
    -keep class com.theeyetribe.clientsdk.request.* { *; }
    -keep class com.theeyetribe.clientsdk.data.* { *; }
    -dontwarn java.lang.invoke.*
    -dontwarn com.google.**

Getting Help
----

- **Have questions or need support?** Visit our [developer forum](http://theeyetribe.com/forum/), to find answers to your questions and share your experiences with others developers.
- **Have a bug to report?** Open a [new issue](https://github.com/EyeTribe/tet-java-client/issues) and tell us what you are experiencing. Please add library version and full log if possible.
- **Have a feature request?** Either open a [new issue](https://github.com/EyeTribe/tet-java-client/issues) or post in our [developer forum](http://theeyetribe.com/forum/). Tell us what feature you are missing and what it should do. 

Feedback
----

If you like using this library, please consider sending out a tweet mentioning [@TheEyeTribe](twitter.com/theeyetribe), announce your app in our [developer forum](http://theeyetribe.com/forum/), or email [support@theeyetribe.com](mailto:support@theeyetribe.com) to let us know.