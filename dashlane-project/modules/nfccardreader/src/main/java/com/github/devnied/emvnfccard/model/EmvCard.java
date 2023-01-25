

package com.github.devnied.emvnfccard.model;

import com.github.devnied.emvnfccard.enums.EmvCardScheme;
import com.github.devnied.emvnfccard.model.enums.CardStateEnum;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;



public class EmvCard extends AbstractData {

	

	private static final long serialVersionUID = 736740432469989941L;

	

	private CPLC cplc;
	
	

	private String holderLastname;

	

	private String holderFirstname;

	

	private EmvCardScheme type;
	
	

	private String at;

	

	private Collection<String> atrDescription;

	

	private EmvTrack2 track2;

	

	private EmvTrack1 track1;

	

	private String bic;

	

	private String iban;

	

	private final List<Application> applications = new ArrayList<Application>();

	

	private CardStateEnum state = CardStateEnum.UNKNOWN;

	

	public String getHolderLastname() {
		String ret = holderLastname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderLastname();
		}
		return ret;
	}

	

	public void setHolderLastname(final String holderLastname) {
		this.holderLastname = holderLastname;
	}

	

	public String getHolderFirstname() {
		String ret = holderFirstname;
		if (ret == null && track1 != null) {
			ret = track1.getHolderFirstname();
		}
		return ret;
	}

	

	public void setHolderFirstname(final String holderFirstname) {
		this.holderFirstname = holderFirstname;
	}

	

	public String getCardNumber() {
		String ret = null;
		if (track2 != null) {
			ret = track2.getCardNumber();
		}
		if (ret == null && track1 != null) {
			ret = track1.getCardNumber();
		}
		return ret;
	}

	

	public Date getExpireDate() {
		Date ret = null;
		if (track2 != null) {
			ret = track2.getExpireDate();
		}
		if (ret == null && track1 != null) {
			ret = track1.getExpireDate();
		}
		return ret;
	}

	

	public EmvCardScheme getType() {
		return type;
	}

	

	public void setType(final EmvCardScheme type) {
		this.type = type;
	}

	@Override
	public boolean equals(final Object arg0) {
		return arg0 instanceof EmvCard && getCardNumber() != null && getCardNumber().equals(((EmvCard) arg0).getCardNumber());
	}

	

	public Collection<String> getAtrDescription() {
		return atrDescription;
	}

	

	public void setAtrDescription(final Collection<String> atrDescription) {
		this.atrDescription = atrDescription;
	}
	
	

	public String getAt() {
		return at;
	}

	

	public void setAt(final String at) {
		this.at = at;
	}

	

	public CardStateEnum getState() {
		return state;
	}

	

	public void setState(final CardStateEnum state) {
		this.state = state;
	}

	

	public EmvTrack2 getTrack2() {
		return track2;
	}

	

	public void setTrack2(final EmvTrack2 track2) {
		this.track2 = track2;
	}

	

	public EmvTrack1 getTrack1() {
		return track1;
	}

	

	public void setTrack1(final EmvTrack1 track1) {
		this.track1 = track1;
	}

	

	public String getBic() {
		return bic;
	}

	

	public void setBic(final String bic) {
		this.bic = bic;
	}

	

	public String getIban() {
		return iban;
	}

	

	public void setIban(final String iban) {
		this.iban = iban;
	}

	

	public List<Application> getApplications() {
		return applications;
	}

	

	public CPLC getCplc() {
		return cplc;
	}

	

	public void setCplc(CPLC cplc) {
		this.cplc = cplc;
	}
	
}
