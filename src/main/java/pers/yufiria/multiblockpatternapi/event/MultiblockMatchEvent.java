package pers.yufiria.multiblockpatternapi.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import pers.yufiria.multiblockpatternapi.api.MatchResult;

public class MultiblockMatchEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final MatchResult result;
    private final Player player;

    public MultiblockMatchEvent(MatchResult result, Player player) {
        this.result = result;
        this.player = player;
    }

    public MatchResult getResult() {
        return result;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }
}
