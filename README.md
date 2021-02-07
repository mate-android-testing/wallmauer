
# Instrumentation

The instrumentation library offers three modules responsible for:

* BranchCoverage Instrumentation
* BranchCoverage Evaluation
* BranchDistance Instrumentation

# BranchDistance Instrumentation:

The android.jar as well as the apktool-cli-all.jar contain both an implementation of the XmlPullParser library, which
leads to a conflict, if the Android API is selected. In this case the following exception occurs:

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
	
To fix this issue, I had to manually remove class files from the android.jar.

# Program Arguments BranchDistance:

Only the path to the APK is required, e.g.:

C:\Users\Michael\git\mate-commander\com.simple.app.apk

# Saving and Fetching Traces:

In order to retrieve the traces, a broadcast need to be sent to the AUT:

`adb install -g <apk>` <br />

`adb root` <br />
`adb shell am broadcast -a STORE_TRACES -n <package-name>/de.uni_passau.fim.auermich.tracer.Tracer --es packageName "<package-name>"` <br />
`adb pull storage/emulated/0/traces.txt` <br />
`adb pull data/data/<package-name>/info.txt` (may require a slash before data on Linux)

With `adb install -g` all necessary permissions are granted. After sending the intent, the traces.txt
is generated and can be found in the external storage. Additionally, once all traces have be written out,
an info.txt is generated within the app internal storage. This file solely indicates how many
traces have been collected.

# Program Arguments BranchCoverage:

Only the path to the APK is required, e.g.:

C:\Users\Michael\git\mate-commander\com.simple.app.apk

# Program Arguments BranchCoverageEvaluation:

1st Argument: Path to branches.txt
2nd Argument: Path to traces.txt

The branches.txt is obtained from the instrumentation process.

Have a look at the corresponding BranchCoverageEvaluationTest.