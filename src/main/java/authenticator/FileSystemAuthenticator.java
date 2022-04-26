package authenticator;

import dto.Credentials;
import enums.Colors;
import enums.HttpMethod;
import lombok.RequiredArgsConstructor;
import utils.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class FileSystemAuthenticator implements Authenticator {

    private final Path usernameListPath;

    private final Path passwordListPath;

    private final URL url;

    private final HttpMethod httpMethod;

    private final String usernameFormParameter;

    private final String passwordFormParameter;

    private final int millis;

    private final ExecutorService executorService = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);
    private final int batchSize = Runtime.getRuntime().availableProcessors() + 1;

    private final Colors defaultColor = Colors.GREEN;

    @RequiredArgsConstructor
    private class CredentialsCheckTask implements Runnable {

        private final Credentials credentials;

        @Override
        public void run() {
            HttpClient httpClient = HttpClient.newBuilder()
                    .version(HttpClient.Version.HTTP_1_1)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(20))
                    .build();

            HttpRequest.Builder requestBuilder = HttpRequest
                    .newBuilder()
                    .uri(URI.create(url.toString()))
                    .setHeader("User-Agent", "Bot");

            Map<String, String> formData = new HashMap<>();
            formData.put(usernameFormParameter, credentials.getUsername());
            formData.put(passwordFormParameter, credentials.getPassword());

            HttpRequest request = Objects.equals(httpMethod, HttpMethod.GET)
                    ? requestBuilder.GET().build()
                    : requestBuilder.POST(HttpUtils.ofFormData(formData)).build();

            long millisBefore = System.currentTimeMillis();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                long millisAfter = System.currentTimeMillis();

                long delta = millisAfter - millisBefore;

                Colors color = delta < millis ? Colors.RED : Colors.GREEN;
                String res = String.format(
                        "%s\t%d\t|\t%s\t|\t%s\t%s",
                        color.getValue(),
                        response.statusCode(),
                        credentials.getUsername(),
                        credentials.getPassword(),
                        defaultColor.getValue()
                );
                System.out.println(res);
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    private Collection<CompletableFuture<Void>> process(String username, Collection<? extends String> passwords) {
        return passwords
                .stream()
                .map(p -> {
                    Credentials c = new Credentials(username, p);
                    return CompletableFuture.runAsync(new CredentialsCheckTask(c), executorService);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void authenticate() {
        try (BufferedReader usernameReader = Files.newBufferedReader(usernameListPath);
             BufferedReader passwordReader = Files.newBufferedReader(passwordListPath)) {
            for (String username; (username = usernameReader.readLine()) != null; ) {
                List<String> passwords = new ArrayList<>(batchSize);
                for (String password; (password = passwordReader.readLine()) != null; ) {
                    passwords.add(password);
                    if (passwords.size() == batchSize) {
                        process(username, passwords);
                        passwords = new ArrayList<>(batchSize);
                    }
                }
                if (!passwords.isEmpty()) {
                    process(username, passwords);
                }
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
