package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClientStatus;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;

import java.util.ArrayDeque;

@CheckData(name = "ProtocolH", alternativeName = "PacketOrderG", configName = "ProtocolH", stableKey = "grim.packetorder.hotbar_inventory_manage_order", description = "Managed hotbar or inventory while performing another conflicting action", experimental = true)
public class ProtocolH extends Check implements PacketReceiveListener, PostPredictionListener {
    private static final Verbose V = Verbose.of(
            "action={str}, attacking={bool}, releasing={bool}, rightClicking={bool}, picking={bool}, digging={bool}");

    static final int ACTION_OPEN_INVENTORY = 0;
    static final int ACTION_SWAP = 1;
    static final int ACTION_DROP = 2;

    public ProtocolH(GrimPlayer player) {
        super(player);
    }

    private final ArrayDeque<FlagData> flags = new ArrayDeque<>();

    static String actionName(int action) {
        return switch (action) {
            case ACTION_OPEN_INVENTORY -> "openInventory";
            case ACTION_SWAP -> "swap";
            case ACTION_DROP -> "drop";
            default -> "unknown";
        };
    }

    private static int action(DiggingAction action) {
        return action == null ? ACTION_OPEN_INVENTORY : action == DiggingAction.SWAP_ITEM_WITH_OFFHAND ? ACTION_SWAP : ACTION_DROP;
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING || (event.getPacketType() == PacketType.Play.Client.CLIENT_STATUS
                && new WrapperPlayClientClientStatus(event).getAction() == WrapperPlayClientClientStatus.Action.OPEN_INVENTORY_ACHIEVEMENT)) {
            DiggingAction action = null;
            if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
                action = new WrapperPlayClientPlayerDigging(event).getAction();
                if (action != DiggingAction.SWAP_ITEM_WITH_OFFHAND
                        && action != DiggingAction.DROP_ITEM
                        && action != DiggingAction.DROP_ITEM_STACK
                ) return;
            }

            if (player.protocolProcessor.isAttackingOrStabbing()
                    || player.protocolProcessor.isReleasing()
                    || player.protocolProcessor.isRightClicking()
                    || player.protocolProcessor.isPicking()
                    || player.protocolProcessor.isDigging()) {
                int actionKind = action(action);
                boolean attacking = player.protocolProcessor.isAttackingOrStabbing();
                boolean releasing = player.protocolProcessor.isReleasing();
                boolean rightClicking = player.protocolProcessor.isRightClicking();
                boolean picking = player.protocolProcessor.isPicking();
                boolean digging = player.protocolProcessor.isDigging();
                if (!player.canSkipTicks()) {
                    if (flag(V.write(verbose())
                            .str(actionName(actionKind))
                            .bool(attacking)
                            .bool(releasing)
                            .bool(rightClicking)
                            .bool(picking)
                            .bool(digging)) && shouldModifyPackets() && canCancel(action)) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    flags.add(new FlagData(actionKind, attacking, releasing, rightClicking, picking, digging));
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3)) {
            for (FlagData data : flags) {
                flag(V.write(verbose())
                        .str(actionName(data.action()))
                        .bool(data.attacking())
                        .bool(data.releasing())
                        .bool(data.rightClicking())
                        .bool(data.picking())
                        .bool(data.digging()));
            }
        }

        flags.clear();
    }

    private record FlagData(
            int action,
            boolean attacking,
            boolean releasing,
            boolean rightClicking,
            boolean picking,
            boolean digging) {
    }
}
