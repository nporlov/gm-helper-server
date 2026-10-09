package ru.nporlov.config;

import com.sun.net.httpserver.HttpServer;
import ru.nporlov.http.ArmorHandler;
import ru.nporlov.http.ResponseWriter;
import ru.nporlov.repository.ArmorRepository;
import ru.nporlov.repository.json.JsonArmorRepository;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class DependencyContainer {
    private final ObjectMapper mapper;
    private final ArmorRepository armorRepository;
    private final ResponseWriter responseWriter;
    private final ArmorHandler armorHandler;
    private final HttpServer server;

    public DependencyContainer(ServerConfig config) throws  IOException {
        this.mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .enable(SerializationFeature.INDENT_OUTPUT)
                .build();

        this.armorRepository = new JsonArmorRepository(
                config.getDataDir().resolve("armor"), mapper
        );

        this.responseWriter = new ResponseWriter(mapper);
        this.armorHandler = new ArmorHandler(armorRepository,  responseWriter);

        this.server = buildServer(config);
    }

    private HttpServer buildServer(ServerConfig config) throws IOException {
        HttpServer server = HttpServer.create(
                new InetSocketAddress(config.getHost(), config.getPort()), 0);
        server.createContext("/armor", armorHandler);
        server.setExecutor(Executors.newFixedThreadPool(config.getPoolSize()));
        return server;
    }

    public HttpServer getServer() {
        return server;
    }
}
