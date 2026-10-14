package ac.darkness.darkness.platform.api.manager;

import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public interface ItemResetHandler {
    /**
     * clears any item usage the player may have, without triggering side effects (ie bow firing)
     */
    void resetItemUsage(@Nullable PlatformPlayer player);
    /**
     * Returns the hand in which the player is using an item, or null if the player isn't using an item
     */
    @Contract("null -> null")
    @Nullable InteractionHand getItemUsageHand(@Nullable PlatformPlayer player);
    @Contract("null -> false")
    boolean isUsingItem(@Nullable PlatformPlayer player);
}
