package HotPot.Client.Interfaces;

import java.util.List;

import HotPot.Common.ScoreboardRecord;

public interface IScoreEvents extends IGameEvents{
    void onReceiveScoreboard(List<ScoreboardRecord> records);
}
