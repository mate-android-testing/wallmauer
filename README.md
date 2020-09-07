
# Remarks:

The android.jar as well as the apktool-cli-all.jar contain both an implementation of the XmlPullParser library, which
leads to a conflict, if the Android API is selected. In this case the following exception occurs:

INFO: Decoding AndroidManifest.xml with resources...
Exception in thread "main" java.lang.RuntimeException: Stub!
	at org.xmlpull.v1.XmlPullParserFactory.newInstance(XmlPullParserFactory.java:117)
	at org.xmlpull.v1.wrapper.XmlPullWrapperFactory.<init>(XmlPullWrapperFactory.java:52)
	at org.xmlpull.v1.wrapper.XmlPullWrapperFactory.newInstance(XmlPullWrapperFactory.java:29)
	at brut.androlib.res.decoder.XmlPullStreamDecoder.decode(XmlPullStreamDecoder.java:50)
	at brut.androlib.res.decoder.XmlPullStreamDecoder.decodeManifest(XmlPullStreamDecoder.java:154)
	at brut.androlib.res.decoder.ResFileDecoder.decodeManifest(ResFileDecoder.java:162)
	at brut.androlib.res.AndrolibResources.decodeManifestWithResources(AndrolibResources.java:204)
	at brut.androlib.Androlib.decodeManifestWithResources(Androlib.java:136)
	at brut.androlib.ApkDecoder.decode(ApkDecoder.java:122)
	at de.uni_passau.fim.auermich.branchdistance.utility.Utility.decodeAPK(Utility.java:184)
	at de.uni_passau.fim.auermich.branchdistance.BranchDistance.main(BranchDistance.java:194)
	
To fix this issue, I had to manually remove class files from the android.jar.

# Program Arguments BranchDistance:

Only the path to the APK is required, e.g.:

C:\Users\Michael\git\mate-commander\com.simple.app.apk

# Saving and Fetching Traces:

In order to retrieve the traces, a broadcast need to be sent to the AUT:

adb install -g <apk> <br />

adb root
adb shell am broadcast -a STORE_TRACES -n <package-name>/de.uni_passau.fim.auermich.branchdistance.tracer.Tracer --es packageName "<package-name>" <br />
adb pull storage/emulated/0/traces.txt <br />
adb pull data/data/<package-name>/info.txt <br /> (may require a slash before data on Linux)

With adb install -g all necessary permissions are granted. After sending the intent, the traces.txt
is generated and can be found in the external storage. Additionally, once all traces have be written out,
an info.txt is generated within the app internal storage. This file solely indicates how many
traces have been collected.

# Program Arguments BranchCoverage:

C:\Users\Michael\com.xabber.androiddev_348\classes.dex <br />
C:\Users\Michael\com.xabber.androiddev_348\instrumented.dex <br />
ws.xsoh.etar <br />
com.android.calendar.AllInOneActivity <br />

The first argument describes the classes.dex file inside the APK. We can extract
it by using apktool as follows: apktool d -s <apk-file>

The second argument describes the instrumented dex file, that is the output
of this program.

The third argument refers to the package name of the AUT.

The fourth argument denotes the main activity of the AUT.