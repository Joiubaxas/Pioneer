package org.pioneer.api.world;

/*
 * Pioneer Project
 * 1.0.0 SNAPSHOT
 *
 * © 2018 Ricardo Borutta
 */

import org.pioneer.api.block.Block;

/**
 * Represents a region of 16x16x16 in a {@link ChunkColumn}.
 */
public interface ChunkSection extends Iterable<Block> {

    /**
     * Gets the block in the region at the coordinates.
     *
     * @param x the x coordinate in the region. Minimum of 0 and a maximum of 15.
     * @param y the y coordinate in the region. Minimum of 0 and a maximum of 15.
     * @param z the z coordinate in the region. Minimum of 0 and a maximum of 15.
     * @return {@link Block} at the coordinate.
     */
    Block getBlock(int x, int y, int z);

    /**
     * Sets the block in the region at the coordinates.
     *
     * @param x the x coordinate in the region. Minimum of 0 and a maximum of 15.
     * @param y the y coordinate in the region. Minimum of 0 and a maximum of 15.
     * @param z the z coordinate in the region. Minimum of 0 and a maximum of 15.
     */
    void setBlock(int x, int y, int z, Block block);

}
