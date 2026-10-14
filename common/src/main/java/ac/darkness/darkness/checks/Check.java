package ac.darkness.darkness.checks;

import ac.darkness.darkness.GrimAPI;
import ac.grim.grimac.api.AbstractCheck;
import ac.grim.grimac.api.config.ConfigManager;
import ac.grim.grimac.api.event.events.FlagEvent;
import ac.grim.grimac.api.storage.verbose.Verbose;
import ac.grim.grimac.api.storage.verbose.VerboseBuf;
import ac.grim.grimac.api.storage.verbose.VerboseRenderContext;
import ac.grim.grimac.internal.storage.verbose.VerboseRegistry;
import ac.darkness.darkness.player.GrimPlayer;
import lombok.Getter;
import lombok.Setter;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

// Class from https://github.com/Tecnio/AntiCheatBase/blob/master/src/main/java/me/tecnio/anticheat/check/Check.java
@Getter
public class Check extends GrimProcessor implements AbstractCheck {
    private static final FlagEvent.Channel FLAG_CHANNEL = GrimAPI.INSTANCE.getEventBus().get(FlagEvent.class);
    private static final ThreadLocal<VerboseBuf> VERBOSE = ThreadLocal.withInitial(VerboseBuf::new);
    private static final Map<String, Boolean> TOGGLED_CHECKS = new ConcurrentHashMap<>();
    public static final Set<String> KNOWN_CHECK_NAMES = ConcurrentHashMap.newKeySet();

    public static void setCheckEnabledGlobally(String configName, boolean enabled) {
        if (configName != null) {
            TOGGLED_CHECKS.put(configName.toLowerCase(Locale.ROOT), enabled);
        }
    }

    public static Boolean getGlobalCheckOverride(String configName) {
        if (configName == null) return null;
        return TOGGLED_CHECKS.get(configName.toLowerCase(Locale.ROOT));
    }

    // violations
    public double violations;
    private long lastViolationTime;
    private boolean lastFlagStoredBinaryVerbose;
    private final VerboseBuf verbose = VERBOSE.get();

    // check data
    private final @Nullable String checkName;
    private final @Nullable String configName;
    private final @Nullable String alternativeName;
    private final @NotNull String stableKey;
    private final boolean experimental;
    private final @NotNull String defaultDescription;
    private final double defaultDecay;
    private final double defaultSetbackVL;

    // configurable
    private @MonotonicNonNull String displayName;
    private @MonotonicNonNull String description;
    private double decay;
    private double setbackVL;
    @Setter private boolean isEnabled = true;

    // permissions
    private boolean exemptPermission;
    private boolean noSetbackPermission;
    private boolean noModifyPacketPermission;

    public static String formatCheckName(String raw) {
        if (raw == null || raw.isEmpty()) return raw;
        if (raw.contains("(") && raw.contains(")")) return raw;

        // Double identical uppercase letters at end: e.g. BadPacketsAA -> BadPackets (AA)
        if (raw.length() > 2 && Character.isLowerCase(raw.charAt(raw.length() - 3))) {
            char last = raw.charAt(raw.length() - 1);
            char secondLast = raw.charAt(raw.length() - 2);
            if (Character.isUpperCase(last) && last == secondLast) {
                return raw.substring(0, raw.length() - 2) + " (" + raw.substring(raw.length() - 2) + ")";
            }
        }

        // Single uppercase letter at end preceded by lowercase: e.g. KillAuraC -> KillAura (C), ProtocolH -> Protocol (H)
        if (raw.length() > 1 && Character.isLowerCase(raw.charAt(raw.length() - 2))) {
            char last = raw.charAt(raw.length() - 1);
            if (Character.isUpperCase(last)) {
                return raw.substring(0, raw.length() - 1) + " (" + last + ")";
            }
        }

        // Handle space/dot/dash separated types like "KillAura C" or "KillAura.c"
        if (raw.matches("^[A-Za-z0-9]+[\\s._-][A-Za-z]{1,2}$")) {
            int splitIdx = Math.max(raw.lastIndexOf(' '), Math.max(raw.lastIndexOf('.'), Math.max(raw.lastIndexOf('_'), raw.lastIndexOf('-'))));
            if (splitIdx > 0 && splitIdx < raw.length() - 1) {
                String base = raw.substring(0, splitIdx).trim();
                String type = raw.substring(splitIdx + 1).trim().toUpperCase(Locale.ROOT);
                return base + " (" + type + ")";
            }
        }

        return raw;
    }

