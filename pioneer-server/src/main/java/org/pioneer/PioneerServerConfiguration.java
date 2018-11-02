package org.pioneer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.pioneer.api.ServerConfiguration;
import org.pioneer.api.configuration.configuration.AbstractConfiguration;
import org.pioneer.api.configuration.configuration.ConfigurationInfo;
import org.pioneer.api.configuration.configuration.ConfigurationProperty;
import org.pioneer.api.network.NetworkProperties;
import org.pioneer.network.PioneerNetworkProperties;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationInfo(filename = "config.json")
public class PioneerServerConfiguration extends AbstractConfiguration implements ServerConfiguration {

    @ConfigurationProperty(name = "network")
    private NetworkProperties networkProperties = new PioneerNetworkProperties();

}
