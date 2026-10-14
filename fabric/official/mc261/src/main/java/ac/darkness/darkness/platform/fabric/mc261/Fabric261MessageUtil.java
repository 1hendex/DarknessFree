package ac.darkness.darkness.platform.fabric.mc261;

import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.fabric.utils.message.IFabricMessageUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class Fabric261MessageUtil implements IFabricMessageUtil {
    @Override
    public Object textLiteral(String message) {
        return Component.literal(message);
    }

    @Override
    public void sendMessage(Sender target, Object message, boolean overlay) {
        ((CommandSourceStack) (Object) target).sendSuccess(() -> (Component) message, overlay);
    }
}
