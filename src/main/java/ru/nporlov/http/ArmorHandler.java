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

        switch (method) {
            case "GET" -> handleGet(exchange, path);
            case "POST" -> handlePost(exchange);
            default -> writer.sendError(exchange, 405, "METHOD_NOT_SUPPORTED",
                        "Method" + method + " not supported");
        }

    }

    private void handlePost(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        CreateArmorRequest request = mapper.readValue(body, CreateArmorRequest.class);
        validate(request);
        Armor toSave = toArmor(request);
        Armor saved = repository.save(toSave);
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

    private void handleGet(HttpExchange exchange, String path) {
        String rest = path.substring(CONTEXT_PATH.length());

        if (rest.isEmpty() || rest.equals("/")) {
            handleGetList(exchange);
            return;
        }

        if (rest.startsWith("/")) {
            handleGetById(exchange, rest);
            return;
        }
        writer.sendError(exchange, 404, "NOT_FOUND", "Unknown path: " + path);
    }

    private void handleGetList(HttpExchange exchange) {
        writer.sendJson(exchange, 200, repository.findAll());
    }

    private void handleGetById(HttpExchange exchange, String rest) {
        String idPart = rest.substring(1);
        int id;
        try {
            id = Integer.parseInt(idPart);
        }
        catch (NumberFormatException e) {
            throw new ValidationException("Invalid id: " + idPart);
        }

        Armor foundArmor = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Armor not found: " + id));

        writer.sendJson(exchange, 200, foundArmor);
    }

}
