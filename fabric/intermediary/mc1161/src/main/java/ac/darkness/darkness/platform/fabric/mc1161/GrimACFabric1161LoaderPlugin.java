package ac.darkness.darkness.platform.fabric.mc1161;

import ac.darkness.darkness.platform.fabric.AbstractFabricPlatformServer;
import ac.darkness.darkness.platform.fabric.GrimACFabricIntermediaryLoaderPlugin;
import ac.darkness.darkness.platform.fabric.mc1161.entity.Fabric1161GrimEntity;
import ac.darkness.darkness.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.darkness.darkness.platform.fabric.mc1161.player.Fabric1161PlatformPlayer;
import ac.darkness.darkness.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.darkness.darkness.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.darkness.darkness.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.darkness.darkness.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.darkness.darkness.platform.fabric.utils.message.IFabricMessageUtil;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric1161LoaderPlugin extends GrimACFabricIntermediaryLoaderPlugin {

    public GrimACFabric1161LoaderPlugin() {
        this(
            new FabricPlatformPlayerFactory(
                Fabric1161PlatformPlayer::new,
                Fabric1161GrimEntity::new,
                Fabric1161PlatformInventory::new
            ),
            new Fabric1140PlatformServer(),
            new Fabric1161MessageUtil(),
            new Fabric1140ConversionUtil()
        );
    }

    protected GrimACFabric1161LoaderPlugin(
            FabricPlatformPlayerFactory playerFactory,
            AbstractFabricPlatformServer platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil
    ) {
        super(GrimACFabricIntermediaryLoaderPlugin::createCommandArguments,
            playerFactory,
            platformServer,
            fabricMessageUtil,
            fabricConversionUtil
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_16_1;
    }
}
