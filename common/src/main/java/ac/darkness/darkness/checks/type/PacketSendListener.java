package ac.darkness.darkness.checks.type;

import com.github.retrooper.packetevents.event.PacketSendEvent;

public interface PacketSendListener {
    void onPacketSend(PacketSendEvent event);
}
