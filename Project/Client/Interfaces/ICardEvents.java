package Project.Client.Interfaces;

import java.util.List;

import Project.Common.Card;

public interface ICardEvents extends IGameEvents {
    /**
     * Passes a Card reference to be added to a list
     * @param card
     */
    void onAddCard(Card card);

    /**
     * Passes a Card reference to be removed from a list
     * @param card
     */
    void onRemoveCard(Card card);

    /**
     * Passes a list of Cards to be set (copied) to a list
     * @param cards
     */
    void onSetCards(List<Card> cards);
}
