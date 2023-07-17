package DCT.client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class TestUI {
    public static void main(String[] args) {
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setPreferredSize(new Dimension(500, 500));
        frame.setLayout(new CardLayout());

        JPanel mixed = new JPanel();
        mixed.setLayout(new BorderLayout(0, 0));

        JPanel left = new JPanel();
        left.setLayout(new BorderLayout());
        left.setVisible(false);
        left.setBackground(Color.RED);
        JPanel right = new JPanel();
        right.setBackground(Color.GREEN);
        right.setLayout(new BorderLayout());

        JPanel container = new JPanel(new BorderLayout(0, 0));

        right.add(container, BorderLayout.CENTER);

        JPanel text = new JPanel();
        text.setBackground(Color.BLUE);

        JPanel list = new JPanel();
        list.setBackground(Color.GRAY);

        JSplitPane splitPaneInner = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, text, list);
        splitPaneInner.setResizeWeight(.7);
        splitPaneInner.setOneTouchExpandable(false);
        splitPaneInner.setEnabled(false);
        container.add(splitPaneInner, BorderLayout.CENTER);

        final JSplitPane splitPaneOuter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, right);
        splitPaneOuter.setResizeWeight(.6);
        splitPaneOuter.setOneTouchExpandable(false);
        splitPaneOuter.setEnabled(false);
        mixed.add(splitPaneOuter, BorderLayout.CENTER);

        // Add a ComponentListener to the left panel
        left.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // Recalculate the divider location when the left panel becomes visible
                splitPaneOuter.setDividerLocation(0.6);
            }
        });

        frame.add(mixed);
        frame.pack();
        frame.setVisible(true);
        new Timer(5000, e -> {
            left.setVisible(true);
        }).start();
    }
}
