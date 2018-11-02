package org.pioneer.api.network;

import java.net.InetSocketAddress;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public interface NetworkProperties {

    /**
     * Get the address where the server socket binds to.
     *
     * @return {@link InetSocketAddress} the address where the server socket binds to.
     */
    InetSocketAddress getAddress();

    /**
     * Set the address where the server socket binds to.
     *
     * @param address {@link InetSocketAddress} the new address where the server socket binds to.
     */
    void setAddress(InetSocketAddress address);

    /**
     * @return true if the native transport is allowed.
     */
    boolean isNativeTransportAllowed();

    /**
     * @param allow the new rule.
     */
    void allowNativeTransport(boolean allow);

    /**
     * Gets the acceptor thread count.
     *
     * @return the thread count.
     */
    int getAcceptorThreadCount();

    /**
     * Set the acceptor thread count.
     *
     * @param count the new thread count.
     */
    void setAcceptorThreadCount(int count);

    /**
     * Gets the worker thread count.
     *
     * @return the thread count.
     */
    int getWorkerThreadCount();

    /**
     * Wet the worker thread count.
     *
     * @param count the new thread count.
     */
    void setWorkerThreadCount(int count);

}
