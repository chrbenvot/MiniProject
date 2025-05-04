package com.info2.miniprojet.data.impl;

import com.info2.miniprojet.data.DataProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class UrlDataProvider implements DataProvider {
    private final String urlString;
    private static final Duration TIMEOUT_DURATION = Duration.ofSeconds(15);

    public UrlDataProvider(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new IllegalArgumentException("URL string cannot be null or empty for UrlDataProvider.");
        }
        if (!urlString.toLowerCase().startsWith("http://") && !urlString.toLowerCase().startsWith("https://")) {
            throw new IllegalArgumentException("Invalid URL scheme, must start with http:// or https://");
        }
        this.urlString = urlString.trim();
    }
    @Override
    public List<String> loadRawLines() throws IOException, InterruptedException {
        System.out.println("UrlDataProvider: Attempting to load from URL: " + urlString);
        List<String> lines = new ArrayList<>();
        HttpClient client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(TIMEOUT_DURATION)
                .build();
        HttpRequest request=null;
        try {
            URI uri=new URI(urlString);
            request = HttpRequest.newBuilder()
                    .uri(uri)
                    .timeout(TIMEOUT_DURATION)
                    .GET()
                    .build();

            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            int statusCode = response.statusCode();
            System.out.println("UrlDataProvider: Response status code: " + statusCode);

            if (statusCode >= 200 && statusCode < 300) { // Success codes
                try (InputStream inputStream = response.body();
                     Scanner scanner = new Scanner(inputStream).useDelimiter("\\R")) { // \\R handles various line endings
                    while (scanner.hasNext()) {
                        lines.add(scanner.next());
                    }
                }
                System.out.println("UrlDataProvider: Successfully read " + lines.size() + " lines from URL.");
                return lines;
            } else {
                // Handle common error codes specifically if desired
                throw new IOException("Failed to fetch from URL '" + urlString + "': Status code " + statusCode);
            }
        } catch (HttpTimeoutException | SocketTimeoutException e) {
            System.err.println("Error: Timeout connecting to or reading from URL: " + urlString);
            throw new IOException("Timeout loading data from URL: " + urlString, e);
        } catch (ConnectException e) {
            System.err.println("Error: Could not connect to URL: " + urlString);
            throw new IOException("Connection error loading data from URL: " + urlString, e);
        }
        catch (URISyntaxException e) {
            System.err.println("Error: Invalid URL: " + urlString);
            throw new IOException("Invalid URL: " + urlString, e);
        }

    }
}
