

package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.exception.CommunicationException;



public interface IProvider {

	

	byte[] transceive(byte[] pCommand) throws CommunicationException;
	
	

	byte[] getAt();

}
