package HotPot.Client.Interfaces;

import java.util.List;

import HotPot.Common.Card;

public interface ICardGameEvents  extends IGameEvents{
    void onHandChange(List<Card> cards);

    void onReceivePercentage(int percentage);
}
