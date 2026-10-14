package ac.darkness.darkness.predictionengine.movementtick;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.predictionengine.predictions.input.Input;
import ac.darkness.darkness.utils.data.packetentity.PacketEntityRideable;
import com.github.retrooper.packetevents.protocol.attribute.Attributes;

public class MovementTickerPig extends MovementTickerRideable {
    public MovementTickerPig(GrimPlayer player) {
        super(player);
        this.movementInput = Input.createInput(player, 0, 0, 1);
    }

    @Override
    public float getSteeringSpeed() { // Vanilla multiples by 0.225f
        PacketEntityRideable pig = (PacketEntityRideable) player.compensatedEntities.self.getRiding();
        return (float) pig.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225f;
    }
}
