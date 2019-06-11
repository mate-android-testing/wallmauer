package de.uni_passau.fim.auermich.tracer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class Tracer {

    // collects visited branches, where a branch consists of Package/Class/Method/BranchIndex
    private static Set<String> visitedBranches = new HashSet<>();

    /*
    // defines the boundary when we should write again to file
    private static final int LIMIT = 1;

    // we need a counter which we increase until we reach the limit
    private static int counter = 0;
    */

    // the filename containing our coverage data (traces)
    private static final String TRACES_FILE = "traces.txt";

    private static final String TAG = "TRACER";


    // simply add new trace
    public static void trace(String identifier) {
        visitedBranches.add(identifier);
    }

    public static void write(String packageName) {

        // path to internal app directory
        String filePath = "data/data/" + packageName;

        System.out.println(filePath);

        File file = new File(filePath, TRACES_FILE);

        try {

            FileWriter writer = new FileWriter(file);

            for(String branch: visitedBranches) {
                writer.append(branch);
                writer.append("\n");
            }

            writer.flush();
            writer.close();

        } catch(IOException e) {
            System.out.println("Writing to internal storage failed.");
            e.printStackTrace();
        }
    }
}