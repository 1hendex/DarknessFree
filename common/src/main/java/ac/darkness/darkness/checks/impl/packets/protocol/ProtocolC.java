package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.checks.type.PostPredictionListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.PredictionComplete;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientClickWindow.WindowClickType;

@CheckData(name = "ProtocolC", alternativeName = "PacketOrderA", configName = "ProtocolC", stableKey = "grim.packetorder.window_click_order", description = "Sent pickup and quick-move inventory clicks in an invalid order", experimental = true)
public class ProtocolC extends Check implements PacketReceiveListener, PostPredictionListener {
    public ProtocolC(final GrimPlayer player) {
        super(player);
    }

    @Override
    public boolean isApplicable() {
        return player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_12_2);
    }

    private int invalid;

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.CLICK_WINDOW) {
            final WindowClickType clickType = new WrapperPlayClientClickWindow(event).getWindowClickType();

            if ((clickType == WindowClickType.PICKUP || clickType == WindowClickType.PICKUP_ALL) && player.protocolProcessor.isQuickMoveClicking()
                    || clickType == WindowClickType.QUICK_MOVE && player.protocolProcessor.isPickUpClicking()) {
                if (!player.canSkipTicks()) {
                    if (flag() && shouldModifyPackets()) {
                        event.setCancelled(true);
                        player.onPacketCancel();
                    }
                } else {
                    invalid++;
                }
            }
        }
    }

    @Override
    public void onPredictionComplete(PredictionComplete predictionComplete) {
        if (!player.canSkipTicks()) return;

        if (player.isTickingReliablyFor(3)) {
            for (; invalid >= 1; invalid--) {
                flag();
            }
        }

        invalid = 0;
    }
}
