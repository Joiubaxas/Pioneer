package org.pioneer.network;

import io.netty.channel.epoll.Epoll;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.pioneer.api.configuration.configuration.ConfigurationProperties;
import org.pioneer.api.configuration.configuration.ConfigurationProperty;
import org.pioneer.api.network.NetworkProperties;

import java.net.InetSocketAddress;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties
public class PioneerNetworkProperties implements NetworkProperties {

    @Getter
    @Setter
    @ConfigurationProperty(name = "address")
    private InetSocketAddress address = new InetSocketAddress(25565);

    @ConfigurationProperty(name = "allowNativeTransport")
    private boolean allowNativeTransport = Epoll.isAvailable();

    @Getter
    @Setter
    @ConfigurationProperty(name = "acceptorThreadCount")
    private int acceptorThreadCount = 1;

    @Getter
    @Setter
    @ConfigurationProperty(name = "workerThreadCount")
    private int workerThreadCount = 2;

    @Override
    public boolean isNativeTransportAllowed() {
        return this.allowNativeTransport;
    }

    @Override
    public void allowNativeTransport(boolean allow) {
        this.allowNativeTransport = allow;
    }
}
