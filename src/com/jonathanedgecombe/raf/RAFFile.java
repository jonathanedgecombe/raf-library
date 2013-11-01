package com.jonathanedgecombe.raf;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import com.jonathanedgecombe.raf.io.RAFFileReader;

/**
 * A class for parsing RAF files.
 * @author Jonathan Edgecombe
 */
public final class RAFFile {
	private final RAFFileReader metadataFile, archiveFile;
	private final List<RAFFileEntry> fileEntries;

	private long fileListOffset, pathListOffset;

	/**
	 * Create a new RAFFile object.
	 * @param metadataFile The path to the metadata (*.raf) file.
	 * @param archiveFile The path to the archive (*.raf.dat) file.
	 * @throws IOException
	 */
	public RAFFile(Path metadataFile, Path archiveFile) throws IOException {
		this.metadataFile = new RAFFileReader(metadataFile.toFile(), RAFFileReader.RAF_READWRITE);
		this.archiveFile = new RAFFileReader(archiveFile.toFile(), RAFFileReader.RAF_READWRITE);

		fileEntries = new ArrayList<>();

		readMetadata();
	}

	/**
	 * Reads the contents of the metadata file.
	 * @throws IOException
	 */
	private void readMetadata() throws IOException {
		/*int magicNumber = */   metadataFile.readInt();
		/*int formatVersion = */ metadataFile.readInt();
		/*int managerIndex = */  metadataFile.readInt();
		fileListOffset = metadataFile.readInt() & 0xFFFFFFFFL;
		pathListOffset = metadataFile.readInt() & 0xFFFFFFFFL;

		metadataFile.setPosition(fileListOffset);
		readFileList();
	}

	/**
	 * Reads the file list to memory.
	 * @throws IOException
	 */
	private void readFileList() throws IOException {
		int entries = metadataFile.readInt();

		for (int i = 0; i < entries; i++) {
			int hash = metadataFile.readInt();
			long dataOffset = metadataFile.readInt() & 0xFFFFFFFFL;
			long dataSize = metadataFile.readInt() & 0xFFFFFFFFL;
			int pathListIndex = metadataFile.readInt();

			long position = metadataFile.getPosition();
			metadataFile.setPosition(pathListOffset + 8 + (pathListIndex * 8));

			long pathOffset = metadataFile.readInt() & 0xFFFFFFFFL;
			int pathSize = metadataFile.readInt();

			metadataFile.setPosition(pathListOffset + pathOffset);
			String path = metadataFile.readString(pathSize).trim();

			if (hash == hash(path)) 
				fileEntries.add(new RAFFileEntry(dataOffset, dataSize, path));
			else
				throw new IOException("Invalid hash for item '" + path + "'.");

			metadataFile.setPosition(position);
		}
	}

	/**
	 * A hash used for checking the file path is correct.
	 * @param string The string to hash.
	 * @return The hash of the string.
	 */
	private int hash(String string) {
		int hash = 0;
		int temp = 0;

		for (byte b : string.toLowerCase().getBytes(StandardCharsets.US_ASCII)) {
			hash = (hash << 4) + b;
			temp = hash & 0xF0000000;

			if (temp != 0) {
				hash = hash ^ (temp >>> 24);
				hash = hash ^ temp;
			}
		}

		return hash;
	}

	/**
	 * Read a file from the archive. If the first two bytes are 0x78 and (0x01 || 0x9C || 0xDA) it is likely to be compressed using zlib deflate.
	 * @param fileEntry The file entry to read from the archive.
	 * @return The bytes of the file read.
	 * @throws IOException
	 */
	public byte[] readFile(RAFFileEntry fileEntry) throws IOException {
		archiveFile.setPosition(fileEntry.getDataOffset());
		if (fileEntry.getDataSize() > 0xFFFFFFFFL) throw new IOException("File too big.");
		byte[] data = archiveFile.readBytes((int) fileEntry.getDataSize());
		return data;
	}

	/**
	 * Retrieve the list of file entries in this archive.
	 * @return
	 */
	public List<RAFFileEntry> getFileEntries() {
		return fileEntries;
	}

	@Override
	public String toString() {
		return RAFFile.class.getName() + "{'" + metadataFile.toString() + "', '" + archiveFile.toString() + "'}@" + Integer.toHexString(this.hashCode());
	}
}
