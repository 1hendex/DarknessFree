package ac.darkness.darkness.manager.init.stop;

import ac.darkness.darkness.manager.init.Initable;

public interface StoppableInitable extends Initable {
    void stop();
}
