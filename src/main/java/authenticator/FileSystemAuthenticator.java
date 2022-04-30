package authenticator;

import dto.CommandLineArgsDto;
import dto.Credentials;
import enums.Colors;
import enums.HttpMethod;
import lombok.RequiredArgsConstructor;
import utils.HttpUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class FileSystemAuthenticator implements Authenticator {
    private final CommandLineArgsDto dto;
    private final ExecutorService executorService;
    private final int batchSize;
    private final Colors defaultColor = Colors.GREEN;
    private final HttpClient httpClient;
    private final Collection<Credentials> credentialsThatFitTimeout = new CopyOnWriteArrayList<>();
    private final AtomicLong completedTasksAmount = new AtomicLong(0);
    private final ScheduledExecutorService progressTrackingService = new ScheduledThreadPoolExecutor(1);

    public FileSystemAuthenticator(CommandLineArgsDto dto) {
        this.dto = dto;
        this.executorService = Executors.newFixedThreadPool(dto.getThreadCount());
        this.batchSize = dto.getThreadCount();
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofMillis(2L * dto.getMillis()))
                .build();
    }

    @RequiredArgsConstructor
    private class CredentialsCheckTask implements Runnable {

        private final Credentials credentials;

        @Override
        public void run() {
            HttpRequest.Builder requestBuilder = HttpRequest
                    .newBuilder()
                    .uri(URI.create(dto.getUrl().toString()))
                    .header("Content-Type", "application/json")
                    .setHeader("User-Agent", "Bot");

            Map<String, String> formData = new HashMap<>();
            formData.put(dto.getUsernameFormParameter(), credentials.getUsername());
            formData.put(dto.getPasswordFormParameter(), credentials.getPassword());

            HttpRequest request = Objects.equals(dto.getHttpMethod(), HttpMethod.GET)
                    ? requestBuilder.GET().build()
                    : requestBuilder.POST(HttpUtils.jsonPublisher(formData)).build();

            long millisBefore = System.currentTimeMillis();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                long millisAfter = System.currentTimeMillis();

                long delta = millisAfter - millisBefore;

                boolean isTimeoutConditionMet = dto.getTimeOutOption()
                        .isTimeoutConditionMet(dto.getMillis(), delta);
                Colors color = isTimeoutConditionMet ? Colors.GREEN : Colors.RED;
                if (isTimeoutConditionMet) {
                    credentialsThatFitTimeout.add(credentials);
                }
                String res = String.format(
                        "%s\t# %d\t|%d\t|\t%s\t|\t%s\t%s\t%s",
                        color.getValue(),
                        completedTasksAmount.get() + 1,
                        response.statusCode(),
                        credentials.getUsername(),
                        credentials.getPassword(),
                        isTimeoutConditionMet ? "Success" : "Failure",
                        defaultColor.getValue()
                );
                System.out.println(res);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            } finally {
                completedTasksAmount.incrementAndGet();
            }

        }
    }

    private void process(String username, Collection<? extends String> passwords) {
        passwords
                .forEach(p -> {
                    Credentials c = new Credentials(username, p);
                    executorService.submit(new CredentialsCheckTask(c));
                });
    }

    @Override
    public CompletableFuture<Collection<Credentials>> authenticate() {
        Throwable thrown = null;
        final CompletableFuture<Collection<Credentials>> out = new CompletableFuture<>();
        long nameCount = 0;
        long passwordCount = 0;
        try (BufferedReader usernameReader = Files.newBufferedReader(dto.getUsersFile())) {
            for (String username; (username = usernameReader.readLine()) != null; ) {
                nameCount++;
                List<String> passwords = new ArrayList<>(batchSize);
                try (BufferedReader passwordReader = Files.newBufferedReader(dto.getPasswordsFile())) {
                    for (String password; (password = passwordReader.readLine()) != null; ) {
                        if (nameCount == 1) {
                            passwordCount++;
                        }
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
            }
        } catch (Throwable e) {
            thrown = e;
            throw new RuntimeException(e);
        } finally {
            executorService.shutdown();
            if (thrown == null) {
                final long taskCount = nameCount * passwordCount;
                int delay = 10;
                int rate = 10;
                progressTrackingService.scheduleAtFixedRate(
                        () -> {
                            if (taskCount == completedTasksAmount.get()) {
                                out.complete(this.credentialsThatFitTimeout);
                                progressTrackingService.shutdownNow();
                            }
                        }, delay, rate, TimeUnit.SECONDS
                );
            }
        }
        return out;
    }
}
