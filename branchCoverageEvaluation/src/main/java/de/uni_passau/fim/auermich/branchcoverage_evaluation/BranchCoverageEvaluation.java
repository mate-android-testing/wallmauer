package de.uni_passau.fim.auermich.branchcoverage_evaluation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BranchCoverageEvaluation {

    private static final Logger LOGGER = Logger.getLogger(BranchCoverageEvaluation.class.getName());

    /**
     * Evaluates the branch coverage based on the given trace file.
     *
     * @param args The command line arguments, see the description below.
     * @throws IOException Should never happen.
     */
    public static void main(String[] args) throws IOException {

        LOGGER.setLevel(Level.ALL);

        if (args.length != 2) {
            LOGGER.info("Usage: java -jar branchCoverageEvaluation.jar <path-to-branches.txt> <path-to-traces.txt>");
        } else {

            // first argument refers to branches.txt
            InputStream branchesInputStream = new FileInputStream(args[0]);
            BufferedReader branchesReader = new BufferedReader(new InputStreamReader(branchesInputStream));

            // tracks the number of total branches per class and method
            Map<String, Map<String, Integer>> branches = new HashMap<>();

            // read number of branches per class (each entry is unique)
            String line;
            while ((line = branchesReader.readLine()) != null) {
                // each line consists of className->methodName->branchID
                String[] triple = line.split("->");

                if (triple.length == 3) {
                    // ignore the blank line of the branches.txt at the end
                    String clazz = triple[0];
                    String method = triple[1];
                    branches.putIfAbsent(clazz, new HashMap<>());
                    branches.get(clazz).merge(method, 1, Integer::sum);
                }
            }

            branchesReader.close();

            // second argument refers to traces.txt
            InputStream tracesInputStream = new FileInputStream(new File(args[1]));
            BufferedReader tracesReader = new BufferedReader(new InputStreamReader(tracesInputStream));

            // tracks the number of visited branches per class and method
            Map<String, Map<String, Integer>> visitedBranches = new HashMap<>();

            Set<String> coveredTraces = new HashSet<>();

            // read the traces
            String trace;
            while ((trace = tracesReader.readLine()) != null) {

                // each trace consists of className->methodName->branchID
                String[] triple = trace.split("->");

                if (triple.length != 3 || trace.contains(":")
                        || trace.endsWith("->exit") || trace.endsWith("->entry")) {
                    // ignore traces related to if statements or branch distance or virtual entry/exit vertices
                    continue;
                }

                String clazz = triple[0];
                String method = triple[1];

                if (!coveredTraces.contains(trace)) {
                    // only new traces are interesting
                    coveredTraces.add(trace);

                    // sum up how many branches have been visited by method and class
                    visitedBranches.putIfAbsent(clazz, new HashMap<>());
                    visitedBranches.get(clazz).merge(method, 1, Integer::sum);
                }
            }

            tracesReader.close();

            double overallCoveredBranches = 0.0;
            double overallBranches = 0.0;

            for (String clazz : branches.keySet()) {

                for (String method : branches.get(clazz).keySet()) {

                    // coverage per method

                    double coveredBranches = 0.0;

                    if (visitedBranches.containsKey(clazz) && visitedBranches.get(clazz).containsKey(method)) {
                        coveredBranches = visitedBranches.get(clazz).get(method);
                    }

                    double totalBranches = branches.get(clazz).get(method);

                    LOGGER.info("We have for the method " + clazz + "->" + method + " a branch coverage of "
                            + (coveredBranches / totalBranches * 100) + "%.");
                }

                // coverage per class

                double coveredBranches = 0.0;

                if (visitedBranches.containsKey(clazz)) {
                    coveredBranches = visitedBranches.get(clazz).values().stream().reduce(0, Integer::sum);
                }

                double totalBranches = branches.get(clazz).values().stream().reduce(0, Integer::sum);

                LOGGER.info("We have for the class " + clazz + " a branch coverage of "
                        + (coveredBranches / totalBranches * 100) + "%.");

                // update total coverage
                overallCoveredBranches += coveredBranches;
                overallBranches += totalBranches;
            }

            // total branch coverage
            LOGGER.info("We have a total branch coverage of "
                    + (overallCoveredBranches / overallBranches * 100) + "%.");
        }
    }
}
