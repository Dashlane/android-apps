

package com.github.devnied.emvnfccard.model.enums;



public enum ServiceCode1Enum implements IKeyEnum {

	INTERNATIONNAL(1, "International interchange", "None"),
	INTERNATIONNAL_ICC(2, "International interchange", "Integrated circuit card"),
	NATIONAL(5, "National interchange", "None"),
	NATIONAL_ICC(6, "National interchange", "Integrated circuit card"),
	PRIVATE(7, "Private", "None");

	private final int value;
	private final String interchange;
	private final String technology;

	

	private ServiceCode1Enum(final int value, final String interchange, final String technology) {
		this.value = value;
		this.interchange = interchange;
		this.technology = technology;
	}

	

	public String getInterchange() {
		return interchange;
	}

	

	public String getTechnology() {
		return technology;
	}

	@Override
	public int getKey() {
		return value;
	}

}