package net.sghill.jenkins.toolkit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class FetchFailed {
    private final OkHttpClient client;
    private final Path outputDir;

    private final String base;
    private final Path script;

    public static void main(String[] args) {
        String base = System.getProperty("url", "http://localhost:8080");
        Path out = Paths.get(System.getProperty("outDir", "out"));
        Path groovyScript = Paths.get(System.getProperty("script"));
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(1, TimeUnit.MINUTES)
                .callTimeout(2, TimeUnit.MINUTES)
                .readTimeout(5, TimeUnit.MINUTES)
                .build();
        new FetchFailed(okHttpClient, out, base, groovyScript).run(args);
    }

    private String getCrumb(OkHttpClient client, String credential) {
        HttpUrl crumbUrl = base.endsWith("/") ? HttpUrl.get(base + "crumbIssuer/api/json") : HttpUrl.get(base + "/crumbIssuer/api/json");
        Request request = new Request.Builder().get().url(crumbUrl)
                .header("Authorization", credential).build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                log.error("{}: Unexpected status {}", request.url(), response);
                throw new IllegalStateException("Unexpected status " + response.code());
            }
            JsonNode responseNode = new ObjectMapper().readValue(response.body().string(), JsonNode.class);
            return responseNode.get("crumb").asText();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void run(String[] args) {
        try {
            Files.createDirectories(outputDir);
            String scriptText = String.join("\n", Files.readAllLines(script));

            HttpUrl scriptUrl = base.endsWith("/") ? HttpUrl.get(base + "scriptText") : HttpUrl.get(base + "/scriptText");
            Request.Builder requestBuilder = new Request.Builder()
                    .post(new FormBody.Builder()
                            .add("script", scriptText)
                            .build())
                    .url(scriptUrl);

            String username = System.getProperty("username");
            String password = System.getProperty("password");
            String basicCredential = null;
            String crumb = null;
            if (!StringUtils.isEmpty(username) && !StringUtils.isEmpty(password)) {
                basicCredential = Credentials.basic(username, password);
                crumb = getCrumb(client, basicCredential);
                requestBuilder = requestBuilder.header("Authorization", basicCredential)
                        .header("Jenkins-Crumb", crumb);
            }

            Call call = client.newCall(requestBuilder
                    .build());
            List<Failed> failed = new LinkedList<>();
            try (Response response = call.execute()) {
                ResponseBody body = response.body();
                assert body != null;
                Scanner scanner = new Scanner(body.byteStream());
                while (scanner.hasNextLine()) {
                    String[] parts = scanner.nextLine().split("\t");
                    String jobName = parts[0];
                    String buildNumber = parts[1];
                    String consoleTextUrl = parts[2];
                    failed.add(new Failed(jobName, buildNumber, consoleTextUrl));
                }
            }
            log.info("Queuing up fetches for {} failed build(s) to store in {}", failed.size(), outputDir);
            CountDownLatch latch = new CountDownLatch(failed.size());
            for (Failed f : failed) {
                Request.Builder consoleRequestBuilder = new Request.Builder()
                        .get()
                        .url(f.getConsoleTextUrl());
                if (basicCredential != null) {
                    consoleRequestBuilder = consoleRequestBuilder.header("Authorization", basicCredential)
                            .header("Jenkins-Crumb", crumb);
                }
                Request r = consoleRequestBuilder
                        .build();
                client.newCall(r).enqueue(new Callback() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        log.warn("Failed to call {}", call.request().url(), e);
                        latch.countDown();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                        try (ResponseBody body = response.body()) {
                            if (!response.isSuccessful()) {
                                log.warn("Received {} from {}", response.code(), call.request().url());
                            } else if (body == null) {
                                log.warn("Received null body from {}", call.request().url());
                            } else {
                                Files.copy(body.byteStream(), outputDir.resolve(String.join(".", f.getJobName(), f.getBuildNumber(), "txt")), StandardCopyOption.REPLACE_EXISTING);
                            }
                        } finally {
                            latch.countDown();
                        }
                    }
                });
            }
            boolean completed = latch.await(10, TimeUnit.MINUTES);
            if (completed) {
                log.info("Fetching output complete");
            } else {
                log.warn("Timeout expired while fetching output");
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Data
    private static class Failed {
        private final String jobName;
        private final String buildNumber;
        private final String consoleTextUrl;
    }
}
