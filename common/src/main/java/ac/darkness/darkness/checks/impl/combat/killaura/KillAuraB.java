package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;

@CheckData(name = "KillAuraB", stableKey = "grim.killaura.pattern", description = "Checks for invalid attack pattern")
public class KillAuraB extends Check implements PacketReceiveListener {
    private final List<Long> samples = Lists.newArrayList();
    private long lastAttack;
    private double buffer;

    public KillAuraB(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity i = new WrapperPlayClientInteractEntity(event);
            if (i.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                return;
            }
            final long delay = time() - lastAttack;
            samples.add(delay);
            if (samples.size() == 10) {
                Collections.sort(samples);
                final long range = samples.get(samples.size() - 1) - samples.get(0);
                if (range < 50L) {
                    if (buffer++ > 4) {
                        flagAndAlert(formatOffset(range) + " | " + buffer);
                    }
                } else {
                    buffer = Math.max(0, buffer - 0.45);
                }
                samples.clear();
            }
            lastAttack = time();
        }
    }
}
