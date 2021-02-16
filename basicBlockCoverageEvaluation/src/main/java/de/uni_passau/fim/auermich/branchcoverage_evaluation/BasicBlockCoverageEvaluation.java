package de.uni_passau.fim.auermich.branchcoverage_evaluation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicBlockCoverageEvaluation {

    // the logger instance
    private static final Logger LOGGER = Logger.getLogger(BasicBlockCoverageEvaluation.class
            .getName());

    public static void main(String[] args) throws IOException {
        LOGGER.setLevel(Level.ALL);

        if (args.length != 2) {
            LOGGER.info("Usage: java -jar basicBlockCoverageEvaluation.jar <path-to-branches.txt> <path-to-traces.txt>");
        } else {
                final Map<String, Integer> totalInstructionsPerClass = totalInstructionsPerFile(args[0].trim());
                final Map<String, Integer> coveredInstructionsPerClass = totalInstructionsPerFile(args[1].trim());

            // compute branch coverage per class
            for (String key : totalInstructionsPerClass.keySet()) {
                final float coveredBranches = coveredInstructionsPerClass.getOrDefault(key, 0);
                final float totalBranches = totalInstructionsPerClass.get(key);
                LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredBranches / totalBranches * 100 + "%");
            }
        }
    }

    private static  Map<String, Integer>  totalInstructionsPerFile(String filePath) throws IOException {
        final Map<String, Integer> instructions = new HashMap<>();

        try (BufferedReader branchesReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                final String clazz = tuple[0];
                final int instruction_count = Integer.parseInt(tuple[2].trim());
                final int recorded = instructions.getOrDefault(clazz, 0);
                instructions.put(clazz, recorded + instruction_count);
            }
        }
        return instructions;
    }

}
