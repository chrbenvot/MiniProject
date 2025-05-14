package com.info2.miniprojet.data.impl;

import com.info2.miniprojet.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class LocalFileProvider implements DataProvider {
    private final String filePath;
    public LocalFileProvider(String filePath) {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty for LocalFileProvider.");
        }
        this.filePath = filePath;
    }
    @Override
    public List<String> loadRawLines() throws IOException {
        Path path = Paths.get(filePath);
        try {
            System.out.println("LocalFileProvider: Reading from file " + path.toAbsolutePath());
            List<String> lines = Files.readAllLines(path);
            System.out.println("LocalFileProvider: Successfully read " + lines.size() + " lines from file.");
            if (!lines.isEmpty()) {
                System.out.println("LocalFileProvider: Skipping header line: '" + lines.get(0) + "'");
                // Return a sublist that excludes the first line (the header)
                return lines.stream().skip(1).collect(Collectors.toList());
            }
            return lines;
        } catch (NoSuchFileException e) {
            System.err.println("Error: File not found at " + path.toAbsolutePath());
            throw e; // Re-throw specific exception
        } catch (IOException e) {
            System.err.println("Error reading file " + path.toAbsolutePath() + ": " + e.getMessage());
            throw e; // Re-throw general IO exception
        }
    }
    @Override
    public String toString() {
        return "LocalFileProvider(" + filePath + ")";
    }
    public String getFilePath() {
        return filePath;
    }
}
