package ru.nporlov;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class GmHelperHttpServer {
    public void start(String host, int port) {
        try  {
            HttpServer server = HttpServer.create (
                    new InetSocketAddress(host, port), 0
            );
            server.setExecutor(null);
            server.start();
        }
        catch (IOException e) {
            System.err.println("Error starting server: " + e.getMessage());
        }
    }
}
