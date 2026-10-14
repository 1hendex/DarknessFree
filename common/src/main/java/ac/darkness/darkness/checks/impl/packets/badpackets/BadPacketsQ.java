package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.grim.grimac.api.config.ConfigManager;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientHeldItemChange;

import java.util.ArrayDeque;
import java.util.Deque;

@CheckData(name = "BadPacketsQ", alternativeName = "AutoActionU", configName = "BadPacketsQ", stableKey = "grim.badpackets.slot_swap_on_use", description = "Checks for invalid slot swap on item use")
public class BadPacketsQ extends Check implements PacketReceiveListener {
    private int currentSlot = -1;
    private int lastSlot = -1;
    private long lastSlotChangeTime;
    private long lastUseItemTime;
    private boolean useItem;
    private final Deque<Long> samples = new ArrayDeque<>(2);
    private long maxDelay = 75L;

    public BadPacketsQ(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            final WrapperPlayClientHeldItemChange wrapper = new WrapperPlayClientHeldItemChange(event);
            final int slot = wrapper.getSlot();
            final long now = System.currentTimeMillis();
            if (useItem) {
                final long delay = now - lastUseItemTime;
                if (!legit(lastSlot, slot) && lastSlot != -1 && slot == lastSlot && delay <= maxDelay) {
                    if (samples.size() >= 2) {
                        samples.pollFirst();
                    }
                    samples.addLast(delay);
                    if (samples.size() >= 2) {
                        long d1 = samples.peekFirst();
                        long d2 = samples.peekLast();
                        final double avg = (d1 + d2) / 2.0;
                        final double max = Math.abs(d1 - d2) / 2.0;
                        if (avg <= maxDelay && max < 10.0) {
                            flag("avg=" + avg + " | max=" + max);
                        }
                    }
                } else {
                    samples.clear();
                }
                useItem = false;
            }
            if (currentSlot != -1 && currentSlot != slot) {
                lastSlot = currentSlot;
            }
            currentSlot = slot;
            lastSlotChangeTime = now;
            return;
        }
        if (event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            final long now = System.currentTimeMillis();
            if (currentSlot != -1 && lastSlot != -1 && currentSlot != lastSlot && (now - lastSlotChangeTime <= maxDelay)) {
                lastUseItemTime = now;
                useItem = true;
            } else {
                useItem = false;
            }
        }
    }

    private static boolean legit(final int oldSlot, final int newSlot) {
        return (oldSlot == 0 && newSlot == 8) || (oldSlot == 8 && newSlot == 0);
    }

    @Override
    public void onReload(final ConfigManager config) {
        maxDelay = config.getLongElse(getConfigName() + ".max_use_delay", 75L);
    }
}
