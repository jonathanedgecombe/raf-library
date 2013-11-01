package com.jonathanedgecombe.raf;

/**
 * A class for representing a file entry within a RAF archive.
 * @author Jonathan Edgecombe
 */
public final class RAFFileEntry {
	private final long dataOffset, dataSize;
	private final String path;

	public RAFFileEntry(long dataOffset, long dataSize, String path) {
		this.dataOffset = dataOffset;
		this.dataSize = dataSize;
		this.path = path;
	}

	public long getDataOffset() {
		return dataOffset;
	}

	public long getDataSize() {
		return dataSize;
	}

	public String getPath() {
		return path;
	}

	@Override
	public String toString() {
		return RAFFileEntry.class.getName() + "{'" + path + "'}";
	}
}
