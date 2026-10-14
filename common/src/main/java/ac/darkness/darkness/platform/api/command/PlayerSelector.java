package ac.darkness.darkness.platform.api.command;

import ac.darkness.darkness.platform.api.sender.Sender;

import java.util.Collection;

public interface PlayerSelector {
    boolean isSingle();

    Sender getSinglePlayer(); // Throws an exception if not a single selection

    Collection<Sender> getPlayers();

    String inputString();
}
