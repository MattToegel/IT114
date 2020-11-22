package server;

import java.awt.Point;
import java.io.Serializable;

public class Payload implements Serializable {

    /**
     * baeldung.com/java-serial-version-uid
     */
    private static final long serialVersionUID = -6687715510484845707L;

    private String clientName;// ~2 bytes per character

    public void setClientName(String s) {
	this.clientName = s;
    }

    public String getClientName() {
	return clientName;
    }

    private String message;// ~2 bytes per character

    public void setMessage(String s) {
	this.message = s;
    }

    public String getMessage() {
	return this.message;
    }

    private PayloadType payloadType;// 4 bytes

    public void setPayloadType(PayloadType pt) {
	this.payloadType = pt;
    }

    public PayloadType getPayloadType() {
	return this.payloadType;
    }

    private int number;// 4 bytes

    public void setNumber(int n) {
	this.number = n;
    }

    public int getNumber() {
	return this.number;
    }

    int x = 0;// 4 bytes
    int y = 0;// 4 bytes

    public void setPoint(Point p) {
	x = p.x;
	y = p.y;
    }

    public Point getPoint() {
	return new Point(x, y);
    }

    // added so two sets of x,y could be sent
    int x2 = 0;// 4 bytes
    int y2 = 0;// 4 bytes

    public void setPoint2(Point p) {
	x2 = p.x;
	y2 = p.y;
    }

    boolean flag = false;// 1 bit

    public void setFlag(boolean flag) {
	this.flag = flag;
    }

    public boolean getFlag() {
	return flag;
    }

    public Point getPoint2() {
	return new Point(x2, y2);
    }

    @Override
    public String toString() {
	return String.format("Type[%s], Number[%s], Message[%s], P1[%s], P2[%s]", getPayloadType().toString(),
		getNumber(), getMessage(), getPoint(), getPoint2());
    }
}