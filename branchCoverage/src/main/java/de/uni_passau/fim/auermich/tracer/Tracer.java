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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * Provides the functionality to trace branches for
 * a given application.
 */
public class Tracer extends BroadcastReceiver {

    // tracks the execution path (prefer List to MultiMap since no external dependencies are required)
    // TODO: use synchronized list if really necessary to avoid synchronized block
    private static List<String> executionPath = Collections.synchronizedList(new ArrayList<>());

    // the output file containing the covered branches
    private static final String TRACES_FILE = "traces.txt";

    // keeps track of the total number of generated traces per test case / trace file
    // FIXME: doesn't reflect updates in onReceive() method for yet unknown reasons
    private static AtomicInteger numberOfTraces = new AtomicInteger(0);

    // we can't use here log4j2 since we would require that dependency bundled with the app otherwise
    private static final Logger LOGGER = Logger.getLogger(Tracer.class
            .getName());

    private static final int CACHE_SIZE = 5000;

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent.getAction() != null && intent.getAction().equals("STORE_TRACES")) {
            String packageName = intent.getStringExtra("packageName");
            LOGGER.info("Received Broadcast");

            if (!isPermissionGranted(context, WRITE_EXTERNAL_STORAGE)) {
                LOGGER.info("Permissions got dropped unexpectedly!");
            }

            // it seems like previous invocations of the tracer can interfere with the following
            synchronized (Tracer.class) {
                write(packageName);
                executionPath.clear();
            }
        }
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
     * Writes the collected traces to the external storage.
     */
    private static void write() {

        File sdCard = Environment.getExternalStorageDirectory();
        File traces = new File(sdCard, TRACES_FILE);

        if (!isPermissionGranted(null, WRITE_EXTERNAL_STORAGE)) {
            LOGGER.info("Permissions got dropped unexpectedly!");
        }

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
            for (Thread thread: threadStackTraces.keySet()) {
                StackTraceElement[] stackTrace = threadStackTraces.get(thread);
                LOGGER.info("Thread[" + thread.getId() + "]: " + thread);
                for (StackTraceElement stackTraceElement : stackTrace) {
                    LOGGER.info(String.valueOf(stackTraceElement));
                }
            }
        } catch (IOException e) {
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

        LOGGER.info("Remaining Traces Size: " + executionPath.size());

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