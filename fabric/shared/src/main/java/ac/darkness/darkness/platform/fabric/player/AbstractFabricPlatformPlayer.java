package ac.darkness.darkness.platform.fabric.player;

import ac.darkness.darkness.platform.api.entity.GrimEntity;
import ac.darkness.darkness.platform.api.player.BlockTranslator;
import ac.darkness.darkness.platform.api.player.PlatformInventory;
import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import ac.darkness.darkness.platform.fabric.FabricPlatformServices;
import ac.darkness.darkness.platform.fabric.entity.AbstractFabricGrimEntity;
import ac.darkness.darkness.platform.fabric.inject.FabricServerPlayerHandle;
import ac.darkness.darkness.utils.common.arguments.CommonGrimArguments;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPluginMessage;
import com.github.retrooper.packetevents.util.Vector3d;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractFabricPlatformPlayer<T> extends AbstractFabricGrimEntity<T> implements PlatformPlayer {
    protected final AbstractFabricPlatformInventory inventory;
    private final @Nullable User user;
    private final BlockTranslator blockTranslator;

    public AbstractFabricPlatformPlayer(T player) {
        super(player);
        this.inventory = FabricPlatformServices.createInventory(this);
        if (CommonGrimArguments.USE_CHAT_FAST_BYPASS.value()) {
            Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(handle().uuid());
            this.user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        } else {
            this.user = null;
        }
        this.blockTranslator = FabricPlatformServices.createBlockTranslator(player);
    }

    protected T serverPlayer() {
        return this.entity;
    }

    protected FabricServerPlayerHandle handle() {
        return (FabricServerPlayerHandle) this.entity;
    }

    @Override
    public void resyncSharedFlags() {
        handle().resyncSharedFlags();
    }

    @Override
    public boolean hasPermission(String permission) {
        return getSender().hasPermission(permission);
    }

    @Override
    public boolean hasPermission(String permission, boolean defaultIfUnset) {
        return getSender().hasPermission(permission, defaultIfUnset);
    }

    @Override
    public void sendMessage(String message) {
        if (CommonGrimArguments.USE_CHAT_FAST_BYPASS.value() && user != null) {
            user.sendMessage(message);
        } else {
            handle().sendSystemText(FabricPlatformServices.textLiteral(message));
        }
    }

    @Override
    public void sendMessage(Component message) {
        if (CommonGrimArguments.USE_CHAT_FAST_BYPASS.value() && user != null) {
            user.sendMessage(message);
        } else {
            Object nativeText = FabricPlatformServices.conversionUtil().toNativeText(message);
            handle().sendSystemText(nativeText);
        }
    }

    @Override
    public boolean isOnline() {
        return !handle().isDisconnected();
    }

    @Override
    public String getName() {
        return handle().usernameString();
    }

    @Override
    public void updateInventory() {
        handle().broadcastInventoryChanges();
    }

    @Override
    public Vector3d getPosition() {
        FabricServerPlayerHandle handle = handle();
        return new Vector3d(handle.posX(), handle.posY(), handle.posZ());
    }

    @Override
    public PlatformInventory getInventory() {
        return inventory;
    }

    @Override
    public GrimEntity getVehicle() {
        Object vehicle = handle().vehicleEntity();
        return vehicle != null ? FabricPlatformServices.createEntity(vehicle) : null;
    }

    @Override
    public GameMode getGameMode() {
        return FabricPlatformServices.conversionUtil().fromNativeGameMode(handle().gameMode());
    }

    @Override
    public java.util.UUID getUniqueId() {
        return handle().uuid();
    }

    @Override
    public boolean isExternalPlayer() {
        return false;
    }

    @Override
    public BlockTranslator getBlockTranslator() {
        return blockTranslator;
    }

    @Override
    public void sendPluginMessage(String channelName, byte[] byteArray) {
        Object channel = PacketEvents.getAPI().getProtocolManager().getChannel(handle().uuid());
        User user = PacketEvents.getAPI().getProtocolManager().getUser(channel);
        if (user != null) {
            user.sendPacket(new WrapperPlayServerPluginMessage(pluginMessageChannel(channelName), byteArray));
        }
    }

    private static String pluginMessageChannel(String channelName) {
        return channelName.equals("BungeeCord") ? "bungeecord:main" : channelName;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void replaceNativePlayer(Object nativePlayerObject) {
        setNativeEntity((T) nativePlayerObject);
    }

    @Override
    public boolean isDead() {
        return handle().isDead();
    }
}
