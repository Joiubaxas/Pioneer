package org.pioneer.api.network;

import io.netty.channel.group.ChannelGroup;

import java.io.Closeable;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */

/**
 * Represents a server socket.
 */
public interface NetworkManager extends Closeable {

    void startEndpoint(NetworkProperties properties);

    ChannelGroup getChannels();


}
