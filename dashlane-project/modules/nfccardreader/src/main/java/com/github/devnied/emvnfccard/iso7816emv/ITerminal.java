

package com.github.devnied.emvnfccard.iso7816emv;



public interface ITerminal {

	

	byte[] constructValue(final TagAndLength pTagAndLength);

}
