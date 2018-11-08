package org.pioneer.network;

import org.pioneer.network.base.Listenable;

import java.io.IOException;
import java.nio.channels.AsynchronousServerSocketChannel;

public class Server extends Listenable<Server> {

    private final AsynchronousServerSocketChannel channel;

    public Server() {
        try {
            channel = AsynchronousServerSocketChannel.open();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
