
# Remarks:

Running the BranchDistance class inside the IDE fails with the following stack trace:

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
	
I primarily assume this is due to XmlPullParser library, which is both contained in the android.jar 
and the apktool-cli-all.jar. However, when android.jar is referenced in this regard, only a stub is called,
which in turn leads to the aforementioned RuntimeException. It would be necessary to delete the respective
class files from the android.jar.

Running the library on the console via 'java -jar branchDistance.jar <APK-path>' works however.

# Program Arguments BranchDistance:

Only the path to the APK is required, e.g.:

C:\Users\Michael\git\mate-commander\com.simple.app.apk

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