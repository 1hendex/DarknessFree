package ac.darkness.darkness.utils.data.packetentity;

import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.data.VectorData;

import java.util.Set;

public interface JumpableEntity {

    boolean isJumping();

    void setJumping(boolean jumping);

    float getJumpPower();

    void setJumpPower(float jumpPower);

    boolean canPlayerJump(GrimPlayer player);

    boolean hasSaddle();

    void executeJump(GrimPlayer player, Set<VectorData> possibleVectors);

}
