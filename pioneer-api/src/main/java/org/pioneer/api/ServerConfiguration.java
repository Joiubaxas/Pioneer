package org.pioneer.api;

import org.pioneer.api.network.NetworkProperties;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public interface ServerConfiguration {

    /**
     * Gets the network properties.
     *
     * @return {@link NetworkProperties} the provided network properties.
     */
    NetworkProperties getNetworkProperties();

    /**
     * Sets the network properties.
     *
     * @param properties the new network properties.
     */
    void setNetworkProperties(NetworkProperties properties);

}
