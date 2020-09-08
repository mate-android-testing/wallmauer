package de.uni_passau.fim.auermich.branchcoverage_evaluation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class BranchCoverageEvaluation {

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(BranchCoverageEvaluation.class
            .getName());

    // tracks the number of total branches per class <class,#branches>
    private static Map<String, Integer> branches = new HashMap<>();

    // tracks the number of visited branches per class <class,#branches>
    private static Map<String, Integer> visitedBranches = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // read number of branches per class
        InputStream branchesInputStream = new FileInputStream(new File("branches.txt"));
        BufferedReader branchesReader = new BufferedReader(new InputStreamReader(branchesInputStream));

        String line;
        while((line = branchesReader.readLine()) != null){
            // each line consists of className: #branches
            String[] tuple = line.split(":");
            branches.put(tuple[0], Integer.parseInt(tuple[1].trim()));
        }

        branchesReader.close();

        // read the traces
        InputStream tracesInputStream = new FileInputStream(new File("traces.txt"));
        BufferedReader tracesReader = new BufferedReader(new InputStreamReader(tracesInputStream));
        Set<String> coveredTraces = new HashSet<>();

        String trace;
        while ((trace = tracesReader.readLine()) != null) {

            // each trace consists of className->methodName->branchID
            String[] triple = trace.split("->");

            if (visitedBranches.containsKey(triple[0])) {
                // only new not yet covered branches are interesting
                if (!coveredTraces.contains(trace)) {
                    // new covered branch for class, increase by one
                    int visitedBranchesOfClass = visitedBranches.get(triple[0]);
                    visitedBranches.put(triple[0], ++visitedBranchesOfClass);
                }
            } else {
                // it's a new entry for the given class
                visitedBranches.put(triple[0], 1);
            }

            coveredTraces.add(trace);
        }

        tracesReader.close();

        // compute branch coverage per class
        for(String key : branches.keySet()) {

            float totalBranches = branches.get(key);
            float coveredBranches = visitedBranches.get(key);
            LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredBranches/totalBranches*100 + "%");
        }
    }
}
