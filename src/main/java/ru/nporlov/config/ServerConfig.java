package ru.nporlov.config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ServerConfig {
    private final static int port = 8080;
    private final static String host = "0.0.0.0";
    private final static Path dataDir = Paths.get(System.getProperty("user.home"), ".gm-helper", "data");
    private final static int poolSize = 1;

    public ServerConfig() throws Exception {}

    public Path getDataDir() {
        return dataDir;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public int getPoolSize() {
        return poolSize;
    }
}
