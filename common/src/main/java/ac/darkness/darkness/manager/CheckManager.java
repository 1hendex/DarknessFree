package ac.darkness.darkness.manager;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.AbstractProcessor;
import ac.grim.grimac.api.common.BasicReloadable;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.impl.combat.aim.AimA;
import ac.darkness.darkness.checks.impl.combat.aim.AimB;
import ac.darkness.darkness.checks.impl.combat.aim.processor.AimProcessor;
import ac.darkness.darkness.checks.impl.combat.aim.processor.ClickProcessor;
import ac.darkness.darkness.checks.impl.combat.aim.processor.RotationProcessor;
import ac.darkness.darkness.checks.impl.combat.killaura.*;
import ac.darkness.darkness.checks.impl.packets.badpackets.*;
import ac.darkness.darkness.checks.impl.packets.freecam.*;
import ac.darkness.darkness.checks.impl.misc.interact.*;
import ac.darkness.darkness.checks.impl.packets.chat.ChatA;
import ac.darkness.darkness.checks.impl.packets.chat.ChatB;
import ac.darkness.darkness.checks.impl.packets.chat.ChatC;
import ac.darkness.darkness.checks.impl.packets.chat.ChatD;
import ac.darkness.darkness.checks.impl.combat.hitbox.HitBoxA;
import ac.darkness.darkness.checks.impl.combat.reach.ReachA;
import ac.darkness.darkness.checks.impl.movement.inventory.*;
import ac.darkness.darkness.checks.impl.misc.crash.*;
import ac.darkness.darkness.checks.impl.movement.elytra.*;
import ac.darkness.darkness.checks.impl.misc.exploit.ExploitA;
import ac.darkness.darkness.checks.impl.misc.exploit.ExploitB;
import ac.darkness.darkness.checks.impl.movement.nofall.NoFallA;
import ac.darkness.darkness.checks.impl.movement.nofall.NoFallB;
import ac.darkness.darkness.checks.impl.misc.ClientBrand;
import ac.darkness.darkness.checks.impl.misc.GhostBlockMitigation;
import ac.darkness.darkness.checks.impl.movement.NoSlowA;
import ac.darkness.darkness.checks.impl.movement.PredictionRunner;
import ac.darkness.darkness.checks.impl.movement.SetbackBlocker;
import ac.darkness.darkness.checks.impl.movement.VehiclePredictionRunner;
import ac.darkness.darkness.checks.impl.packets.multiactions.*;
import ac.darkness.darkness.checks.impl.packets.protocol.*;
import ac.darkness.darkness.checks.impl.movement.prediction.DebugHandler;
import ac.darkness.darkness.checks.impl.movement.prediction.OffsetHandler;
import ac.darkness.darkness.checks.impl.movement.prediction.PhaseA;
import ac.darkness.darkness.checks.impl.scaffold.*;
import ac.darkness.darkness.checks.impl.movement.sprint.*;
import ac.darkness.darkness.checks.impl.packets.timer.*;
import ac.darkness.darkness.checks.impl.movement.vehicle.*;
import ac.darkness.darkness.checks.impl.combat.velocity.VelocityA;
import ac.darkness.darkness.checks.impl.combat.velocity.VelocityB;
import ac.darkness.darkness.checks.type.*;
import ac.darkness.darkness.events.packets.PacketChangeGameState;
import ac.darkness.darkness.events.packets.PacketEntityReplication;
import ac.darkness.darkness.events.packets.PacketPlayerAbilities;
import ac.darkness.darkness.events.packets.PacketWorldBorder;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.darkness.darkness.manager.init.start.SuperDebug;
import ac.darkness.darkness.platform.api.permissions.PermissionDefaultValue;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.predictionengine.GhostBlockDetector;
import ac.darkness.darkness.predictionengine.SneakingEstimator;
import ac.darkness.darkness.utils.anticheat.update.*;
import ac.darkness.darkness.utils.latency.CompensatedCameraEntity;
import ac.darkness.darkness.utils.latency.CompensatedCooldown;
import ac.darkness.darkness.utils.latency.CompensatedFireworks;
import ac.darkness.darkness.utils.latency.CompensatedInventory;
import ac.darkness.darkness.utils.team.TeamHandler;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.ImmutableClassToInstanceMap;
import org.jetbrains.annotations.NotNull;

