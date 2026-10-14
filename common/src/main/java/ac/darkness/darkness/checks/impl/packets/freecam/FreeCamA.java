package ac.darkness.darkness.checks.impl.packets.freecam;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;

@CheckData(name = "FreeCamA", alternativeName = "BadPacketsR", configName = "FreeCamA", stableKey = "grim.freecam.position_starvation", description = "Stopped sending position updates while still responding to transactions", decay = 0.25)
public class FreeCamA extends Check implements PacketReceiveListener {
    private int positions = 0;
    private long clock = 0;
    private long lastTransTime;
    private int oldTransId = 0;

    public FreeCamA(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(final PacketReceiveEvent event) {
        if (isTransaction(event.getPacketType()) && player.packetStateData.lastTransactionPacketWasValid) {
            long ms = (player.getPlayerClockAtLeast() - clock) / 1000000L;
            long diff = (System.currentTimeMillis() - lastTransTime);
            if (diff > 2000 && ms > 2000) {
                if (positions == 0 && clock != 0 && player.cameraEntity.isSelf() && !player.compensatedEntities.self.isDead) {
                    flag("time=" + ms + "ms, lst=" + diff + "ms, positions=" + positions);
                } else {
                    reward();
                }
                player.compensatedEntities.entitiesRemovedThisTick.clear();
                player.compensatedWorld.removeInvalidPistonLikeStuff(oldTransId);
                positions = 0;
                clock = player.getPlayerClockAtLeast();
                lastTransTime = System.currentTimeMillis();
                oldTransId = player.lastTransactionSent.get();
            }
        }

        if ((event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION ||
                event.getPacketType() == PacketType.Play.Client.PLAYER_POSITION) && !player.inVehicle()) {
            positions++;
        } else if ((event.getPacketType() == PacketType.Play.Client.STEER_VEHICLE || event.getPacketType() == PacketType.Play.Client.VEHICLE_MOVE)
                && player.inVehicle()) {
            positions++;
        }
    }
}
