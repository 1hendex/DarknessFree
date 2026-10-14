package ac.darkness.darkness.platform.fabric.mc1216;

import ac.darkness.darkness.platform.fabric.command.FabricPlayerSelectorParser;
import ac.darkness.darkness.platform.fabric.manager.FabricCloudPlatformCommandArguments;
import ac.darkness.darkness.platform.fabric.mc1194.GrimACFabric1190LoaderPlugin;
import ac.darkness.darkness.platform.fabric.mc1194.entity.Fabric1194GrimEntity;
import ac.darkness.darkness.platform.fabric.mc1194.player.Fabric1193PlatformInventory;
import ac.darkness.darkness.platform.fabric.mc1205.Fabric1203PlatformServer;
import ac.darkness.darkness.platform.fabric.mc1205.convert.Fabric1200MessageUtil;
import ac.darkness.darkness.platform.fabric.mc1205.convert.Fabric1205ConversionUtil;
import ac.darkness.darkness.platform.fabric.mc1216.convert.Fabric1216ConversionUtil;
import ac.darkness.darkness.platform.fabric.mc1216.player.Fabric1212PlatformPlayer;
import ac.darkness.darkness.platform.fabric.mc1216.player.Fabric1215PlatformInventory;
import ac.darkness.darkness.platform.fabric.player.FabricPlatformPlayerFactory;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.manager.server.ServerVersion;

public class GrimACFabric1212LoaderPlugin extends GrimACFabric1190LoaderPlugin {

    public GrimACFabric1212LoaderPlugin() {
        super(
                GrimACFabric1212LoaderPlugin::createCommandArguments,
                new FabricPlatformPlayerFactory(
                        Fabric1212PlatformPlayer::new,
                        Fabric1194GrimEntity::new,
                        PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_4)
                            ? Fabric1215PlatformInventory::new : Fabric1193PlatformInventory::new
                ),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_10) ?
                        new Fabric12111PlatformServer() : new Fabric1203PlatformServer(),
                new Fabric1200MessageUtil(),
                PacketEvents.getAPI().getServerManager().getVersion().isNewerThan(ServerVersion.V_1_21_5)
                        ? new Fabric1216ConversionUtil() : new Fabric1205ConversionUtil()
        );
    }

    public static FabricCloudPlatformCommandArguments createCommandArguments() {
        return new FabricCloudPlatformCommandArguments(new FabricPlayerSelectorParser<>(
                selector -> LOADER.getFabricSenderFactory().wrap(selector.single().createCommandSourceStack()),
                selector -> selector.inputString()
        ));
    }

    @Override
    public ServerVersion getNativeVersion() {
        return ServerVersion.V_1_21_11;
    }
}
