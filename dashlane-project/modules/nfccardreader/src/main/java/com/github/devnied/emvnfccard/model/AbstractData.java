package com.github.devnied.emvnfccard.model;

import org.apache.commons.lang3.SystemUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public abstract class AbstractData implements Serializable {

	private static final long serialVersionUID = -456811026151402726L;

	public static final int UNKNOWN = -1;

	private static final ToStringStyle CUSTOM_STYLE = new CustomToStringStyle();

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, CUSTOM_STYLE);
	}

	private static final class CustomToStringStyle extends ToStringStyle {

		private static final long serialVersionUID = 1L;

		CustomToStringStyle() {
			super();
			setUseShortClassName(true);
			setUseIdentityHashCode(false);
			setContentStart("[");
			setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
			setFieldSeparatorAtStart(true);
			setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
		}

	}

}
