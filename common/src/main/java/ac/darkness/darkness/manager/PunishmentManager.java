package ac.darkness.darkness.manager;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.config.ConfigReloadable;
import ac.grim.grimac.api.event.events.CommandExecuteEvent;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.events.packets.ProxyAlertMessenger;
import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.LogUtil;
import ac.darkness.darkness.utils.anticheat.MessageUtil;
import lombok.RequiredArgsConstructor;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class PunishmentManager implements ConfigReloadable {
    private static final CommandExecuteEvent.Channel COMMAND_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(CommandExecuteEvent.class);
    private final GrimPlayer player;
    private final List<PunishGroup> groups = new ArrayList<>();
    private String experimentalSymbol = " (Experimental)";
    private String alertString;
    private String alertConsoleFormat;
    private boolean testMode;
    private String proxyAlertString = "";
    private String barOpening = "&8[";
    private String barClosing = "&8]";
    private String barFill = "&f|";
    private String barEmpty = "&7|";
    private int barSize = 18;
    private boolean alertHoverEnabled = true;
    private String alertHoverCommand = "/darkness spectate %player%";
    private String alertHoverJoined = "";

    public PunishmentManager(GrimPlayer player) {
        this.player = player;
    }

    @Override
    public void reload(ConfigManager config) {
        List<String> punish = config.getStringListElse("Punishments", new ArrayList<>());
        experimentalSymbol = config.getStringElse("experimental_symbol",
                config.getStringElse("experimental-symbol", " (Experimental)"));

        alertString = config.getStringElse(
                "alerts_format",
                config.getStringElse("alerts-format",
                        "%prefix% &c%player% &7failed &c%check_name%%experimental% &7(x&c%bar%&7)")
        );

        alertConsoleFormat = config.getStringElse(
                "alerts_format_console",
                config.getStringElse("alerts-format-console",
                        "%prefix% &c%player% &7failed &c%check_name%%experimental% &7(x&c%vl%&7) &c%verbose%")
        );

        testMode = config.getBooleanElse("test_mode", config.getBooleanElse("test-mode", false));

        proxyAlertString = config.getStringElse(
                "alerts_format_proxy",
                config.getStringElse("alerts-format-proxy",
                        "%prefix% &f[&cPROXY&f] &f%player% &7failed &f%check_name%%experimental% &f(x&c%vl%&f) &7%verbose%")
        );

        barOpening = config.getStringElse("bar.opening_bracket", "&8[");
        barClosing = config.getStringElse("bar.closing_bracket", "&8]");
        barFill = config.getStringElse("bar.fill", "&f|");
        barEmpty = config.getStringElse("bar.empty", "&7|");
        barSize = config.getIntElse("bar.size", 18);

        alertHoverEnabled = config.getBooleanElse("alert_hover_enabled", config.getBooleanElse("alert-hover-enabled", true));
        alertHoverCommand = config.getStringElse("alert_hover_command", config.getStringElse("alert-hover-command", "/darkness spectate %player%"));
        List<String> alertHover = config.getStringListElse("alert_hover", config.getStringListElse("alert-hover", Arrays.asList(
                "&7Description:&r",
                "&c%description%",
                " ",
                "&7Information:&r",
                "&c%information%",
                " ",
                "&7Version: &c&r%version%",
                " ",
                "&7Ping: &c&r%ping%",
                " ",
                "&7TPS: &c&r%tps%",
                " ",
                "&cClick to start spectating"
        )));
        alertHoverJoined = String.join("\n", alertHover);

        try {
            groups.clear();

            // To support reloading
            for (AbstractCheck check : player.getChecks()) {
                check.setEnabled(false);
            }

            for (Object s : punish) {
                LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) s;

                List<String> checks = (List<String>) map.getOrDefault("checks", new ArrayList<>());
                List<String> commands = (List<String>) map.getOrDefault("commands", new ArrayList<>());
                Object rva = map.get("remove-violations-after");
                if (rva == null) rva = map.get("remove_violations_after");
                int removeViolationsAfter = rva instanceof Number n ? n.intValue() : 300;

                List<ParsedCommand> parsed = new ArrayList<>();
                List<AbstractCheck> checksList = new ArrayList<>();
                List<AbstractCheck> excluded = new ArrayList<>();
                for (String command : checks) {
                    command = command.toLowerCase(Locale.ROOT);
                    boolean exclude = false;
                    if (command.startsWith("!")) {
                        exclude = true;
                        command = command.substring(1);
                    }
                    for (AbstractCheck check : player.getChecks()) { // o(n) * o(n)?
                        String checkName = check.getCheckName();
                        String altName = check.getAlternativeName();
                        String cfgName = check instanceof Check gc ? gc.getConfigName() : null;
                        if (checkName != null) {
                            String normCheck = checkName.replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT);
                            String normCmd = command.replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT);
                            if (checkName.toLowerCase(Locale.ROOT).contains(command)
                                    || normCheck.contains(normCmd)
                                    || (altName != null && altName.toLowerCase(Locale.ROOT).contains(command))
                                    || (cfgName != null && cfgName.toLowerCase(Locale.ROOT).contains(command))) {
                                if (exclude) {
                                    excluded.add(check);
                                } else {
                                    checksList.add(check);
                                }
                            }
                        }
                    }
                    for (AbstractCheck check : excluded) checksList.remove(check);
                }
                for (AbstractCheck check : checksList) {
                    check.setEnabled(true);
                }

                for (String command : commands) {
                    String firstNum = command.substring(0, command.indexOf(":"));
                    String secondNum = command.substring(command.indexOf(":"), command.indexOf(" "));

                    int threshold = Integer.parseInt(firstNum);
                    int interval = Integer.parseInt(secondNum.substring(1));
                    String commandString = command.substring(command.indexOf(" ") + 1);

                    parsed.add(new ParsedCommand(threshold, interval, commandString));
                }

                groups.add(new PunishGroup(checksList, parsed, removeViolationsAfter * 1000));
            }
        } catch (Exception e) {
            LogUtil.error("Error while loading punishments.yml! This is likely your fault!", e);
        }
    }

    private int getMaxVl(Check check) {
        int maxVl = 10;
        Integer punish = null;
        Integer ban = null;

        for (PunishGroup group : groups) {
            if (group.checks.contains(check)) {
                for (ParsedCommand command : group.commands) {
                    String cmd = command.command.toLowerCase(Locale.ROOT);
                    if (cmd.contains("punish")) {
                        if (punish == null || command.threshold > punish) {
                            punish = command.threshold;
                        }
                    } else if (cmd.contains("ban")) {
                        if (ban == null || command.threshold > ban) {
                            ban = command.threshold;
                        }
                    }
                    if (command.threshold > maxVl) {
                        maxVl = command.threshold;
                    }
                }
            }
        }
        if (punish != null) {
            return punish;
        } else if (ban != null) {
            return ban;
        } else {
            return maxVl;
        }
    }

    private String buildVlBar(int current, int max) {
        if (max <= 0) max = 1;
        int progressVl = current % max;
        if (progressVl == 0 && current > 0) progressVl = max;
        int filled = (int) Math.round((progressVl / (double) max) * barSize);
        filled = Math.max(0, Math.min(barSize, filled));
        return barOpening + barFill.repeat(filled) + barEmpty.repeat(barSize - filled) + barClosing;
    }

    private String getTrustColor(int vl, int maxVl) {
        if (maxVl <= 0) maxVl = 1;
        if (vl < 0) vl = 0;
        int progressVl = vl % maxVl;
        if (progressVl == 0 && vl != 0) {
            progressVl = maxVl;
        }
        double percent = (progressVl / (double) maxVl) * 100.0;
        if (percent < 40) {
            return "&a";
        } else if (percent < 80) {
            return "&6";
        } else {
            return "&c";
        }
    }

    private String formatHitBoxVerbose(String verbose) {
        if (verbose == null) return "";
        return verbose.replace(", min=", ",\n min=").replace(", max=", ",\n max=");
    }

    private Component buildAlertComponent(GrimPlayer player, String message, Check check, String verbose) {
        String spectate = MessageUtil.replacePlaceholders(player, alertHoverCommand.replace("%player%", player.getName()));
        if (!alertHoverEnabled || alertHoverJoined.isEmpty()) {
            return MessageUtil.miniMessage(message)
                    .clickEvent(ClickEvent.runCommand(spectate));
        }
        String replaced = MessageUtil.replacePlaceholders(player, alertHoverJoined
                .replace("%description%", check.getDescription())
                .replace("%information%", formatHitBoxVerbose(verbose)));
        Component hover = MessageUtil.miniMessage(replaced);
        return MessageUtil.miniMessage(message)
                .hoverEvent(HoverEvent.showText(hover))
                .clickEvent(ClickEvent.runCommand(spectate));
    }

    private String replaceAlertPlaceholders(String original, int vl, Check check, String verbose) {
        int maxVl = getMaxVl(check);
        int progressForBar = vl % maxVl;
        if (progressForBar == 0 && vl > 0) progressForBar = maxVl;
        String vlBar = buildVlBar(progressForBar, maxVl);
        String trustColor = getTrustColor(vl, maxVl);
        String trustName = trustColor.replace("&a", "GREEN").replace("&6", "ORANGE").replace("&c", "RED");
        String displayName = check.getDisplayName();
        String checkName = displayName;
        String checkType = "";
        if (displayName.contains("(") && displayName.contains(")")) {
            int start = displayName.indexOf("(");
            int end = displayName.indexOf(")");
            checkName = displayName.substring(0, start).trim();
            checkType = displayName.substring(start + 1, end).trim();
        }

        return MessageUtil.replacePlaceholders(player, original
                .replace("[alert]", alertString)
                .replace("[proxy]", proxyAlertString)
                .replace("%check%", checkName)
                .replace("%check_name%", displayName)
                .replace("%check_type%", checkType)
                .replace("%trust_color%", trustColor)
                .replace("%trust_name%", trustName)
                .replace("%max_vl%", Integer.toString(maxVl))
                .replace("%experimental%", check.isExperimental() ? experimentalSymbol : "")
                .replace("%vl%", Integer.toString(vl))
                .replace("%bar%", vlBar)
                .replace("%description%", check.getDescription())
                .replace("%information%", formatHitBoxVerbose(verbose))
                .replace("%stable_key%", check.getStableKey())
        ).replace("%verbose%", MessageUtil.miniMessageSafe(verbose));
    }

    public boolean handleAlert(GrimPlayer player, String verbose, Check check) {
        String value = verbose == null ? "" : verbose;
        return handleAlert(player, () -> value, check);
    }

    public boolean handleAlert(GrimPlayer player, Supplier<String> verbose, Check check) {
        boolean sentDebug = false;

        // Check commands
        for (PunishGroup group : groups) {
            if (group.checks.contains(check)) {
                final int vl = getViolations(group, check);
                final int violationCount = group.violations.size();
                for (ParsedCommand command : group.commands) {
                    @Nullable Set<@Nullable PlatformPlayer> verboseListeners = null;
                    boolean consoleReceivedVerbose = false;
                    // Verbose that prints all flags: /grim verbose subscribers get EVERY flag, not just thresholded
                    // alerts. The verbose string is only rendered (safeGet) when there are listeners, so the flag
                    // path stays lazy when nobody is subscribed.
                    if (command.command.equals("[alert]") && GrimAPI.INSTANCE.getAlertManager().hasVerboseListeners()) {
                        sentDebug = true;
                        String verboseForListeners = safeGet(verbose);
                        String listenerCmd = replaceAlertPlaceholders(command.command, vl, check, verboseForListeners);
                        Component listenerComp = buildAlertComponent(player, listenerCmd, check, verboseForListeners);
                        verboseListeners = GrimAPI.INSTANCE.getAlertManager().sendVerbose(listenerComp, Collections.singleton(null));
                        if (GrimAPI.INSTANCE.getAlertManager().hasConsoleVerboseEnabled()) {
                            String consoleMsg = replaceAlertPlaceholders(alertConsoleFormat, vl, check, verboseForListeners);
                            GrimAPI.INSTANCE.getPlatformServer().getConsoleSender().sendMessage(MessageUtil.miniMessage(consoleMsg));
                            consoleReceivedVerbose = true;
                        }
                    }
                    if (violationCount >= command.threshold) {
                        boolean shouldRun = command.interval == 0
                                ? command.executeCount == 0
                                : violationCount >= command.nextBoundary;
                        if (shouldRun) {
                            String renderedVerbose = safeGet(verbose);
                            String cmd = replaceAlertPlaceholders(command.command, vl, check, renderedVerbose);
                            boolean canceled = COMMAND_CHANNEL.fire(player, check, renderedVerbose, cmd);
                            if (command.interval == 0) {
                                command.executeCount++;
                            } else {
                                advanceBoundary(command, violationCount);
                            }
                            if (canceled) continue;

                            switch (command.command) {
                                case "[webhook]" -> GrimAPI.INSTANCE.getDiscordManager().sendAlert(player, renderedVerbose, check.getDisplayName(), vl);
                                case "[log]" -> {
                                    // Binary flags already stored a row; avoid an extra legacy text row.
                                    if (!check.isLastFlagStoredBinaryVerbose()) {
                                        String verboseWithoutGl = renderedVerbose.replaceAll(" /gl .*", "");
                                        GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks()
                                                .recordFlagFromCheck(player, check, vl, verboseWithoutGl);
                                    }
                                }
                                case "[proxy]" -> ProxyAlertMessenger.sendPluginMessage(cmd);
                                case "[alert]" -> {
                                    sentDebug = true;
                                    Component message = buildAlertComponent(player, cmd, check, renderedVerbose);
                                    if (testMode) { // secret test mode
                                        if (verboseListeners == null || verboseListeners.contains(player.platformPlayer)) {
                                            player.sendMessage(message);
                                        }
                                    } else {
                                        Set<PlatformPlayer> exclude = new HashSet<>();
                                        if (verboseListeners != null) {
                                            exclude.addAll(verboseListeners);
                                        }
                                        exclude.add(null);
                                        GrimAPI.INSTANCE.getAlertManager().sendAlert(message, exclude);
                                    }
                                    if (GrimAPI.INSTANCE.getAlertManager().hasConsoleAlertsEnabled() && !consoleReceivedVerbose) {
                                        String consoleStr = replaceAlertPlaceholders(alertConsoleFormat, vl, check, renderedVerbose);
                                        Component consoleComponent = MessageUtil.miniMessage(consoleStr);
                                        GrimAPI.INSTANCE.getPlatformServer().getConsoleSender().sendMessage(consoleComponent);
                                    }
                                }
                                default -> GrimAPI.INSTANCE.getScheduler().getGlobalRegionScheduler().run(GrimAPI.INSTANCE.getGrimPlugin(), () ->
                                        GrimAPI.INSTANCE.getPlatformServer().dispatchCommand(
                                                GrimAPI.INSTANCE.getPlatformServer().getConsoleSender(),
                                                cmd
                                        )
                                );
                            }
                        }

                        if (command.interval > 0) command.executeCount++;
                    } else {
                        // Interval commands re-arm after the active rolling count
                        // cools below their threshold.
                        command.nextBoundary = command.threshold;
                    }
                }
            }
        }

        return sentDebug;
    }

    private static String safeGet(Supplier<String> supplier) {
        try {
            String value = supplier.get();
            return value == null ? "" : value;
        } catch (RuntimeException ignored) {
            return "";
        }
    }

    private static void advanceBoundary(ParsedCommand command, int violationCount) {
        command.nextBoundary += ((violationCount - command.nextBoundary) / command.interval + 1) * command.interval;
    }

    public void handleViolation(Check check) {
        for (PunishGroup group : groups) {
            if (group.checks.contains(check)) {
                long currentTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime());

                group.violations.record(currentTime, check, group.removeViolationsAfter);
            }
        }
    }

    private int getViolations(PunishGroup group, Check check) {
        return group.violations.count(check);
    }
}

@RequiredArgsConstructor
class PunishGroup {
    public final List<AbstractCheck> checks;
    public final List<ParsedCommand> commands;
    public final ViolationHistory<AbstractCheck> violations = new ViolationHistory<>();
    public final int removeViolationsAfter; // time to remove violations after in milliseconds
}

class ParsedCommand {
    public final int threshold;
    public final int interval;
    public final String command;
    // Legacy M=0 gate: execute once for this loaded command state.
    public int executeCount;
    // For M>0, the next active violation count that should run this command.
    public int nextBoundary;

    public ParsedCommand(int threshold, int interval, String command) {
        this.threshold = threshold;
        this.interval = interval;
        this.command = command;
        this.nextBoundary = threshold;
    }
}
