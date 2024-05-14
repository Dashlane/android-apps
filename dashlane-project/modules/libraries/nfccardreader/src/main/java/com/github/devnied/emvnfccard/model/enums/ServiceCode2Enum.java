package com.github.devnied.emvnfccard.model.enums;

public enum ServiceCode2Enum implements IKeyEnum {

	NORMAL(0, "Normal"),
	BY_ISSUER(2, "By issuer"),
	BY_ISSUER_WIHOUT_BI_AGREEMENT(4, "By issuer unless explicit bilateral agreement applies");

	private final int value;
	private final String authorizationProcessing;

	private ServiceCode2Enum(final int value, final String authorizationProcessing) {
		this.value = value;
		this.authorizationProcessing = authorizationProcessing;
	}

	public String getAuthorizationProcessing() {
		return authorizationProcessing;
	}

	@Override
	public int getKey() {
		return value;
	}

}