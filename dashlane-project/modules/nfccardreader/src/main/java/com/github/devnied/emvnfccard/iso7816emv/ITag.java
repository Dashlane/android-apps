package com.github.devnied.emvnfccard.iso7816emv;

import com.github.devnied.emvnfccard.enums.TagTypeEnum;
import com.github.devnied.emvnfccard.enums.TagValueTypeEnum;






public interface ITag {

	public enum Class {
		UNIVERSAL, APPLICATION, CONTEXT_SPECIFIC, PRIVATE
	}

	boolean isConstructed();

	byte[] getTagBytes();

	String getName();

	String getDescription();

	TagTypeEnum getType();

	TagValueTypeEnum getTagValueType();

	Class getTagClass();

	int getNumTagBytes();

}
