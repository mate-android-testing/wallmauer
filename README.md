# Program Arguments Example:

C:\Users\Michael\com.xabber.androiddev_348\classes.dex
C:\Users\Michael\com.xabber.androiddev_348\instrumented.dex
ws.xsoh.etar
com.android.calendar.AllInOneActivity

The first argument describes the classes.dex file inside the APK. We can extract
it by using apktool as follows: apktool d -s <apk-file>

The second argument describes the instrumented dex file, that is the output
of this program.

The third argument refers to the package name of the AUT.

The fourth argument denotes the main activity of the AUT.