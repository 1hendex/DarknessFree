package ac.darkness.darkness.utils.data;

import ac.darkness.darkness.utils.math.Vector3dm;

public class HitData {
    Vector3dm blockHitLocation;

    public HitData(Vector3dm blockHitLocation) {
        this.blockHitLocation = blockHitLocation;
    }

    public Vector3dm getBlockHitLocation() {
        return this.blockHitLocation;
    }
}
