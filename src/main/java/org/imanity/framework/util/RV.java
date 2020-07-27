package org.imanity.framework.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RV {

	private final String target;
	private final String replacement;

	public static RV o(final String target, final String replacement) {
		return new RV(target, replacement);
	}

}