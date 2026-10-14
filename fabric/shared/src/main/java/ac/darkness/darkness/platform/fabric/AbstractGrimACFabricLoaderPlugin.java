package ac.darkness.darkness.platform.fabric;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.darkness.darkness.command.CloudCommandService;
import ac.grim.grimac.internal.plugin.resolver.GrimExtensionManager;
import ac.darkness.darkness.platform.api.PlatformLoader;
import ac.darkness.darkness.platform.api.PlatformServer;
import ac.darkness.darkness.platform.api.command.CommandService;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.manager.ItemResetHandler;
import ac.darkness.darkness.platform.api.manager.MessagePlaceHolderManager;
import ac.darkness.darkness.platform.api.manager.PermissionRegistrationManager;
import ac.darkness.darkness.platform.api.manager.PlatformPluginManager;
import ac.darkness.darkness.platform.api.player.PlatformPlayerFactory;
import ac.darkness.darkness.platform.api.scheduler.PlatformScheduler;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.api.sender.SenderFactory;
import ac.darkness.darkness.platform.fabric.manager.FabricMessagePlaceHolderManager;
import ac.darkness.darkness.platform.fabric.manager.FabricPlatformPluginManager;
import ac.darkness.darkness.platform.fabric.resolver.FabricResolverRegistrar;
import ac.darkness.darkness.platform.fabric.utils.convert.IFabricConversionUtil;
import ac.darkness.darkness.platform.fabric.utils.message.IFabricMessageUtil;
import ac.darkness.darkness.utils.anticheat.LogUtil;
import ac.darkness.darkness.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.PacketEventsAPI;
import com.github.retrooper.packetevents.manager.server.ServerVersion;
import lombok.Getter;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.SenderMapper;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.fabric.FabricServerCommandManager;
import org.jetbrains.annotations.NotNull;

public abstract class AbstractGrimACFabricLoaderPlugin<
        P extends PlatformPlayerFactory,
        S extends PlatformServer,
        H extends PlatformScheduler,
        F extends SenderFactory<?>,
        I extends ItemResetHandler,
        A extends CloudPlatformCommandArguments
        > implements PlatformLoader {

    protected final LazyHolder<H> scheduler;
    protected final PacketEventsAPI<?> packetEvents = PacketEvents.getAPI();
    protected final LazyHolder<F> senderFactory;
    protected final LazyHolder<I> itemResetHandler;
    protected final LazyHolder<A> commandArguments;
    protected final LazyHolder<CommandService> commandService = LazyHolder.simple(this::createCommandService);
    protected final LazyHolder<? extends PermissionRegistrationManager> permissionManager;
    protected final GrimPlugin plugin;
    @Getter
    protected final PlatformPluginManager pluginManager = new FabricPlatformPluginManager();
    @Getter
    protected final MessagePlaceHolderManager messagePlaceHolderManager = new FabricMessagePlaceHolderManager();
    protected final P playerFactory;
    protected final S platformServer;
    protected final IFabricMessageUtil fabricMessageUtil;
    @Getter
    protected final IFabricConversionUtil fabricConversionUtil;

    protected AbstractGrimACFabricLoaderPlugin(
            LazyHolder<H> scheduler,
            LazyHolder<F> senderFactory,
            LazyHolder<I> itemResetHandler,
            LazyHolder<A> commandArguments,
            LazyHolder<? extends PermissionRegistrationManager> permissionManager,
            P playerFactory,
            S platformServer,
            IFabricMessageUtil fabricMessageUtil,
            IFabricConversionUtil fabricConversionUtil
    ) {
        this.scheduler = scheduler;
        this.senderFactory = senderFactory;
        this.itemResetHandler = itemResetHandler;
        this.commandArguments = commandArguments;
        this.permissionManager = permissionManager;
        this.playerFactory = playerFactory;
        this.platformServer = platformServer;
        this.fabricMessageUtil = fabricMessageUtil;
        this.fabricConversionUtil = fabricConversionUtil;

        FabricResolverRegistrar resolverRegistrar = new FabricResolverRegistrar();
        GrimExtensionManager extensionManager = GrimAPI.INSTANCE.getExtensionManager();
        resolverRegistrar.registerAll(extensionManager);
        plugin = extensionManager.getPlugin("GrimAC");
    }

    @Override
    public H getScheduler() {
        return scheduler.get();
    }

    @Override
    public PacketEventsAPI<?> getPacketEvents() {
        return packetEvents;
    }

    @Override
    public I getItemResetHandler() {
        return itemResetHandler.get();
    }

    @Override
    public CommandService getCommandService() {
        return commandService.get();
    }

    @Override
    public SenderFactory<?> getSenderFactory() {
        return senderFactory.get();
    }

    @Override
    public GrimPlugin getPlugin() {
        return plugin;
    }

    @Override
    public void registerAPIService() {
        GrimAPIProvider.init(GrimAPI.INSTANCE.getExternalAPI());
    }

    @Override
    public PermissionRegistrationManager getPermissionManager() {
        return permissionManager.get();
    }

    @Override
    public P getPlatformPlayerFactory() {
        return playerFactory;
    }

    @Override
    public S getPlatformServer() {
        return platformServer;
    }

    public IFabricMessageUtil getFabricMessageUtils() {
        return fabricMessageUtil;
    }

    private CommandService createCommandService() {
        try {
            return createPlatformCommandService();
        } catch (Throwable t) {
            LogUtil.warn("IMPORTANT: Command Framework failed to load (Missing Cloud Library?). \n" +
                    "Grim will run without commands enabled!");
            if (!(t instanceof NoClassDefFoundError)) {
                LogUtil.error(t);
            }
            return () -> {};
        }
    }

    protected CommandService createPlatformCommandService() {
        @SuppressWarnings({"rawtypes", "unchecked"})
        CommandManager<@NotNull Sender> manager = new FabricServerCommandManager(
                ExecutionCoordinator.simpleCoordinator(),
                SenderMapper.identity()
        );
        return new CloudCommandService(() -> manager, commandArguments.get());
    }

    public abstract ServerVersion getNativeVersion();
}
