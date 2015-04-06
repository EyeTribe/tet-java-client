Java SDK for the EyeTribe Dev Kit
====
<p>

Introduction
----

This is the Java library implementation for the EyeTribe Dev Kit. This reference implementation provides a simple Java interface for communicating with the EyeTribe Server through our open [TET API](http://dev.theeyetribe.com/api/). This should allow developers to get started quickly to focus their efforts on creating truly immersive and innovative apps using our eye tracking technology. 

This version is to be considered **_beta_**. Feedback and bug fix submissions are welcome.

Please visit our [developer website](http://dev.theeyetribe.com) for documentation and tutorials. Please use our [public forum](http://theeyetribe.com/forum) for questions and support.


Dependencies
----

The implementation is Java 6 compliant for backwards compatibility and [Android OS](http://developer.android.com) support. The library uses [google-gson](http://code.google.com/p/google-gson/) for JSON parsing.


Build
----

1. Install [Java JDK](http://www.oracle.com/technetwork/java/javase/downloads) (and optionally [Eclipse for Java](http://www.eclipse.org/downloads/)) 
2. To build, either import and build from Eclipse IDE (Eclipse project files included) or use Apache Ant to run included *build.xml* configuration.
3. Alternatively, import source code into your favorite Java IDE and build from there.


Tutorials
----

A simple guide to using this Java SDK is found in the [tutorials section](http://dev.theeyetribe.com/java/) of our developer website. More tutorials will be provided in the near future.


Samples
----

There are currently no samples available for the Java SDK, but they will be added as they become available. For now, consult our [C# samples on GitHub](https://github.com/eyetribe) for inspiration as the C# reference implementation is very similar to Java. 


Documentation
----
Find documentation of this library at [EyeTribe Java SDK Doc](http://eyetribe.github.io/tet-java-client).


API Reference
----

The complete API specification used by the Java SDK to communicate with the server is available on our [developer website](http://dev.theeyetribe.com/api/).


Changelog
----
0.9.56 (2015-03-18)

- Added *hashCode()* implementation for all public data types
- Fixing bugs associated to *CalibrationPoint* resampling
- Clearing *Listener* types now requires explicit call to *GazeManager.deactivate()*
- Minimizing object allocation
- Fixed network initialization and deinitialization bugs
- Updated Gson to 2.3.1

0.9.49 (2014-12-09)

- Ensured callback order of listener types during activation 
- Ensured thread safety in singletons
- Refactored internal blocking queues
- More consistent stacktrace output on callback errors
- Unified constructors and operators for all data types
- Added utility methods to GazeData class
- Updated Gson to 2.3

0.9.35 (2014-05-20)

-    Updated license
-    Fixed bug related to ICalibrationResultListener

0.9.34 (2014-05-09)

-    Improved documentation
-    Fixed bug related to initialization lock
-    Fixed bug related to broadcasting calibration updates

0.9.33 (2014-04-15)

-    Added support for listening to EyeTribe Server conneciton state (IConnectionStateListener)
-    Minor API timestamp change
-    Minor refactoring and formatting
-    Generel bug fixing and optimization

0.9.27 (2014-02-12)

- Fixed tab/space formatting
- New methods to GazeUtils
- Minor internal refactoring

0.9.26 (2014-01-30)

- Initial release
