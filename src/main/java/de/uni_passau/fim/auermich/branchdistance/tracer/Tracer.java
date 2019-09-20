package de.uni_passau.fim.auermich.branchdistance.tracer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;

/**
 * Provides the functionality to trace branches for
 * a given application.
 */
public class Tracer {

    // tracks the execution path (prefer List to MultiMap since no external dependencies are required)
    private static List<String> executionPath = new LinkedList<>();

    // the output file containing the covered branches
    private static final String TRACES_FILE = "traces.txt";

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Tracer.class
            .getName());

    /**
     * Gets the current system time formatted as string.
     *
     * @return Returns a string representation of the current system time.
     */
    private static String getCurrentTimeStamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
    }

    /**
     * Adds a new branch to the set of covered branches. The set
     * ensures that we don't track duplicate branches.
     *
     * @param identifier Uniquely identifies the given branch.
     */
    public static void trace(String identifier) {
        executionPath.add(identifier);
    }

    /**
     * Writes the collected (unique) branches to the app internal storage, which
     * is specified through the package name {@param packageName}.
     *
     * @param packageName The packageName describing the path of the app
     *                    internal storage. (data/data/packageName)
     */
    public static void write(String packageName) {

        // path to internal app directory
        String filePath = "data/data/" + packageName;

        LOGGER.info(filePath);

        File file = new File(filePath, TRACES_FILE);

        try {

            FileWriter writer = new FileWriter(file);

            // record when new traces file was generated
            writer.append(getCurrentTimeStamp() + ": NEW TRACE");
            writer.append(System.lineSeparator());

            for (String pathNode : executionPath) {
                writer.append(pathNode);
                writer.append(System.lineSeparator());
            }

            // reset executionPath
            executionPath.clear();

            writer.flush();
            writer.close();

        } catch(IOException e) {
            LOGGER.info("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }
}