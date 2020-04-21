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
	
	private int id;
	public void setId(int n) {
		this.id = n;
	}
	public int getId() {
		return this.id;
	}
	private int x;
	private int y;
	public void setX(int x) {
		this.x = x;
	}
	public void setY(int y) {
		this.y = y;
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	@Override
	public String toString() {
		return String.format("Type[%s], id[%s], x,y(%s,%s) Message[%s]",
					getPayloadType().toString(), getId()+"", getX()+"", getY()+"", getMessage());
	}
}
