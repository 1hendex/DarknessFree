package ac.darkness.darkness.checks.debug;

import ac.darkness.darkness.checks.GrimProcessor;
import ac.darkness.darkness.player.GrimPlayer;

public abstract class AbstractDebugHandler extends GrimProcessor {
    public AbstractDebugHandler(GrimPlayer player) {
        super(player);
    }

    public abstract void toggleListener(GrimPlayer player);

    public abstract boolean toggleConsoleOutput();
}
