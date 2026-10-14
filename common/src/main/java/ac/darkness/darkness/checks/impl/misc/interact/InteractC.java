package ac.darkness.darkness.checks.impl.misc.interact;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.lists.EvictingQueue;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "InteractC", alternativeName = "FastPlace", configName = "InteractC", stableKey = "darkness.interact.fast_place", description = "Checks for fast place/use")
public class InteractC extends Check implements PacketReceiveListener {
    private final EvictingQueue<Long> samples = new EvictingQueue<>(15);
    private long lastTime = 0, lastDelta = 0;

    public InteractC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_BLOCK_PLACEMENT || event.getPacketType() == PacketType.Play.Client.USE_ITEM) {
            final long time = event.getTimestamp();
            final long delta = time - lastTime;
            if (delta > 30 && delta != lastDelta && delta < 5000) {
                samples.add(delta);
            }
            lastTime = time;
            lastDelta = delta;
            if (samples.size() != 15) {
                return;
            }
            long sum = 0L;
            for (long s : samples) {
                sum += s;
            }
            final long avg = samples.isEmpty() ? 0L : sum / samples.size();
            flagIf(avg <= 50, "delta=" + delta);
            if (delta > 1000) samples.clear();
        }
    }
}
