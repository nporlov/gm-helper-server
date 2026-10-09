package ru.nporlov.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import ru.nporlov.exception.ConflictException;
import ru.nporlov.exception.NotFoundException;
import ru.nporlov.exception.ValidationException;

import java.io.IOException;

public class ErrorHandler implements HttpHandler {

    private final HttpHandler delegate;
    private final ResponseWriter writer;

    public ErrorHandler(HttpHandler delegate, ResponseWriter writer) {
        this.delegate = delegate;
        this.writer = writer;
    }

    @Override
    public void handle(HttpExchange exchange) {
        try {
            delegate.handle(exchange);
        } catch (NotFoundException nfe) {
            writer.sendError(exchange, 404, "NOT_FOUND", nfe.getMessage());
        } catch (ValidationException ve) {
            writer.sendError(exchange, 400, "VALIDATION_ERROR", ve.getMessage(), ve.getDetails());
        } catch (ConflictException ce) {
            writer.sendError(exchange, 409,  "CONFLICT", ce.getMessage());
        } catch (IOException uioe) {
            System.err.println("IO error: " + uioe.getMessage());
            uioe.printStackTrace();
            writer.sendError(exchange, 500, "INTERNAL_ERROR", "Internal server error");
        } catch (RuntimeException re) {
            System.err.println("Unexpected error: " + re.getMessage());
            re.printStackTrace();
            writer.sendError(exchange, 500, "INTERNAL_ERROR", "Internal server error");
        }
    }
}
