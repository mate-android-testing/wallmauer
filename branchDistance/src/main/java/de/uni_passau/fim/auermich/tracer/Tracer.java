package de.uni_passau.fim.auermich.tracer;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Provides the functionality to trace branches and compute the branch distance  for a given application.
 * The collected traces/branches are written to the external storage in an incremental manner.
 * By sending a special intent to the broadcast receiver, the remaining traces are written to the traces file.
 */
public class Tracer extends BroadcastReceiver {

    /*
     * FIXME: There seems to be some sort of race condition, since every 2-3 write of the
     *  remaining traces, the actual number of traces in the trace file is higher by a multiple
     *  of the specified cache size than the logged number of traces (numberOfTraces), although
     *  the writing and logging happens in the same synchronized block. The only possible explanation
     *  to me right now is that the method trace() is called a couple of times after the broadcast,
     *  but Android suppresses the logs, which is nothing new. This would at least explain why there
     *  are more traces than expected. However, the reason why those logs are suppressed is unclear,
     *  might be related to the fact that the AUT is reset by MATE shortly after the broadcast is sent.
     */

    // contains the collected traces per 'CACHE_SIZE'
    private static Set<String> traces = new LinkedHashSet<>();

    // the output file containing the covered branches and branch distance values
    private static final String TRACES_FILE = "traces.txt";

    // keeps track of the total number of generated traces per test case / trace file
    private static int numberOfTraces = 0;

    // we can't use here log4j2 since we would require that dependency bundled with the app otherwise
    private static final Logger LOGGER = Logger.getLogger(Tracer.class.getName());

    // how many traces should be cached before written to the traces file
    private static final int CACHE_SIZE = 5000;