import ac.darkness.darkness.checks.CheckCategory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;

public class CheckManager implements BasicReloadable {
    private static final AtomicBoolean initedAtomic = new AtomicBoolean(false);
    private static boolean inited;
    public final ClassToInstanceMap<AbstractProcessor> processors;
    public final Collection<AbstractCheck> checks;

    private final PreViaPacketReceiveListener[] preViaPacketReceiveListeners;
    private final PreViaPacketSendListener[] preViaPacketSendListeners;
    private final PacketReceiveListener[] packetReceiveListeners;
    private final PacketSendListener[] packetSendListeners;
    private final PositionListener[] positionListeners;
    private final RotationListener[] rotationListeners;
    private final VehicleListener[] vehicleListeners;
    private final PrePredictionPacketReceiveListener[] prePredictionPacketReceiveListeners;
    private final BlockBreakListener[] blockBreakListeners;
    private final BlockPlaceListener[] blockPlaceListeners;
    private final PostFlyingBlockPlaceListener[] postFlyingBlockPlaceListeners;
    private final PostFlyingBlockBreakListener[] postFlyingBlockBreakListeners;
    private final PostPredictionListener[] postPredictionListeners;

    public CheckManager(GrimPlayer player) {
        processors = new ImmutableClassToInstanceMap.Builder<AbstractProcessor>()
                .put(CompensatedCameraEntity.class, player.cameraEntity)
                .put(ChatA.class, new ChatA(player))
                .put(ChatB.class, new ChatB(player))
                .put(ChatC.class, new ChatC(player))
                .put(ChatD.class, new ChatD(player))
                .put(BadPacketsA.class, new BadPacketsA(player))
                .put(BadPacketsC.class, new BadPacketsC(player))
                .put(BadPacketsF.class, new BadPacketsF(player))
                .put(BadPacketsG.class, new BadPacketsG(player))
                .put(BadPacketsI.class, new BadPacketsI(player))
                .put(BadPacketsK.class, new BadPacketsK(player))
                .put(BadPacketsM.class, new BadPacketsM(player))
                .put(BadPacketsY.class, new BadPacketsY(player))
                .put(BadPacketsZ.class, new BadPacketsZ(player))
                .put(ProtocolU.class, new ProtocolU(player))
                .put(ProtocolV.class, new ProtocolV(player))
                .put(ProtocolE.class, new ProtocolE(player))
                .put(InteractK.class, new InteractK(player))
                .put(MultiActionsA.class, new MultiActionsA(player))
                .put(MultiActionsE.class, new MultiActionsE(player))
                .put(VehicleA.class, new VehicleA(player))
                .put(VehicleB.class, new VehicleB(player))

        // TODO: migrate the rest of these to pre-via
                .put(ProtocolProcessor.class, player.protocolProcessor)
                .put(ReachA.class, new ReachA(player))
                .put(PacketEntityReplication.class, player.packetEntityReplication)
                .put(PacketChangeGameState.class, new PacketChangeGameState(player))
                .put(CompensatedInventory.class, player.inventory)
                .put(PacketPlayerAbilities.class, new PacketPlayerAbilities(player))
                .put(PacketWorldBorder.class, new PacketWorldBorder(player))
                .put(AttackCooldownHandler.class, player.attackCooldown)
                .put(TeamHandler.class, new TeamHandler(player))
                .put(ClientBrand.class, new ClientBrand(player))
                .put(NoFallA.class, new NoFallA(player))
                .put(ExploitA.class, new ExploitA(player))
                .put(ExploitB.class, new ExploitB(player))
                .put(BadPacketsD.class, new BadPacketsD(player))
                .put(BadPacketsE.class, new BadPacketsE(player))
                .put(BadPacketsJ.class, new BadPacketsJ(player))
                .put(BadPacketsL.class, new BadPacketsL(player))
                .put(BadPacketsO.class, new BadPacketsO(player))
                .put(BadPacketsP.class, new BadPacketsP(player))
                .put(BadPacketsQ.class, new BadPacketsQ(player))
                .put(BadPacketsR.class, new BadPacketsR(player))
                .put(BadPacketsS.class, new BadPacketsS(player))
                .put(InteractL.class, new InteractL(player))
                .put(BadPacketsU.class, new BadPacketsU(player))
                .put(BadPacketsV.class, new BadPacketsV(player))
                .put(FreeCamA.class, new FreeCamA(player))
                .put(FreeCamB.class, new FreeCamB(player))
                .put(FreeCamC.class, new FreeCamC(player))
                .put(MultiActionsC.class, new MultiActionsC(player))
                .put(MultiActionsD.class, new MultiActionsD(player))
                .put(ProtocolD.class, new ProtocolD(player))
                .put(ProtocolO.class, new ProtocolO(player))
                .put(ProtocolQ.class, new ProtocolQ(player))
                .put(ProtocolR.class, new ProtocolR(player))
                .put(ProtocolS.class, new ProtocolS(player))
                .put(VehicleD.class, new VehicleD(player))
                .put(VehicleE.class, new VehicleE(player))
                .put(VehicleF.class, new VehicleF(player))
                .put(CrashB.class, new CrashB(player))
                .put(CrashD.class, new CrashD(player))
                .put(CrashE.class, new CrashE(player))
                .put(CrashF.class, new CrashF(player))
                .put(CrashH.class, new CrashH(player))
                .put(CrashI.class, new CrashI(player))
                .put(SetbackBlocker.class, new SetbackBlocker(player)) // Must be last class otherwise we can't check while blocking packets

                .put(PredictionRunner.class, new PredictionRunner(player))
                .put(CompensatedCooldown.class, new CompensatedCooldown(player))
                .put(AimProcessor.class, new AimProcessor(player))
                .put(ClickProcessor.class, player.clickProcessor)
                .put(RotationProcessor.class, player.rotationProcessor)
                .put(ActionManager.class, player.actionManager)
                .put(AimB.class, new AimB(player))
                .put(AimA.class, new AimA(player))
                .put(VehiclePredictionRunner.class, new VehiclePredictionRunner(player))

                .put(NegativeTimer.class, new NegativeTimer(player))
                .put(VelocityA.class, new VelocityA(player))
                .put(VelocityB.class, new VelocityB(player))
                .put(GhostBlockDetector.class, new GhostBlockDetector(player))
                .put(PhaseA.class, new PhaseA(player))
                .put(ProtocolA.class, new ProtocolA(player))
                .put(ProtocolC.class, new ProtocolC(player))
                .put(ProtocolF.class, new ProtocolF(player))
                .put(ProtocolG.class, new ProtocolG(player))
                .put(ProtocolH.class, new ProtocolH(player))
                .put(ProtocolI.class, new ProtocolI(player))
                .put(ProtocolJ.class, new ProtocolJ(player))
                .put(ProtocolK.class, new ProtocolK(player))
                .put(ProtocolL.class, new ProtocolL(player))
                .put(ProtocolM.class, new ProtocolM(player))
                .put(ProtocolN.class, new ProtocolN(player))
                .put(ProtocolT.class, new ProtocolT(player))
                .put(KillAuraA.class, new KillAuraA(player))
                .put(KillAuraB.class, new KillAuraB(player))
                .put(KillAuraC.class, new KillAuraC(player))
                .put(KillAuraD.class, new KillAuraD(player))
                .put(KillAuraE.class, new KillAuraE(player))
                .put(KillAuraF.class, new KillAuraF(player))
                .put(KillAuraG.class, new KillAuraG(player))
                .put(KillAuraH.class, new KillAuraH(player))
                .put(NoFallB.class, new NoFallB(player))
                .put(OffsetHandler.class, new OffsetHandler(player))
                .put(SuperDebug.class, new SuperDebug(player))
                .put(DebugHandler.class, new DebugHandler(player))
                .put(BadPacketsX.class, new BadPacketsX(player))
                .put(NoSlowA.class, new NoSlowA(player))
                .put(SprintA.class, new SprintA(player))
                .put(SprintB.class, new SprintB(player))
                .put(SprintC.class, new SprintC(player))
                .put(SprintD.class, new SprintD(player))
                .put(SprintE.class, new SprintE(player))
                .put(SprintF.class, new SprintF(player))
                .put(SprintG.class, new SprintG(player))
                .put(SprintH.class, new SprintH(player))
                .put(SprintI.class, new SprintI(player))
                .put(KillAuraI.class, new KillAuraI(player))
                .put(KillAuraJ.class, new KillAuraJ(player))
                .put(ElytraA.class, new ElytraA(player))
                .put(ElytraB.class, new ElytraB(player))
                .put(ElytraC.class, new ElytraC(player))
                .put(ElytraD.class, new ElytraD(player))
                .put(ElytraE.class, new ElytraE(player))
                .put(ElytraF.class, new ElytraF(player))
                .put(ElytraG.class, new ElytraG(player))
                .put(ElytraH.class, new ElytraH(player))
                .put(ElytraI.class, new ElytraI(player))
                .put(ElytraJ.class, new ElytraJ(player))
                .put(ElytraK.class, new ElytraK(player))
                .put(SetbackTeleportUtil.class, new SetbackTeleportUtil(player)) // Avoid teleporting to new position, update safe pos last
                .put(CompensatedFireworks.class, player.fireworks)
                .put(SneakingEstimator.class, new SneakingEstimator(player))
                .put(LastInstanceManager.class, player.lastInstanceManager)

                .put(InvalidPlaceCursor.class, new InvalidPlaceCursor(player))
                .put(InvalidPlaceFace.class, new InvalidPlaceFace(player))
                .put(AirLiquidPlace.class, new AirLiquidPlace(player))
                .put(MultiPlace.class, new MultiPlace(player))
                .put(MultiActionsF.class, new MultiActionsF(player))
                .put(MultiActionsG.class, new MultiActionsG(player))
                .put(BadPacketsH.class, new BadPacketsH(player))
                .put(CrashG.class, new CrashG(player))
                .put(FarPlace.class, new FarPlace(player))
                .put(FabricatedPlace.class, new FabricatedPlace(player))
                .put(PositionPlace.class, new PositionPlace(player))
                .put(RotationPlace.class, new RotationPlace(player))
                .put(ProtocolP.class, new ProtocolP(player))
                .put(DuplicateRotPlace.class, new DuplicateRotPlace(player))
                .put(GhostBlockMitigation.class, new GhostBlockMitigation(player))

                .put(Timer.class, new Timer(player))
                .put(TickTimer.class, new TickTimer(player))
                .put(TimerLimit.class, new TimerLimit(player))
                .put(CrashA.class, new CrashA(player))
                .put(CrashC.class, new CrashC(player))
                .put(CrashJ.class, new CrashJ(player))
                .put(VehicleTimer.class, new VehicleTimer(player))

                .put(InteractM.class, new InteractM(player))
                .put(InteractF.class, new InteractF(player))
                .put(InteractG.class, new InteractG(player))
                .put(InteractA.class, new InteractA(player))
                .put(InteractB.class, new InteractB(player))
                .put(InteractH.class, new InteractH(player))
                .put(InteractI.class, new InteractI(player))
                .put(InteractJ.class, new InteractJ(player))
                .put(InteractD.class, new InteractD(player))
                .put(InteractE.class, new InteractE(player))
                .put(MultiActionsB.class, new MultiActionsB(player))
                .put(InteractC.class, new InteractC(player))

        // All checks that have no listeners, generally invoked by other code to flag
        // TODO migrate more checks to here
                // BadPacketsB/N/W, VehicleC, and ProtocolB are packet checks with no listener
                .put(BadPacketsB.class, new BadPacketsB(player))
                .put(BadPacketsN.class, new BadPacketsN(player))
                .put(InteractN.class, new InteractN(player))
                .put(ProtocolB.class, new ProtocolB(player))
                .put(VehicleC.class, new VehicleC(player))
                .put(HitBoxA.class, new HitBoxA(player)) // HitBoxA is invoked by Reach
                .put(KillAuraK.class, new KillAuraK(player))
                .put(KillAuraL.class, new KillAuraL(player))

                .put(InventoryA.class, new InventoryA(player))
                .put(InventoryB.class, new InventoryB(player))
                .put(InventoryC.class, new InventoryC(player))
                .put(InventoryD.class, new InventoryD(player))
                .put(InventoryE.class, new InventoryE(player))
                .put(InventoryF.class, new InventoryF(player))
                .put(InventoryG.class, new InventoryG(player))
                .build();

        ArrayList<AbstractCheck> checks = new ArrayList<>();
        ArrayList<PreViaPacketReceiveListener> preViaPacketReceiveListeners = new ArrayList<>();
        ArrayList<PreViaPacketSendListener> preViaPacketSendListeners = new ArrayList<>();
        ArrayList<PacketReceiveListener> packetReceiveListeners = new ArrayList<>();
        ArrayList<PacketSendListener> packetSendListeners = new ArrayList<>();
        ArrayList<PrePredictionPacketReceiveListener> prePredictionPacketReceiveListeners = new ArrayList<>();
        ArrayList<PositionListener> positionListeners = new ArrayList<>();
        ArrayList<RotationListener> rotationListeners = new ArrayList<>();
        ArrayList<VehicleListener> vehicleListeners = new ArrayList<>();
        ArrayList<PostPredictionListener> postPredictionListeners = new ArrayList<>();
        ArrayList<BlockPlaceListener> blockPlaceListeners = new ArrayList<>();
        ArrayList<PostFlyingBlockPlaceListener> postFlyingBlockPlaceListeners = new ArrayList<>();
        ArrayList<BlockBreakListener> blockBreakListeners = new ArrayList<>();
        ArrayList<PostFlyingBlockBreakListener> postFlyingBlockBreakListeners = new ArrayList<>();

        for (AbstractProcessor processor : processors.values()) {
            if (processor instanceof AbstractCheck abstractCheck) {
                checks.add(abstractCheck);
                if (processor instanceof Check grimCheck && !grimCheck.isApplicable()) continue;
            }

            if (processor instanceof PacketReceiveListener packetReceiveListener) packetReceiveListeners.add(packetReceiveListener);
            if (processor instanceof PrePredictionPacketReceiveListener prePredictionPacketReceiveListener) prePredictionPacketReceiveListeners.add(prePredictionPacketReceiveListener);
            if (processor instanceof PreViaPacketReceiveListener preViaPacketReceiveListener) preViaPacketReceiveListeners.add(preViaPacketReceiveListener);
            if (processor instanceof PacketSendListener packetSendListener) packetSendListeners.add(packetSendListener);
            if (processor instanceof PreViaPacketSendListener preViaPacketSendListener) preViaPacketSendListeners.add(preViaPacketSendListener);
            if (processor instanceof PositionListener positionListener) positionListeners.add(positionListener);
            if (processor instanceof RotationListener rotationListener) rotationListeners.add(rotationListener);
            if (processor instanceof VehicleListener vehicleListener) vehicleListeners.add(vehicleListener);
            if (processor instanceof PostPredictionListener postPredictionListener) postPredictionListeners.add(postPredictionListener);
            if (processor instanceof BlockPlaceListener blockPlaceListener) blockPlaceListeners.add(blockPlaceListener);
            if (processor instanceof PostFlyingBlockPlaceListener postFlyingBlockPlaceListener) postFlyingBlockPlaceListeners.add(postFlyingBlockPlaceListener);
            if (processor instanceof BlockBreakListener blockBreakListener) blockBreakListeners.add(blockBreakListener);
            if (processor instanceof PostFlyingBlockBreakListener postFlyingBlockBreakListener) postFlyingBlockBreakListeners.add(postFlyingBlockBreakListener);
        }

        checks.trimToSize();
        this.checks = Collections.unmodifiableCollection(checks);
        this.preViaPacketReceiveListeners = preViaPacketReceiveListeners.toArray(new PreViaPacketReceiveListener[preViaPacketReceiveListeners.size()]);
        this.preViaPacketSendListeners = preViaPacketSendListeners.toArray(new PreViaPacketSendListener[preViaPacketSendListeners.size()]);
        this.packetReceiveListeners = packetReceiveListeners.toArray(new PacketReceiveListener[packetReceiveListeners.size()]);
        this.packetSendListeners = packetSendListeners.toArray(new PacketSendListener[packetSendListeners.size()]);
        this.prePredictionPacketReceiveListeners = prePredictionPacketReceiveListeners.toArray(new PrePredictionPacketReceiveListener[prePredictionPacketReceiveListeners.size()]);
        this.positionListeners = positionListeners.toArray(new PositionListener[positionListeners.size()]);
        this.rotationListeners = rotationListeners.toArray(new RotationListener[rotationListeners.size()]);
        this.vehicleListeners = vehicleListeners.toArray(new VehicleListener[vehicleListeners.size()]);
        this.postPredictionListeners = postPredictionListeners.toArray(new PostPredictionListener[postPredictionListeners.size()]);
        this.blockPlaceListeners = blockPlaceListeners.toArray(new BlockPlaceListener[blockPlaceListeners.size()]);
        this.postFlyingBlockPlaceListeners = postFlyingBlockPlaceListeners.toArray(new PostFlyingBlockPlaceListener[postFlyingBlockPlaceListeners.size()]);
        this.blockBreakListeners = blockBreakListeners.toArray(new BlockBreakListener[blockBreakListeners.size()]);
        this.postFlyingBlockBreakListeners = postFlyingBlockBreakListeners.toArray(new PostFlyingBlockBreakListener[postFlyingBlockBreakListeners.size()]);

        init();
    }

