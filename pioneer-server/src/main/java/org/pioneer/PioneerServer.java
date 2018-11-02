package org.pioneer;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import org.pioneer.api.Server;
import org.pioneer.api.ServerConfiguration;
import org.pioneer.api.network.NetworkManager;
import org.pioneer.network.PioneerNetworkManager;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public class PioneerServer implements Server {

    private ServerConfiguration configuration;

    private EventLoopGroup executors;
    private NetworkManager networkManager;

    public PioneerServer(ServerConfiguration configuration) {
        this.configuration = configuration;
    }

    protected void start() {

        this.executors = new NioEventLoopGroup(1);
        this.networkManager = new PioneerNetworkManager(this.executors);

        this.networkManager.startEndpoint(this.configuration.getNetworkProperties());
    }

    protected void stop() {

    }

    @Override
    public void shutdown() {
        System.exit(0);
    }
}
