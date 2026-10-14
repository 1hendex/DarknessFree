package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;

@CheckData(name = "KillAuraF", alternativeName = "KillAuraL", configName = "KillAuraF", stableKey = "grim.killaura.post", description = "Checks for post aura")
public class KillAuraF extends Check implements PacketReceiveListener {
    private long lastFlying, lastFlyingDelay;
    private boolean sentAttack;
    private double average = 25L;
    private int hits;
    private int buffer;

    public KillAuraF(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (isFlying(event.getPacketType())) {
            long now = time();
            long delta = now - lastFlying;
            if (sentAttack) {
                if (delta > 40L && delta < 100L) {
                    if (++buffer > 3) {
                        flagAndAlert("delta=" + delta);
                    }
                } else {
                    buffer = Math.max(0, buffer - 1);
                }
                sentAttack = false;
            }
            lastFlyingDelay = now - lastFlying;
            lastFlying = now;
        } else if (isAttack(event)) {
            final long now = time();
            final double delta = now - lastFlying;
            average = ((average * 9) + delta) / 10;
            if (delta >= 0 && delta < 10L) {
                sentAttack = true;
            }
            if (lastFlyingDelay > 10L && lastFlyingDelay < 90L) {
                if (average < 10 && hits > 10) {
                    flagAndAlert("delta=" + (float) average);
                    average = 20;
                }
            }
            hits++;
        }
    }
}
