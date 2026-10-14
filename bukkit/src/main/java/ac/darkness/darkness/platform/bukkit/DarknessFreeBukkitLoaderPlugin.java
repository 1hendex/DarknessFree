package ac.darkness.darkness.platform.bukkit;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.GrimExternalAPI;
import ac.grim.grimac.api.GrimAPIProvider;
import ac.grim.grimac.api.GrimAbstractAPI;
import ac.grim.grimac.api.event.EventBus;
import ac.grim.grimac.api.plugin.GrimPlugin;
import ac.darkness.darkness.command.CloudCommandService;
import ac.grim.grimac.internal.platform.bukkit.resolver.BukkitResolverRegistrar;
import ac.darkness.darkness.manager.init.Initable;
import ac.darkness.darkness.manager.init.start.ExemptOnlinePlayersOnReload;
import ac.darkness.darkness.manager.init.start.StartableInitable;
import ac.darkness.darkness.platform.api.Platform;
import ac.darkness.darkness.platform.api.PlatformLoader;
import ac.darkness.darkness.platform.api.PlatformServer;
import ac.darkness.darkness.platform.api.command.CommandService;
import ac.darkness.darkness.platform.api.manager.ItemResetHandler;
import ac.darkness.darkness.platform.api.manager.MessagePlaceHolderManager;
import ac.darkness.darkness.platform.api.manager.PlatformPluginManager;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.player.PlatformPlayerFactory;
import ac.darkness.darkness.platform.api.scheduler.PlatformScheduler;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.api.sender.SenderFactory;
import ac.darkness.darkness.platform.bukkit.initables.BukkitBStats;
import ac.darkness.darkness.platform.bukkit.initables.BukkitEventManager;
import ac.darkness.darkness.platform.bukkit.initables.BukkitLuckPermsInitable;
import ac.darkness.darkness.platform.bukkit.initables.BukkitTickEndEvent;
import ac.darkness.darkness.platform.bukkit.manager.BukkitItemResetHandler;
import ac.darkness.darkness.platform.bukkit.manager.BukkitMessagePlaceHolderManager;
import ac.darkness.darkness.platform.bukkit.manager.BukkitCloudPlatformCommandArguments;
import ac.darkness.darkness.platform.bukkit.manager.BukkitPermissionRegistrationManager;
import ac.darkness.darkness.platform.bukkit.manager.BukkitPlatformPluginManager;
import ac.darkness.darkness.platform.bukkit.player.BukkitPlatformPlayerFactory;
import ac.darkness.darkness.platform.bukkit.scheduler.bukkit.BukkitPlatformScheduler;
import ac.darkness.darkness.platform.bukkit.scheduler.folia.FoliaPlatformScheduler;
import ac.darkness.darkness.platform.bukkit.sender.BukkitSenderFactory;
import ac.darkness.darkness.platform.bukkit.utils.placeholder.PlaceholderAPIExpansion;
import ac.darkness.darkness.utils.anticheat.LogUtil;
import ac.darkness.darkness.utils.lazy.LazyHolder;
import com.github.retrooper.packetevents.PacketEventsAPI;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.brigadier.BrigadierSetting;
import org.incendo.cloud.brigadier.CloudBrigadierManager;
import org.incendo.cloud.bukkit.CloudBukkitCapabilities;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.paper.LegacyPaperCommandManager;

public final class DarknessFreeBukkitLoaderPlugin extends JavaPlugin implements PlatformLoader {
    public static DarknessFreeBukkitLoaderPlugin LOADER;

    private final LazyHolder<PlatformScheduler> scheduler = LazyHolder.simple(this::createScheduler);
    private final LazyHolder<PacketEventsAPI<?>> packetEvents = LazyHolder.simple(() -> SpigotPacketEventsBuilder.build(this));
    private final LazyHolder<BukkitSenderFactory> senderFactory = LazyHolder.simple(BukkitSenderFactory::new);
    private final LazyHolder<ItemResetHandler> itemResetHandler = LazyHolder.simple(BukkitItemResetHandler::new);
    private final LazyHolder<CommandService> commandService = LazyHolder.simple(this::createCommandService);
    private final CloudPlatformCommandArguments commandArguments = new BukkitCloudPlatformCommandArguments();

