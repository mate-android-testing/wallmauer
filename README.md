
# WallMauer:

The instrumentation library offers six modules responsible for:

* BranchCoverage Instrumentation
* BranchCoverage Evaluation
* BranchDistance Instrumentation
* BasicBlockCoverage Instrumentation
* BasicBlockCoverage Evaluation
* BasicBlockBranchDistance Instrumentation  
* MethodCoverage Instrumentation

Note that you can supply to each instrumentation module the optional flag `--only-aut` to only instrument the classes
belonging to the application package.

# BranchCoverage Instrumentation:

Generate the **branchCoverage.jar** using the supplied gradle task `customFatJar` of the **branchCoverage module**. 
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar branchCoverage.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **branches.txt** will be generated in the current working directory. It contains the instrumented
branches, which is relevant for the evaluation of the branch coverage.

# BranchDistance Instrumentation:

Generate the **branchDistance.jar** using the supplied gradle task `customFatJar` of the **branchDistance module**. 
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar branchDistance.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **branches.txt** will be generated in the current working directory. It contains the instrumented
branches, which is relevant for the evaluation of the branch coverage. Another file called **instrumentation-points.txt**
contains not only the instrumented branches but also the instrumented if and switch instructions that are relevant for
the branch distance computation.
The JAR file can be found within the `build/libs/` folder of the respective module.


# BasicBlockBranchDistance Instrumentation:

Generate the **basicBlockBranchDistance.jar** using the supplied gradle task `customFatJar` of the **basicBlockBranchDistance module**.
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar basicBlockBranchDistance.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **blocks.txt** will be generated in the current working directory. It contains the instrumented
basic blocks, which is relevant for the evaluation of the basic block coverage. Another file called **instrumentation-points.txt**
contains not only the instrumented branches but also the instrumented if and switch instructions that are relevant for
the branch distance computation. Also the **branches.txt** is generated that lists the instrumented branches.
The JAR file can be found within the `build/libs/` folder of the respective module.

# BasicBlockCoverage Instrumentation:

Generate the **basicBlockCoverage.jar** using the supplied gradle task `customFatJar` of the **basicBlockCoverage module**.
To invoke the instrumentation run the following command: <br >
`java -jar basicBlockCoverage.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **blocks.txt** will be generated in the current working directory. It contains
a list of all basic blocks. For each basic block its class, method, block id (which is only unique within the same method),
the number of instructions it contains and whether the block is the target of an if/else branch is recorded. This information
can be used to calculate basic block (actually line) and branch coverage.

# MethodCoverage Instrumentation:

Generate the **methodCoverage.jar** using the supplied gradle task `customFatJar` of the **methodCoverage module**.
To invoke the instrumentation run the following command: <br >
`java -jar methodCoverage.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **methods.txt** will be generated in the current working directory. It contains
the number of methods per class, which is relevant for the evaluation of the method coverage.

# Workflow:

Once you have instrumented the APK, sign it using `apksigner` (comes with the Android-SDK).
Then follow the instructions below:

`adb install -g <apk>` <br />

This installs the APK on the emulator and grants read/write access on the external storage among other things.
Now, it is time to explore the application. Whenever a new branch is visited, a trace is collected by the incorporated
`Tracer` class. After 5000 traces, those are written to a file **traces.txt** on the external storage of the emulator.
In order to pull all traces, invoke the following commands:

`adb root` (only works on rooted devices!) <br />
`adb shell am broadcast -a STORE_TRACES -n <package-name>/de.uni_passau.fim.auermich.tracer.Tracer` <br />
`adb pull storage/emulated/0/traces.txt` <br />
`adb pull storage/emulated/0/info.txt` (may require an additional slash on Linux, i.e. /storage)

The broadcast ensures that all collected traces are written to the **traces.txt** file. You have to specify at the
placeholder the package name of the application. You can find this information within the **AndroidManifest.xml** 
(first line) or run the command: `aapt dump badging <path-to-apk> | grep package:\ name` <br >
The second pull command retrieves a file called **info.txt**, which solely contains the number of collected traces.
This file is present once writing the traces to the file is completed.

# MATE Integration:

This instrumentation library is internally used by the Android Test Generator called 
[MATE](https://github.com/mate-android-testing/mate) to report coverage. You simply copy the instrumented APK into the
respective **apps** folder belonging to your local [MATE-Commander](https://github.com/mate-android-testing/mate-commander)
installation. In addition, you copy the additional artifacts produced by the instrumentation, e.g. the **branches.txt**
into the respective **apps/<package-name>** folder. Note that [MATE](https://github.com/mate-android-testing/mate) requires
you to name your APKs according to the app's package name, i.e., `<package-name>.apk`.

# BranchCoverage Evaluation:

Generate the **branchCoverageEvaluation.jar** using the supplied gradle task `customFatJar` of the **branchCoverageEvaluation module**. 
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the evaluation run the following command: <br >

`java -jar branchCoverageEvaluation.jar <path-to-branches.txt> <path-to-traces.txt>` <br >

Have a look at the supplied **BranchCoverageEvaluationTest**.

# BasicBlockCoverage Evaluation:

Generate the **basicBlockCoverageEvaluation.jar** using the supplied gradle task `customFatJar` of the **basicBlockCoverageEvaluation module**.
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the evaluation run the following command: <br >

`java -jar basicBlockCoverageEvaluation.jar <path-to-blocks.txt> <path-to-traces.txt>` <br >

Have a look at the supplied **BasicBlockCoverageEvaluationTest**.
