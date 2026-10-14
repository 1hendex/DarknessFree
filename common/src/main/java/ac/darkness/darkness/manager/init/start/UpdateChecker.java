package ac.darkness.darkness.manager.init.start;

import ac.darkness.darkness.GrimAPI;
import ac.darkness.darkness.command.commands.GrimVersion;

public class UpdateChecker implements StartableInitable {
    @Override
    public void start() {
        if (GrimAPI.INSTANCE.getConfigManager().getConfig().getBooleanElse("check-for-updates", true)) {
            GrimVersion.checkForUpdatesAsync(GrimAPI.INSTANCE.getPlatformServer().getConsoleSender());
        }
    }
}
