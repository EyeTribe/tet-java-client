# Change Log #
---

Version 0.9.77.1 (2016-11-23)
---
- Default states for all GazeManager enum types
- Fixed default network state
- Update Gradle version & dependencies

Version 0.9.77 (2016-05-17)
---
- Moving all data types from 64-bit to 32-bit floating point precision
- Fixing bug affecting all listener callbacks
- Adapting to EyeTribe API changes, deprecating obsolete *GazeManager* method calls 
- Updated Gson to 2.5

Version 0.9.60 (2015-11-18)
---
- Extensive rewrite of core classes
- Changed project structure to support Gradle
- Added support for methods of async nature in *GazeManager*
- Added support for debug mode via *GazeManagerCore.IS\_DEBUG\_MODE*
- Added calibration evaluation class *CalibUtils*
- Added JavaFX calibration example 
- Added Unit Tests

Version 0.9.56 (2015-03-18)
---

- Added *hashCode()* implementation for all public data types
- Fixing bugs associated to *CalibrationPoint* resampling
- Clearing *Listener* types now requires explicit call to *GazeManager.deactivate()*
- Minimizing object allocation
- Fixed network initialization and deinitialization bugs
- Updated Gson to 2.3.1

Version 0.9.49 (2014-12-09)
---

- Ensured callback order of listener types during activation 
- Ensured thread safety in singletons
- Refactored internal blocking queues
- More consistent stacktrace output on callback errors
- Unified constructors and operators for all data types
- Added utility methods to *GazeData* class
- Updated Gson to 2.3

Version Version 0.9.35 (2014-05-20)
---

-    Updated license
-    Fixed bug related to *ICalibrationResultListener*

Version 0.9.34 (2014-05-09)
---

-    Improved documentation
-    Fixed bug related to initialization lock
-    Fixed bug related to broadcasting calibration updates

Version 0.9.33 (2014-04-15)
---

-    Added support for listening to EyeTribe Server conneciton state (*IConnectionStateListener*)
-    Minor API timestamp change
-    Minor refactoring and formatting
-    Generel bug fixing and optimization

Version 0.9.27 (2014-02-12)
---

- Fixed tab/space formatting
- New methods to *GazeUtils*
- Minor internal refactoring

Version 0.9.26 (2014-01-30)
---

- Initial release