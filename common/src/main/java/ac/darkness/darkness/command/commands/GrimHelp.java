package ac.darkness.darkness.command.commands;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.command.BuildableCommand;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.utils.anticheat.MessageUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.description.Description;
import org.jetbrains.annotations.NotNull;

public class GrimHelp implements BuildableCommand {
    @Override
    public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
        commandManager.command(
                commandManager.commandBuilder("darkness")
                        .literal("help", Description.of("Display help information"))
                        .permission("grim.help")
                        .handler(this::handleHelp)
        );
    }

    private void handleHelp(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();

        for (String string : GrimAPI.INSTANCE.getConfigManager().getConfig().getStringList("help")) {
            if (string == null) continue;
            string = MessageUtil.replacePlaceholders(sender, string);
            sender.sendMessage(MessageUtil.miniMessage(string));
        }
    }
}
