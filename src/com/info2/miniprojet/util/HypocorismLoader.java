package com.info2.miniprojet.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


// Source for nicknames file:https://github.com/carltonnorthern/nicknames (Apache 2.0 License)
public class HypocorismLoader {

    // Logger for potential errors (optional, can use System.err.println)
    private static final Logger LOGGER = Logger.getLogger(HypocorismLoader.class.getName());

    /**
     * Loads hypocorisms from a CSV file into a Map where the key is the nickname
     * and the value is the canonical name.
     * Assumes the first column in the CSV is the canonical name, and subsequent
     * columns on the same line are nicknames for that canonical name.
     * If a nickname is associated with multiple canonical names across different lines
     * (or even on the same line if the canonical was also a nickname - though less likely),
     * this implementation will only store the *first* canonical name it encounters for that nickname.
     *
     * @param csvFilePath Path to the CSV file.
     * @return A Map of Nickname (String) to CanonicalName (String).
     */
    public static Map<String, String> loadNicknameToCanonicalMap(String csvFilePath) {
        Map<String, String> nicknameToCanonicalMap = new HashMap<>();

        // Using try-with-resources for automatic closing of BufferedReader
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(csvFilePath))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    continue;
                }

                // StringTokenizer is older; using String.split is often preferred for simplicity,
                // but StringTokenizer handles empty trailing tokens differently. Let's stick to your original.
                // For robustness with CSVs containing commas within quoted fields, a proper CSV parser
                // library would be better, but for simple comma-separated values, this is okay.
                StringTokenizer st = new StringTokenizer(line, ",");

                if (!st.hasMoreTokens()) {
                    LOGGER.warning("Skipping malformed line (no tokens) at: " + lineNumber + " -> " + line);
                    continue;
                }

                String canonicalName = st.nextToken().trim().toLowerCase(); // Normalize key for lookup
                if (canonicalName.isEmpty()) {
                    LOGGER.warning("Skipping line with empty canonical name at: " + lineNumber + " -> " + line);
                    continue;
                }

                while (st.hasMoreTokens()) {
                    String nickname = st.nextToken().trim().toLowerCase(); // Normalize nickname
                    if (!nickname.isEmpty()) {
                        // If this nickname hasn't been seen before, map it to the current canonical name.
                        // This implements the "take the first occurrence" rule if a nickname could
                        // theoretically map to multiple canonicals.
                        if (!nicknameToCanonicalMap.containsKey(nickname)) {
                            nicknameToCanonicalMap.put(nickname, canonicalName);
                        } else {
                            // Optional: Log if a nickname is found again with a different canonical
                            // String existingCanonical = nicknameToCanonicalMap.get(nickname);
                            // if (!existingCanonical.equals(canonicalName)) {
                            //     LOGGER.info("Nickname '" + nickname + "' already mapped to '" + existingCanonical +
                            //                   "'. Ignoring new mapping to '" + canonicalName + "' from line " + lineNumber);
                            // }
                        }
                        // Also, consider if the canonical name itself should be a "nickname" for itself
                        // to handle cases where the input might be the canonical form already.
                        // This is usually handled by checking if a name needs normalization or not.
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading hypocorism file: " + csvFilePath, e);
            // Depending on requirements, you might want to throw this exception,
            // return an empty map, or return a partially filled map.
            // For robustness, returning an empty map or throwing might be better.
            return new HashMap<>(); // Return empty on error
        }

        return nicknameToCanonicalMap;
    }

    public static void main(String[] args) {
        // Example usage:
        // Create a dummy CSV file named "nicknames.csv" in the project root:
        // Robert,Bob,Rob,Bobby
        // William,Will,Bill,Billy
        // Richard,Rich,Dick
        // Bob,Robert // Example of a nickname potentially appearing with a different canonical
        // Elizabeth,Liz,Beth,Lizzie,Betty
        // Liz,Lisa // Liz appears again, this won't override the first mapping to Elizabeth

        String filePath = "/home/chrbenvot/IdeaProjects/MiniProjet/nicknames.csv"; // hard-coded
        Map<String, String> hypocorisms = loadNicknameToCanonicalMap(filePath);

        System.out.println("Loaded Hypocorisms (Nickname -> Canonical):");
        hypocorisms.forEach((nickname, canonical) ->
                System.out.println(nickname + " -> " + canonical)
        );

        // Example test:
        String testName = "bill";
        String canonical = hypocorisms.get(testName.toLowerCase()); // Normalize testName for lookup
        if (canonical != null) {
            System.out.println("\n'" + testName + "' normalizes to '" + canonical + "'");
        } else {
            System.out.println("\n'" + testName + "' not found in nickname map.");
        }
    }
}