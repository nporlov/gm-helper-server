package ru.nporlov.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.nporlov.config.ObjectMapperFactory;
import ru.nporlov.model.Armor;
import ru.nporlov.model.ArmorType;
import ru.nporlov.model.SavingThrowName;
import ru.nporlov.repository.ArmorRepository;
import ru.nporlov.repository.json.JsonArmorRepository;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArmorHandlerTest {
    @TempDir
    Path tempDir;

    HttpServer server;
    HttpClient client;
    String baseUrl;
    ArmorRepository repository;
    ObjectMapper mapper;

    Armor lightArmor = new Armor(ArmorType.LIGHT, false, List.of());
    Armor mediumArmor = new Armor(ArmorType.MEDIUM, false, List.of(SavingThrowName.MENTAL));
    Armor heavyArmor = new Armor(ArmorType.HEAVY, false, List.of(
            SavingThrowName.EVASION, SavingThrowName.MENTAL
    ));

    @BeforeEach
    void setUp() throws IOException {
        mapper = ObjectMapperFactory.create();
        repository = new JsonArmorRepository(tempDir.resolve("armor"), mapper);

        ResponseWriter writer = new ResponseWriter(mapper);
        ArmorHandler handler = new ArmorHandler(repository, writer);
        ErrorHandler wrapped = new ErrorHandler(handler, writer);

        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        server.createContext("/armor", wrapped);
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();

        int port = server.getAddress().getPort();
        baseUrl = "http://localhost:" + port;

        client = HttpClient.newHttpClient();
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    // emulate GET from client
    private HttpResponse<String> get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .GET()
                .build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void getArmor_emptyRepository_ReturnsEmptyArray() throws Exception {
        HttpResponse<String> response = get("/armor");

        assertEquals(200, response.statusCode());

        List<?> list = mapper.readValue(response.body(), List.class);
        assertTrue(list.isEmpty());
        assertTrue(response.headers().firstValue("Content-Type")
                .orElse("").startsWith("application/json")
        );
    }

    @Test
    void getArmorById_existing_returnsArmor() throws Exception {
        Armor savedArmor = repository.save(lightArmor);

        HttpResponse<String> response = get("/armor/" + savedArmor.getId());

        assertEquals(200, response.statusCode());

        Armor paredArmor = mapper.readValue(response.body(), Armor.class);
        assertEquals(savedArmor, paredArmor);
    }

    @Test
    void getArmor_twoSaved_returnsBoth() throws Exception {
        repository.save(mediumArmor);
        repository.save(heavyArmor);

        HttpResponse<String> response = get("/armor");
        assertEquals(200, response.statusCode());

        List<?> list = mapper.readValue(response.body(), List.class);
        assertEquals(2, list.size());
    }

    @Test
    void getArmorById_notExisting_returns404 () throws Exception {
        HttpResponse<String> response = get("/armor/999");
        assertEquals(404, response.statusCode());
    }

    @Test
    void getArmorById_invalidId_returns400() throws Exception {
        HttpResponse<String> response = get("/armor/abc");
        assertEquals(400, response.statusCode());
    }

    @Test
    void getArmor_trailingSlash_returnsList() throws Exception {
        HttpResponse<String> response = get("/armor/");
        assertEquals(200, response.statusCode());
    }

    @Test
    void postArmor_notSupported_returns405() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/armor"))
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(405, response.statusCode());
    }
}