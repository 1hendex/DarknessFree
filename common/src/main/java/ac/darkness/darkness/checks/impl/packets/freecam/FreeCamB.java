package ac.darkness.darkness.checks.impl.packets.freecam;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;

@CheckData(name = "FreeCamB", alternativeName = "BadPacketsE", configName = "FreeCamB", stableKey = "grim.freecam.invalid_position", description = "Sent too many movement packets without updating position")
public class FreeCamB extends Check implements PacketReceiveListener {
    private int noReminderTicks;
    private final int maxNoReminderTicks = player.getClientVersion().isOlderThanOrEquals(ClientVersion.V_1_8) ? 20 : 19;
    private final boolean isViaPleaseStopUsingProtocolHacksOnYourServer = player.getClientVersion().isNewerThanOrEquals(ClientVersion.V_1_21_2) || PacketEvents.getAPI().getServerManager().getVersion().isNewerThanOrEquals(ServerVersion.V_1_21_2);

    public FreeCamB(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION ||
                event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) {
            noReminderTicks = 0;
        } else if (WrapperPlayClientPlayerFlying.isFlying(event.getPacketType()) && !player.packetStateData.lastPacketWasTeleport) {
            if (++noReminderTicks > maxNoReminderTicks) {
                flag("ticks=" + noReminderTicks);
            }
        } else if (event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE
                || (isViaPleaseStopUsingProtocolHacksOnYourServer && player.inVehicle())) {
            noReminderTicks = 0; // Exempt vehicles
        }
    }

    public void handleRespawn() {
        noReminderTicks = 0;
    }
}
