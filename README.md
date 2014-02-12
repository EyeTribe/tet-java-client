Java SDK for The Eye Tribe Tracker
====
<p>

Introduction
----

This is the Java reference implementation for the EyeTribe Server. The implementation provides a simple Java interface for communicating with the server through the [TET API](http://dev.theeyetribe.com/api/). This should allow developers to get started quickly to focus their efforts on creating truly immersive and innovative apps using our eye tracking technology. 

This version is to be considered **_alpha_**. Feedback and bug fix submissions are welcome.

Please visit our [developer website](http://dev.theeyetribe.com) for more information.


Dependencies
----

The implementation is Java 6 compliant and uses [google-gson](http://code.google.com/p/google-gson/) for JSON parsing.


Build
----

1. Install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads) (and optionally [Eclipse for Java](http://www.eclipse.org/downloads/)) 
2. To build, either import and build from Eclipse IDE (Eclipse project files included) or use Apache Ant to run included build.xml configuration.
3. Alternatively, import source code into your favorite Java IDE and build from there.

Samples
----

There are currently no samples available for the Java SDK, but they will be added as they become available. For now, consult our [C# samples on GitHub](https://github.com/eyetribe) for inspiration as the C# reference implementation is very similar to Java. 


API Reference
----

The complete API specification used by the Java SDK to communicate with the server is available on our [developer website](http://dev.theeyetribe.com/api/).


Changelog
----

0.9.27 (2014-02-12)
- Fixed tab/space formatting
- New methods to GazeUtils
- Minor internal refactoring

0.9.26 (2014-01-30)
- Initial release
