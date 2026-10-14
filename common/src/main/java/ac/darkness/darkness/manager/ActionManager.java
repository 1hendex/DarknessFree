package ac.darkness.darkness.manager;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.packetentity.PacketEntity;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import lombok.Getter;

@Getter
public class ActionManager extends GrimProcessor implements PacketReceiveListener {
    private boolean attacking = false;
    private long lastAttack = 0;
    private double vovka;
    private PacketEntity t;

    public ActionManager(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (isAttack(event)) {
            t = player.compensatedEntities.entityMap.get(getAttackedEntityId(event));
            if (t != null) {
                vovka = t.getPossibleCollisionBoxes().distance(player.boundingBox);
            }
            attacking = true;
            lastAttack = System.currentTimeMillis();
            player.setLastHit(lastAttack);
        } else if (isTickPacketIncludingNonMovement(event.getPacketType())) {
            attacking = false;
        }
    }

    public boolean hasAttackedSince(long time) {
        return System.currentTimeMillis() - lastAttack < time;
    }
}
