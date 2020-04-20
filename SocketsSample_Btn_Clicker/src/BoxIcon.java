import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JButton;

class BoxIcon implements Icon {
  private Color color;
  private String text;
  private int borderWidth;
  private int width;
  private int height;
  private JButton parent;
  BoxIcon(Color color, int width, int height, int borderWidth) {
    this.color = color;
    this.borderWidth = borderWidth;
    this.width = width;
    this.height = height;
  }
  public void setParent(JButton b) {
	  parent = b;
  }
  public void setText(String text) {
	  this.text = text;
  }
  public void setColor(Color c) {
	  this.color = c;
  }
  public int getIconWidth() {
    return width;
  }

  public int getIconHeight() {
    return height;
  }

  public void paintIcon(Component c, Graphics g, int x, int y) {
    g.setColor(Color.black);
    g.fillRect(x, y, getIconWidth(), getIconHeight());
    if(parent != null) {
    	if(parent.getText().contains("Off")) {
    		color = Color.RED;
    	}
    	else {
    		color = Color.green;
    	}
    }
    g.setColor(color);
    g.fillRect(x + borderWidth, y + borderWidth, getIconWidth() - 2 * borderWidth,
        getIconHeight() - 2 * borderWidth);
    g.setColor(Color.black);
    if(parent == null) {
    	return;
    }
    g.drawString(parent.getText(), width/2,height/2);
  }
}