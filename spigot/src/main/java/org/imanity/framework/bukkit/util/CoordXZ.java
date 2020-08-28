package org.imanity.framework.bukkit.util;

public class CoordXZ {
	public int x, z;

	public CoordXZ(final int x, final int z) {
		this.x = x;
		this.z = z;
	}

	public CoordXZ(final int x, final int z, final boolean chunk) {
		if (chunk) {
			this.x = blockToChunk(x);
			this.z = blockToChunk(z);
			return;
		}
		this.x = x;
		this.z = z;
	}

	public static int blockToChunk(final int blockVal) {
		return blockVal >> 4;
	}

	public static int blockToRegion(final int blockVal) {
		return blockVal >> 9;
	}

	public static int chunkToRegion(final int chunkVal) {
		return chunkVal >> 5;
	}

	public static int chunkToBlock(final int chunkVal) {
		return chunkVal << 4;
	}

	public static int regionToBlock(final int regionVal) {
		return regionVal << 9;
	}

	public static int regionToChunk(final int regionVal) {
		return regionVal << 5;
	}


	@Override
	public boolean equals(final Object obj) {
		if (this == obj)
			return true;
		else if (obj == null || obj.getClass() != this.getClass())
			return false;

		final CoordXZ test = (CoordXZ) obj;
		return test.x == this.x && test.z == this.z;
	}

	@Override
	public int hashCode() {
		return (this.x << 9) + this.z;
	}
}