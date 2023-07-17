package DCT.client.views;

import java.awt.CardLayout;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import DCT.client.Card;
import DCT.client.Client;
import DCT.client.ICardControls;
import DCT.client.IGameEvents;
import DCT.common.Cell;
import DCT.common.CellType;
import DCT.common.Character;
import DCT.common.Phase;
import DCT.common.Character.CharacterType;

public class GamePanel extends JPanel implements IGameEvents {
    private CellPanel[][] cells;
    private JPanel gridPanel;
    private CardLayout cardLayout;

    public GamePanel(ICardControls controls) {
        super(new CardLayout());
        cardLayout = (CardLayout) this.getLayout();
        this.setName(Card.GAME_SCREEN.name());
        Client.INSTANCE.addCallback(this);
        // this is purely for debugging
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                System.out.println("GamePanel Resized to " + e.getComponent().getSize());
            }

            @Override
            public void componentMoved(ComponentEvent e) {
                // System.out.println("Moved to " + e.getComponent().getLocation());
            }
        });
        createReadyPanel();
        createOptionsPanel();
        gridPanel = new JPanel();
        add(gridPanel);
        setVisible(false);
        // don't need to add this to ClientUI as this isn't a primary panel(it's nested
        // in ChatGamePanel)
        // controls.addPanel(Card.GAME_SCREEN.name(), this);
    }

    private void createReadyPanel() {
        JPanel readyPanel = new JPanel();
        JButton readyButton = new JButton();
        readyButton.setText("Ready");
        readyButton.addActionListener(l -> {
            try {
                Client.INSTANCE.sendReadyStatus();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        readyPanel.add(readyButton);
        this.add(readyPanel);
    }

    private void createOptionsPanel() {
        JPanel characterOptions = new JPanel();
        JButton tank = new JButton();
        tank.setText("Tank");
        tank.addActionListener(l -> {
            try {
                Client.INSTANCE.sendCreateCharacter(CharacterType.TANK);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        JButton damage = new JButton();
        damage.setText("Attacker");
        damage.addActionListener(l -> {
            try {
                Client.INSTANCE.sendCreateCharacter(CharacterType.DAMAGE);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        JButton support = new JButton();
        support.setText("Support");
        support.addActionListener(l -> {
            try {
                Client.INSTANCE.sendCreateCharacter(CharacterType.SUPPORT);
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        });
        characterOptions.add(tank);
        characterOptions.add(damage);
        characterOptions.add(support);
        add(characterOptions);
    }

    private void resetView() {
        if (gridPanel == null) {
            return;
        }
        if (gridPanel.getLayout() != null) {
            gridPanel.setLayout(null);
        }
        cells = null;
        gridPanel.removeAll();
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void makeGrid(int rows, int columns) {
        resetView();
        cells = new CellPanel[rows][columns];
        gridPanel.setLayout(new GridLayout(rows, columns));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                cells[i][j] = new CellPanel();
                cells[i][j].setType(CellType.NONE, i, j, false);
                gridPanel.add(cells[i][j]);
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void onClientConnect(long id, String clientName, String message) {
    }

    @Override
    public void onClientDisconnect(long id, String clientName, String message) {
    }

    @Override
    public void onMessageReceive(long id, String message) {
    }

    @Override
    public void onReceiveClientId(long id) {
    }

    @Override
    public void onSyncClient(long id, String clientName) {
    }

    @Override
    public void onResetUserList() {
    }

    @Override
    public void onReceiveRoomList(String[] rooms, String message) {
    }

    @Override
    public void onRoomJoin(String roomName) {
    }

    @Override
    public void onReceivePhase(Phase phase) {
        // I'll temporarily do next(), but there may be scenarios where the screen can
        // be inaccurate
        System.out.println("Received phase: " + phase.name());
        if (phase == Phase.READY) {
            if (!isVisible()) {
                setVisible(true);
                this.getParent().revalidate();
                this.getParent().repaint();
                System.out.println("GamePanel visible");
            } else {
                cardLayout.next(this);
            }
        } else if (phase == Phase.SELECTION) {
            cardLayout.next(this);
        } else if (phase == Phase.PREPARING) {
            cardLayout.next(this);
        }
    }

    @Override
    public void onReceiveReady(long clientId) {
    }

    @Override
    public void onReceiveCell(List<Cell> cells) {
        for (Cell c : cells) {
            CellPanel target = this.cells[c.getX()][c.getY()];
            target.setType(c.getCellType(), c.getX(), c.getY(), c.isBlocked());
            target.setOccupiedCount(c.getCharactersInCell().size());
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    @Override
    public void onReceiveGrid(int rows, int columns) {
        resetView();
        if (rows > 0 && columns > 0) {
            makeGrid(rows, columns);
        }
    }

    @Override
    public void onReceiveCharacter(long clientId, Character character) {
        // kind of a sideways way of interacting with the ChatPanel, will likely
        // refactor this later
        ChatGamePanel cgp = (ChatGamePanel) this.getParent().getParent();
        cgp.getChatPanel().addText(Client.INSTANCE.getClientNameById(clientId) + " summoned " + character.getName());
    }
}