    private void registerBuiltInVerboseTemplates() {
        VerboseRegistry registry = GrimAPI.INSTANCE.getDataStoreLifecycle().verboseRegistry();
        if (registry == null) return;
        registry.registerTemplates(() -> {
            for (AbstractCheck check : checks) {
                if (check instanceof Check grimCheck) {
                    grimCheck.registerVerboseTemplates(registry);
                }
            }
        });
    }

    @Override
    public void reload() {
        for (AbstractProcessor processor : processors.values()) {
            processor.reload();
        }
    }

    public <T extends AbstractProcessor> T get(@NotNull Class<T> check) {
        return processors.getInstance(check);
    }

    public <T extends AbstractProcessor> T getCheck(@NotNull Class<T> check) {
        return get(check);
    }

    public <T extends AbstractProcessor> T getPacketCheck(@NotNull Class<T> check) {
        return get(check);
    }

    public void onPrePredictionReceivePacket(final PacketReceiveEvent packet) {
        for (PrePredictionPacketReceiveListener check : prePredictionPacketReceiveListeners) {
            check.onPrePredictionPacketReceive(packet);
        }
    }

    public void onPacketReceive(final PacketReceiveEvent packet) {
        for (PacketReceiveListener check : packetReceiveListeners) {
            check.onPacketReceive(packet);
        }
    }