    public Check(final @NotNull GrimPlayer player) {
        super(player, false);

        final CheckData checkData = this.getClass().getAnnotation(CheckData.class);
        if (checkData != null) {
            String rawName = checkData.name();
            String rawConfig = checkData.configName().equals("DEFAULT")
                    ? rawName
                    : checkData.configName();
            this.configName = rawConfig;
            String formattedName = formatCheckName(rawName);
            if (!formattedName.contains("(") && rawConfig != null && !rawConfig.equals("DEFAULT")) {
                String formattedConfig = formatCheckName(rawConfig);
                if (formattedConfig.contains("(")) {
                    formattedName = formattedConfig;
                }
            }
            this.checkName = formattedName;
            this.defaultDecay = checkData.decay();
            this.defaultSetbackVL = checkData.setback();
            this.alternativeName = checkData.alternativeName();
            this.experimental = checkData.experimental();
            this.defaultDescription = checkData.description();
            this.stableKey = checkData.stableKey();
            this.displayName = this.checkName;
        } else {
            this.defaultDescription = CheckData.DEFAULT_DESCRIPTION;
            this.defaultDecay = CheckData.DEFAULT_DECAY;
            this.defaultSetbackVL = CheckData.DEFAULT_SETBACK;
            this.stableKey = "";
            this.alternativeName = null;
            this.checkName = null;
            this.configName = null;
            this.experimental = false;
        }

        if (this.checkName != null) {
            KNOWN_CHECK_NAMES.add(this.checkName);
            if (this.configName != null) {
                KNOWN_CHECK_NAMES.add(this.configName);
            }
        }

        reload();
    }

    public boolean shouldModifyPackets() {
        return isEnabled
                && !player.disableGrim
                && !player.noModifyPacketPermission
                && !noModifyPacketPermission
                && !exemptPermission;
    }

    /**
     * Evaluated once when CheckManager builds the dispatch arrays.
     * Implementations must only depend on immutable connection properties.
     */
    public boolean isApplicable() {
        return true;
    }

