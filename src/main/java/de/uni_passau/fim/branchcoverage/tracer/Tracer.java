package de.uni_passau.fim.branchcoverage.tracer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Provides the functionality to trace branches for
 * a given application.
 */
public class Tracer {

    // collects visited branches, where a branch consists of class->method->branchID
    private static Set<String> visitedBranches = new HashSet<>();

    // the output file containing the covered branches
    private static final String TRACES_FILE = "traces.txt";

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(Tracer.class
            .getName());

    /**
     * Adds a new branch to the set of covered branches. The set
     * ensures that we don't track duplicate branches.
     *
     * @param identifier Uniquely identifies the given branch.
     */
    public static void trace(String identifier) {
        visitedBranches.add(identifier);
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

            for(String branch: visitedBranches) {
                writer.append(branch);
                writer.append(System.lineSeparator());
            }

            writer.flush();
            writer.close();

        } catch(IOException e) {
            LOGGER.info("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }
}