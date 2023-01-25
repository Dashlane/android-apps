

package com.github.devnied.emvnfccard.model.enums;



public enum TransactionTypeEnum implements IKeyEnum {

	

	PURCHASE(0x00),
	

	CASH_ADVANCE(0x01),
	

	CASHBACK(0x09),
	

	REFUND(0x20),
	
	

	LOADED(0xFE),
	

	UNLOADED(0xFF);

	

	private final int value;

	

	private TransactionTypeEnum(final int value) {
		this.value = value;
	}

	@Override
	public int getKey() {
		return value;
	}
}
