package ac.darkness.darkness.checks.impl.misc.interact;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.BlockBreakListener;
import ac.darkness.darkness.checks.type.PreViaPacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.update.BlockBreak;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;

@CheckData(name = "InteractH", alternativeName = "NoSwingBreak", configName = "InteractH", stableKey = "grim.breaking.no_swing_break", description = "Did not swing while breaking block", experimental = true)
public class InteractH extends Check implements BlockBreakListener, PreViaPacketReceiveListener {
    private boolean sentAnimation;
    private boolean sentBreak;

    public InteractH(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onBlockBreak(BlockBreak blockBreak) {
        if (blockBreak.action != DiggingAction.CANCELLED_DIGGING) {
            sentBreak = true;
        }
    }

    @Override
    public void onPreViaPacketReceive(PacketReceiveEvent event) {
        if (isAnimation(event.getPacketType())) {
            sentAnimation = true;
        }

        if (isTickPacket(event.getPacketType())) {
            if (sentBreak && !sentAnimation) {
                flag();
            }

            sentAnimation = sentBreak = false;
        }
    }
}
