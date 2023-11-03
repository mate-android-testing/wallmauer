package de.uni_passau.fim.auermich.basicblockcoverage_evaluation;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BasicBlockCoverageEvaluation {

    private static final Logger LOGGER = LogManager.getLogger(BasicBlockCoverageEvaluation.class);

    private static Map<String, Integer> totalInstructionsPerClass;
    private static Map<String, Integer> totalBranchesPerClass;
    private static Map<String, Integer> coveredInstructionsPerClass;
    private static Map<String, Integer> coveredBranchesPerClass;

    /**
     * Evaluates the basic block coverage based on the given trace file.
     *
     * @param args The command line arguments, see the description below.
     * @throws IOException Should never happen.
     */
    public static void main(String[] args) throws IOException {

        Configurator.setAllLevels(LogManager.getRootLogger().getName(), Level.INFO);

        if (args.length != 2) {
            LOGGER.info("Usage: java -jar basicBlockCoverageEvaluation.jar <path-to-blocks.txt> <path-to-traces.txt>");
        } else {
                totalPerClass(args[0].trim());
                coveredPerClass(args[1].trim());

            for (final String key : totalInstructionsPerClass.keySet()) {
                final float coveredInstructions = coveredInstructionsPerClass.getOrDefault(key, 0);
                final float totalInstructions = totalInstructionsPerClass.get(key);
                if (coveredInstructions > 0) {
                    LOGGER.info("We have for the class " + key + " a line coverage of: "
                            + coveredInstructions / totalInstructions * 100 + "%");
                }
            }

            for(final String key : totalBranchesPerClass.keySet()) {
                final float coveredBranches = coveredBranchesPerClass.getOrDefault(key, 0);
                final float totalBranches = totalBranchesPerClass.get(key);
                if (coveredBranches > 0) {
                    LOGGER.info("We have for the class " + key + " a branch coverage of: "
                            + coveredBranches / totalBranches * 100 + "%");
                }
            }

            final int totalInstructions = totalInstructionsPerClass.values().stream().mapToInt(Integer::intValue).sum();
            final int coveredInstructions = coveredInstructionsPerClass.values().stream().mapToInt(Integer::intValue).sum();
            final double totalLineCoverage = (double) coveredInstructions / (double) totalInstructions * 100d;
            LOGGER.info("Total line coverage: " + totalLineCoverage + "%");

            final int totalBranches = totalBranchesPerClass.values().stream().mapToInt(Integer::intValue).sum();
            final int coveredBranches = coveredBranchesPerClass.values().stream().mapToInt(Integer::intValue).sum();
            final double totalBranchCoverage = (double) coveredBranches / (double) totalBranches * 100d;
            LOGGER.info("Total branch coverage: " + totalBranchCoverage + "%");
        }
    }

    private static  void totalPerClass(final String filePath) throws IOException {
        totalInstructionsPerClass = new HashMap<>();
        totalBranchesPerClass = new HashMap<>();

        // Assumes there are not duplicate lines in the file
        try (BufferedReader branchesReader
                     = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {

            // an entry looks as follows: class name -> method name -> block id -> block size -> isBranch
            String line;
            while ((line = branchesReader.readLine()) != null) {

                final String[] tokens = line.split("->");
                final String clazz = tokens[0];

                final int instructionCount = Integer.parseInt(tokens[3]);
                final int recorded = totalInstructionsPerClass.getOrDefault(clazz, 0);
                totalInstructionsPerClass.put(clazz, recorded + instructionCount);

                boolean isBranch = tokens[4].equals("isBranch");

                // aggregate branches count per class
                if (isBranch) {
                    // add 1 to current count
                    totalBranchesPerClass.merge(clazz, 1, Integer::sum);
                }
            }
        }
    }
    
    private static void coveredPerClass(final String filePath) throws IOException {
        // The same basic blocks can be executed multiple times during a run
        // But for the coverage we only need to count each block once, even if it is executed multiple times

        // Class name -> Method name -> Basic block id -> Instruction count
        final Map<String, Map<String, Map<Integer, Integer>>> instructionCount = new HashMap<>();

        // Class name -> Method name -> Basic block id
        final Map<String, Map<String, Set<Integer>>> coveredBranches = new HashMap<>();

        try (BufferedReader branchesReader
                     = new BufferedReader(new InputStreamReader(new FileInputStream(new File(filePath))))) {

            // Class name -> method name -> id -> instructions count -> isBranch
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                final String clazz = tuple[0];
                final String method = tuple[1];
                final Integer blockId = Integer.parseInt(tuple[2]);
                final int count = Integer.parseInt(tuple[3]);
                final boolean isBranch = tuple[4].equals("isBranch");

                instructionCount.putIfAbsent(clazz, new HashMap<>());
                instructionCount.get(clazz).putIfAbsent(method, new HashMap<>());
                instructionCount.get(clazz).get(method).putIfAbsent(blockId, count);

                if(isBranch) {
                    coveredBranches.putIfAbsent(clazz, new HashMap<>());
                    coveredBranches.get(clazz).putIfAbsent(method, new HashSet<>());
                    coveredBranches.get(clazz).get(method).add(blockId);
                }
            }
        }

        coveredInstructionsPerClass = new HashMap<>();
        instructionCount.keySet().forEach(clazz -> {
            final int coveredInstructions = instructionCount.get(clazz).entrySet().stream()
                    .flatMap(e -> e.getValue().entrySet().stream()).mapToInt(Map.Entry::getValue).sum();
            coveredInstructionsPerClass.put(clazz, coveredInstructions);
        });

        coveredBranchesPerClass = new HashMap<>();
        coveredBranches.keySet().forEach(clazz -> {
            final int count = coveredBranches.get(clazz).values().stream().mapToInt(Set::size).sum();
            coveredBranchesPerClass.put(clazz, count);
        });
    }
}
