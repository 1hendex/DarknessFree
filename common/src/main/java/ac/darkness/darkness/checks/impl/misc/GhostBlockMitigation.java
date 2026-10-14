package ac.darkness.darkness.checks.impl.misc;

import ac.grim.grimac.api.config.ConfigManager;
import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.BlockPlaceListener;
import ac.darkness.darkness.platform.api.world.PlatformWorld;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockPlace;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.util.Vector3i;
import org.jetbrains.annotations.NotNull;

public class GhostBlockMitigation extends GrimProcessor implements BlockPlaceListener {

    private boolean allow;
    private int distance;

    public GhostBlockMitigation(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockPlace(final BlockPlace place) {
        if (allow || player.platformPlayer == null) return;

        PlatformWorld world = player.platformPlayer.getWorld();
        Vector3i pos = place.getPlacedBlockPos();

        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();

        try {
            for (int i = x - distance; i <= x + distance; i++) {
                for (int j = y - distance; j <= y + distance; j++) {
                    for (int k = z - distance; k <= z + distance; k++) {
                        if (i == x && j == y && k == z) {
                            continue;
                        }

                        if (!world.isChunkLoaded(i >> 4, k >> 4)) {
                            continue;
                        }

                        WrappedBlockState type = world.getBlockAt(i, j, k);

                        if (!type.getType().isAir()) {
                            return;
                        }
                    }
                }
            }

            place.resync();
        } catch (Exception ignored) {}
    }

    @Override
    public void onReload(@NotNull ConfigManager config) {
        allow = config.getBooleanElse("exploit.allow-building-on-ghostblocks", true);
        distance = config.getIntElse("exploit.distance-to-check-for-ghostblocks", 2);

        if (distance < 2 || distance > 4) distance = 2;
    }
}
