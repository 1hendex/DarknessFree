package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.lists.EvictingList;
import ac.darkness.darkness.utils.math.GrimMath;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity.InteractAction;

@CheckData(name = "KillAuraD", stableKey = "grim.killaura.range_diff", description = "Checks for repeated range differences")
public class KillAuraD extends Check implements PacketReceiveListener {
    private int ticks = 0;
    private final EvictingList<Integer> samples = new EvictingList<>(10);

    public KillAuraD(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == InteractAction.ATTACK) {
                if (ticks > 10 && ticks < 40) {
                    samples.add(ticks);
                    if (samples.isFull()) {
                        int dup = 0;
                        double std = GrimMath.stdInt(samples);
                        for (int i = 1; i < samples.size(); i++) {
                            if (samples.get(i).equals(samples.get(i - 1))) {
                                dup++;
                            }
                        }
                        if (dup >= 8 && std < 0.1) {
                            if (++violations > 2) {
                                flagAndAlert("Type: A | " + dup + " | " + std);
                            }
                        } else {
                            violations = Math.max(0, violations - 0.5);
                        }
                        samples.clear();
                    }
                }
                ticks = 0;
            }
        } else if (isFlying(event.getPacketType())) {
            ++ticks;
        }
    }
}
