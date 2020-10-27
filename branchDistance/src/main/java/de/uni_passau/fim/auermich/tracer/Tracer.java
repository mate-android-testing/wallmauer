package de.uni_passau.fim.auermich.tracer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 * Provides the functionality to trace branches for
 * a given application.
 */
public class Tracer extends BroadcastReceiver {

    // tracks the execution path (prefer List to MultiMap since no external dependencies are required)
    private static List<String> executionPath = Collections.synchronizedList(new ArrayList<>());

    // the output file containing the covered branches
    private static final String TRACES_FILE = "traces.txt";

    // keeps track of the total number of generated traces per test case / trace file
    // FIXME: doesn't reflect updates in onReceive() method for yet unknown reasons
    private static AtomicInteger numberOfTraces = new AtomicInteger(0);

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Tracer.class
            .getName());

    private static final int CACHE_SIZE = 5000;

    /**
     * Gets the current system time formatted as string.
     * FIXME: unfortunately Ljava/time/LocalDateTime; ins not included in the ART libs
     *
     * @return Returns a string representation of the current system time.
     */
    private static String getCurrentTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("STORE_TRACES")) {
            String packageName = intent.getStringExtra("packageName");
            LOGGER.info("Received Broadcast");
            // it seems like previous invocations of the tracer can interfere with the following
            synchronized (Tracer.class) {
                write(packageName);
                executionPath.clear();
            }
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
        LOGGER.info("BRANCH_DISTANCE: " + distance);
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
        LOGGER.info("BRANCH_DISTANCE: " + distance);
        trace(trace);
    }

    /**
     * Adds a new branch to the set of covered branches. The set
     * ensures that we don't track duplicate branches.
     *
     * @param identifier Uniquely identifies the given branch.
     */
    public static void trace(String identifier) {
        synchronized (Tracer.class) {
            executionPath.add(identifier);

            if (executionPath.size() == CACHE_SIZE) {
                write();
                executionPath.clear();
            }
        }
    }

    private static void write() {

        File sdCard = Environment.getExternalStorageDirectory();
        File traces = new File(sdCard, TRACES_FILE);

        try {

            FileWriter writer = new FileWriter(traces, true);
            BufferedWriter br = new BufferedWriter(writer);

            for (int i = 0; i < CACHE_SIZE; i++) {
                String pathNode = executionPath.get(i);
                br.write(pathNode);
                br.newLine();
            }

            // keep track of collected traces per test case / trace file
            LOGGER.info("Accumulated traces size: " + numberOfTraces.addAndGet(CACHE_SIZE));

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
     * Writes the collected (unique) branches to the app internal storage, which
     * is specified through the package name {@param packageName}.
     *
     * @param packageName The packageName describing the path of the app
     *                    internal storage. (data/data/packageName)
     */
    private static void write(String packageName) {

        // sd card
        File sdCard = Environment.getExternalStorageDirectory();
        File traces = new File(sdCard, TRACES_FILE);

        LOGGER.info("Size: " + executionPath.size());

        if (!executionPath.isEmpty()) {
            LOGGER.info("First entry: " + executionPath.get(0));
            LOGGER.info("Last entry: " + executionPath.get(executionPath.size() - 1));
        }

        // write out remaining traces
        try {

            FileWriter writer = new FileWriter(traces, true);
            BufferedWriter br = new BufferedWriter(writer);

            for (int i = 0; i < executionPath.size(); i++) {
                String pathNode = executionPath.get(i);
                br.write(pathNode);
                br.newLine();
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

            writer.append(String.valueOf(numberOfTraces.addAndGet(executionPath.size())));
            LOGGER.info("Total number of traces: " + numberOfTraces.get());

            // reset traces counter
            numberOfTraces.set(0);

            writer.flush();
            writer.close();

        } catch (IOException e) {
            LOGGER.info("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }
}