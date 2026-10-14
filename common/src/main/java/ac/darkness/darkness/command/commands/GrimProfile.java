package ac.darkness.darkness.command.commands;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.command.BuildableCommand;
import ac.darkness.darkness.platform.api.command.PlayerSelector;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.player.PlatformPlayer;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.player.GrimPlayer;
import ac.darkness.darkness.utils.anticheat.MessageUtil;
import net.kyori.adventure.text.Component;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GrimProfile implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
        commandManager.command(
                commandManager.commandBuilder("darkness")
                        .literal("profile")
                        .permission("grim.profile")
                        .required("target", arguments.singlePlayerSelectorParser())
                        .handler(this::handleProfile)
        );
    }

    private void handleProfile(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        PlayerSelector target = context.get("target");

        PlatformPlayer targetPlatformPlayer = target.getSinglePlayer().getPlatformPlayer();
        if (Objects.requireNonNull(targetPlatformPlayer, "targetPlatformPlayer").isExternalPlayer()) {
            sender.sendMessage(MessageUtil.getParsedComponent(sender,"player-not-this-server", "%prefix% &cThis player isn't on this server!"));
            return;
        }

        GrimPlayer grimPlayer = GrimAPI.INSTANCE.getPlayerDataManager().getPlayer(targetPlatformPlayer.getUniqueId());
        if (grimPlayer == null) {
            sender.sendMessage(MessageUtil.getParsedComponent(sender, "player-not-found", "%prefix% &cPlayer is exempt or offline!"));
            return;
        }

        java.util.List<String> profileMessages = GrimAPI.INSTANCE.getConfigManager().getConfig().getStringList("profile_command");
        if (profileMessages == null || profileMessages.isEmpty()) {
            profileMessages = GrimAPI.INSTANCE.getConfigManager().getConfig().getStringList("profile");
        }
        if (profileMessages != null) {
            for (String message : profileMessages) {
                final Component component = MessageUtil.miniMessage(message);
                sender.sendMessage(MessageUtil.replacePlaceholders(grimPlayer, component));
            }
        }
    }
}
