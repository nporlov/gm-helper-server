package ru.nporlov;

import ru.nporlov.config.DependencyContainer;
import ru.nporlov.config.ServerConfig;

public class Main {
    static void main() throws Exception {
        ServerConfig config = new ServerConfig();
        DependencyContainer container = new DependencyContainer(config);

        container.getServer().start();

        System.out.println("Server started on http://" + config.getHost() + ":" + config.getPort());

        Thread.currentThread().join();
    }
}
