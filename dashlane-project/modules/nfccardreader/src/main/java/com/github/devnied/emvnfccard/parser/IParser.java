

package com.github.devnied.emvnfccard.parser;

import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.model.Application;

import java.util.regex.Pattern;



public interface IParser {

	

	Pattern getId();

	

	boolean parse(Application pApplication) throws CommunicationException;

}
