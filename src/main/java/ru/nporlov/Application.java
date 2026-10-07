package ru.nporlov;

import ru.nporlov.config.Properties;

public class Application {
    void main() {
        GmHelperHttpServer server = new GmHelperHttpServer();
        server.start(Properties.host, Properties.port);
    }
}
