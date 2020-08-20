package org.imanity.framework.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RV {

	private final String target;
	private final String replacement;

	public RV(String target, Object replacement) {
		this(target, replacement.toString());
	}

	public static RV o(final String target, final String replacement) {
		return new RV(target, replacement);
	}

	public static RV o(final String target, final Object replacement) {
		return new RV(target, replacement);
	}


}