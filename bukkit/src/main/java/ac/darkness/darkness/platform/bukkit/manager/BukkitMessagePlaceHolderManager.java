package ac.darkness.darkness.platform.bukkit.manager;

import ac.darkness.darkness.platform.api.manager.MessagePlaceHolderManager;
import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import ac.darkness.darkness.platform.bukkit.player.BukkitPlatformPlayer;
import ac.darkness.darkness.utils.reflection.ReflectionUtils;
import me.clip.placeholderapi.PlaceholderAPI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BukkitMessagePlaceHolderManager implements MessagePlaceHolderManager {
    public static final boolean hasPlaceholderAPI = ReflectionUtils.hasClass("me.clip.placeholderapi.PlaceholderAPI");

    @Override
    public @NotNull String replacePlaceholders(@Nullable PlatformPlayer player, @NotNull String string) {
        if (!hasPlaceholderAPI) return string;
        return PlaceholderAPI.setPlaceholders(player instanceof BukkitPlatformPlayer bukkitPlatformPlayer ? bukkitPlatformPlayer.getBukkitPlayer() : null, string);
    }
}
