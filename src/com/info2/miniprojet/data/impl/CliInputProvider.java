package com.info2.miniprojet.data.impl;


import com.info2.miniprojet.data.DataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class CliInputProvider implements DataProvider {

    private final Scanner scanner;
    private static final String DONE_KEYWORD = "DONE"; // Case-insensitive check below

    /**
     * Creates a provider that reads input from the specified Scanner.
     * @param scanner The Scanner connected to the input source (e.g., System.in).
     */
    public CliInputProvider(Scanner scanner) {
        if (scanner == null) {
            throw new IllegalArgumentException("Scanner cannot be null for CliInputProvider.");
        }
        this.scanner = scanner;
    }

    @Override
    public List<String> loadRawLines() throws InterruptedException {
        List<String> lines = new ArrayList<>();
        System.out.println("\n--- Manual Name Input ---");
        System.out.println("Enter one name per line. Type '" + DONE_KEYWORD + "' (case-insensitive) when finished.");
        System.out.println("Entries must be in the following format: id,name");
        System.out.println("Example: 1,John");
        System.out.println("IF you don't want to enter an id,an ID will be made automatically in the format 'L_lineNumber");
        System.out.println("Example: John as input will result in 'L_1' as the ID");

        int count = 1;
        while (true) {
            System.out.print("Name " + count + " (or DONE): ");
            String line = scanner.nextLine().trim(); // Read the whole line

            if (line.equalsIgnoreCase(DONE_KEYWORD)) {
                break; // Exit loop if user types DONE
            }

            if (!line.isEmpty()) { // Add non-empty lines
                lines.add(line);
                count++;
            } else {
                System.out.println("(Skipping empty input)");
                // Optionally re-prompt without incrementing count if you want exactly N non-empty names
            }
            // Check for potential interruption if running in a context where it matters
            if (Thread.interrupted()){ //through ctrl c for example
                System.out.println("Manual input interrupted.");
                throw new InterruptedException("Manual input was interrupted.");
            }
        }

        System.out.println("--- Finished Manual Input (" + lines.size() + " names entered) ---");
        return lines;
    }
    @Override
    public String toString() {
        return "CliInputProvider";
    }
}