package ac.darkness.darkness.checks.impl.movement.sprint;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientEntityAction;

@CheckData(name = "SprintH", alternativeName = "SprintC", configName = "SprintH", stableKey = "grim.sprint.fast", description = "Checks for fast sprint")
public class SprintH extends Check implements PacketReceiveListener {
    private long lastStopSprinting;

    public SprintH(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (player.getTransactionPing() > 1000 || player.isGliding) return;
        if (event.getPacketType() == PacketType.Play.Client.ENTITY_ACTION) {
            WrapperPlayClientEntityAction packet = new WrapperPlayClientEntityAction(event);
            if (packet.getAction() == WrapperPlayClientEntityAction.Action.START_SPRINTING) {
                long delta = time() - lastStopSprinting;
                if (delta < 75L) {
                    if (time() - player.getLastHit() < 1000L) {
                        if (violations++ > 15) {
                            flagAndAlert("delta=" + delta);
                        }
                    }
                } else {
                    violations = 0;
                }
            } else if (packet.getAction() == WrapperPlayClientEntityAction.Action.STOP_SPRINTING) {
                lastStopSprinting = time();
            }
        }
    }
}
