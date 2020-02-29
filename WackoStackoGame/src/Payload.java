import java.io.Serializable;
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625037986217386003L;
	private String message;
	public void setMessage(String s) {
		this.message = s;
	}
	public String getMessage() {
		return this.message;
	}
	
	private PayloadType payloadType;
	public void setPayloadType(PayloadType pt) {
		this.payloadType = pt;
	}
	public PayloadType getPayloadType() {
		return this.payloadType;
	}
	private int x;
	public void setX(int x) {
		this.x = x;
	}
	public int getX() {
		return this.x;
	}
	private int y;
	public void setY(int y) {
		this.y = y;
	}
	public int getY() {
		return this.y;
	}
	
	@Override
	public String toString() {
		return String.format("Type[%s], x,y[%s,%s], Message[%s]",
					getPayloadType().toString(), getX(), getY(), getMessage());
	}
}
