package ru.nporlov.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.nporlov.exception.NotFoundException;
import ru.nporlov.exception.ValidationException;
import ru.nporlov.model.Armor;
import ru.nporlov.repository.ArmorRepository;

public class ArmorHandler implements HttpHandler {
    private final ArmorRepository repository;
    private final ResponseWriter writer;

    private static final String CONTEXT_PATH = "/armor";

    public ArmorHandler(ArmorRepository repository, ResponseWriter writer) {
        this.repository = repository;
        this.writer = writer;
    }

    @Override
    public void handle(HttpExchange exchange) {
        String method = exchange.getRequestMethod();
        if (!"GET".equals(method)) {
            writer.sendError(exchange, 405, "METHOD_NOT_SUPPORTED",
                    "Method" + method + " not supported");
            return;
        }

        String path = exchange.getRequestURI().getPath();
        String rest = path.substring(CONTEXT_PATH.length());

        if (rest.isEmpty() || rest.equals("/")) {
            handleList(exchange);
            return;
        }

        if (rest.startsWith("/")) {
            handleById(exchange, rest);
            return;
        }

        writer.sendError(exchange, 404, "NOT_FOUND", "Unknown path: " + path);
    }

    private void handleList(HttpExchange exchange) {
        writer.sendJson(exchange, 200, repository.findAll());
    }

    private void handleById(HttpExchange exchange, String rest) {
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
