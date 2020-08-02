package org.imanity.framework.player;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.UUID;

@Getter
public class PlayerInfo {

	private final UUID uuid;
	@Setter
	private String name;

	public PlayerInfo(final UUID uuid, final String name) {
		this.uuid = uuid;
		this.name = name;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlayerInfo that = (PlayerInfo) o;
		return Objects.equals(uuid, that.uuid) &&
				Objects.equals(name, that.name);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid, name);
	}

}
