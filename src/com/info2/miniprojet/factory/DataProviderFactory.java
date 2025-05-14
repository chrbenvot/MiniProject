package com.info2.miniprojet.factory;

import com.info2.miniprojet.data.DataProvider;
import com.info2.miniprojet.data.impl.CliInputProvider;
import com.info2.miniprojet.data.impl.LocalFileProvider;
import com.info2.miniprojet.data.impl.UrlDataProvider;
import java.util.Scanner;

//NB: why do we need this? well, we need to create a data provider based on the user input and then give a way for the main
// to be able to get the url/file path to use it as a key for the caching hashmap.
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