package org.pioneer.api.network;

import org.pioneer.api.mojang.GameProfile;

import java.util.UUID;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public interface PlayerConnection {

    UUID getUniqueId();

    GameProfile getGameProfile();

    AbstractPacketHandler getHandler();

    void setHandler(AbstractPacketHandler handler);

}
