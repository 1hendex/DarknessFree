package ac.darkness.darkness.platform.fabric.mc1194.player;

import ac.darkness.darkness.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.darkness.darkness.platform.fabric.player.AbstractFabricPlatformPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.MenuType;

public class Fabric1193PlatformInventory extends Fabric1161PlatformInventory {
    public Fabric1193PlatformInventory(AbstractFabricPlatformPlayer player) {
        super(player);
    }

    @Override
    protected Object getScreenID(MenuType<?> type) {
        return BuiltInRegistries.MENU.getKey(type);
    }
}
