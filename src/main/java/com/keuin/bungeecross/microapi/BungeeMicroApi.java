package com.keuin.bungeecross.microapi;

import com.keuin.bungeecross.intercommunicate.repeater.MessageRepeatable;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.logging.Logger;

public class BungeeMicroApi {

    private final HttpServer server;
    private final Logger logger = Logger.getLogger(BungeeMicroApi.class.getName());

    public BungeeMicroApi(int port, MessageRepeatable redisRepeater) throws IOException {
        Objects.requireNonNull(redisRepeater);
        if (port <= 0)
            throw new IllegalArgumentException("API listening port must be positive.");

        logger.info(String.format("Starting MicroApi at localhost:%d...", port));
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new RootHandler());
        server.createContext("/message", new MessageHandler(redisRepeater));
        server.setExecutor(null);
        server.start();
    }

    public void stop() {
        server.stop(3);
    }

}
