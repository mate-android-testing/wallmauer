package de.uni_passau.fim.auermich.branchcoverage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class BranchCoverageEvaluation {

    // tracks the number of total branches per class <class,#branches>
    private static Map<String, Integer> branches = new HashMap<>();

    // tracks the number of visited branches per class <class,#branches>
    private static Map<String, Integer> visitedBranches = new HashMap<>();

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(BranchCoverageEvaluation.class
            .getName());

    public static void main(String[] args) throws IOException {

        // track the number of branches per class
        File file = new File("branches.txt");
        InputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while((line = reader.readLine()) != null){
            // each line consists of className: #branches
            String[] tuple = line.split(":");
            branches.put(tuple[0], Integer.parseInt(tuple[1].trim()));
        }

        reader.close();

        // track which branches were covered
        File traces = new File("traces.txt");
        InputStream inputStreamTraces = new FileInputStream(traces);
        BufferedReader readerTraces = new BufferedReader(new InputStreamReader(inputStreamTraces));

        String trace;
        while ((trace = readerTraces.readLine()) != null) {
            // each trace consists of className->methodName->branchID
            String[] triple = trace.split("->");
            // we only have unique traces in our file (see Tracer.java)
            if (visitedBranches.containsKey(triple[0])) {
                int visitedBranchesOfClass = visitedBranches.get(triple[0]);
                // new covered branch for class, increase by one
                visitedBranches.put(triple[0], ++visitedBranchesOfClass);
            } else {
                // if it's a new entry
                visitedBranches.put(triple[0], 1);
            }
        }

        // compute branch coverage per class
        for(String key : branches.keySet()) {

            float totalBranches = branches.get(key);
            float coveredBranches = visitedBranches.get(key);

            LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredBranches/totalBranches*100 + "%");

        }
    }
}
