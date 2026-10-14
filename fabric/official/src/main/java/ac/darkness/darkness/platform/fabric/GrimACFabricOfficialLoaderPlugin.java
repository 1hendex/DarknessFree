package ac.darkness.darkness.platform.fabric;

import ac.darkness.darkness.platform.fabric.manager.FabricItemResetHandler;
import ac.darkness.darkness.platform.fabric.manager.FabricCloudPlatformCommandArguments;
import ac.darkness.darkness.platform.fabric.manager.FabricPermissionRegistrationManager;
import ac.darkness.darkness.platform.fabric.command.FabricPlayerSelectorParser;
import ac.darkness.darkness.platform.fabric.player.FabricPlatformPlayerFactory;
import ac.darkness.darkness.platform.fabric.scheduler.FabricPlatformScheduler;
import ac.darkness.darkness.platform.fabric.sender.AbstractFabricSenderFactory;
import ac.darkness.darkness.platform.fabric.sender.FabricOfficialSenderFactory;
import me.lucko.fabric.api.permissions.v0.Permissions;
import ac.darkness.darkness.platform.fabric.utils.FabricOfficialPolymerHook;
import ac.darkness.darkness.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.darkness.darkness.platform.fabric.utils.message.IFabricMessageUtil;
import ac.darkness.darkness.utils.lazy.LazyHolder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public abstract class GrimACFabricOfficialLoaderPlugin extends AbstractGrimACFabricLoaderPlugin<
        FabricPlatformPlayerFactory,
        AbstractFabricPlatformServer,
        FabricPlatformScheduler,
        FabricOfficialSenderFactory,
        FabricItemResetHandler,
        FabricCloudPlatformCommandArguments
        > {
    public static MinecraftServer FABRIC_SERVER;
    public static GrimACFabricOfficialLoaderPlugin LOADER;

    public GrimACFabricOfficialLoaderPlugin(
            FabricPlatformPlayerFactory playerFactory,
            AbstractFabricPlatformServer platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil
    ) {
        super(
                LazyHolder.simple(FabricPlatformScheduler::new),
                LazyHolder.simple(FabricOfficialSenderFactory::new),
                LazyHolder.simple(() -> new FabricItemResetHandler(fabricConversionUtil)),
                LazyHolder.simple(GrimACFabricOfficialLoaderPlugin::createCommandArguments),
                LazyHolder.simple(() -> new FabricPermissionRegistrationManager(
                        LOADER.getFabricSenderFactory(),
                        name -> {
                            if (AbstractFabricSenderFactory.HAS_PERMISSIONS_API) {
                                Permissions.check(FABRIC_SERVER.createCommandSourceStack(), name);
                            }
                        })),
                playerFactory,
                platformServer,
                fabricMessageUtil,
                fabricConversionUtil
        );
        FabricPlatformServices.configure(
                playerFactory::getPlatformInventory,
                playerFactory::getPlatformEntity,
                player -> FabricOfficialPolymerHook.createTranslator((ServerPlayer) player),
                fabricMessageUtil::textLiteral,
                platformServer::getProfileByName,
                fabricConversionUtil
        );
    }

    public FabricOfficialSenderFactory getFabricSenderFactory() {
        return senderFactory.get();
    }

    public static FabricCloudPlatformCommandArguments createCommandArguments() {
        return new FabricCloudPlatformCommandArguments(new FabricPlayerSelectorParser<>(
                selector -> LOADER.getFabricSenderFactory().wrap(selector.single().createCommandSourceStack()),
                selector -> selector.inputString()
        ));
    }

}
