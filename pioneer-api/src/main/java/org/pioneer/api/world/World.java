package org.pioneer.api.world;

import java.util.UUID;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public interface World {

    UUID getUniqueId();

    ChunkProvider getChunkProvider();


}
