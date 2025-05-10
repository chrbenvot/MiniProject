package com.info2.miniprojet.factory;

import com.info2.miniprojet.data.DataProvider;
import com.info2.miniprojet.data.impl.CliInputProvider; // Assuming this class exists
import com.info2.miniprojet.data.impl.LocalFileProvider; // Assuming this class exists
import com.info2.miniprojet.data.impl.UrlDataProvider;   // Assuming this class exists
import java.util.Scanner;

public class DataProviderFactory {
    public static DataProvider createDataProvider(String identifier, Scanner cliScanner) {
        if (identifier == null || identifier.trim().isEmpty()) {
            System.err.println("Error: Data source identifier cannot be empty.");
            return null;
        }
        String trimmedIdentifier = identifier.trim();
        if (trimmedIdentifier.equalsIgnoreCase("MANUAL")) {
            return new CliInputProvider(cliScanner); // Pass scanner for CLI input
        } else if (trimmedIdentifier.toLowerCase().startsWith("http://") || trimmedIdentifier.toLowerCase().startsWith("https://")) {
            return new UrlDataProvider(trimmedIdentifier);
        } else {
            // Assume it's a file path
            return new LocalFileProvider(trimmedIdentifier);
        }
    }
}