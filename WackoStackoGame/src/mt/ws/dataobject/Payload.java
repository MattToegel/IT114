package mt.ws.dataobject;
import java.io.Serializable;

import org.dyn4j.geometry.Vector2;
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625037986217386003L;
	private int id;
	public void setID(int id) {
		this.id = id;
	}
	public int getID() {
		return id;
	}
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
	private String clientName;
	public void setClientName(String cn) {
		this.clientName = cn;
	}
	public String getClientName() {
		return this.clientName;
	}
	private Vector2 position;
	private Vector2 speed;
	private Vector2 direction;
	public Vector2 getPosition() {
		return position;
	}
	public void setPosition(Vector2 position) {
		this.position = position;
	}
	public Vector2 getSpeed() {
		return speed;
	}
	public void setSpeed(Vector2 speed) {
		this.speed = speed;
	}
	public Vector2 getDirection() {
		return direction;
	}
	public void setDirection(Vector2 direction) {
		this.direction = direction;
	}
	
	
	@Override
	public String toString() {
		return String.format("Type[%s], x,y[%s,%s], Message[%s]",
					getPayloadType().toString(), position.x, position.y, getMessage());
	}
}
