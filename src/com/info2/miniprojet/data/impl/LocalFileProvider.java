package com.info2.miniprojet.data.impl;

import com.info2.miniprojet.data.DataProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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
        Path path = Paths.get(filePath); // Use Paths.get
        try {
            System.out.println("LocalFileProvider: Reading from file " + path.toAbsolutePath());
            List<String> lines = Files.readAllLines(path); // Assumes default charset (UTF-8 usually)
            System.out.println("LocalFileProvider: Successfully read " + lines.size() + " lines from file.");
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
}
