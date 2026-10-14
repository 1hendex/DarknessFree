package ac.darkness.darkness.platform.bukkit.initables;

import ac.darkness.darkness.manager.init.start.StartableInitable;
import ac.darkness.darkness.platform.bukkit.DarknessFreeBukkitLoaderPlugin;
import ac.darkness.darkness.platform.luckperms.AbstractLuckPermsHandler;
import ac.darkness.darkness.utils.anticheat.LogUtil;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.EventSubscription;
import net.luckperms.api.event.LuckPermsEvent;
import net.luckperms.api.event.context.ContextUpdateEvent;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.Consumer;

/// @see BukkitLuckPermsInitable
public class BukkitLuckPermsHandler extends AbstractLuckPermsHandler implements StartableInitable {

    @Override
    public void start() {
        try {
            register(LuckPermsProvider.get());
        } catch (IllegalStateException e) {
            LogUtil.warn("LuckPerms detected but its API was not available for the permission refresh hook");
        } catch (Exception e) {
            LogUtil.error("Error when initializing LuckPerms hook", e);
        }
    }

    @Override
    protected <T extends LuckPermsEvent> EventSubscription<T> subscribe(
            LuckPerms luckPerms,
            Class<T> eventClass,
            Consumer<? super T> handler
    ) {
        return luckPerms.getEventBus().subscribe(DarknessFreeBukkitLoaderPlugin.LOADER, eventClass, handler);
    }

    @Override
    protected UUID contextSubjectUuid(ContextUpdateEvent event) {
        return event.getSubject(Player.class)
                .map(Player::getUniqueId)
                .orElse(null);
    }
}
