package com.mt.examples.drawing;

import java.io.Serializable;
/***
 * Data packet to send between client and server
 * @author matt
 *
 */
public class Payload implements Serializable{
	private static final long serialVersionUID = -7358730485627664992L;
	public String ipAddress;
	public String target;
	public PayloadType payloadType;
	public int x;
	public int y;
	public String extra = null;
	
	public Payload(String address, PayloadType type) {
		this(address, type, 0,0, null);
	}
	public Payload(String address, PayloadType type, int x, int y) {
		this(address, type, x, y, null);
	}
	public Payload(String address, PayloadType type, int x, int y, String extra) {
		this(address, type, x, y, extra, null);
	}
	public Payload(String address, PayloadType type, int x, int y, String extra, String target) {
		ipAddress = address;
		payloadType = type;
		this.x = x;
		this.y = y;
		this.extra = extra;
		this.target = target;
	}
	
	@Override
	public String toString() {
		return this.ipAddress + "-" + this.payloadType.toString() + "(" + x + "," + y +") - " + extra;
	}
}
