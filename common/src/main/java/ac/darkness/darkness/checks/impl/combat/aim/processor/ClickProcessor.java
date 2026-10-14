package ac.darkness.darkness.checks.impl.combat.aim.processor;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Queue;

public class ClickProcessor extends GrimProcessor implements PacketReceiveListener {
    private final Queue<Long> cps = new ArrayDeque<>();
    private long lastTime = 0L;
    @Getter private long delay;
    @Getter private boolean digging;

    public ClickProcessor(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (event.getPacketType() == PacketType.Play.Client.PLAYER_DIGGING) {
            WrapperPlayClientPlayerDigging wrapper = new WrapperPlayClientPlayerDigging(event);
            switch (wrapper.getAction()) {
                case START_DIGGING:
                    this.digging = true;
                    break;
                case CANCELLED_DIGGING:
                case FINISHED_DIGGING:
                case RELEASE_USE_ITEM:
                case DROP_ITEM:
                case DROP_ITEM_STACK:
                case SWAP_ITEM_WITH_OFFHAND:
                    this.digging = false;
                    break;
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.HELD_ITEM_CHANGE) {
            this.digging = false;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
            if (wrapper.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                this.digging = false;
            }
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.ATTACK) {
            this.digging = false;
            return;
        }

        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            final long now = System.currentTimeMillis();

            this.delay = now - this.lastTime;
            this.lastTime = now;

            if (!digging && !player.protocolProcessor.isDropping() && (now - player.getLastHit()) < 1000L) {
                cps.add(now);
            }
        }
    }

    public int getCps() {
        final long now = System.currentTimeMillis();
        while (!cps.isEmpty() && (now - cps.peek()) > 1000L) {
            cps.poll();
        }
        return cps.size();
    }

    public void hardReset() {
        cps.clear();
        digging = false;
        lastTime = 0L;
        delay = 0L;
    }
}
