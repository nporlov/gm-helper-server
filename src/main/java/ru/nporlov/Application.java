package ru.nporlov;

public class Application {
    void main() {
        GmHelperHttpServer server = new GmHelperHttpServer();
        server.start(Properties.host, Properties.port);
    }
}
