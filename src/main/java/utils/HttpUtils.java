package utils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils {

    private HttpUtils(){}

    public static HttpRequest.BodyPublisher formDataPublisher(Map<?, ?> data) {
        var builder = new StringBuilder();
        for (Map.Entry<?, ?> entry : data.entrySet()) {
            if (builder.length() > 0) {
                builder.append("&");
            }
            builder.append(URLEncoder.encode(entry.getKey().toString(), StandardCharsets.UTF_8));
            builder.append("=");
            builder.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
        }
        return HttpRequest.BodyPublishers.ofString(builder.toString());
    }

    public static HttpRequest.BodyPublisher jsonPublisher(Map<?, ?> data) {
        try {
            String json = new ObjectMapper().writeValueAsString(data);
            return HttpRequest.BodyPublishers.ofString(json);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
