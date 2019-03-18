package de.uni_passau.fim.branchcoverage;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class BranchCoverageEvaluation {

    private static Map<String, Integer> branches = new HashMap<>();
    private static Map<String, Integer> visitedBranches = new HashMap<>();

    public static void main(String[] args) throws IOException {

        File file = new File("branches.txt");
        InputStream inputStream = new FileInputStream(file);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while((line = reader.readLine()) != null){
            // each line consists of className:#branches
            String[] tuple = line.split(":");
            branches.put(tuple[0], Integer.parseInt(tuple[1].trim()));
        }

        reader.close();

        File traces = new File("traces.txt");
        InputStream inputStreamTraces = new FileInputStream(traces);
        BufferedReader readerTraces = new BufferedReader(new InputStreamReader(inputStreamTraces));

        String trace;
        while ((trace = readerTraces.readLine()) != null) {
            // each trace consists of className->methodName->branchID
            String[] triple = trace.split("->");
            // we only have unique traces in our file (see Tracer_saved.java using Set and single write)
            if (visitedBranches.containsKey(triple[0])) {
                int visitedBranchesOfClass = visitedBranches.get(triple[0]);
                visitedBranches.put(triple[0], ++visitedBranchesOfClass);
            } else {
                // if it's a new entry
                visitedBranches.put(triple[0], 1);
            }
        }

        for(String key : branches.keySet()) {

            float totalBranches = branches.get(key);
            float coveredBranches = visitedBranches.get(key);

            System.out.println("We have for the class " + key + " a branch coverage of: " + coveredBranches/totalBranches*100 + "%");

        }



        }

}
