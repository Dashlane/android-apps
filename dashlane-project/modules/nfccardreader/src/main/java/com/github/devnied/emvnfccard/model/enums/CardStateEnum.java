

package com.github.devnied.emvnfccard.model.enums;



public enum CardStateEnum implements IKeyEnum {

	UNKNOWN, LOCKED, ACTIVE;

	@Override
	public int getKey() {
		return 0;
	}

}