package ac.darkness.darkness.checks.impl.packets.protocol;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "ProtocolB", alternativeName = "TransactionOrder", configName = "ProtocolB", stableKey = "grim.protocol.transaction_order", description = "Sent transaction or ping responses in an invalid order")
public class ProtocolB extends Check {
    public ProtocolB(GrimPlayer player) {
        super(player);
    }
}