    @Getter private final PlatformPlayerFactory platformPlayerFactory = new BukkitPlatformPlayerFactory();
    @Getter private final PlatformPluginManager pluginManager = new BukkitPlatformPluginManager();
    @Getter private final GrimPlugin plugin;
    @Getter private final PlatformServer platformServer = new BukkitPlatformServer();
    @Getter private final MessagePlaceHolderManager messagePlaceHolderManager = new BukkitMessagePlaceHolderManager();
    @Getter private final BukkitPermissionRegistrationManager permissionManager = new BukkitPermissionRegistrationManager();

    public DarknessFreeBukkitLoaderPlugin() {
        BukkitResolverRegistrar registrar = new BukkitResolverRegistrar();
        registrar.registerAll(GrimAPI.INSTANCE.getExtensionManager());
        this.plugin = registrar.resolvePlugin(this);
    }

    @Override
    public void onLoad() {
        LOADER = this;
        GrimAPI.INSTANCE.load(this, this.getBukkitInitTasks());
    }

    private Initable[] getBukkitInitTasks() {
        return new Initable[] {
                new ExemptOnlinePlayersOnReload(),
                new BukkitEventManager(),
                new BukkitTickEndEvent(),
                new BukkitBStats(),
                new BukkitLuckPermsInitable(),
                (StartableInitable) () -> {
                    if (BukkitMessagePlaceHolderManager.hasPlaceholderAPI) {
                        new PlaceholderAPIExpansion().register();
                    }
                }
        };
    }

    private static final String RED = "\u001B[91m";
    private static final String RESET = "\u001B[0m";

    @Override
    public void onEnable() {
        String core = Bukkit.getServer().getName();
        String ver;
        try {
            String[] split = Bukkit.getServer().getVersion().split("MC: ");
            ver = split.length > 1 ? split[1].replace(")", "") : Bukkit.getBukkitVersion();
        } catch (Throwable t) {
            ver = Bukkit.getBukkitVersion();
        }
        String authors;
        try {
            authors = String.join(" x ", GrimAPI.INSTANCE.getGrimPlugin().getDescription().getAuthors());
        } catch (Throwable t) {
            authors = "1hendex, clopdisabler";
        }
        LogUtil.info("   ____             _                        ");
        LogUtil.info("   |  _ \\  __ _ _ __| | ___ __   ___  ___ ___ ");
        LogUtil.info("   | | | |/ _` | '__| |/ / '_ \\ / _ \\/ __/ __|");
        LogUtil.info("   | |_| | (_| | |  |   <| | | |  __/\\__ \\__ \\ ");
        LogUtil.info("   |____/ \\__,_|_|  |_|\\_\\_| |_|\\___||___/___/ ");
        LogUtil.info("");
        LogUtil.info(RED + "       Running on " + core + " (MC: " + ver + ")" + RESET);
        LogUtil.info("");
        LogUtil.info(RED + "        Authors: " + authors + RESET);
        LogUtil.info(" ");
        LogUtil.warn("[WARN] You are running a free build of Darkness Liquidation!");
        LogUtil.warn("[WARN] Purchase the full version at https://darkness.rip");
        LogUtil.warn("[WARN] Made with ❤ by 1hendex");
        LogUtil.info(" ");
        GrimAPI.INSTANCE.start();
    }

    @Override
    public void onDisable() {
        GrimAPI.INSTANCE.stop();
    }

    @Override
    public PlatformScheduler getScheduler() {
        return scheduler.get();
    }

    @Override
    public PacketEventsAPI<?> getPacketEvents() {
        return packetEvents.get();
    }

    @Override
    public ItemResetHandler getItemResetHandler() {
        return itemResetHandler.get();
    }

    @Override
    public CommandService getCommandService() {
        return commandService.get();
    }

    @Override
    public SenderFactory<CommandSender> getSenderFactory() {
        return senderFactory.get();
    }

