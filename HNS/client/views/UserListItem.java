package HNS.client.views;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;

import HNS.client.ClientUtils;
import HNS.common.Constants;

public class UserListItem extends JPanel {
    private long clientId;
    private String clientName;
    private long points;
    JEditorPane text = new JEditorPane("text/plain", "");
    JButton hostIndicator = new JButton();
    JButton outIndicator = new JButton("x");

    public UserListItem(String clientName, long clientId) {
        this.clientId = clientId;
        this.clientName = clientName;
        hostIndicator.setEnabled(false);
        hostIndicator.setVisible(false);
        outIndicator.setEnabled(false);
        outIndicator.setBackground(Color.RED);
        outIndicator.setVisible(false);
        Dimension d = new Dimension(24, 24);
        hostIndicator.setPreferredSize(d);
        hostIndicator.setMinimumSize(d);
        hostIndicator.setMaximumSize(d);

        outIndicator.setPreferredSize(d);
        outIndicator.setMinimumSize(d);
        outIndicator.setMaximumSize(d);
        // setBackground(Color.BLUE);

        text.setEditable(false);
        text.setText(getBaseText());
        this.add(hostIndicator);
        this.add(outIndicator);
        this.add(text);
        ClientUtils.clearBackground(text);
    }

    private String getBaseText() {
        return String.format("%s[%s] Pts.(%s)", clientName, clientId, points);
    }

    public long getClientId() {
        return clientId;
    }

    public void setSeeker(long clientId) {
        if (this.clientId == clientId && clientId != Constants.DEFAULT_CLIENT_ID) {
            this.setBackground(Color.CYAN);
        } else {
            this.setBackground(Color.WHITE);
        }
        revalidate();
        repaint();
    }

    public void setOut(long clientId) {
        if (this.clientId == clientId && clientId != Constants.DEFAULT_CLIENT_ID) {
            outIndicator.setVisible(true);
        } else {
            outIndicator.setVisible(false);
        }
        revalidate();
        repaint();
    }

    public void setPoints(long points) {
        this.points = points;
        text.setText(getBaseText());
        repaint();
    }

    public void setHost(long clientId) {
        if (this.clientId == clientId && clientId != Constants.DEFAULT_CLIENT_ID) {
            hostIndicator.setBackground(Color.GREEN);
            hostIndicator.setVisible(true);
        } else {
            hostIndicator.setBackground(Color.WHITE);
            hostIndicator.setVisible(false);
        }
        revalidate();
        repaint();
    }
}