    public void onPreViaPacketReceive(final PacketReceiveEvent packet) {
        for (PreViaPacketReceiveListener check : preViaPacketReceiveListeners) {
            check.onPreViaPacketReceive(packet);
        }
    }

    public void onPacketSend(final PacketSendEvent packet) {
        for (PacketSendListener check : packetSendListeners) {
            check.onPacketSend(packet);
        }
    }

    public void onPreViaPacketSend(final PacketSendEvent packet) {
        for (PreViaPacketSendListener check : preViaPacketSendListeners) {
            check.onPreViaPacketSend(packet);
        }
    }

    public void onPositionUpdate(final PositionUpdate position) {
        for (PositionListener check : positionListeners) {
            check.onPositionUpdate(position);
        }
    }

    public void onRotationUpdate(final RotationUpdate rotation) {
        for (RotationListener check : rotationListeners) {
            check.process(rotation);
        }
    }

    public void onVehiclePositionUpdate(final VehiclePositionUpdate update) {
        for (VehicleListener check : vehicleListeners) {
            check.process(update);
        }
    }

    public void onPredictionFinish(final PredictionComplete complete) {
        for (PostPredictionListener check : postPredictionListeners) {
            check.onPredictionComplete(complete);
        }
    }

    public void onBlockPlace(final BlockPlace place) {
        for (BlockPlaceListener check : blockPlaceListeners) {
            check.onBlockPlace(place);
        }
    }

