package DCT.client.views;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import DCT.client.Card;
import DCT.client.ICardControls;

public class Menu extends JMenuBar {
    public Menu(ICardControls controls) {
        JMenu roomsMenu = new JMenu("Rooms");
        JMenuItem roomsSearch = new JMenuItem("Search");
        roomsSearch.addActionListener((event) -> {
            controls.show(Card.ROOMS.name());
        });
        roomsMenu.add(roomsSearch);
        this.add(roomsMenu);
    }
}
