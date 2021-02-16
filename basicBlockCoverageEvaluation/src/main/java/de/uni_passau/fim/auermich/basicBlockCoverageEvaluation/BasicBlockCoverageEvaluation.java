package de.uni_passau.fim.auermich.basicBlockCoverageEvaluation;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicBlockCoverageEvaluation {

    private static final Logger LOGGER = Logger.getLogger(BasicBlockCoverageEvaluation.class
            .getName());

    public static void main(String[] args) throws IOException {
        LOGGER.setLevel(Level.ALL);

        if (args.length != 2) {
            LOGGER.info("Usage: java -jar basicBlockCoverageEvaluation.jar <path-to-branches.txt> <path-to-traces.txt>");
        } else {
                final Map<String, Integer> totalInstructionsPerClass = totalInstructionsPerClass(args[0].trim());
                final Map<String, Integer> coveredInstructionsPerClass = totalInstructionsCoveredPerClass(args[1].trim());

            for (final String key : totalInstructionsPerClass.keySet()) {
                final float coveredBranches = coveredInstructionsPerClass.getOrDefault(key, 0);
                final float totalBranches = totalInstructionsPerClass.get(key);
                if(coveredBranches > 0) {
                    LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredBranches / totalBranches * 100 + "%");
                }
            }
        }
    }

    private static  Map<String, Integer>  totalInstructionsPerClass(final String filePath) throws IOException {
        final Map<String, Integer> instructions = new HashMap<>();

        // Assumes there are not duplicate lines in the file
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

    private static Map<String, Integer> totalInstructionsCoveredPerClass(final String filePath) throws IOException {
        // The same basic blocks can be executed multiple times during a run
        // But for the coverage we only need to count each block once, even if it is executed multiple times

        // Class name -> Method name -> Basic block id -> Instruction count
        final Map<String, Map<String, Map<Integer, Integer>>> map = new HashMap<>();
        try (BufferedReader branchesReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                final String clazz = tuple[0];
                final String method = tuple[1];
                final Integer blockId = Integer.parseInt(tuple[2].trim());
                final int instruction_count = Integer.parseInt(tuple[3].trim());

                map.putIfAbsent(clazz, new HashMap<>());
                map.get(clazz).putIfAbsent(method, new HashMap<>());
                map.get(clazz).get(method).putIfAbsent(blockId, instruction_count);
            }
        }

        final Map<String, Integer> result = new HashMap<>();
        for (final String clazz : map.keySet()) {
            final int coveredInstructions = map.get(clazz).entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()).mapToInt(Map.Entry::getValue).sum();
            result.put(clazz, coveredInstructions);
        }

        return result;
    }
}
