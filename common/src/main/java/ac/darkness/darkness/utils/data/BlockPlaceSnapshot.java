package ac.darkness.darkness.utils.data;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;

public record BlockPlaceSnapshot(PacketWrapper<?> wrapper, boolean sneaking) {}
