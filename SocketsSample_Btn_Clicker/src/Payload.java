import java.io.Serializable;
public class Payload implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6625037986217386003L;
	private String message;
	private boolean isOn = false;
	public void IsOn(boolean isOn) {
		this.isOn = isOn;
	}
	public boolean IsOn() {
		return this.isOn;
	}
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
	
	private int number;
	public void setNumber(int n) {
		this.number = n;
	}
	public int getNumber() {
		return this.number;
	}
	@Override
	public String toString() {
		return String.format("Type[%s], isOn[%s], Number[%s], Message[%s]",
					getPayloadType().toString(), IsOn()+"", getNumber(), getMessage());
	}
}
