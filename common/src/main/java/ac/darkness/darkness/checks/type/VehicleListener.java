package ac.darkness.darkness.checks.type;

import ac.darkness.darkness.utils.anticheat.update.VehiclePositionUpdate;

public interface VehicleListener {
    void process(final VehiclePositionUpdate vehicleUpdate);
}
