package de.uni_passau.fim.auermich.basicblockcoverage_evaluation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

public class BasicBlockCoverageEvaluationTest {

    private static final Logger LOGGER = Logger.getLogger(BasicBlockCoverageEvaluationTest.class
            .getName());

    @DisplayName("Testing basic block branch coverage evaluation!")
    @Test
    public void testBasicBlockBranchCoverageEvaluation() throws IOException {

        InputStream blocksInputStream = getClass().getClassLoader().getResourceAsStream("blocks.txt");
        InputStream tracesInputStream = getClass().getClassLoader().getResourceAsStream("traces.txt");

        final Map<String, Integer> totalBranchesPerClass = totalBranchesPerClass(blocksInputStream);
        final Map<String, Integer> coveredBranchesPerClass = coveredBranchesPerClass(tracesInputStream);

        for (final String key : totalBranchesPerClass.keySet()) {
            final float coveredBranches = coveredBranchesPerClass.getOrDefault(key, 0);
            final float totalBranches = totalBranchesPerClass.get(key);
            if (coveredBranches > 0) {
                LOGGER.info("We have for the class " + key + " a branch coverage of: "
                        + coveredBranches / totalBranches * 100 + "%");
            }
        }

        final int totalBranches = totalBranchesPerClass.values().stream().mapToInt(Integer::intValue).sum();
        final int coveredBranches = coveredBranchesPerClass.values().stream().mapToInt(Integer::intValue).sum();
        final double totalBranchCoverage = (double) coveredBranches / (double) totalBranches * 100d;
        LOGGER.info("Total branch coverage: " + totalBranchCoverage + "%");
    }

    @DisplayName("Testing basic block line coverage evaluation!")
    @Test
    public void testBasicBlockLineCoverageEvaluation() throws IOException {

        InputStream blocksInputStream = getClass().getClassLoader().getResourceAsStream("blocks.txt");
        InputStream tracesInputStream = getClass().getClassLoader().getResourceAsStream("traces.txt");

        final Map<String, Integer> totalInstructionsPerClass = totalInstructionsPerClass(blocksInputStream);
        final Map<String, Integer> coveredInstructionsPerClass = coveredInstructionsPerClass(tracesInputStream);

        for (final String key : totalInstructionsPerClass.keySet()) {
            final float coveredInstructions = coveredInstructionsPerClass.getOrDefault(key, 0);
            final float totalInstructions = totalInstructionsPerClass.get(key);
            if (coveredInstructions > 0) {
                LOGGER.info("We have for the class " + key + " a line coverage of: "
                        + coveredInstructions / totalInstructions * 100 + "%");
            }
        }

        final int totalInstructions = totalInstructionsPerClass.values().stream().mapToInt(Integer::intValue).sum();
        final int coveredInstructions = coveredInstructionsPerClass.values().stream().mapToInt(Integer::intValue).sum();
        final double totalLineCoverage = (double) coveredInstructions / (double) totalInstructions * 100d;
        LOGGER.info("Total line coverage: " + totalLineCoverage + "%");
    }

