package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;
import org.junit.jupiter.api.Test;
// import org.mockito.Mockito; // Uncomment if you want to use Mockito for more complex tests later
// import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

class PipelinePreprocessorTest {

    @Test
    void getNameShouldReflectPipelineStages() {
        // Create simple stub/dummy preprocessors for testing name generation
        Preprocessor stage1 = new Preprocessor() {
            public List<String> preprocess(List<String> t) { return t; }
            public String getName() { return "TOKENIZE"; }
        };
        Preprocessor stage2 = new Preprocessor() {
            public List<String> preprocess(List<String> t) { return t; }
            public String getName() { return "LOWERCASE"; }
        };
        PipelinePreprocessor pipeline = new PipelinePreprocessor(Arrays.asList(stage1, stage2));
        assertEquals("PIPELINE[TOKENIZE,LOWERCASE]", pipeline.getName());
    }

    @Test
    void preprocessShouldApplyStagesInOrder() {
        // Using actual simple implementations for stages
        // The initial input to pipeline.preprocess is List.of(rawString)
        // The SimpleTokenizer will then convert List.of("Jöhn Smîth") to List.of("Jöhn", "Smîth")
        // The LowercaseNormalizer will convert that to List.of("jöhn", "smîth")
        // The AccentRemover will convert that to List.of("john", "smith")

        Preprocessor tokenizer = new SimpleTokenizer();
        Preprocessor lowercaser = new LowercaseNormalizer();
        Preprocessor accentRemover = new AccentRemover();

        List<Preprocessor> stages = Arrays.asList(tokenizer, lowercaser, accentRemover);
        PipelinePreprocessor pipeline = new PipelinePreprocessor(stages);

        List<String> initialPipelineInput = List.of("Jöhn Smîth JR."); // Raw string wrapped in list
        List<String> expected = Arrays.asList("john", "smith", "jr.");
        List<String> actual = pipeline.preprocess(initialPipelineInput);

        assertEquals(expected, actual);
    }

    @Test
    void preprocessWithSingleStageShouldWorkAsThatStage() {
        Preprocessor lowercaser = new LowercaseNormalizer();
        PipelinePreprocessor pipeline = new PipelinePreprocessor(List.of(lowercaser));
        List<String> input = Arrays.asList("HELLO", "WORLD"); // Already tokenized
        List<String> expected = Arrays.asList("hello", "world");
        List<String> actual = pipeline.preprocess(input);
        assertEquals(expected, actual);
    }

    @Test
    void preprocessEmptyInputListShouldReturnEmptyList() {
        PipelinePreprocessor pipeline = new PipelinePreprocessor(List.of(new LowercaseNormalizer()));
        List<String> actual = pipeline.preprocess(Collections.emptyList());
        assertTrue(actual.isEmpty());
    }

    @Test
    void preprocessNullInputListShouldReturnEmptyList() {
        PipelinePreprocessor pipeline = new PipelinePreprocessor(List.of(new LowercaseNormalizer()));
        List<String> actual = pipeline.preprocess(null);
        assertTrue(actual.isEmpty());
    }

    @Test
    void constructorShouldThrowExceptionForNullStages() {
        assertThrows(IllegalArgumentException.class, () -> new PipelinePreprocessor(null));
    }

    @Test
    void constructorShouldThrowExceptionForEmptyStages() {
        assertThrows(IllegalArgumentException.class, () -> new PipelinePreprocessor(new ArrayList<>()));
    }

    @Test
    void pipelineWithNicknameNormalizer() {
        Map<String, String> nicknameMap = new HashMap<>();
        nicknameMap.put("bob", "robert");
        nicknameMap.put("liz", "elizabeth");

        // Order: Tokenize -> Nickname -> Lowercase
        List<Preprocessor> stages = Arrays.asList(
                new SimpleTokenizer(),
                new HypocorismNormalizerPreprocessor(), // Assume constructor takes map
                new LowercaseNormalizer()
        );
        PipelinePreprocessor pipeline = new PipelinePreprocessor(stages);

        List<String> initialInput = List.of("Bob O'Liz");
        List<String> expected = Arrays.asList("bert", "O'Liz"); // Nicknames replaced and then lowercased
        // O'Liz -> o'elizabeth (lowercase applied after nickname)
        List<String> actual = pipeline.preprocess(initialInput);
        assertEquals(expected, actual);
    }
}