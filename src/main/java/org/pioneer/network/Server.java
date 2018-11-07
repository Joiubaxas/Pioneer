package org.pioneer.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * The entity that all {@link Client}s will connect to.
 *
 * @author Jacob G.
 * @since November 1, 2017
 *
 * Modified for use within Pioneer Minecraft Server
 */
public final class Server extends Receiver<Consumer<Client>> implements Channeled<AsynchronousServerSocketChannel> {
    private final ThreadPoolExecutor executor;
    private final AsynchronousServerSocketChannel channel;


    public Server() {
        this(4096);
    }

    public Server(int bufferSize) {
        super(bufferSize);

        try {
            int numThreads = Math.max(1, Runtime.getRuntime().availableProcessors() - 1);

            executor = new ThreadPoolExecutor(numThreads, numThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(), runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(false);
                return thread;
            });

            executor.prestartAllCoreThreads();

            channel = AsynchronousServerSocketChannel.open(AsynchronousChannelGroup.withThreadPool(executor));
            channel.setOption(StandardSocketOptions.SO_RCVBUF, bufferSize);
        } catch (IOException e) {
            throw new IllegalStateException("Unable to open the channel!");
        }
    }

    public void bind(String address, int port) {
        if (port < 0 || port > 65535) {
            throw new IllegalArgumentException("The port must be between 0 and 65535!");
        }

        try {
            channel.bind(new InetSocketAddress(address, port));

            final Client.Listener listener = new Client.Listener() {
                @Override
                public void failed(Throwable t, Client client) {
                    client.close();
                }
            };

            channel.accept(null, new CompletionHandler<AsynchronousSocketChannel, Void>() {
                @Override
                public void completed(AsynchronousSocketChannel channel, Void attachment) {
                    var client = new Client(bufferSize, channel);
                    connectListeners.forEach(consumer -> consumer.accept(client));
                    Server.this.channel.accept(null, this);
                    channel.read(client.getBuffer(), client, listener);
                }

                @Override
                public void failed(Throwable t, Void attachment) {

                }
            });

            System.out.println(String.format("Successfully bound to %s:%d!", address, port));
        } catch (AlreadyBoundException e) {
            throw new IllegalStateException("A server is already running!");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to bind the server!");
        }
    }

    @Override
    public void close() {
        close($ -> System.err.println("There was an error closing the Server channel!"));
        executor.shutdownNow();
    }

    @Override
    public AsynchronousServerSocketChannel getChannel() {
        return channel;
    }

}