    /**
     * Computes the covered instructions per class by traversing the given trace file.
     *
     * @param tracesInputStream An input stream on the traces.txt file.
     * @return Returns a mapping of class to covered instruction count.
     * @throws IOException Should never happen.
     */
    private static Map<String, Integer> coveredInstructionsPerClass(InputStream tracesInputStream) throws IOException {

        // stores a mapping of class -> (method -> (basic block -> number of instructions of block))
        final Map<String, Map<String, Map<Integer, Integer>>> coveredInstructions = new HashMap<>();

        try (var traceReader = new BufferedReader(new InputStreamReader(tracesInputStream))) {

            // a trace looks as follows: class name -> method name -> basic block id (instruction index)
            // -> number of instructions of block -> isBranch
            String line;
            while ((line = traceReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                if (tuple.length == 5) {

                    final String clazz = tuple[0].trim();
                    final String method = tuple[1].trim();
                    final Integer blockId = Integer.parseInt(tuple[2].trim());
                    final int count = Integer.parseInt(tuple[3].trim());

                    // ignore duplicate traces
                    coveredInstructions.putIfAbsent(clazz, new HashMap<>());
                    coveredInstructions.get(clazz).putIfAbsent(method, new HashMap<>());
                    coveredInstructions.get(clazz).get(method).putIfAbsent(blockId, count);
                } else {
                    LOGGER.warning("Found incomplete line: " + line);
                }
            }
        }

        // group the covered instructions per class
        final Map<String, Integer> coveredInstructionsPerClass = new HashMap<>();
        coveredInstructions.keySet().forEach(clazz -> {
            final int coveredInstructionsCount = coveredInstructions.get(clazz).entrySet().stream()
                    .flatMap(e -> e.getValue().entrySet().stream()).mapToInt(Map.Entry::getValue).sum();
            coveredInstructionsPerClass.put(clazz, coveredInstructionsCount);
        });

        return coveredInstructionsPerClass;
    }

    /**
     * Retrieves the total number of instructions per class.
     *
     * @param blocksInputStream An input stream on the blocks.txt file.
     * @return Returns the total number of instructions per class.
     * @throws IOException Should never happen.
     */
    private static Map<String, Integer> totalInstructionsPerClass(final InputStream blocksInputStream) throws IOException {

        final Map<String, Integer> totalInstructionsPerClass = new HashMap<>();

        try (var blocksReader = new BufferedReader(new InputStreamReader(blocksInputStream))) {

            // an entry looks as follows: class name -> method name -> block id -> block size -> isBranch
            String line;
            while ((line = blocksReader.readLine()) != null) {

                final String[] tokens = line.split("->");
                final String clazz = tokens[0];
                final int instructionCount = Integer.parseInt(tokens[3]);

                // update aggregation count per class
                final int recorded = totalInstructionsPerClass.getOrDefault(clazz, 0);
                totalInstructionsPerClass.put(clazz, recorded + instructionCount);
            }
        }
        return totalInstructionsPerClass;
    }

    /**
     * Retrieves the total number of branches per class.
     *
     * @param blocksInputStream An input stream on the blocks.txt file.
     * @return Returns the total number of branches per class.
     * @throws IOException Should never happen.
     */
    private static Map<String, Integer> totalBranchesPerClass(final InputStream blocksInputStream)
            throws IOException {

        final Map<String, Integer> totalBranchesPerClass = new HashMap<>();

        try (var blocksReader = new BufferedReader(new InputStreamReader(blocksInputStream))) {

            // an entry looks as follows: class name -> method name -> block id -> block size -> isBranch
            String line;
            while ((line = blocksReader.readLine()) != null) {

                final String[] tokens = line.split("->");
                final String clazz = tokens[0];
                boolean isBranch = tokens[4].equals("isBranch");

                // aggregate branches count per class
                if (isBranch) {
                    // add 1 to current count
                    totalBranchesPerClass.merge(clazz, 1, Integer::sum);
                }
            }
        }
        return totalBranchesPerClass;
    }

    /**
     * Computes the covered branches per class by traversing the given trace file.
     *
     * @param tracesInputStream An input stream on the traces.txt file.
     * @return Returns a mapping of class to covered branches count.
     * @throws IOException Should never happen.
     */
    private static Map<String, Integer> coveredBranchesPerClass(InputStream tracesInputStream)
            throws IOException {

        // stores a mapping of class -> (method -> basic block id) where a basic block can only contain a single branch!
        final Map<String, Map<String, Set<Integer>>> coveredBranches = new HashMap<>();
        try (var branchesReader = new BufferedReader(new InputStreamReader(tracesInputStream))) {

            // a trace looks as follows: class name -> method name -> basic block id (instruction index)
            // -> number of instructions of block -> isBranch
            String line;
            while ((line = branchesReader.readLine()) != null) {
                final String[] tuple = line.split("->");
                if (tuple.length == 5) {

                    final String clazz = tuple[0].trim();
                    final String method = tuple[1].trim();
                    final Integer blockId = Integer.parseInt(tuple[2].trim());
                    final boolean isBranch = tuple[4].trim().equals("isBranch");

                    if (isBranch) {
                        // ignore duplicate traces
                        coveredBranches.putIfAbsent(clazz, new HashMap<>());
                        coveredBranches.get(clazz).putIfAbsent(method, new HashSet<>());
                        coveredBranches.get(clazz).get(method).add(blockId);
                    }
                } else {
                    LOGGER.warning("Found incomplete line: " + line);
                }
            }
        }

        final Map<String, Integer> coveredBranchesPerClass = new HashMap<>();
        coveredBranches.keySet().forEach(clazz -> {
            final int count = coveredBranches.get(clazz).values().stream().mapToInt(Set::size).sum();
            coveredBranchesPerClass.put(clazz, count);
        });
        return coveredBranchesPerClass;
    }
}
