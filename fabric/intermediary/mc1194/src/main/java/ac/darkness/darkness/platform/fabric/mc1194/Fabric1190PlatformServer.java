package ac.darkness.darkness.platform.fabric.mc1194;

import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.fabric.GrimACFabricIntermediaryLoaderPlugin;
import ac.darkness.darkness.platform.fabric.mc1171.Fabric1171PlatformServer;
import net.minecraft.commands.CommandSourceStack;

public class Fabric1190PlatformServer extends Fabric1171PlatformServer {
    @Override
    public void dispatchCommand(Sender sender, String command) {
        CommandSourceStack commandSource = GrimACFabricIntermediaryLoaderPlugin.LOADER.getFabricSenderFactory().unwrap(sender);
        GrimACFabricIntermediaryLoaderPlugin.FABRIC_SERVER.getCommands().performPrefixedCommand(commandSource, command);
    }
}
