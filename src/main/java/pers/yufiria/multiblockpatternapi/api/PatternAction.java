package pers.yufiria.multiblockpatternapi.api;

@FunctionalInterface
public interface PatternAction {
    void onMatch(MatchResult result);
}
