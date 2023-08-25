package com.github.devnied.emvnfccard.enums;

public enum CommandEnum {

	SELECT(0x00, 0xA4, 0x04, 0x00),

	READ_RECORD(0x00, 0xB2, 0x00, 0x00),

	GPO(0x80, 0xA8, 0x00, 0x00),

	GET_DATA(0x80, 0xCA, 0x00, 0x00),
	
	GET_RESPONSE(0x00, 0x0C, 0x00, 0x00);

	private final int cla;

	private final int ins;

	private final int p1;

	private final int p2;

	private CommandEnum(final int cla, final int ins, final int p1, final int p2) {
		this.cla = cla;
		this.ins = ins;
		this.p1 = p1;
		this.p2 = p2;
	}

	public int getCla() {
		return cla;
	}

	public int getIns() {
		return ins;
	}

	public int getP1() {
		return p1;
	}

	public int getP2() {
		return p2;
	}

}
