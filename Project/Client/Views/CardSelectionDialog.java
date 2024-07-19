package Project.Client.Views;

import Project.Common.Card;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class CardSelectionDialog extends JDialog {
    private Card selectedCard;
    private boolean isDiscarded;

    public CardSelectionDialog(Frame parent, List<Card> cards) {
        super(parent, "Select a Card", true);
        setLayout(new BorderLayout());

        // Set preferred width and make the dialog resizable
        setPreferredSize(new Dimension(600, 400)); // Increased size to better fit the content
        setResizable(true);

        JPanel cardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        
        for (Card card : cards) {
            JPanel cardContainer = new JPanel();
            cardContainer.setLayout(new BoxLayout(cardContainer, BoxLayout.Y_AXIS));
            cardContainer.setBorder(BorderFactory.createLineBorder(Color.BLACK));
            cardContainer.setPreferredSize(new Dimension(200, 150));
            
            JLabel nameLabel = new JLabel(card.getName());
            JLabel descriptionLabel = new JLabel("<html>" + card.getDescription() + "</html>");
            JLabel energyLabel = new JLabel("Energy: " + card.getEnergy());
            JLabel typeLabel = new JLabel("Type: " + card.getType().name());

            JButton useButton = new JButton("Use");
            useButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedCard = card;
                    isDiscarded = false;
                    setVisible(false);
                }
            });

            JButton discardButton = new JButton("Discard");
            discardButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    selectedCard = card;
                    isDiscarded = true;
                    setVisible(false);
                }
            });

            cardContainer.add(nameLabel);
            cardContainer.add(descriptionLabel);
            cardContainer.add(energyLabel);
            cardContainer.add(typeLabel);
            cardContainer.add(useButton);
            cardContainer.add(discardButton);

            cardPanel.add(cardContainer);
        }

        // Wrap the card panel in a JScrollPane to handle overflow
        JScrollPane scrollPane = new JScrollPane(cardPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JButton dismissButton = new JButton("Dismiss");
        dismissButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectedCard = null;
                setVisible(false);
            }
        });

        add(scrollPane, BorderLayout.CENTER);
        add(dismissButton, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(parent);
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public boolean isDiscarded() {
        return isDiscarded;
    }
}
