package ac.darkness.darkness.platform.fabric.initables;

import ac.darkness.darkness.manager.init.OptionalReflectiveInitable;
import net.fabricmc.loader.api.FabricLoader;

public final class FabricLuckPermsInitable extends OptionalReflectiveInitable {
    private static final String LUCKPERMS_MOD_ID = "luckperms";
    private static final String HANDLER_CLASS =
            "ac.darkness.darkness.platform.fabric.initables.FabricLuckPermsHandler";

    public FabricLuckPermsInitable() {
        super(HANDLER_CLASS, "Error when initializing LuckPerms hook");
    }

    @Override
    protected boolean isAvailable() {
        return FabricLoader.getInstance().isModLoaded(LUCKPERMS_MOD_ID);
    }
}
