package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.checks.type.PacketReceiveListener;
import ac.darkness.darkness.player.GrimPlayer;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.item.type.ItemType;
import com.github.retrooper.packetevents.protocol.item.type.ItemTypes;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.ClientVersion;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientUseItem;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@CheckData(name = "ProtocolT", stableKey = "grim.protocol.t", description = "Checks for invalid swing while use item packet")
public class ProtocolT extends Check implements PacketReceiveListener {
    private boolean isCheckActive = false;
    private ItemType usedItemType;
    private int lastCooldown;
    private InteractionHand hand = InteractionHand.MAIN_HAND;
    private int buffer;
    private static final Set<ItemType> THROWABLE = new HashSet<>(Arrays.asList(
            ItemTypes.EGG,
            ItemTypes.SNOWBALL,
            ItemTypes.WIND_CHARGE,
            ItemTypes.SPLASH_POTION,
            ItemTypes.FISHING_ROD,
            ItemTypes.EXPERIENCE_BOTTLE,
            ItemTypes.LINGERING_POTION
    ));

    public ProtocolT(final GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if (player.getClientVersion().isOlderThan(ClientVersion.V_1_15)) return;
        PacketTypeCommon type = event.getPacketType();
        if (type == PacketType.Play.Client.USE_ITEM) {
            WrapperPlayClientUseItem wrapper = new WrapperPlayClientUseItem(event);
            ItemStack itemStack = player.inventory.getItemInHand(wrapper.getHand());
            boolean hasCooldown = player.checkManager.getCompensatedCooldown().hasItem(itemStack);
            if (hasCooldown) {
                lastCooldown = 20;
                return;
            }
            if (isThrowable(itemStack.getType()) && !event.isCancelled() && lastCooldown == 0) {
                isCheckActive = true;
                usedItemType = itemStack.getType();
                hand = wrapper.getHand();
            } else {
                isCheckActive = false;
            }

            return;
        }

        if (type == PacketType.Play.Client.ANIMATION && new WrapperPlayClientAnimation(event).getHand() == hand) {
            isCheckActive = false;
            buffer = 0;
            return;
        }

        if (isTickPacket(type)) {
            if (lastCooldown > 0) {
                lastCooldown--;
                isCheckActive = false;
                return;
            }
            if (isCheckActive && ++buffer > 0) {
                flagAndAlert("item=" + usedItemType.getName().getKey());
                isCheckActive = false;
            }
        }
    }

    private boolean isThrowable(ItemType type) {
        if (THROWABLE.contains(type)) return true;

        return type == ItemTypes.FIREWORK_ROCKET && player.isGliding;
    }
}
