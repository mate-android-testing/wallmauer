
# Instrumentation

The instrumentation library offers five modules responsible for:

* BranchCoverage Instrumentation
* BranchCoverage Evaluation
* BranchDistance Instrumentation
* BasicBlockCoverage Instrumentation
* BasicBlockCoverage Evaluation

# BranchCoverage Instrumentation:

Generate the **branchCoverage.jar** using the supplied gradle task `customFatJar` of the **branchCoverage module**. 
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar branchCoverage.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **branches.txt** will be generated in the current working directory. It contains
the number of branches per class, which is relevant for the evaluation of the branch coverage.

# BranchDistance Instrumentation:

Generate the **branchDistance.jar** using the supplied gradle task `customFatJar` of the **branchDistance module**. 
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar branchDistance.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **branches.txt** will be generated in the current working directory. It contains
the number of branches per class, which is relevant for the evaluation of the branch coverage.

# BasicBlockCoverage Instrumentation:

Generate the **basicBlockCoverage.jar** using the supplied gradle task `customFatJar` of the **basicBlockCoverage module**.
The JAR file can be found within the `build/libs/` folder of the respective module.

To invoke the instrumentation run the following command: <br >

`java -jar basicBlockCoverage.jar <path-to-apk>` <br >

This will produce an APK where the original APK resided with the name **<original-apk-name>-instrumented.apk**.
In addition, a file called **blocks.txt** will be generated in the current working directory. It contains
a list of all basic blocks. For each basic block its class, method, block id (which is only unique within the same method),
the number of instructions it contains and whether the block is the target of an if/else-Branch is recorded. This information
can be used to calculate line- and branch-coverage.

# Workflow:

Once you have instrumented the APK, sign it using `apksigner` (comes with the Android-SDK).
Then follow the instructions below:

`adb install -g <apk>` <br />

This installs the APK on the emulator and grants read/write access on the external storage among other things.
Now, it is time to explore the application. Whenever a new branch is visited, a trace is collected by the incorporated
`Tracer` class. After 5000 traces, those are written to a file **traces.txt** on the external storage of the emulator.
In order to pull all traces, invoke the following commands:

`adb root` (only works on rooted devices!) <br />
`adb shell am broadcast -a STORE_TRACES -n <package-name>/de.uni_passau.fim.auermich.tracer.Tracer --es packageName "<package-name>"` <br />
`adb pull storage/emulated/0/traces.txt` <br />
`adb pull data/data/<package-name>/info.txt` (may require an additional slash on Linux, i.e. /data/data/)

The broadcast ensures that all collected traces are written to the **traces.txt** file. You have to specify at the two
placeholders the package name of the application. You can find this information within the **AndroidManifest.xml** 
(first line) or run the command: `aapt dump badging <path-to-apk> | grep package:\ name` <br >
The second pull command retrieves a file called **info.txt**, which solely contains the number of collected traces.
This file is present once writing the traces to the file is completed.

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

# Building Issues:

The `android.jar` as well as the `apktool-cli-all.jar` contain both an implementation of the `XmlPullParser` library, 
which leads to a conflict, if the Android API is selected. In this case the following exception occurs:

INFO: Decoding AndroidManifest.xml with resources... <br>
Exception in thread "main" java.lang.RuntimeException: Stub! <br>
	at org.xmlpull.v1.XmlPullParserFactory.newInstance(XmlPullParserFactory.java:117) <br>
	at org.xmlpull.v1.wrapper.XmlPullWrapperFactory.<init>(XmlPullWrapperFactory.java:52) <br>
	at org.xmlpull.v1.wrapper.XmlPullWrapperFactory.newInstance(XmlPullWrapperFactory.java:29) <br>
	at brut.androlib.res.decoder.XmlPullStreamDecoder.decode(XmlPullStreamDecoder.java:50) <br>
	at brut.androlib.res.decoder.XmlPullStreamDecoder.decodeManifest(XmlPullStreamDecoder.java:154) <br>
	at brut.androlib.res.decoder.ResFileDecoder.decodeManifest(ResFileDecoder.java:162) <br>
	at brut.androlib.res.AndrolibResources.decodeManifestWithResources(AndrolibResources.java:204) <br>
	at brut.androlib.Androlib.decodeManifestWithResources(Androlib.java:136) <br>
	at brut.androlib.ApkDecoder.decode(ApkDecoder.java:122) <br>
	at de.uni_passau.fim.Utility.decodeAPK(Utility.java:184) <br>
	at de.uni_passau.fim.BranchDistance.main(BranchDistance.java:194) <br>
	
To fix this issue, one has to manually remove certain class files from the `android.jar`. In the future,
we should adjust the build.gradle file in order to exclude the `XmlPullParser` library of the `android.jar`.
See https://docs.gradle.org/current/userguide/dependency_downgrade_and_exclude.html for more details.