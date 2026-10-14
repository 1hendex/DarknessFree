package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import ac.darkness.darkness.utils.nmsutil.BlockBreakSpeed;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;

@CheckData(name = "ProtocolJ", alternativeName = "PacketOrderI", configName = "ProtocolJ", stableKey = "grim.packetorder.input_tick_order", description = "Sent combat, use, release, or digging packets in an invalid tick order", experimental = true)
public class ProtocolJ extends Check implements PacketReceiveListener, PostPredictionListener {
    private static final Verbose V = Verbose
            .of("type={str}[, attacking={bool}][, rightClicking={bool}][, picking={bool}][, releasing={bool}], digging={bool}");

    static final int TYPE_INTERACT = 0;
    static final int TYPE_PLACE_USE = 1;
    static final int TYPE_RELEASE = 2;
    static final int TYPE_ATTACK = 3;

    public ProtocolJ(final GrimPlayer player) {
        super(player);
    }

    private boolean exemptPlacingWhileDigging;

    private boolean setback;
    // for placing
    private boolean cancelledDigging;
    private WrappedBlockState startedDiggingBlock;
    private boolean digging;
    private final ArrayDeque<FlagData> flags = new ArrayDeque<>();

    static String typeName(int type) {
        return switch (type) {
            case TYPE_INTERACT -> "interact";
            case TYPE_PLACE_USE -> "place/use";
            case TYPE_RELEASE -> "release";
            case TYPE_ATTACK -> "attack";
            default -> "unknown";
        };
    }

    private Verbose.Writer write(
            int type,
            boolean attacking,
            boolean rightClicking,
            boolean picking,
            boolean releasing,
            boolean digging) {
        boolean release = type == TYPE_RELEASE;
        boolean attack = type == TYPE_ATTACK;
        return V.write(verbose())
                .str(typeName(type))
                .bool(release).bool(attacking)
                .bool(release || attack).bool(rightClicking)
                .bool(release || attack).bool(picking)
                .bool(!release).bool(releasing)
                .bool(digging);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            if (new WrapperPlayClientInteractEntity(event).getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                onAttack(event);
            } else if (player.protocolProcessor.isReleasing() || player.protocolProcessor.isDigging()) {
                boolean releasing = player.protocolProcessor.isReleasing();
                boolean digging = player.protocolProcessor.isDigging();
                if (!player.canSkipTicks()) {
                    if (flag(write(TYPE_INTERACT, false, false, false, releasing, digging)) && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    flags.add(new FlagData(TYPE_INTERACT, false, false, false, releasing, digging));
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            digging |= cancelledDigging;

            if (startedDiggingBlock != null && !digging) {
                double damage = BlockBreakSpeed.getBlockDamage(player, startedDiggingBlock);
                if (damage < 1 && (damage > 0 || player.gamemode != GameMode.CREATIVE)) {
                    digging = true;
                }
            }

            if (player.protocolProcessor.isReleasing() || digging) {
                boolean releasing = player.protocolProcessor.isReleasing();
                if (!player.canSkipTicks()) {
                    if (flag(write(TYPE_PLACE_USE, false, false, false, releasing, digging)) && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    flags.add(new FlagData(TYPE_PLACE_USE, false, false, false, releasing, digging));
                }
            }
        }

        if (event.getPacketType() == PacketType.Play.Client.ATTACK || event.getPacketType() == PacketType.Play.Client.SPECTATE_ENTITY) {
            onAttack(event);
        }

        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);

            switch (packet.getAction()) {
                case STAB -> onAttack(event);
                case RELEASE_USE_ITEM -> {
                    if (player.protocolProcessor.isAttackingOrStabbing() || player.protocolProcessor.isRightClicking() || player.protocolProcessor.isPicking() || player.protocolProcessor.isDigging()) {
                        boolean attacking = player.protocolProcessor.isAttackingOrStabbing();
                        boolean rightClicking = player.protocolProcessor.isRightClicking();
                        boolean picking = player.protocolProcessor.isPicking();
                        boolean digging = player.protocolProcessor.isDigging();
                        if (!player.canSkipTicks()) {
                            if (flag(write(TYPE_RELEASE, attacking, rightClicking, picking, false, digging))) {
                                setback = true;
                            }
                        } else {
                            flags.add(new FlagData(TYPE_RELEASE, attacking, rightClicking, picking, false, digging));
                            setback = true;
                        }
                    }
                }
                case START_DIGGING -> {
                    if (shouldCheckPlacingWhileDigging()) {
                        cancelledDigging = false;
                        startedDiggingBlock = player.compensatedWorld.getBlock(packet.getBlockPosition());
                    }
                }
                case CANCELLED_DIGGING -> cancelledDigging = shouldCheckPlacingWhileDigging();
                case FINISHED_DIGGING -> digging = shouldCheckPlacingWhileDigging();
            }
        }

        if (!player.cameraEntity.isSelf() || isTickPacket(event.getPacketType())) {
            cancelledDigging = digging = false;
            startedDiggingBlock = null;
        }
    }

    private boolean shouldCheckPlacingWhileDigging() {
        return !exemptPlacingWhileDigging && player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_8);
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) {
            if (setback) {
                setbackIfAboveSetbackVL();
                setback = false;
            }
            return;
        }

        if (player.isTickingReliablyFor(3)) {
            for (FlagData data : flags) {
                if (flag(write(data.type(), data.attacking(), data.rightClicking(), data.picking(),
                        data.releasing(), data.digging())) && setback) {
                    setbackIfAboveSetbackVL();
                    setback = false;
                }
            }
        }

        flags.clear();
        setback = false;
    }

    private void onAttack(PacketReceiveEvent event) {
        if (player.protocolProcessor.isRightClicking() || player.protocolProcessor.isPicking() || player.protocolProcessor.isReleasing() || player.protocolProcessor.isDigging()) {
            boolean rightClicking = player.protocolProcessor.isRightClicking();
            boolean picking = player.protocolProcessor.isPicking();
            boolean releasing = player.protocolProcessor.isReleasing();
            boolean digging = player.protocolProcessor.isDigging();
            if (!player.canSkipTicks()) {
                if (flag(write(TYPE_ATTACK, false, rightClicking, picking, releasing, digging)) && shouldModifyPackets()) {
                    event.setCancelled(true);
                    player.onPacketCancel();
                }
            } else {
                flags.add(new FlagData(TYPE_ATTACK, false, rightClicking, picking, releasing, digging));
            }
        }
    }

    @Override
    public void onReload(@NotNull ConfigManager config) {
        exemptPlacingWhileDigging = config.getBooleanElse(getConfigName() + ".exempt-placing-while-digging",
                config.getBooleanElse("PacketOrderI.exempt-placing-while-digging", false));
    }

    private record FlagData(
            int type,
            boolean attacking,
            boolean rightClicking,
            boolean picking,
            boolean releasing,
            boolean digging) {}
}
