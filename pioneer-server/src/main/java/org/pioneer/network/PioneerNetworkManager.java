package org.pioneer.network;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.pioneer.api.network.NetworkManager;
import org.pioneer.api.network.NetworkProperties;
import org.pioneer.api.util.function.TriFunction;

import java.io.IOException;
import java.util.concurrent.ThreadFactory;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public class PioneerNetworkManager implements NetworkManager {

    private final ChannelGroup channels;

    public PioneerNetworkManager(EventLoopGroup group) {
        this.channels = new DefaultChannelGroup("ChannelGroup", group.next());
    }

    @Override
    public void startEndpoint(NetworkProperties properties) {
        System.out.println("Starting endpoint to " + properties.getAddress() + "...");

        TriFunction<Boolean, Integer, ThreadFactory, EventLoopGroup> createGroup = (allowNative, threadCount, factory) -> {
            if (!allowNative) {
                return new NioEventLoopGroup(threadCount, factory);
            }
            return new EpollEventLoopGroup(threadCount, factory);
        };

        boolean nativeTransport = properties.isNativeTransportAllowed() && Epoll.isAvailable();

        if (nativeTransport) {
            System.out.println("    Use native transport.");
        }

        int acceptorThreadCount = properties.getAcceptorThreadCount();
        ThreadFactory acceptorThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Acceptor Thread #%d").build();

        System.out.println("    Creating acceptor executor with " + acceptorThreadCount + " threads...");
        EventLoopGroup acceptorGroup = createGroup.apply(nativeTransport, acceptorThreadCount, acceptorThreadFactory);

        int workerThreadCount = properties.getWorkerThreadCount();
        ThreadFactory workerThreadFactory = new ThreadFactoryBuilder()
                .setNameFormat("Worker Thread #%d").build();

        System.out.println("    Creating worker executor with " + acceptorThreadCount + " threads...");
        EventLoopGroup workerGroup = createGroup.apply(nativeTransport, workerThreadCount, workerThreadFactory);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(acceptorGroup, workerGroup);
        bootstrap.childHandler(new LoggingHandler(LogLevel.INFO));
        bootstrap.channel(nativeTransport ? EpollServerSocketChannel.class : NioServerSocketChannel.class);

        ChannelFuture future = bootstrap.bind(properties.getAddress());
        future.addListener(future1 -> {
            if (!future1.isSuccess()) {
                System.err.println("Failed to start the endpoint to " + properties.getAddress() + ": \"" + future1.cause().getMessage() + "\"");
                acceptorGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
                return;
            }
            System.out.println("Endpoint bound to " + properties.getAddress() + " successfully.");
        });

        this.channels.add(future.channel());
    }

    @Override
    public ChannelGroup getChannels() {
        return this.channels;
    }

    @Override
    public void close() throws IOException {
        this.channels.close();
    }
}
