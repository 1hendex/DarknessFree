package ac.darkness.darkness.command.commands;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.checks.Check;
import ac.darkness.darkness.checks.CheckCategory;
import ac.darkness.darkness.command.BuildableCommand;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.MessageUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.permission.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class GrimChecks implements BuildableCommand {

    @Override
    public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
        Permission checksPermission = Permission.anyOf(
                Permission.permission("darkness.checks"),
                Permission.permission("grim.checks")
        );

        commandManager.command(
                commandManager.commandBuilder("darkness")
                        .literal("checks")
                        .permission(checksPermission)
                        .handler(this::handleChecks)
        );
    }

    private void handleChecks(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        var config = GrimAPI.INSTANCE.getConfigManager().getConfig();

        GrimPlayer targetPlayer = null;
        if (sender.isPlayer()) {
            targetPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(sender.getUniqueId());
        } else {
            targetPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getEntries().stream().findFirst().orElse(null);
        }

        if (targetPlayer == null) {
            sender.sendMessage(Component.text("§cOnly players can view the checks list GUI/Message!"));
            return;
        }

        final GrimPlayer grimPlayer = targetPlayer;
        String typeSpace = config.getStringElse("checks_command.space", " ");
        String typeNone = config.getStringElse("checks_command.not_found_type", "&8» &cОтсутствуют.");
        String experimentalSymbol = config.getStringElse("experimental_symbol", "*");
        String separator = config.getStringElse("checks_command.separator", "&7, ");

        CompletableFuture.supplyAsync(() -> {
            Map<CheckCategory, List<Component>> categoryList = new Object2ObjectLinkedOpenHashMap<>();
            for (CheckCategory category : CheckCategory.values()) {
                if (category == CheckCategory.OTHER) continue;

                List<Check> checks = new ObjectArrayList<>(grimPlayer.getChecksByCategory(category));
                List<Component> messageLines = new ObjectArrayList<>();
                messageLines.add(MessageUtil.miniMessage(typeSpace));
                String categoryTitle = config.getStringElse("checks_command.type", "&8» &7Категория проверки: &f%checkType%")
                        .replace("%checkType%", category.displayName());
                messageLines.add(MessageUtil.miniMessage(categoryTitle));

                if (checks.isEmpty()) {
                    messageLines.add(MessageUtil.miniMessage(typeNone));
                } else {
                    checks.sort((c1, c2) -> c1.getCheckName().compareToIgnoreCase(c2.getCheckName()));
                    for (int i = 0; i < checks.size(); i += 20) {
                        int end = Math.min(i + 20, checks.size());
                        List<Check> subList = checks.subList(i, end);
                        StringBuilder checkListBuilder = new StringBuilder();

                        for (Check check : subList) {
                            String checkNameKey = check.isEnabled()
                                    ? (check.isExemptPermission() ? "checks_command.check.exempted" : "checks_command.check.enabled")
                                    : "checks_command.check.disabled";

                            String checkFormat = config
                                    .getStringElse(checkNameKey, "&a%checkName%%experimental%")
                                    .replace("%checkName%", check.getCheckName())
                                    .replace("%experimental%", check.isExperimental() ? experimentalSymbol : "");

                            checkListBuilder.append(checkFormat).append(separator);
                        }

                        String finalRow = checkListBuilder.toString();
                        if (finalRow.endsWith(separator)) {
                            finalRow = finalRow.substring(0, finalRow.length() - separator.length());
                        }
                        messageLines.add(MessageUtil.miniMessage(finalRow));
                    }
                }
                messageLines.add(MessageUtil.miniMessage(typeSpace));
                categoryList.put(category, messageLines);
            }
            return categoryList;
        }).exceptionally(e -> {
            e.printStackTrace();
            sender.sendMessage(MessageUtil.miniMessage(
                    config.getStringElse("checks_command.not_found_total",
                            "&cНе найдено чеков в группе") + " (Проверьте консоль)"));
            return null;
        }).thenAccept(categoryMap -> {
            if (categoryMap == null) return;
            String border = config.getStringElse("checks_command.space", " ");
            sender.sendMessage(MessageUtil.miniMessage(border));

            for (CheckCategory category : CheckCategory.values()) {
                if (category == CheckCategory.OTHER) continue;
                List<Component> lines = categoryMap.get(category);
                if (lines != null) {
                    for (Component line : lines) {
                        sender.sendMessage(line);
                    }
                }
            }
            sender.sendMessage(MessageUtil.miniMessage(border));
        });
    }
}
