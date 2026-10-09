package ru.nporlov.http;

import com.sun.net.httpserver.HttpExchange;
import ru.nporlov.dto.ApiError;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.Map;

public class ResponseWriter {
    private final ObjectMapper mapper;

    public ResponseWriter(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    void sendJson (HttpExchange exchange, int status, Object body) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(body);
            exchange.getResponseHeaders().set("Content-Type",  "application/json; charset=utf-8");
            exchange.sendResponseHeaders(status, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        finally {
            exchange.close();
        }
    }

    public void sendError (HttpExchange exchange, int status, String code,
                           String message, Map<String, String> details) {
        ApiError error = new ApiError(status, code, message, Instant.now(), details);
        sendJson(exchange, status, error);
    }

    public void sendError (HttpExchange exchange, int status, String code, String message) {
        sendError(exchange, status, code, message, null);
    }
}
