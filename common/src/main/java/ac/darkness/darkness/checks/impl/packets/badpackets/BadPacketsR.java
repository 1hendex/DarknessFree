package ac.darkness.darkness.checks.impl.packets.badpackets;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.checks.type.PacketSendListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientKeepAlive;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerKeepAlive;

import java.util.LinkedHashMap;
import java.util.Map;

@CheckData(name = "BadPacketsR", alternativeName = "BadPacketsBB", configName = "BadPacketsR", stableKey = "grim.badpackets.delayed_keepalive", description = "Checks for delayed keepalive packets")
public class BadPacketsR extends Check implements PacketReceiveListener, PacketSendListener {

    private static class KeepAliveData {
        final long id;
        final long timeSent;
        long timeReceived;
        long transReceived;

        KeepAliveData(long id, long timeSent) {
            this.id = id;
            this.timeSent = timeSent;
        }
    }

    private final Map<Long, KeepAliveData> pingMap = new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<Long, KeepAliveData> eldest) {
            return size() > 100;
        }
    };

    public BadPacketsR(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketSend(PacketSendEvent event) {
        if (event.getPacketType() == PacketType.Play.Server.KEEP_ALIVE) {
            long id = new WrapperPlayServerKeepAlive(event).getId();
            final KeepAliveData keepAliveData = new KeepAliveData(id, System.currentTimeMillis());
            pingMap.put(id, keepAliveData);

            // sandwich the keep alive packet
            player.sendTransaction();
            player.addRealTimeTaskNext(() -> {
                final long receivedAt = System.currentTimeMillis();
                keepAliveData.transReceived = receivedAt;
            });
            event.getTasksAfterSend().add(player::sendTransaction);
        }
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.KEEP_ALIVE) {
            long id = new WrapperPlayClientKeepAlive(event).getId();
            final KeepAliveData data = pingMap.get(id);
            if (data != null && data.timeReceived == 0) {
                final long time = System.currentTimeMillis();
                data.timeReceived = time;
                final long diff = time - data.transReceived;

                // some clients when tabbed out won't send transaction packets, so
                // if the player hasn't rotated in the past 20 seconds, ignore them
                final long lastRotated = time - player.lastRotated;

                if (diff > 25 && lastRotated < 20000 && data.transReceived != 0) {
                    flag("diff=" + diff + "ms, lr=" + lastRotated + "ms");
                }
            }
        }
    }
}
