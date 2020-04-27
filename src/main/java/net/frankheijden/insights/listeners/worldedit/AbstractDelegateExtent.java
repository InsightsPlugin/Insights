package net.frankheijden.insights.listeners.worldedit;

import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.entity.BaseEntity;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.OperationQueue;
import com.sk89q.worldedit.math.BlockVector2;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldedit.world.biome.BiomeType;
import com.sk89q.worldedit.world.block.*;

import javax.annotation.Nullable;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A modified version of {@Link com.sk89q.worldedit.extent.AbstractDelegateExtent} to
 * allow override of the commit operation.
 * ------
 * A base class for {@link Extent}s that merely passes extents onto another.
 */
public abstract class AbstractDelegateExtent implements Extent {

    private final Extent extent;

    /**
     * Create a new instance.
     *
     * @param extent the extent
     */
    protected AbstractDelegateExtent(Extent extent) {
        checkNotNull(extent);
        this.extent = extent;
    }

    /**
     * Get the extent.
     *
     * @return the extent
     */
    public Extent getExtent() {
        return extent;
    }

    @Override
    public BlockState getBlock(BlockVector3 position) {
        return extent.getBlock(position);
    }

    @Override
    public BaseBlock getFullBlock(BlockVector3 position) {
        return extent.getFullBlock(position);
    }

    @Override
    public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
        return extent.setBlock(location, block);
    }

    @Override
    @Nullable
    public Entity createEntity(Location location, BaseEntity entity) {
        return extent.createEntity(location, entity);
    }

    @Override
    public List<? extends Entity> getEntities() {
        return extent.getEntities();
    }

    @Override
    public List<? extends Entity> getEntities(Region region) {
        return extent.getEntities(region);
    }

    @Override
    public BiomeType getBiome(BlockVector2 position) {
        return extent.getBiome(position);
    }

    @Override
    public boolean setBiome(BlockVector2 position, BiomeType biome) {
        return extent.setBiome(position, biome);
    }

    @Override
    public BlockVector3 getMinimumPoint() {
        return extent.getMinimumPoint();
    }

    @Override
    public BlockVector3 getMaximumPoint() {
        return extent.getMaximumPoint();
    }

    protected Operation commitBefore() {
        return null;
    }

    @Override
    public @Nullable Operation commit() {
        Operation ours = commitBefore();
        Operation other = extent.commit();
        if (ours != null && other != null) {
            return new OperationQueue(ours, other);
        } else if (ours != null) {
            return ours;
        } else if (other != null) {
            return other;
        } else {
            return null;
        }
    }
}
