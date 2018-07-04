## JUnit Runner

### Execution

For start JUnit Runner use `com.blazemeter.taurus.CustomRunner.main()` method with 1 argument - PATH to Properties file.
    
### Properties file

This file contains properties as `{key}={value}` pairs. Each property should be define at the new line.

Custom Runner expects the following properties:

- `target_***` - paths to `*.jar` file, that contains test classes extends from `junit.framework.TestCase` class 

_Example:_ 

_target_lib1=/home/user/tests/libs/myLib.jar_

_target_lib2=C:/libs/myLib.jar_

- `report_file` - path to report file

- `iterations` - execution count of Test Suite (long value)

- `hold_for` - duration limit in seconds (float value)

Also you can specify custom properties, that will be pass to Java System Properties, e.g.:

    var1=value1
    var2=val2
