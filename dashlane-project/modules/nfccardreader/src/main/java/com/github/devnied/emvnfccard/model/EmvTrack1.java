

package com.github.devnied.emvnfccard.model;

import java.util.Date;



public class EmvTrack1 extends AbstractData {

	

	private static final long serialVersionUID = 6619730513813482135L;

	

	private byte[] raw;

	

	private String formatCode;

	

	private String cardNumber;

	

	private Date expireDate;

	

	private String holderLastname;

	

	private String holderFirstname;

	

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

	

	public String getFormatCode() {
		return formatCode;
	}

	

	public void setFormatCode(final String formatCode) {
		this.formatCode = formatCode;
	}

	

	public String getHolderLastname() {
		return holderLastname;
	}

	

	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	

	public String getHolderFirstname() {
		return holderFirstname;
	}

	

	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	

	public Service getService() {
		return service;
	}

	

	public void setService(final Service service) {
		this.service = service;
	}

}
