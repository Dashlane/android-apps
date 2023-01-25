

package com.github.devnied.emvnfccard.model;

import java.util.Date;



public class EmvTrack2 extends AbstractData {

	

	private static final long serialVersionUID = -2906133619803198319L;

	

	private byte[] raw;

	

	private String cardNumber;

	

	private Date expireDate;

	

	private Service service;

	

	public byte[] getRaw() {
		return raw;
	}

	

	public void setRaw(final byte[] raw) {
		this.raw = raw;
	}

	

	public String getCardNumber() {
		return cardNumber;
	}

	

	public void setCardNumber(final String cardNumber) {
		this.cardNumber = cardNumber;
	}

	

	public Date getExpireDate() {
		return expireDate;
	}

	

	public void setExpireDate(final Date expireDate) {
		this.expireDate = expireDate;
	}

	

	public Service getService() {
		return service;
	}

	

	public void setService(final Service service) {
		this.service = service;
	}

}
