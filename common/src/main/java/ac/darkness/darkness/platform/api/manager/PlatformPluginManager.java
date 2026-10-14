package ac.darkness.darkness.platform.api.manager;

import ac.darkness.darkness.platform.api.PlatformPlugin;

public interface PlatformPluginManager {

    PlatformPlugin[] getPlugins();

    PlatformPlugin getPlugin(String pluginName);

    default boolean isPluginEnabled(String pluginName) {
        PlatformPlugin plugin = getPlugin(pluginName);
        return plugin != null && plugin.isEnabled();
    }
}
