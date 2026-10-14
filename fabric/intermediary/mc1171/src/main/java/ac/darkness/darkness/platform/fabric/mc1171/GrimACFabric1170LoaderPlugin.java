package ac.darkness.darkness.platform.fabric.mc1171;

import ac.darkness.darkness.platform.fabric.AbstractFabricPlatformServer;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.fabric.GrimACFabricIntermediaryLoaderPlugin;
import ac.darkness.darkness.platform.fabric.mc1171.player.Fabric1170PlatformPlayer;
import ac.darkness.darkness.platform.fabric.mc1161.Fabric1140PlatformServer;
import ac.darkness.darkness.platform.fabric.mc1161.player.Fabric1161PlatformInventory;
import ac.darkness.darkness.platform.fabric.mc1171.entity.Fabric1170GrimEntity;
import ac.darkness.darkness.platform.fabric.mc1161.util.convert.Fabric1140ConversionUtil;
import ac.darkness.darkness.platform.fabric.mc1161.util.convert.Fabric1161MessageUtil;
import ac.darkness.darkness.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.darkness.darkness.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.darkness.darkness.platform.fabric.utils.message.IFabricMessageUtil;
import ac.darkness.darkness.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;


public class GrimACFabric1170LoaderPlugin extends GrimACFabricIntermediaryLoaderPlugin {

    public GrimACFabric1170LoaderPlugin() {
        this(GrimACFabricIntermediaryLoaderPlugin::createCommandArguments,
                new FabricPlatformPlayerFactory(
                        Fabric1170PlatformPlayer::new,
                        Fabric1170GrimEntity::new,
                        Fabric1161PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_17)
                        ? new Fabric1171PlatformServer() : new Fabric1140PlatformServer(),
                new Fabric1161MessageUtil(),
                new Fabric1140ConversionUtil()
        );
    }

    protected GrimACFabric1170LoaderPlugin(LazyHolder<CloudPlatformCommandArguments> commandArguments,
                                           FabricPlatformPlayerFactory playerFactory,
                                           AbstractFabricPlatformServer platformServer,
                                           IFabricMessageUtil fabricMessageUtil,
                                           IFabricConversionUtil fabricConversionUtil) {
        super(
                commandArguments,
                playerFactory,
                platformServer,
                fabricMessageUtil,
                fabricConversionUtil
        );
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_17_1;
    }
}
