package ac.darkness.darkness.checks;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.AbstractProcessor;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.config.ConfigReloadable;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.common.ConfigReloadObserver;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAttack;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import static com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying.isFlying;

// Not everything has to be a check for it to process packets & be configurable
public class GrimProcessor implements AbstractProcessor, ConfigReloadable, ConfigReloadObserver {

    protected final @NotNull GrimPlayer player;

    public GrimProcessor(final @NotNull GrimPlayer player) {
        this(player, true);
    }

    public GrimProcessor(final @NotNull GrimPlayer player, boolean reload) {
        this.player = Objects.requireNonNull(player, "player");
        if (reload) {
            reload();
        }
    }

    @Override
    public final void reload() {
        reload(GrimAPI.INSTANCE.getConfigManager().getConfig());
    }

    @Override
    public void reload(@NotNull ConfigManager configuration) {
        onReload(configuration);
    }

    @Override
    public void onReload(@NotNull ConfigManager config) {}

    @Override
    public String getConfigName() {
        return null;
    }

    public static boolean isTransaction(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.PONG ||
                packetType == PacketType.Play.Client.WINDOW_CONFIRMATION;
    }

    public static boolean isAsync(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.KEEP_ALIVE
                || packetType == PacketType.Play.Client.CHUNK_BATCH_ACK
                || packetType == PacketType.Play.Client.RESOURCE_PACK_STATUS;
    }

    public final boolean isUpdate(PacketTypeCommon packetType) {
        return isTickPacket(packetType) || isTransaction(packetType);
    }

    public static boolean isFlying(PacketTypeCommon packetType) {
        return WrapperPlayClientPlayerFlying.isFlying(packetType);
    }

    public static boolean isAttack(PacketReceiveEvent event) {
        return event.getPacketType() == PacketType.Play.Client.ATTACK
                || (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY
                && new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK);
    }

    public static int getAttackedEntityId(PacketReceiveEvent event) {
        PacketTypeCommon type = event.getPacketType();
        if (type == PacketType.Play.Client.ATTACK) {
            return new WrapperPlayClientAttack(event).getEntityId();
        }
        if (type == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return wrapper.getEntityId();
            }
        }
        return -1;
    }

    public static boolean isRotation(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.PLAYER_ROTATION
                || packetType == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION;
    }

    public static boolean isAnimation(PacketTypeCommon packetType) {
        return packetType == PacketType.Play.Client.ANIMATION || packetType == PacketType.Play.Client.PUNCH;
    }

    public final boolean isTickPacket(PacketTypeCommon packetType) {
        if (isTickPacketIncludingNonMovement(packetType)) {
            if (isFlying(packetType)) {
                return !player.packetStateData.lastPacketWasTeleport && !player.packetStateData.lastPacketWasOnePointSeventeenDuplicate;
            }
            return true;
        }
        return false;
    }

    public final boolean isTickPacketIncludingNonMovement(PacketTypeCommon packetType) {
        // On 1.21.2+ fall back to the TICK_END packet IF the player did not send a movement packet for their tick
        // TickTimer checks to see if player did not send a tick end packet before new flying packet is sent
        if (player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2)
                && !player.packetStateData.didSendMovementBeforeTickEnd) {
            if (packetType == PacketType.Play.Client.CLIENT_TICK_END) {
                return true;
            }
        }

        return isFlying(packetType);
    }

    // prevent causing exploits with packet cancelling (ie NoSlow)
    public final boolean canCancel(DiggingAction action) {
        return action != DiggingAction.RELEASE_USE_ITEM
                // we check client version here because 1.8- doesn't predict dropping items, so we can cancel them. (see CompensatedInventory)
                && (action != DiggingAction.DROP_ITEM && action != DiggingAction.DROP_ITEM_STACK || player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8));
    }
}
