package ru.nporlov.http;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.nporlov.config.ObjectMapperFactory;
import ru.nporlov.dto.ApiError;
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

import static org.junit.jupiter.api.Assertions.*;

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
        ArmorHandler handler = new ArmorHandler(repository, writer, mapper);
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

    // emulate POST from client
    private HttpResponse<String> post(String path, String body) throws Exception{
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
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
        assertError(response, 404, "NOT_FOUND");
    }

    @Test
    void getArmorById_invalidId_returns400() throws Exception {
        HttpResponse<String> response = get("/armor/abc");
        assertError(response, 400, "VALIDATION_ERROR");
    }

    @Test
    void getArmor_trailingSlash_returnsList() throws Exception {
        HttpResponse<String> response = get("/armor/");
        assertEquals(200, response.statusCode());
    }

    @Test
    void postArmor_validBody_returns201() throws Exception {
        String body = """
                {
                "type": "LIGHT",
                "is-shield": false,
                "penalties": []
                }
                """;
        HttpResponse<String> response = post("/armor/", body);

        assertEquals(201, response.statusCode());

        String location = response.headers().firstValue("Location").orElseThrow();
        assertTrue(location.startsWith("/armor/"));

        Armor parsed = mapper.readValue(response.body(), Armor.class);
        assertNotNull(parsed.getId());
        assertEquals(ArmorType.LIGHT, parsed.getType());

        assertTrue(repository.findById(parsed.getId()).isPresent());
    }

    @Test
    void postArmor_emptyBody_returns400() throws Exception {
        HttpResponse<String> response = post("/armor/", "");
        assertEquals(400, response.statusCode());
    }

    @Test
    void postArmor_malformedJson_returns400() throws Exception {
        HttpResponse<String> response = post("/armor/", "{not json");
        assertError(response, 400, "MALFORMED_JSON");
    }

    private void assertError(HttpResponse<String> response, int expectedStatus, String expectedCode) {
        assertEquals(expectedStatus, response.statusCode(), "Body: " + response.body());
        ApiError error = mapper.readValue(response.body(), ApiError.class);
        assertEquals(expectedStatus, error.getStatus());
        assertEquals(expectedCode, error.getCode());
        assertNotNull(error.getMessage());
    }
}