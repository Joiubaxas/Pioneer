package org.pioneer.api.world;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */

/**
 * Represents a region of 16x256x16 in the {@link World}.
 */
public interface ChunkColumn {

    /**
     * Gets a section in the column.
     *
     * @param chunkY the y coordinate in the column with a minimum of 0 and a maximum of 15.
     * @return {@link ChunkSection} the chunk section at the coordinate.
     */
    ChunkSection getChunkSection(int chunkY);

    /**
     * Sets the section in the column.
     *
     * @param chunkY  the y coordinate in the column with a minimum of 0 and a maximum of 15.
     * @param section the section that sets at the coordinate.
     */
    void setChunkSection(int chunkY, ChunkSection section);
}
