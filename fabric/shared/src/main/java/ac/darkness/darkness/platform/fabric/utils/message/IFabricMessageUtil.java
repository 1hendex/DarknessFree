package ac.darkness.darkness.platform.fabric.utils.message;

import ac.darkness.darkness.platform.api.sender.Sender;

public interface IFabricMessageUtil {
    Object textLiteral(String message);

    void sendMessage(Sender target, Object message, boolean overlay);
}
