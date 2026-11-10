package ru.nporlov.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.nporlov.dto.CreateArmorRequest;
import ru.nporlov.exception.MalformedJsonException;
import ru.nporlov.exception.NotFoundException;
import ru.nporlov.exception.ValidationException;
import ru.nporlov.model.Armor;
import ru.nporlov.repository.ArmorRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

public class ArmorHandler implements HttpHandler {
    private final ArmorRepository repository;
    private final ResponseWriter writer;
    private final ObjectMapper mapper;

    private static final String CONTEXT_PATH = "/armor";

    public ArmorHandler(ArmorRepository repository, ResponseWriter writer,  ObjectMapper mapper) {
        this.repository = repository;
        this.writer = writer;
        this.mapper = mapper;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        String rest = path.substring(CONTEXT_PATH.length());

        boolean isCollection = rest.isEmpty() || rest.equals("/");
        boolean isSingle = rest.startsWith("/") && rest.length() > 1;

        switch (method) {
            case "GET" -> {
                if (isCollection) handleGetList(exchange);
                else if (isSingle) handleGetById(exchange, rest.substring(1));
                else sendNotFound(exchange, path);
            }
            case "POST" -> {
                if (isCollection) handlePost(exchange);
                else sendMethodNotAllowed(exchange, method);
            }
            case "PUT" -> {
                if (isSingle) handlePut(exchange, rest.substring(1));
                else sendMethodNotAllowed(exchange, method);
            }
            case "DELETE" -> {
                if (isSingle) handleDelete(exchange, rest.substring(1));
                else sendMethodNotAllowed(exchange, method);
            }
            default -> sendMethodNotAllowed(exchange, method);
        }

    }

    private void sendMethodNotAllowed (HttpExchange exchange, String method) {
        writer.sendError(exchange, 405, "METHOD_NOT_SUPPORTED",
                "Method" + method + " not supported");
    }

    private void sendNotFound (HttpExchange exchange, String path) {
        writer.sendError(exchange, 404, "NOT_FOUND", "Invalid path: " + path);
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        CreateArmorRequest request = readBody(exchange, CreateArmorRequest.class);
        validate(request);

        Armor saved = repository.save(toArmor(request));
        writer.sendJson(exchange, 201, saved, Map.of("Location", "/armor/" + saved.getId()));
    }

    private <T> T readBody(HttpExchange exchange, Class<T> type) throws IOException{
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        if (bytes.length == 0) {
            throw new ValidationException("Request body must not be empty");
        }
        try {
            return mapper.readValue(bytes, type);
        } catch (JacksonException e) {
            throw new MalformedJsonException("Malformed JSON: " + e.getLocalizedMessage(), e);
        }
    }

    private void validate(CreateArmorRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (request.getType() == null) {
            errors.put("type", "must not be null");
        }
        if (request.isShield() == null) {
            errors.put("is-shield", "must not be null");
        }
        if (request.getPenalties() == null) {
            errors.put("penalties", "must not be null");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Validation error", errors);
        }
    }

    private Armor toArmor(CreateArmorRequest request) {
        return new Armor(request.getType(), request.isShield(), request.getPenalties());
    }

    private void handleGetList(HttpExchange exchange) {
        writer.sendJson(exchange, 200, repository.findAll());
    }

    private void handleGetById(HttpExchange exchange, String rest) {
        String idPart = rest.substring(1);
        int id = parseId(idPart);

        Armor foundArmor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Armor not found: " + id));

        writer.sendJson(exchange, 200, foundArmor);
    }

    private int parseId(String idPart) {
        try {
            return Integer.parseInt(idPart);
        } catch (NumberFormatException nfe) {
            throw new ValidationException("Invalid id: " + idPart);
        }
    }

    private void handlePut (HttpExchange exchange, String idPart) throws IOException{
        int id = parseId(idPart);
        CreateArmorRequest request = readBody(exchange, CreateArmorRequest.class);
        validate(request);

        if(!repository.existsById(id)) {
            throw new NotFoundException("Armor not found: " + id);
        }

        Armor armor = toArmor(request);
        armor.setId(id);
        Armor updated = repository.save(armor);
        writer.sendJson(exchange, 200, updated);
    }

    private void handleDelete (HttpExchange exchange, String idPart) {
        int id = parseId(idPart);

        if(!repository.existsById(id)) {
            throw new NotFoundException("Armor not found: " + id);
        }

        repository.deleteById(id);
        writer.sendNoContent(exchange);
    }
}
