package org.pioneer.api.world;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */
public interface ChunkProvider {

    /**
     * Provides the {@link ChunkSection} at the coordinates.
     *
     * @param chunkX the x coordinate.
     * @param chunkZ the z coordinate.
     * @return {@link ChunkSection} provides from the coordinates.
     */
    ChunkSection getSection(int chunkX, int chunkZ);

}
