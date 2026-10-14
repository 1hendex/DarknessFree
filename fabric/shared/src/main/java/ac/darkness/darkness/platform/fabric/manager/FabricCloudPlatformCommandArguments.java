package ac.darkness.darkness.platform.fabric.manager;

import ac.darkness.darkness.platform.api.command.PlayerSelector;
import ac.darkness.darkness.platform.api.manager.cloud.CloudPlatformCommandArguments;
import ac.darkness.darkness.platform.api.sender.Sender;
import ac.darkness.darkness.platform.fabric.AbstractGrimACFabricEntryPoint;
import ac.darkness.darkness.platform.fabric.command.FabricPlayerSelectorParser;
import ac.darkness.darkness.platform.fabric.inject.FabricServerPlayerHandle;
import lombok.RequiredArgsConstructor;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.Suggestion;
import org.incendo.cloud.suggestion.SuggestionProvider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
public class FabricCloudPlatformCommandArguments implements CloudPlatformCommandArguments {

    private final FabricPlayerSelectorParser<Sender> fabricPlayerSelectorParser;

    @Override
    public ParserDescriptor<Sender, PlayerSelector> singlePlayerSelectorParser() {
        return fabricPlayerSelectorParser.descriptor();
    }

    @Override
    public SuggestionProvider<Sender> onlinePlayerSuggestions() {
        return (context, input) -> {
            Collection<FabricServerPlayerHandle> players = AbstractGrimACFabricEntryPoint.server().onlinePlayers();
            List<Suggestion> suggestions = new ArrayList<>(players.size());

            for (FabricServerPlayerHandle player : players) {
                suggestions.add(Suggestion.suggestion(player.usernameString()));
            }

            return CompletableFuture.completedFuture(suggestions);
        };
    }
}
