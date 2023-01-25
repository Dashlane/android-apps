

package com.github.devnied.emvnfccard.exception;

import java.io.IOException;



public class CommunicationException extends IOException {

	

	private static final long serialVersionUID = -7916924250407562185L;

	

	public CommunicationException() {
		super();
	}

	

	public CommunicationException(final String pMessage) {
		super(pMessage);
	}

}
