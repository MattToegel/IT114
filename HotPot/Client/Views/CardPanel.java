package HotPot.Client.Views;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.Timer;

import HotPot.Common.Card;
import HotPot.Client.Client;

public class CardPanel extends JPanel {
    private Card card;
    private boolean clicked = false;

    public CardPanel(Card card) {
        this.card = card;

         // Use GridBagLayout for flexible stacking
         this.setLayout(new GridBagLayout());
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.fill = GridBagConstraints.HORIZONTAL;
         gbc.weightx = 1.0; // Use full width of the panel
         gbc.insets = new Insets(5, 5, 5, 5); // Add padding around elements
 
         // Add Name (20% height)
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 1.0; // Full width
         gbc.weighty = 0.2; // 20% height
         JLabel nameLabel = new JLabel("Name: " + card.getName());
         nameLabel.setOpaque(false); // Make it transparent to mouse events
         nameLabel.setFocusable(false); // Prevent it from taking focus
         this.add(nameLabel, gbc);
 
         // Add Description (60% height)
         gbc.gridy = 1;
         gbc.weighty = 0.6; // 60% height
         JTextArea descriptionArea = new JTextArea("Description: " + card.getDescription());
         descriptionArea.setLineWrap(true);
         descriptionArea.setWrapStyleWord(true);
         descriptionArea.setEditable(false);
         descriptionArea.setOpaque(false); // Make it transparent to mouse events
         descriptionArea.setFocusable(false); // Prevent it from taking focus
         descriptionArea.setBorder(null);
         this.add(descriptionArea, gbc);
 
         // Add Value (20% height)
         gbc.gridy = 2;
         gbc.weighty = 0.2; // 20% height
         JLabel valueLabel = new JLabel("Value: " + card.getValue());
         valueLabel.setOpaque(false); // Make it transparent to mouse events
         valueLabel.setFocusable(false); // Prevent it from taking focus
         this.add(valueLabel, gbc);

        // Add a mouse listener (unchanged)
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!clicked) {
                    clicked = true;
                    try {
                        Client.INSTANCE.sendUseCard(card);
                    } catch (IOException e1) {
                        clicked = false;
                        e1.printStackTrace();
                    }
                    new Timer(1500, (action) -> clicked = false).start();
                }
            }
        });

        // Optional: Set a preferred size for the card panel
        this.setPreferredSize(new Dimension(75, 125));
        this.setBorder(BorderFactory.createLineBorder(Color.BLACK)); // Add a border for clarity
    }

}
