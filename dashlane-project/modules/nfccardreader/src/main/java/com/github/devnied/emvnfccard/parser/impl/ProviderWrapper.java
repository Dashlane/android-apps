

package com.github.devnied.emvnfccard.parser.impl;

import com.github.devnied.emvnfccard.enums.CommandEnum;
import com.github.devnied.emvnfccard.enums.SwEnum;
import com.github.devnied.emvnfccard.exception.CommunicationException;
import com.github.devnied.emvnfccard.parser.IProvider;
import com.github.devnied.emvnfccard.utils.CommandApdu;
import com.github.devnied.emvnfccard.utils.ResponseUtils;



public class ProviderWrapper implements IProvider{
	
	

	private IProvider provider;
	
	

	public ProviderWrapper(IProvider pProvider) {
		provider = pProvider;
	}

	@Override
	public byte[] transceive(byte[] pCommand) throws CommunicationException {
		byte[] ret = provider.transceive(pCommand);
		
		if (ResponseUtils.isEquals(ret, SwEnum.SW_6C)) {
			pCommand[pCommand.length - 1] = ret[ret.length - 1];
			ret = provider.transceive(pCommand);
		} else if (ResponseUtils.isEquals(ret, SwEnum.SW_61)) { 
			ret = provider.transceive(new CommandApdu(CommandEnum.GET_RESPONSE, null, ret[ret.length - 1]).toBytes());
		}
		return ret;
	}

	@Override
	public byte[] getAt() {
		return provider.getAt();
	}

}
