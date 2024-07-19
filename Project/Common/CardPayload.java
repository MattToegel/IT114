package Project.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CardPayload extends XYPayload {
    private List<Card> cards;

    public List<Card> getCards() {
        return cards;
    }

    public Card getCard() {
        return cards.get(0);
    }

    public void setCard(Card card) {
        cards = new ArrayList<Card>();

        cards.add(Card.copy(card));
    }

    public void setCards(List<Card> cards) {
        this.cards = cards.stream().map(c->Card.copy(c)).collect(Collectors.toList());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(super.toString()); // Invoke superclass's toString method if it provides useful information
        sb.append("Cards:\n");
        if (cards != null) {
            for (Card card : cards) {
                sb.append(card.toString()).append("\n");
            }
        } else {
            sb.append("No cards available\n");
        }
        return sb.toString();
    }
}