    public void onPostFlyingBlockPlace(final BlockPlace place) {
        for (PostFlyingBlockPlaceListener check : postFlyingBlockPlaceListeners) {
            check.onPostFlyingBlockPlace(place);
        }
    }

    public void onBlockBreak(final BlockBreak blockBreak) {
        for (BlockBreakListener check : blockBreakListeners) {
            check.onBlockBreak(blockBreak);
        }
    }

    public void onPostFlyingBlockBreak(final BlockBreak blockBreak) {
        for (PostFlyingBlockBreakListener check : postFlyingBlockBreakListeners) {
            check.onPostFlyingBlockBreak(blockBreak);
        }
    }

    public VelocityB getExplosionHandler() {
        return get(VelocityB.class);
    }

    public NoFallA getNoFall() {
        return get(NoFallA.class);
    }

    public VelocityA getKnockbackHandler() {
        return get(VelocityA.class);
    }

    public VelocityA getVelocityA() {
        return get(VelocityA.class);
    }

    public VelocityB getVelocityB() {
        return get(VelocityB.class);
    }

    public CompensatedCooldown getCompensatedCooldown() {
        return get(CompensatedCooldown.class);
    }

    public NoSlowA getNoSlow() {
        return get(NoSlowA.class);
    }

    public ReachA getReach() {
        return get(ReachA.class);
    }