    public final void updatePermissions() {
        if (getConfigName() == null) return;
        final String id = getConfigName().toLowerCase(Locale.ROOT);
        final String cleanName = checkName != null ? checkName.replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT) : id;
        String baseName = null;
        if (displayName != null && displayName.contains("(") && displayName.contains(")")) {
            baseName = displayName.substring(0, displayName.indexOf('(')).replaceAll("[\\s()\\-_.]", "").toLowerCase(Locale.ROOT);
        }
        boolean disableBypass = GrimAPI.INSTANCE.getConfigManager().isDisablePermissionBypass();
        exemptPermission = !disableBypass && (player.hasPermission("grim.exempt." + id)
                || player.hasPermission("darkness.exempt." + id)
                || player.hasPermission("darkness.exempt." + cleanName)
                || player.hasPermission("grim.exempt." + cleanName)
                || (baseName != null && (player.hasPermission("darkness.exempt." + baseName) || player.hasPermission("grim.exempt." + baseName))));
        noSetbackPermission = !disableBypass && (player.hasPermission("grim.nosetback." + id)
                || player.hasPermission("darkness.nosetback." + id)
                || player.hasPermission("darkness.nosetback." + cleanName)
                || player.hasPermission("grim.nosetback." + cleanName)
                || (baseName != null && (player.hasPermission("darkness.nosetback." + baseName) || player.hasPermission("grim.nosetback." + baseName))));
        noModifyPacketPermission = !disableBypass && (player.hasPermission("grim.nomodifypacket." + id)
                || player.hasPermission("darkness.nomodifypacket." + id)
                || player.hasPermission("darkness.nomodifypacket." + cleanName)
                || player.hasPermission("grim.nomodifypacket." + cleanName)
                || (baseName != null && (player.hasPermission("darkness.nomodifypacket." + baseName) || player.hasPermission("grim.nomodifypacket." + baseName))));
    }

    public final boolean flag() {
        return flag("");
    }

    public final boolean flag(String verbose) {
        Supplier<String> alertText = constant(verbose);
        if (recordFlag(alertText)) {
            alert(alertText);
            return true;
        }
        return false;
    }

    public final boolean flag(@NotNull Verbose.Writer verbose) {
        BinaryVerbose binary = lazyVerbose(verbose);
        if (recordFlag(binary)) {
            alert(binary.rendered());
            return true;
        }
        return false;
    }

    public final boolean flag(@NotNull Verbose.Writer verbose, @NotNull Supplier<String> alertText) {
        BinaryVerbose binary = lazyVerbose(verbose);
        if (recordFlag(binary)) {
            alert(memoize(Objects.requireNonNull(alertText, "alertText")));
            return true;
        }
        return false;
    }

    private boolean recordFlag(@NotNull Supplier<String> verbose) {
        if (!isEnabled || player.disableGrim || (experimental && !player.isExperimentalChecks()) || exemptPermission)
            return false; // Avoid calling event if disabled

        if (FLAG_CHANNEL.fire(player, this, verbose)) return false;

        lastFlagStoredBinaryVerbose = false;
        player.punishmentManager.handleViolation(this);
        lastViolationTime = System.currentTimeMillis();
        violations++;
        return true;
    }

    private boolean recordFlag(@NotNull BinaryVerbose verbose) {
        Supplier<String> rendered = verbose.rendered();
        byte[] verboseData = verbose.data();

        if (!isEnabled || player.disableGrim || (experimental && !player.isExperimentalChecks()) || exemptPermission)
            return false; // Avoid calling event if disabled

        if (FLAG_CHANNEL.fire(player, this, rendered)) return false;

        lastFlagStoredBinaryVerbose = true;
        player.punishmentManager.handleViolation(this);
        lastViolationTime = System.currentTimeMillis();
        violations++;
        GrimAPI.INSTANCE.getDataStoreLifecycle().liveWriteHooks()
                .recordFlagDataFromCheck(player, this, violations, verboseData);
        return true;
    }

    private @NotNull BinaryVerbose lazyVerbose(@NotNull Verbose.Writer writer) {
        Objects.requireNonNull(writer, "writer");
        byte[] verboseData = writer.end().toByteArray();
        Verbose template = writer.verbose();
        Supplier<String> rendered = memoize(() -> template.render(verboseData, new VerboseRenderContext(
                player.getClientVersion().getProtocolVersion(),
                GrimAPI.INSTANCE.getPlatformServer().getPlatformImplementationString())));
        return new BinaryVerbose(verboseData, rendered);
    }

    public final void registerVerboseTemplates(@Nullable VerboseRegistry registry) {
        if (registry == null || stableKey.isEmpty()) return;
        String pluginVersion = GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
        for (Verbose template : Verbose.declaredBy(getClass(), Check.class)) {
            registry.registerTemplate(stableKey, checkName, description, pluginVersion, template);
        }
    }

    protected final @NotNull VerboseBuf verbose() {
        return verbose;
    }

    public final boolean flagWithSetback() {
        return flagWithSetback("");
    }

    public final boolean flagWithSetback(String verbose) {
        if (flag(verbose)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final boolean flagWithSetback(@NotNull Verbose.Writer verbose) {
        if (flag(verbose)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final boolean flagWithSetback(@NotNull Verbose.Writer verbose, @NotNull Supplier<String> alertText) {
        if (flag(verbose, alertText)) {
            setbackIfAboveSetbackVL();
            return true;
        }
        return false;
    }

    public final void reward() {
        violations = Math.max(0, violations - decay);
    }

    @Override
    public final void reload(@NotNull ConfigManager configuration) {
        if (getConfigName() != null) {
            Boolean override = getGlobalCheckOverride(getConfigName());
            if (override != null) {
                this.isEnabled = override;
            } else {
                this.isEnabled = configuration.getBooleanElse(getConfigName(),
                        configuration.getBooleanElse(getConfigName() + ".enabled",
                                configuration.getBooleanElse(getConfigName() + ".enable", true)));
            }
            decay = configuration.getDoubleElse(getConfigName() + ".decay", defaultDecay);
            setbackVL = configuration.getDoubleElse(getConfigName() + ".setbackvl", defaultSetbackVL);
            displayName = formatCheckName(configuration.getStringElse(getConfigName() + ".displayname", checkName));
            description = configuration.getStringElse(getConfigName() + ".description", defaultDescription);

            if (setbackVL == -1) setbackVL = Double.MAX_VALUE;
        } else {
            this.isEnabled = true;
        }
        super.reload(configuration);
    }

    public CheckCategory getCategory() {
        String pkg = getClass().getPackageName();
        if (pkg.contains(".combat")) return CheckCategory.COMBAT;
        if (pkg.contains(".movement")) return CheckCategory.MOVEMENT;
        if (pkg.contains(".packets") || pkg.contains(".inventory") || pkg.contains(".scaffold") || pkg.contains(".misc")) return CheckCategory.WORLD;
        return CheckCategory.OTHER;
    }

    public boolean alert(String verbose) {
        return alert(constant(verbose));
    }

    public boolean alert(@NotNull Supplier<String> verbose) {
        return player.punishmentManager.handleAlert(player, memoize(Objects.requireNonNull(verbose, "verbose")), this);
    }

    public boolean setbackIfAboveSetbackVL() {
        if (shouldSetback()) {
            return player.getSetbackTeleportUtil().executeViolationSetback();
        }
        return false;
    }

    public boolean shouldSetback() {
        return !noSetbackPermission && violations > setbackVL;
    }

    public boolean executeViolationSetback() {
        return !noSetbackPermission && player.getSetbackTeleportUtil().executeViolationSetback();
    }

    public String formatOffset(double offset) {
        return offset > 0.001 ? String.format("%.5f", offset) : String.format("%.2E", offset);
    }

    public final boolean flagAndAlert() {
        return flag("");
    }

    public final boolean flagAndAlert(String verbose) {
        return flag(verbose);
    }

    public final boolean flagAndAlertWithSetback() {
        return flagWithSetback("");
    }

    public final boolean flagAndAlertWithSetback(String verbose) {
        return flagWithSetback(verbose);
    }

    protected final boolean flagIf(boolean condition, String message) {
        return condition && flagAndAlert(message);
    }

    protected long time() {
        return System.currentTimeMillis();
    }

    private static @NotNull Supplier<String> constant(String verbose) {
        String value = verbose == null ? "" : verbose;
        return () -> value;
    }

    private static @NotNull Supplier<String> memoize(@NotNull Supplier<String> supplier) {
        return new Supplier<>() {
            private String value;
            private boolean computed;

            @Override
            public synchronized String get() {
                if (!computed) {
                    try {
                        value = supplier.get();
                        if (value == null) value = "";
                    } catch (RuntimeException ignored) {
                        value = "";
                    }
                    computed = true;
                }
                return value;
            }
        };
    }

    private record BinaryVerbose(byte @NotNull [] data, @NotNull Supplier<String> rendered) {}
}
