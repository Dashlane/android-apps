

package com.github.devnied.emvnfccard.model.enums;

import com.github.devnied.emvnfccard.model.Application;

import java.util.List;



public enum ApplicationStepEnum implements IKeyEnum {

	

	NOT_SELECTED(0),
	

	SELECTED(1), 
	

	READ(2);
	
	

	private int key;
	
	

	private ApplicationStepEnum(final int pKey) {
		key = pKey;
	}

	@Override
	public int getKey() {
		return key;
	}
	
	

	public static boolean isAtLeast(final List<Application> pApplications, final ApplicationStepEnum pStep){
		boolean ret = false;
		if (pApplications != null && pStep != null){
			for (Application app: pApplications){
				if (app != null && app.getReadingStep() != null && app.getReadingStep().key >= pStep.getKey()){
					ret = true;
				}
			}
		}
		return ret;
	}

}