    /**
     * Called when a broadcast is received. Writes the remaining traces to
     * the traces file.
     *
     * @param context The application context object.
     * @param intent The intent that represents the broadcast message.
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("STORE_TRACES")) {
            String packageName = intent.getStringExtra("packageName");
            LOGGER.info("Received Broadcast");

            if (!isPermissionGranted(context, WRITE_EXTERNAL_STORAGE)) {
                LOGGER.info("Permissions got dropped unexpectedly!");
            }

            /*
             * We use here 'Tracer.class' as monitor object instead of 'this', because
             * the same monitor object needs to be used for a flawless synchronization,
             * and 'Tracer.class' can only be used in the static trace() method.
             */
            synchronized (Tracer.class) {
                write(packageName);
                traces.clear();
            }
        }
    }

    // https://stackoverflow.com/questions/2002288/static-way-to-get-context-in-android
    private static Application getApplicationUsingReflection() {
        try {
            return (Application) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            LOGGER.info("Couldn't retrieve global context object!");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Checks whether the application has the given permission.
     *
     * @param context The application context object.
     * @param permission The permission to check.
     * @return Returns {@code true} if the permission is granted,
     *          otherwise {@code false} is returned.
     */
    private static boolean isPermissionGranted(Context context, final String permission) {

        if (context == null) {
            context = getApplicationUsingReflection();
        }

        if (context == null) {
            throw new IllegalStateException("Couldn't access context object!");
        } else {
            return context.checkSelfPermission(permission) == PERMISSION_GRANTED;
        }
    }

    /**
     * Adds a new trace to the set of covered traces. This method is called
     * directly through the app code of the AUT. In particular, each branch
     * and if statement of the AUT contains such invocation.
     *
     * @param identifier A unique identifier of the trace.
     */
    public static void trace(String identifier) {
        synchronized (Tracer.class) {
            traces.add(identifier);

            if (traces.size() == CACHE_SIZE) {
                write();
                traces.clear();
            }
        }
    }

    /**
     * Writes the collected traces to the external storage. Only called
     * once the specified cache size is reached.
     */
    private static void write() {

        File sdCard = Environment.getExternalStorageDirectory();
        File traceFile = new File(sdCard, TRACES_FILE);

        if (!isPermissionGranted(null, WRITE_EXTERNAL_STORAGE)) {
            LOGGER.info("Permissions got dropped unexpectedly!");
        }

        try {

            FileWriter writer = new FileWriter(traceFile, true);
            BufferedWriter br = new BufferedWriter(writer);

            for (String trace : traces) {
                br.write(trace);
                br.newLine();
            }

            // keep track of collected traces per test case / trace file
            numberOfTraces = numberOfTraces + CACHE_SIZE;
            LOGGER.info("Accumulated traces size: " + numberOfTraces);

            br.flush();
            br.close();
            writer.close();

        } catch (IndexOutOfBoundsException e) {
            LOGGER.info("Synchronization issue!");
            Map<Thread, StackTraceElement[]> threadStackTraces = Thread.getAllStackTraces();
            for (Thread thread : threadStackTraces.keySet()) {
                StackTraceElement[] stackTrace = threadStackTraces.get(thread);
                LOGGER.info("Thread[" + thread.getId() + "]: " + thread);
                for (StackTraceElement stackTraceElement : stackTrace) {
                    LOGGER.info(String.valueOf(stackTraceElement));
                }
            }
        }
        catch (IOException e) {
            LOGGER.info("Writing to external storage failed.");
            e.printStackTrace();
        }
    }

    /**
     * Writes the remaining traces to the external storage.
     * Also writes a file 'info.txt' to the internal storage containing the number
     * collected traces since the last broadcast.
     *
     * @param packageName The packageName of the AUT.
     */
    private static void write(String packageName) {

        // sd card
        File sdCard = Environment.getExternalStorageDirectory();
        File traceFile = new File(sdCard, TRACES_FILE);

        LOGGER.info("Remaining traces size: " + traces.size());

        // write out remaining traces
        try {

            FileWriter writer = new FileWriter(traceFile, true);
            BufferedWriter br = new BufferedWriter(writer);

            Iterator<String> iterator = traces.iterator();
            String element = null;

            while (iterator.hasNext()) {

                if (element == null) {
                    element = iterator.next();
                    LOGGER.info("First entry: " + element);
                } else {
                    element = iterator.next();
                }

                br.write(element);

                // avoid new line at end
                if (iterator.hasNext()) {
                    br.newLine();
                }
            }

            if (!traces.isEmpty()) {
                LOGGER.info("Last entry: " + element);
            }

            br.flush();
            br.close();
            writer.close();

        } catch (IOException e) {
            LOGGER.info("Writing to external storage failed.");
            e.printStackTrace();
        }

        // signal that we finished writing out traces
        try {
            String filePath = "data/data/" + packageName;
            File info = new File(filePath, "info.txt");
            FileWriter writer = new FileWriter(info);

            numberOfTraces = numberOfTraces + traces.size();
            writer.append(String.valueOf(numberOfTraces));
            LOGGER.info("Total number of traces in file: " + numberOfTraces);

            // reset traces counter
            numberOfTraces = 0;

            writer.flush();
            writer.close();

        } catch (IOException e) {
            LOGGER.info("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }

    // unary operation - object types
    public static void computeBranchDistance(String operation, Object argument) {
        computeBranchDistance(operation, argument, null);
    }

    // binary operation - object types
    public static void computeBranchDistance(String operation, Object argument1, Object argument2) {

        int distance = 0;

        String[] tokens = operation.split(":");
        int opcode = Integer.parseInt(tokens[0]);
        final String identifier = tokens[1];

        switch (opcode) {
            case 0: // if-eqz, if-eq
                if (argument1 == argument2) {
                    distance = 0;
                } else {
                    distance = 1;
                }
                break;
            case 1: // if-nez, if-ne
                if (argument1 != argument2) {
                    distance = 0;
                } else {
                    distance = 1;
                }
                break;
            default:
                // Further comparisons on object types seem to be not reasonable.
                throw new UnsupportedOperationException("Comparison operator " + operation + " not yet supported!");
        }

        final String trace = identifier + ":" + distance;
        LOGGER.info("Branch distance for " + identifier + ": " + distance);
        trace(trace);
    }

    // unary operation - primitive types
    public static void computeBranchDistance(String operation, int argument) {
        computeBranchDistance(operation, argument, 0);
    }

    // binary operation - primitive types
    public static void computeBranchDistance(String operation, int argument1, int argument2) {

        int distance = 0;

        String[] tokens = operation.split(":");
        int opcode = Integer.parseInt(tokens[0]);
        final String identifier = tokens[1];

        switch (opcode) {
            case 0: // if-eqz, if-eq
            case 1: // if-nez, if-ne
                distance = Math.abs(argument1 - argument2);
                break;
            case 2: // if-lez, if-le
            case 3: // if-ltz, if-lt
                distance = argument1 - argument2;
                break;
            case 4: // if-gez, if-ge
            case 5: // if-gtz, if-gt
                distance = argument2 - argument1;
                break;
            default:
                throw new UnsupportedOperationException("Comparison operator not yet supported!");
        }

        final String trace = identifier + ":" + distance;
        LOGGER.info("Branch distance for " + identifier + ": " + distance);
        trace(trace);
    }
}