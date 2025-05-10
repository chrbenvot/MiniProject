package com.info2.miniprojet.preprocessing.impl;

import com.info2.miniprojet.preprocessing.Preprocessor;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HypocorismNormalizerV2Preprocessor implements Preprocessor {

    private static final Logger LOGGER = Logger.getLogger(HypocorismNormalizerV2Preprocessor.class.getName());
    private final Map<String, String> nicknameToCanonicalMap;
    private final String mapFilePath; // Store the path for reference/debugging

    /**
     * Constructor that loads the nickname-to-canonical map from a CSV file.
     * The CSV file is expected to have two columns:
     * Column 1: Nickname
     * Column 2: Canonical Name
     * The path to the CSV file is hardcoded for this version.
     */
    public HypocorismNormalizerV2Preprocessor() {
        // HARDCODED PATH - Change this to your actual CSV file path
        this.mapFilePath = "corrected_nicknames.csv"; // Example: place in project root or provide full path
        this.nicknameToCanonicalMap = loadNicknameMapFromFile(this.mapFilePath);
        System.out.println("DEBUG: HypocorismNormalizerV2 loaded " + this.nicknameToCanonicalMap.size() + " nickname mappings from " + this.mapFilePath);
    }

    private Map<String, String> loadNicknameMapFromFile(String filePath) {
        Map<String, String> map = new HashMap<>();
        // Try loading as a resource first, then as an absolute/relative file path
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(filePath);

        if (inputStream == null) {
            // If not found as resource, try as a file path
            try {
                inputStream = new FileInputStream(filePath);
                System.out.println("DEBUG: Loading nickname map from file system path: " + filePath);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Nickname CSV file not found either as resource or file path: " + filePath, e);
                return map; // Return empty map if file not found
            }
        } else {
            System.out.println("DEBUG: Loading nickname map as resource: " + filePath);
        }


        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty() || line.startsWith("#")) { // Skip empty lines or comments
                    continue;
                }

                // Expecting two columns: Nickname,CanonicalName
                String[] parts = line.split(",", 2); // Split on the first comma only

                if (parts.length == 2) {
                    String nickname = parts[0].trim().toLowerCase();
                    String canonicalName = parts[1].trim().toLowerCase(); // Store canonical also in lowercase for consistency

                    if (!nickname.isEmpty() && !canonicalName.isEmpty()) {
                        // If nickname already exists, the first one encountered wins (or log a warning)
                        if (!map.containsKey(nickname)) {
                            map.put(nickname, canonicalName);
                        } else {
                            // Optional: Log if a nickname is being overwritten or found multiple times
                            // LOGGER.warning("Duplicate nickname '" + nickname + "' found at line " + lineNumber +
                            //                ". Keeping first mapping to '" + map.get(nickname) + "'.");
                        }
                    } else {
                        LOGGER.warning("Skipping malformed line (empty nickname or canonical) at "
                                + this.mapFilePath + ":" + lineNumber + " -> " + line);
                    }
                } else {
                    LOGGER.warning("Skipping malformed line (expected 2 columns) at "
                            + this.mapFilePath + ":" + lineNumber + " -> " + line);
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading hypocorism file: " + filePath, e);
        }
        return map;
    }

    @Override
    public List<String> preprocess(List<String> inputTokens) {
        if (inputTokens == null) {
            return new ArrayList<>();
        }
        if (nicknameToCanonicalMap.isEmpty()) {
            // If map is empty (e.g., file not found), just return tokens as is
            return new ArrayList<>(inputTokens);
        }

        List<String> outputTokens = new ArrayList<>(inputTokens.size());
        for (String token : inputTokens) {
            if (token == null) {
                outputTokens.add(null);
                continue;
            }
            // Lookup the lowercase version of the token in the map
            String canonicalName = nicknameToCanonicalMap.get(token.toLowerCase());
            if (canonicalName != null) {
                // If a canonical form exists, use it (it's already lowercase from the loader)
                outputTokens.add(canonicalName);
            } else {
                // If not a known nickname, keep the original token
                outputTokens.add(token);
            }
        }
        return outputTokens;
    }

    @Override
    public String getName() {
        return "NICKNAME_NORMALIZER_V2"; // Distinguish from any previous version
    }

}