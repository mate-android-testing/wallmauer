package de.uni_passau.fim.auermich.basicBlockCoverageEvaluation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BasicBlockCoverageEvaluation {

    private static final Logger LOGGER = Logger.getLogger(BasicBlockCoverageEvaluation.class
            .getName());

    private static Map<String, Integer> totalInstructionsPerClass;
    private static Map<String, Integer> totalBranchesPerClass;
    private static Map<String, Integer> coveredInstructionsPerClass;
    private static Map<String, Integer> coveredBranchesPerClass;

    public static void main(String[] args) throws IOException {
        LOGGER.setLevel(Level.ALL);

        if (args.length != 2) {
            LOGGER.info("Usage: java -jar basicBlockCoverageEvaluation.jar <path-to-branches.txt> <path-to-traces.txt>");
        } else {
                totalInstructionsPerClass(args[0].trim());
                totalInstructionsCoveredPerClass(args[1].trim());

            for (final String key : totalInstructionsPerClass.keySet()) {
                final float coveredInstructions = coveredInstructionsPerClass.getOrDefault(key, 0);
                final float totalInstructions = totalInstructionsPerClass.get(key);
                if(coveredInstructions > 0) {
                    LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredInstructions / totalInstructions * 100 + "%");
                }
            }

            for(final String key : totalBranchesPerClass.keySet()) {
                final float coveredBranches = coveredBranchesPerClass.getOrDefault(key, 0);
                final float totalBranches = totalBranchesPerClass.get(key);
                if(coveredBranches > 0) {
                    LOGGER.info("We have for the class " + key + " a branch coverage of: " + coveredBranches / totalBranches * 100 + "%");
                }
            }
        }
    }

    private static  void  totalInstructionsPerClass(final String filePath) throws IOException {
        totalInstructionsPerClass = new HashMap<>();
        totalBranchesPerClass = new HashMap<>();

        // Assumes there are not duplicate lines in the file
        try (BufferedReader branchesReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {

            // Class name -> method name -> id -> instructions count -> isBranch
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                final String clazz = tuple[0];
                final int instruction_count = Integer.parseInt(tuple[2].trim());
                final int recorded = totalInstructionsPerClass.getOrDefault(clazz, 0);
                totalInstructionsPerClass.put(clazz, recorded + instruction_count);

                final boolean isBranch = tuple[3].trim().equals("isBranch");
                if(isBranch) {
                    final int count = totalBranchesPerClass.getOrDefault(clazz, 0);
                    totalBranchesPerClass.put(clazz, count + 1);
                }

            }
        }

    }
    
    private static void totalInstructionsCoveredPerClass(final String filePath) throws IOException {
        // The same basic blocks can be executed multiple times during a run
        // But for the coverage we only need to count each block once, even if it is executed multiple times

        // Class name -> Method name -> Basic block id -> Instruction count
        final Map<String, Map<String, Map<Integer, Integer>>> instruction_count = new HashMap<>();

        // Class name -> Method name -> Basic block id
        final Map<String, Map<String, Set<Integer>>> covered_branches = new HashMap<>();

        try (BufferedReader branchesReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {

            // Class name -> method name -> id -> instructions count -> isBranch
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                final String clazz = tuple[0];
                final String method = tuple[1];
                final Integer blockId = Integer.parseInt(tuple[2].trim());
                final int count = Integer.parseInt(tuple[3].trim());
                final boolean isBranch = tuple[3].trim().equals("isBranch");

                instruction_count.putIfAbsent(clazz, new HashMap<>());
                instruction_count.get(clazz).putIfAbsent(method, new HashMap<>());
                instruction_count.get(clazz).get(method).putIfAbsent(blockId, count);

                if(isBranch) {
                    covered_branches.putIfAbsent(clazz, new HashMap<>());
                    covered_branches.get(clazz).putIfAbsent(method, new HashSet<>());
                    covered_branches.get(clazz).get(method).add(blockId);
                }
            }
        }

        coveredInstructionsPerClass = new HashMap<>();
        instruction_count.keySet().forEach(clazz -> {
            final int coveredInstructions = instruction_count.get(clazz).entrySet().stream().flatMap(e -> e.getValue().entrySet().stream()).mapToInt(Map.Entry::getValue).sum();
            coveredBranchesPerClass.put(clazz, coveredInstructions);
        });

        coveredBranchesPerClass = new HashMap<>();
        covered_branches.keySet().forEach(clazz -> {
            final int count = covered_branches.get(clazz).values().stream().mapToInt(Set::size).sum();
            coveredInstructionsPerClass.put(clazz, count);
        });
    }
}
