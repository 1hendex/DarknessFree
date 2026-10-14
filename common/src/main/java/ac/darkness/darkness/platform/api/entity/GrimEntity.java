package ac.darkness.darkness.platform.api.entity;

import ac.grim.grimac.api.GrimIdentity;
import ac.darkness.darkness.platform.api.world.PlatformWorld;
import ac.darkness.darkness.utils.math.Location;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;

public interface GrimEntity extends GrimIdentity {
    /**
     * Eject any passenger.
     *
     * @return True if there was a passenger.
     */
    boolean eject();

    CompletableFuture<Boolean> teleportAsync(Location location);

    @NotNull
    Object getNative();

    boolean isDead();

    PlatformWorld getWorld();

    Location getLocation();

    double distanceSquared(double x, double y, double z);
}
