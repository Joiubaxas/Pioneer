package org.pioneer.world;

import org.pioneer.api.world.ChunkProvider;
import org.pioneer.api.world.World;

import java.util.UUID;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public class PioneerWorld implements World {

    private UUID uniqueId;
    private ChunkProvider provider;

    public PioneerWorld(String name, ChunkProvider provider) {
        this.uniqueId = UUID.nameUUIDFromBytes(("World: " + name).getBytes());
        this.provider = provider;
    }

    @Override
    public UUID getUniqueId() {
        return this.uniqueId;
    }

    @Override
    public ChunkProvider getChunkProvider() {
        return this.provider;
    }
}
