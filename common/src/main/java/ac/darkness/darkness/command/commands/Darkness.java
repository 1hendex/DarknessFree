package ac.darkness.darkness.command.commands;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.command.BuildableCommand;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.utils.anticheat.MessageUtil;
import org.incendo.cloud.CommandManager;
import org.incendo.cloud.context.CommandContext;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class Darkness implements BuildableCommand {

    @Override
    public void register(CommandManager<Sender> commandManager, CloudPlatformCommandArguments arguments) {
        commandManager.command(
                commandManager.commandBuilder("darkness")
                        .handler(this::handleDefault)
        );
    }

    private void handleDefault(@NotNull CommandContext<Sender> context) {
        Sender sender = context.sender();
        String authors;
        try {
            Collection<String> authorsList = GrimAPI.INSTANCE.getGrimPlugin().getDescription().getAuthors();
            if (authorsList != null && !authorsList.isEmpty() && !authorsList.contains("Darkness")) {
                authors = String.join(", ", authorsList);
            } else {
                authors = "1hendex, clopdisabler";
            }
        } catch (Throwable t) {
            authors = "1hendex, clopdisabler";
        }
        String version = GrimAPI.INSTANCE.getExternalAPI().getGrimVersion();
        String message = "%prefix% &fThis server is running &cDarknessFree&f version &cv" + version + " &fby&c " + authors;
        message = MessageUtil.replacePlaceholders(sender, message);
        sender.sendMessage(MessageUtil.miniMessage(message));
    }
}
