package ac.darkness.darkness.checks.impl.movement.vehicle;

import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckData;
import ac.darkness.darkness.player.GrimPlayer;

@CheckData(name = "VehicleC", stableKey = "grim.vehicle.vehicle_control", description = "Moved a vehicle in a way that did not match predicted vehicle control")
public class VehicleC extends Check {
    public VehicleC(GrimPlayer player) {
        super(player);
    }
}
