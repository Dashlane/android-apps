

package com.github.devnied.emvnfccard.model;



public class Afl {

	

	private int sfi;

	

	private int firstRecord;

	

	private int lastRecord;

	

	private boolean offlineAuthentication;

	

	public int getSfi() {
		return sfi;
	}

	

	public void setSfi(final int sfi) {
		this.sfi = sfi;
	}

	

	public int getFirstRecord() {
		return firstRecord;
	}

	

	public void setFirstRecord(final int firstRecord) {
		this.firstRecord = firstRecord;
	}

	

	public int getLastRecord() {
		return lastRecord;
	}

	

	public void setLastRecord(final int lastRecord) {
		this.lastRecord = lastRecord;
	}

	

	public boolean isOfflineAuthentication() {
		return offlineAuthentication;
	}

	

	public void setOfflineAuthentication(final boolean offlineAuthentication) {
		this.offlineAuthentication = offlineAuthentication;
	}

}