    @Override
    @SuppressWarnings("removal")
    public void registerAPIService() {
        final GrimExternalAPI externalAPI = GrimAPI.INSTANCE.getExternalAPI();
        final EventBus eventBus = externalAPI.getEventBus();
        final ac.grim.grimac.api.plugin.GrimPlugin plugin = GrimAPI.INSTANCE.getGrimPlugin();

        // Bridge Grim events → legacy Bukkit Event API so pre-1.3 plugins that
        // listened for ac.grim.grimac.api.events.* Bukkit events keep working.
        // Typed channel subscriptions here are plugin-bound so they go away if
        // GrimAC itself is disabled.

        eventBus.get(ac.grim.grimac.api.event.events.GrimJoinEvent.class).onJoin(plugin, (user) -> {
            Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimJoinEvent(user));
        });

        eventBus.get(ac.grim.grimac.api.event.events.GrimQuitEvent.class).onQuit(plugin, (user) -> {
            Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimQuitEvent(user));
        });

        eventBus.get(ac.grim.grimac.api.event.events.GrimReloadEvent.class).onReload(plugin, (success) -> {
            Bukkit.getPluginManager().callEvent(new ac.grim.grimac.api.events.GrimReloadEvent(success));
        });

        eventBus.subscribe(plugin, ac.grim.grimac.api.event.events.FlagEvent.class, event -> {
            ac.grim.grimac.api.events.FlagEvent bukkitEvent =
                    new ac.grim.grimac.api.events.FlagEvent(event.getUser(), event.getCheck(), event::getVerbose);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            event.setCancelled(event.isCancelled() || bukkitEvent.isCancelled());
        }, 0, false, DarknessFreeBukkitLoaderPlugin.class);

        eventBus.get(ac.grim.grimac.api.event.events.CommandExecuteEvent.class).onCommandExecuteSupplier(plugin, (user, check, verbose, command, cancelled) -> {
            ac.grim.grimac.api.events.CommandExecuteEvent bukkitEvent =
                    new ac.grim.grimac.api.events.CommandExecuteEvent(user, check, verbose, command);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            return cancelled || bukkitEvent.isCancelled();
        });

        eventBus.get(ac.grim.grimac.api.event.events.CompletePredictionEvent.class).onCompletePrediction(plugin, (user, check, offset, cancelled) -> {
            // Legacy Bukkit event has a verbose field that the new channel event does not; pass empty.
            ac.grim.grimac.api.events.CompletePredictionEvent bukkitEvent =
                    new ac.grim.grimac.api.events.CompletePredictionEvent(user, check, "", offset);
            Bukkit.getPluginManager().callEvent(bukkitEvent);
            return cancelled || bukkitEvent.isCancelled();
        });

        GrimAPIProvider.init(externalAPI);
        Bukkit.getServicesManager().register(GrimAbstractAPI.class, externalAPI, this, ServicePriority.Normal);
    }

    private PlatformScheduler createScheduler() {
        return GrimAPI.INSTANCE.getPlatform() == Platform.FOLIA ? new FoliaPlatformScheduler() : new BukkitPlatformScheduler();
    }

    private CommandService createCommandService() {
        try {
            return new CloudCommandService(this::createCloudCommandManager, commandArguments);
        } catch (Throwable t) {
            LogUtil.warn("CRITICAL: Failed to initialize Command Framework. " +
                    "Grim will continue to run with no commands.", t);
            return () -> {};
        }
    }

    private CommandManager<Sender> createCloudCommandManager() {
        LegacyPaperCommandManager<Sender> manager = new LegacyPaperCommandManager<>(
                this,
                ExecutionCoordinator.simpleCoordinator(),
                senderFactory.get()
        );
        if (manager.hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            try {
                manager.registerBrigadier();
                CloudBrigadierManager<Sender, ?> cbm = manager.brigadierManager();
                cbm.settings().set(BrigadierSetting.FORCE_EXECUTABLE, true);
            } catch (Throwable t) {
                LogUtil.error("Failed to register Brigadier native completions. Falling back to standard completions.", t);
            }
        } else if (manager.hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            manager.registerAsynchronousCompletions();
        }
        return manager;
    }

    public BukkitSenderFactory getBukkitSenderFactory() {
        return senderFactory.get();
    }
}
