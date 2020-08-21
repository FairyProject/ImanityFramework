package org.imanity.framework.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.imanity.framework.bukkit.Imanity;

import java.util.StringJoiner;


@Getter
@Setter
@AllArgsConstructor
public class CustomLocation {

	private final long timestamp = System.currentTimeMillis();

	private String world;

	private double x;
	private double y;
	private double z;

	private float yaw;
	private float pitch;

	public CustomLocation(final double x, final double y, final double z) {
		this(x, y, z, 0.0F, 0.0F);
	}

	public CustomLocation(final String world, final double x, final double y, final double z) {
		this(world, x, y, z, 0.0F, 0.0F);
	}

	public CustomLocation(final double x, final double y, final double z, final float yaw, final float pitch) {
		this("world", x, y, z, yaw, pitch);
	}

	public static CustomLocation fromBukkitLocation(final Location location) {
		return new CustomLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
				location.getYaw(), location.getPitch());
	}

	public static CustomLocation stringToLocation(final String string) {
		final String[] split = string.split(", ");

		final double x = Double.parseDouble(split[0]);
		final double y = Double.parseDouble(split[1]);
		final double z = Double.parseDouble(split[2]);

		final CustomLocation customLocation = new CustomLocation(x, y, z);

		if (split.length == 4) {
			customLocation.setWorld(split[3]);
		} else if (split.length >= 5) {
			customLocation.setYaw(Float.parseFloat(split[3]));
			customLocation.setPitch(Float.parseFloat(split[4]));

			if (split.length >= 6) {
				customLocation.setWorld(split[5]);
			}
		}
		return customLocation;
	}

	public static String locationToString(final CustomLocation loc) {
		final StringJoiner joiner = new StringJoiner(", ");
		joiner.add(Double.toString(loc.getX()));
		joiner.add(Double.toString(loc.getY()));
		joiner.add(Double.toString(loc.getZ()));
		if (loc.getYaw() == 0.0f && loc.getPitch() == 0.0f) {
			if (loc.getWorld().equals("world"))
				return joiner.toString();
			else {
				joiner.add(loc.getWorld());
				return joiner.toString();
			}
		} else {
			joiner.add(Float.toString(loc.getYaw()));
			joiner.add(Float.toString(loc.getPitch()));
			if (loc.getWorld().equals("world"))
				return joiner.toString();
			else {
				joiner.add(loc.getWorld());
				return joiner.toString();
			}
		}
	}

	public Location toBukkitLocation() {
		return new Location(this.toBukkitWorld(), this.x, this.y, this.z, this.yaw, this.pitch);
	}

	public double getGroundDistanceTo(final CustomLocation location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.z - location.z, 2));
	}

	public double getDistanceTo(final CustomLocation location) {
		return Math.sqrt(Math.pow(this.x - location.x, 2) + Math.pow(this.y - location.y, 2) + Math.pow(this.z - location.z, 2));
	}

	public World toBukkitWorld() {
		if (this.world == null)
			return Bukkit.getServer().getWorlds().get(0);
		else
			return Bukkit.getServer().getWorld(this.world);
	}

	@Override
	public boolean equals(final Object obj) {
		if (!(obj instanceof CustomLocation))
			return false;

		final CustomLocation location = (CustomLocation) obj;
		return location.x == this.x && location.y == this.y && location.z == this.z
				&& location.pitch == this.pitch && location.yaw == this.yaw;
	}

	@Override
	public String toString() {
		return CustomLocation.locationToString(this);
	}

	public static int locToBlock(final double loc) {
		return NumberConversions.floor(loc);
	}

	public int getBlockX() {
		return locToBlock(x);
	}

	public int getBlockY() {
		return locToBlock(y);
	}

	public int getBlockZ() {
		return locToBlock(z);
	}

	public void teleport(Player player, double range) {
		this.teleport(player, range, true);
	}

	public void teleport(Player player, double range, boolean safe) {
		double rand = -range + (range * 2) * Imanity.RANDOM.nextDouble();
		player.teleport(this.toBukkitLocation().add(rand, safe ? 0.5D : 0.0D, rand));
	}

}
