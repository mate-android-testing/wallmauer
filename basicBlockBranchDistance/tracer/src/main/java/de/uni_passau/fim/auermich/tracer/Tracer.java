package de.uni_passau.fim.auermich.tracer;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
 * Provides the functionality to trace basic blocks for a given application. The collected traces/basic blocks
 * are written to the external storage in an incremental manner. By sending a special intent to the
 * broadcast receiver, the remaining traces are written to the traces file.
 *
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

    // we can't use here log4j2 since we would require that dependency bundled with the app otherwise
    private static final Logger LOGGER = Logger.getLogger(Tracer.class.getName());

    /*
     * We provide a custom uncaught exception handler that calls through Tracer.writeRemainingTraces() before the
     * exception is passed to the default uncaught exception handler. This ensures that we don't loose any traces
     * caused through a crash.
     */
    private static Thread.UncaughtExceptionHandler uncaughtExceptionHandler;

    /*
     * We initialize our custom uncaught exception handler once the class is loaded. In order to ensure that no other
     * class overrides this exception handler, we save our exception handler and perform a check whenever we write out
     * the collected traces. If the exception handler has been overridden, we simply override it again.
     */
    static {

        Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();

        Thread.UncaughtExceptionHandler uncaughtExceptionHandler = new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                LOGGER.info("Uncaught exception!");
                Tracer.writeRemainingTraces();
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        Tracer.uncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    // contains the collected traces per 'CACHE_SIZE'
    private static final Set<String> traces = new LinkedHashSet<>();

    // the output file containing the covered basic blocks and computed branch distances
    private static final String TRACES_FILE = "traces.txt";

    // the file containing the number of generated traces
    private static final String INFO_FILE = "info.txt";

    // the file signaling that the tracer is currently dumping traces
    private static final String RUNNING_FILE = "running.txt";

    // keeps track of the total number of generated traces per test case / trace file
    private static int numberOfTraces = 0;

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
            LOGGER.info("Received Broadcast");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!isPermissionGranted(context, WRITE_EXTERNAL_STORAGE)) {
                    LOGGER.info("Permissions got dropped unexpectedly!");
                }
            }

            /*
             * We use here 'Tracer.class' as monitor object instead of 'this', because
             * the same monitor object needs to be used for a flawless synchronization,
             * and 'Tracer.class' can only be used in the static trace() method.
             */
            synchronized (Tracer.class) {
                writeRemainingTraces();
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
     *         otherwise {@code false} is returned.
     */
    private static boolean isPermissionGranted(Context context, final String permission) {

        if (context == null) {
            context = getApplicationUsingReflection();
        }

        if (context == null) {
            throw new IllegalStateException("Couldn't access context object!");
        } else {
            // requires at least API 23
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
                writeTraces();
            }
        }
    }

    /**
     * Writes the collected traces to the external storage. Only called once the specified cache size is reached.
     */
    private static void writeTraces() {

        // re-overwrite uncaught exception handler if necessary
        if (!uncaughtExceptionHandler.equals(Thread.getDefaultUncaughtExceptionHandler())) {
            LOGGER.info("Default exception handler has been overridden!");
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }

        // signal that the tracer is currently dumping traces
        createRunFile();

        File sdCard = Environment.getExternalStorageDirectory();
        File traceFile = new File(sdCard, TRACES_FILE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!isPermissionGranted(null, WRITE_EXTERNAL_STORAGE)) {
                LOGGER.info("Permissions got dropped unexpectedly!");
            }
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
        } catch (Exception e) {
            LOGGER.info("Writing traces.txt to external storage failed.");
            e.printStackTrace();
        }

        // reset traces
        traces.clear();
        deleteRunFile();
    }

    /**
     * Writes the remaining traces to the external storage. Also writes a file 'info.txt' to the external storage
     * containing the number collected traces since the last broadcast.
     */
    private static void writeRemainingTraces() {

        // re-overwrite uncaught exception handler if necessary
        if (!uncaughtExceptionHandler.equals(Thread.getDefaultUncaughtExceptionHandler())) {
            LOGGER.info("Default exception handler has been overridden!");
            Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler);
        }

        // signal that the tracer is currently dumping traces
        createRunFile();

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
                br.newLine();
            }

            if (!traces.isEmpty()) {
                LOGGER.info("Last entry: " + element);
            }

            br.flush();
            br.close();
            writer.close();

        } catch (Exception e) {
            LOGGER.info("Writing traces.txt to external storage failed.");
            e.printStackTrace();
        }

        // signal that we finished writing out traces
        final File infoFile = new File(sdCard, INFO_FILE);

        try {
            FileWriter writer = new FileWriter(infoFile);

            numberOfTraces = numberOfTraces + traces.size();
            writer.append(String.valueOf(numberOfTraces));
            LOGGER.info("Total number of traces in file: " + numberOfTraces);

            // reset traces counter
            numberOfTraces = 0;

            writer.flush();
            writer.close();

        } catch (Exception e) {
            LOGGER.info("Writing info.txt to external storage failed.");
            e.printStackTrace();
        }

        // reset traces
        traces.clear();
        deleteRunFile();
    }

    // unary operation - object types
    public static void computeBranchDistance(String operation, Object argument) {
        computeBranchDistance(operation, argument, null);
    }

    // binary operation - object types
    public static void computeBranchDistance(String operation, Object argument1, Object argument2) {

        int distanceThenBranch = 0;
        int distanceElseBranch = 0;

        String[] tokens = operation.split(":");
        int opcode = Integer.parseInt(tokens[0]);
        final String identifier = tokens[1];

        /*
         * The branch distance computation is based on the following two resources:
         *
         * https://github.com/EvoSuite/evosuite/blob/master/client/src/main/java/org/evosuite/testcase/execution/ExecutionTracer.java#L540
         * https://ieeexplore.ieee.org/document/5477082
         */
        switch (opcode) {
            case 0: // if-eqz, if-eq
                if (argument1 == argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = 1;
                } else {
                    distanceThenBranch = 1;
                    distanceElseBranch = 0;
                }
                break;
            case 1: // if-nez, if-ne
                if (argument1 != argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = 1;
                } else {
                    distanceThenBranch = 1;
                    distanceElseBranch = 0;
                }
                break;
            default:
                // Further comparisons on object types seem to be not reasonable.
                throw new UnsupportedOperationException("Comparison operator " + operation + " not yet supported!");
        }

        final String traceThenBranch = identifier + ":" + distanceThenBranch;
        final String traceElseBranch = identifier + ":" + distanceElseBranch;
        trace(traceThenBranch);
        trace(traceElseBranch);
    }

    // unary operation - primitive types
    public static void computeBranchDistance(String operation, int argument) {
        computeBranchDistance(operation, argument, 0);
    }

    // binary operation - primitive types
    public static void computeBranchDistance(String operation, int argument1, int argument2) {

        int distanceThenBranch = 0;
        int distanceElseBranch = 0;

        String[] tokens = operation.split(":");
        int opcode = Integer.parseInt(tokens[0]);
        final String identifier = tokens[1];

        /*
         * The branch distance computation is based on the following two resources:
         *
         * https://github.com/EvoSuite/evosuite/blob/master/client/src/main/java/org/evosuite/testcase/execution/ExecutionTracer.java#L540
         * https://ieeexplore.ieee.org/document/5477082
         */
        switch (opcode) {
            case 0: // if-eqz, if-eq
                if (argument1 != argument2) {
                    distanceThenBranch = Math.abs(argument1 - argument2);
                    distanceElseBranch = 0;
                } else {
                    distanceThenBranch = 0;
                    distanceElseBranch = 1;
                }
                break;
            case 1: // if-nez, if-ne
                if (argument1 != argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = Math.abs(argument1 - argument2);
                } else {
                    distanceThenBranch = 1;
                    distanceElseBranch = 0;
                }
                break;
            case 2: // if-lez, if-le
                if (argument1 <= argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = Math.abs(argument2 - argument1) + 1; // +1 necessary in case arg1 == arg2
                } else {
                    distanceThenBranch = Math.abs(argument1 - argument2);
                    distanceElseBranch = 0;
                }
                break;
            case 3: // if-ltz, if-lt
                if (argument1 < argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = Math.abs(argument2 - argument1);
                } else {
                    distanceThenBranch = Math.abs(argument1 - argument2) + 1; // +1 necessary in case arg1 == arg2
                    distanceElseBranch = 0;
                }
                break;
            case 4: // if-gez, if-ge
                if (argument1 >= argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = Math.abs(argument1 - argument2) + 1; // +1 necessary in case arg1 == arg2
                } else {
                    distanceThenBranch = Math.abs(argument2 - argument1);
                    distanceElseBranch = 0;
                }
                break;
            case 5: // if-gtz, if-gt
                if (argument1 > argument2) {
                    distanceThenBranch = 0;
                    distanceElseBranch = Math.abs(argument1 - argument2);
                } else {
                    distanceThenBranch = Math.abs(argument2 - argument1) + 1; // +1 necessary in case arg1 == arg2
                    distanceElseBranch = 0;
                }
                break;
            default:
                throw new UnsupportedOperationException("Comparison operator not yet supported!");
        }

        final String traceThenBranch = identifier + ":" + distanceThenBranch;
        final String traceElseBranch = identifier + ":" + distanceElseBranch;
        trace(traceThenBranch);
        trace(traceElseBranch);
    }

    /**
     * Computes the branch distance for a switch instruction. Note that we use a binary metric right now, i.e. the
     * covered case statement gets assigned a branch distance of 0 and all other case statements a distance of 1.
     *
     * @param trace The trace describing the switch instruction.
     * @param switchValue The value of the switch instruction to be compared with each case statement.
     * @param cases The case statements (positions and values).
     */
    public static void computeBranchDistanceSwitch(String trace, int switchValue, String cases) {

        final String[] casePosValuePairs = cases.split(",");

        for (String casePosValuePair : casePosValuePairs) {

            final int casePosition = Integer.parseInt(casePosValuePair.split(":")[0]);
            final int caseValue = Integer.parseInt(casePosValuePair.split(":")[1]);

            // TODO: Use same metric as for if-eq.
            int branchDistance = 1;

            if (switchValue == caseValue) {
                // the taken case/branch has a distance of 0
                branchDistance = 0;
            }

            final String tracePrefix = trace.substring(0, trace.lastIndexOf("->"));
            final String traceCase = tracePrefix + "->" + casePosition + ":" + branchDistance;
            trace(traceCase);
        }
    }

    /**
     * Computes the branch distance for a switch instruction without an explicit default branch. Note that we use a
     * binary metric right now, i.e. the covered case statement gets assigned a branch distance of 0 and all other case
     * statements a distance of 1.
     *
     * @param trace The trace describing the switch instruction.
     * @param switchValue The value of the switch instruction to be compared with each case statement.
     * @param cases The case statements (positions and values).
     */
    public static void computeBranchDistanceSwitchNoDefaultBranch(String trace, int switchValue, String cases) {

        // remember whether the default branch was taken or not
        boolean tookDefaultBranch = true;

        final String[] casePosValuePairs = cases.split(",");

        for (String casePosValuePair : casePosValuePairs) {

            final int casePosition = Integer.parseInt(casePosValuePair.split(":")[0]);
            final int caseValue = Integer.parseInt(casePosValuePair.split(":")[1]);

            // TODO: Use same metric as for if-eq once a reasonable distance can be assigned to the default branch.
            int branchDistance = 1;

            if (switchValue == caseValue) {
                // the taken case/branch has a distance of 0
                branchDistance = 0;
                tookDefaultBranch = false;
            }

            final String tracePrefix = trace.substring(0, trace.lastIndexOf("->"));
            final String traceCase = tracePrefix + "->" + casePosition + ":" + branchDistance;
            trace(traceCase);
        }

        // we require an explicit trace for the default branch
        final String tracePrefix = trace.substring(0, trace.lastIndexOf("->"));

        // the default branch is always the direct successor of the switch statement
        String[] tokens = trace.split("->");
        final int defaultBranchPosition = Integer.parseInt(tokens[tokens.length - 1]) + 1;
        final int defaultBranchDistance = tookDefaultBranch ? 0 : 1;
        final String traceDefaultBranch = tracePrefix + "->" + defaultBranchPosition + ":" + defaultBranchDistance;
        trace(traceDefaultBranch);
    }

    /**
     * Creates a file on the external storage to indicate that the tracer is currently dumping traces.
     */
    private static synchronized void createRunFile() {

        File sdCard = Environment.getExternalStorageDirectory();
        File file = new File(sdCard, RUNNING_FILE);

        try {
            boolean ignored = file.createNewFile();
        } catch (IOException e) {
            LOGGER.warning("Failed to create file " + RUNNING_FILE);
            LOGGER.warning(e.getMessage());
        }
    }

    /**
     * Removes the generated file by {@link #createRunFile()} to indicate that the tracer finished dumping the traces.
     */
    private static synchronized void deleteRunFile() {
        File sdCard = Environment.getExternalStorageDirectory();
        boolean ignored = new File(sdCard, RUNNING_FILE).delete();
    }
}