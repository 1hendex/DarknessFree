package ac.darkness.darkness.checks.impl.combat.killaura;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.potion.PotionTypes;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;

@CheckData(name = "KillAuraC", stableKey = "grim.killaura.fall_distance", description = "Checks for perfect fall distance when attack")
public class KillAuraC extends Check implements PacketReceiveListener {
    private double lastJumpY = 0;
    private double buffer = 0;
    private long lastAttack = 0;

    public KillAuraC(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity action = new WrapperPlayClientInteractEntity(event);
            if (action.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                final long now = event.getTimestamp();
                if (now == lastAttack) {
                    return;
                }
                lastAttack = now;
                if (!player.onGround && player.clickProcessor.getCps() < 4) {
                    if (player.verticalCollision || player.wasTouchingWater || player.wasTouchingLava
                            || player.isSwimming || player.isClimbing || player.isGliding || player.isFlying
                            || player.isInWeb || player.gamemode != GameMode.SURVIVAL || player.likelyKB != null
                            || player.inVehicle() || player.compensatedEntities.self.isDead
                            || player.packetStateData.lastPacketWasTeleport || player.packetStateData.tryingToRiptide
                            || player.compensatedEntities.getSlowFallingAmplifier().isPresent()
                            || player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.LEVITATION).isPresent()
                            || player.compensatedEntities.getPotionLevelForPlayer(PotionTypes.JUMP_BOOST).isPresent()
                            || player.stuckSpeedMultiplier.getX() < 0.99 || player.firstBreadKB != null
                            || player.firstBreadExplosion != null || player.likelyExplosions != null) {
                        buffer = 0;
                        lastJumpY = -1;
                        return;
                    }
                    double jumpY = player.y % 1;
                    if (lastJumpY >= 0 && Math.abs(jumpY - lastJumpY) < 1.0E-7 &&
                            jumpY != 0.20000004768371582) {
                        if (++buffer > 15) {
                            flagAndAlert("delta=" + jumpY);
                            buffer = 12;
                        }
                    } else {
                        buffer = buffer / 2;
                    }
                    lastJumpY = jumpY;
                }
            }
        }
    }
}
