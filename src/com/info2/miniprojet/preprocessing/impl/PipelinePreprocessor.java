package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor; // The interface it implements

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class PipelinePreprocessor implements Preprocessor {

    private final List<Preprocessor> stages;
    private final String pipelineName;

    /**
     * Constructs a PipelinePreprocessor.
     *
     * @param stages A list of Preprocessor instances that will be executed in order.
     *               The list should not be null or empty.
     * @throws IllegalArgumentException if stages is null or empty.
     */
    public PipelinePreprocessor(List<Preprocessor> stages) {
        if (stages == null || stages.isEmpty()) {
            throw new IllegalArgumentException("Pipeline stages cannot be null or empty.");
        }
        // Create a defensive copy of the stages list to prevent external modification
        this.stages = new ArrayList<>(stages);

        // Construct a descriptive name for this pipeline instance
        this.pipelineName = "PIPELINE[" +
                this.stages.stream()
                        .map(Preprocessor::getName) // Get name of each stage
                        .collect(Collectors.joining(",")) + // Join names with comma
                "]";
        System.out.println("DEBUG: PipelinePreprocessor created with stages: " + this.pipelineName);
    }

    /**
     * Applies each preprocessor stage in the pipeline sequentially.
     * The input list of tokens is transformed by each stage in order.
     *
     * @param inputTokens The initial list of tokens. For the very first call in an
     *                    overall preprocessing chain, this list might contain a single
     *                    element which is the raw input string. Subsequent internal
     *                    stages in the pipeline will receive the token list output
     *                    from the previous stage.
     * @return The final list of tokens after all pipeline stages have been applied.
     */
    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            System.err.println("PipelinePreprocessor received null input, returning empty list.");
            return new ArrayList<>();
        }

        System.out.println("DEBUG: PipelinePreprocessor processing input: " + inputTokens);
        List<String> currentTokens = new ArrayList<>(inputTokens); // Work on a copy

        for (int i = 0; i < stages.size(); i++) {
            Preprocessor stage = stages.get(i);
            System.out.println("DEBUG: Pipeline applying stage " + (i + 1) + "/" + stages.size() + ": " + stage.getName());
            currentTokens = stage.preprocess(currentTokens); // Output of one becomes input to next
            System.out.println("DEBUG: Pipeline after stage " + stage.getName() + ": " + currentTokens);
            if (currentTokens == null) { // A stage should not return null
                System.err.println("ERROR: Preprocessor stage '" + stage.getName() + "' returned null. Aborting pipeline.");
                return new ArrayList<>(); // Or throw an exception
            }
        }
        System.out.println("DEBUG: PipelinePreprocessor finished. Output: " + currentTokens);
        return currentTokens; // Return the final processed list
    }

    /**
     * Returns the dynamically constructed name of this pipeline,
     * indicating the sequence of stages it contains.
     *
     * @return The name of the pipeline.
     */
    @Override
    public String getName() {
        return this.pipelineName;
    }
}