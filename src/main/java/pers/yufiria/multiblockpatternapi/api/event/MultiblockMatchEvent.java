package pers.yufiria.multiblockpatternapi.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import pers.yufiria.multiblockpatternapi.api.MatchResult;

public class MultiblockMatchEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MatchResult result;

    public MultiblockMatchEvent(MatchResult result) {
        this.result = result;
    }

    public MatchResult getResult() {
        return result;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
