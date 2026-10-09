package ru.nporlov.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.nporlov.model.Armor;
import ru.nporlov.repository.ArmorRepository;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ArmorHandler implements HttpHandler {
    private final ArmorRepository repository;
    private final ResponseWriter writer;

    private static final String CONTEXT_PATH = "/armor";

    public ArmorHandler(ArmorRepository repository, ResponseWriter writer) {
        this.repository = repository;
        this.writer = writer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            if (!"GET".equals(method)) {
                writer.sendJson(exchange, 405, Map.of("error", "Method not supported"));
                return;
            }

            String path = exchange.getRequestURI().getPath();
            String rest = path.substring(CONTEXT_PATH.length());

            if (rest.isEmpty() || rest.equals("/"))
                handleList(exchange);

            if (rest.startsWith("/"))
                handleById(exchange,  rest);

            writer.sendJson(exchange, 400, Map.of("error", "Not found"));
        }
        catch (UncheckedIOException e) {
            writer.sendJson(exchange, 500, Map.of("error", "Initial server error"));
        }
    }

    private void handleList(HttpExchange exchange) throws IOException {
        List<Armor> allArmor = repository.findAll();
        writer.sendJson(exchange, 200, allArmor);
    }

    private void handleById(HttpExchange exchange, String rest) throws IOException {
        String idPart = rest.substring(1);
        int id;
        try {
            id = Integer.parseInt(idPart);
        }
        catch (NumberFormatException e) {
            writer.sendJson(exchange, 400, Map.of("error", "Invalid ID: " + idPart));
            return;
        }

        Optional<Armor> foundArmor = repository.findById(id);
        if (foundArmor.isEmpty()) {
            writer.sendJson(exchange, 404, Map.of("error", "Armor not found: " + idPart));
            return;
        }
        writer.sendJson(exchange, 200, foundArmor.get());
    }
}
