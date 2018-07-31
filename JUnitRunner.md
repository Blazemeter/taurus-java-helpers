## JUnit Runner

### Execution

For execute JUnit Runner from command-line use the following command:

```    
java -cp taurus-java-helpers-[VERSION].jar:junit-[VERSION].jar com.blazemeter.taurus.junit.CustomRunner [PATH_TO_PROPERTIES_FILE]
```

** Since version 1.4 all target *.jar files should be included in CLASSPATH **

    
### Properties file

This file contains properties as `{key}={value}` pairs. Each property should be define at the new line.

Custom Runner expects the following properties:

- `target_***` - (DEPRECATED) paths to `*.jar` file, that contains JUnit test case classes (removed in 1.4)

- `report_file` - path to report file

- `concurrency` - number of target concurrent virtual users

- `ramp_up` - ramp-up time to reach target concurrency in seconds (float value)

- `hold_for` - time to hold target concurrency in seconds (float value)

- `iterations` - limit Test Suite iterations number (long value)

- `steps` - allows users to apply stepping ramp-up for concurrency, requires `ramp-up` (int value)

- `junit_version` - select JUnit version (supports versions 4 and 5)

- `run_items` - class names or method names, that will be run (and packages for JUnit 5)

- `include_category` and `exclude_category` - category names, that will be run (tags and packages fot JUnit 5)

_NOTE: The priority for run test with JUnit 4 is: run_items -> categories -> all found test classes_

Also you can specify custom properties, that will be pass to Java System Properties, e.g.:

```
var1=value1
var2=val2
```

#### Example 
```
#
# Set paths to *.jars
#
target_lib1=/home/user/tests/libs/myLib.jar
target_lib2=C:/libs/myLib.jar

#
# Set output file
#
report_file=/home/user/reports/junit.xml

#
# Set target concurrency
#
concurrency=90

#
# Set ramp-up 30 sec
#
ramp_up=30

#
# Set count of steps for reach target concurrency
#
steps=3

#
# Set test duration
#
hold_for=60

#
# Set execution count 
#
iterations=0

#
# Select JUnit 5 version for run tests
#
junit_version=5

#
# Set test classes or test methods
#
run_items=package.Class1,package.Class2#test2

#
# Or set categories
#
include_category=categories.FastTests,categories.SmokeTests
exclude_category=categories.SlowTests


#
# Set custom properties
#
var1=value1
var2=val2
```
