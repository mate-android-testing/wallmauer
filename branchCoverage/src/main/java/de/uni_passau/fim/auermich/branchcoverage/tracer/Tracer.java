package de.uni_passau.fim.auermich.branchcoverage.tracer;

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
import java.util.logging.Logger;

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

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Tracer.class
            .getName());

    private static final int CACHE_SIZE = 5000;

    @Override
    public void onReceive(Context context, Intent intent) {
        LOGGER.info("Received Broadcast");

        if (intent.getAction() != null && intent.getAction().equals("STORE_TRACES")) {
            String packageName = intent.getStringExtra("packageName");
            write(packageName);
            executionPath.clear();
        }
    }

    /**
     * Adds a new branch to the set of covered branches. The set
     * ensures that we don't track duplicate branches.
     *
     * @param identifier Uniquely identifies the given branch.
     */
    public static void trace(String identifier) {
        executionPath.add(identifier);

        if (executionPath.size() == CACHE_SIZE) {
            write();
            executionPath.clear();
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

            br.flush();
            br.close();
            writer.close();

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

        System.out.println("Size: " + executionPath.size());

        if (!executionPath.isEmpty()) {
            System.out.println("First entry: " + executionPath.get(0));
            System.out.println("Last entry: " + executionPath.get(executionPath.size() - 1));
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

        // FIXME: handle empty traces file -> check size() before accessing first/last element
        int size = executionPath.size();
        System.out.println("Size: " + size);
        System.out.println("First entry afterwards: " + executionPath.get(0));
        System.out.println("Last entry afterwards: " + executionPath.get(executionPath.size() - 1));

        // signal that we finished writing out traces
        try {
            String filePath = "data/data/" + packageName;
            File info = new File(filePath, "info.txt");
            FileWriter writer = new FileWriter(info);

            writer.append(String.valueOf(size));
            writer.flush();
            writer.close();

        } catch (IOException e) {
            LOGGER.info("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }
}