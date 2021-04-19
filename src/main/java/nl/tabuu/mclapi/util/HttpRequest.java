package nl.tabuu.mclapi.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRequest {

    private static ExecutorService SERVICE = Executors.newFixedThreadPool(5);

    public static String getParameterizedUri(String uri, Map<String, String> parameters) {
        StringBuilder result = new StringBuilder(uri);

        int index = 0;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            result.append(index > 0 ? "&" : "?");

            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));

            index++;
        }

        return result.toString();
    }

    public static CompletableFuture<Integer> doPostRequest(String uri, Map<String, String> headers, JsonObject body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(uri);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if(Objects.nonNull(headers))
                    headers.forEach(connection::setRequestProperty);

                connection.setRequestMethod("POST");

                if(Objects.nonNull(body)) {
                    connection.setDoOutput(true);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                    outputStream.writeBytes(body.toString());
                    outputStream.flush();
                    outputStream.close();
                }

                return connection.getResponseCode();
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }, SERVICE);
    }

    public static CompletableFuture<JsonObject> doJsonBodyRequest(String uri, String method) {
        return doJsonBodyRequest(uri, method, null, null);
    }

    public static CompletableFuture<JsonObject> doJsonBodyRequest(String uri, String method, Map<String, String> headers, JsonObject body) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = new URL(uri);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if(Objects.nonNull(headers))
                    headers.forEach(connection::setRequestProperty);

                connection.setRequestMethod(method);

                if(Objects.nonNull(body)) {
                    connection.setDoOutput(true);
                    DataOutputStream outputStream = new DataOutputStream(connection.getOutputStream());

                    outputStream.writeBytes(body.toString());
                    outputStream.flush();
                    outputStream.close();
                }

                int status = connection.getResponseCode();
                boolean error = status > 299; // TODO: Do some sort of error handling.

                BufferedReader inputReader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8)
                );

                String line;
                StringBuilder content = new StringBuilder();
                while ((line = inputReader.readLine()) != null)
                    content.append(line);

                inputReader.close();
                connection.disconnect();

                return (JsonObject) JsonParser.parseString(content.toString());
            } catch (IOException exception) {
                throw new IllegalStateException(exception);
            }
        }, SERVICE);
    }
}