    public PhaseA getPhase() {
        return get(PhaseA.class);
    }

    public SetbackTeleportUtil getSetbackUtil() {
        return get(SetbackTeleportUtil.class);
    }

    public DebugHandler getDebugHandler() {
        return get(DebugHandler.class);
    }

    public List<Check> getChecksByCategory(CheckCategory category) {
        List<Check> list = new ArrayList<>();
        for (AbstractCheck c : checks) {
            if (c instanceof Check check && check.getCategory() == category) {
                list.add(check);
            }
        }
        return list;
    }

    public Check getCheckByName(String name) {
        if (name == null) return null;
        String normalized = name.replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT);
        for (AbstractCheck c : checks) {
            if (c instanceof Check check) {
                if (check.getCheckName() != null && check.getCheckName().equalsIgnoreCase(name)) return check;
                if (check.getConfigName() != null && check.getConfigName().equalsIgnoreCase(name)) return check;
                if (check.getAlternativeName() != null && check.getAlternativeName().equalsIgnoreCase(name)) return check;
                if (check.getCheckName() != null && check.getCheckName().replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT).equals(normalized)) return check;
                if (check.getConfigName() != null && check.getConfigName().replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT).equals(normalized)) return check;
            }
        }
        return null;
    }

    private void init() {
        if (inited || initedAtomic.getAndSet(true)) return;
        inited = true;

        registerBuiltInVerboseTemplates();

        final String[] permissions = {
                "grim.exempt.",
                "grim.nosetback.",
                "grim.nomodifypacket.",
                "darkness.exempt.",
                "darkness.nosetback.",
                "darkness.nomodifypacket.",
        };

        for (final AbstractCheck check : checks) {
            if (check.getConfigName() == null) continue;
            final String id = check.getConfigName().toLowerCase();
            for (String permissionName : permissions) {
                GrimAPI.INSTANCE.getPermissionManager().registerPermission(permissionName + id, PermissionDefaultValue.FALSE);
            }
        }
    }
}
