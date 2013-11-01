package com.jonathanedgecombe.raf.io;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;

/**
 * A class for reading data from RAF files.
 * @author Jonathan Edgecombe
 */
public final class RAFFileReader {
	public final static String RAF_READ = "r", RAF_READWRITE = "rw", RAF_READWRITE_SYNCHRONOUS = "rws", RAF_READWRITE_CONTENTSYNCHRONOUS = "rwd";
	private final RandomAccessFile raf;

	/**
	 * Create a new RAFFileReader wrapped around a File object.
	 * @param file The file to wrap.
	 * @param mode The mode to create the RandomAccessFile in. Use RAFFileReader.RAF_*.
	 * @throws FileNotFoundException
	 */
	public RAFFileReader(File file, String mode) throws FileNotFoundException {
		raf = new RandomAccessFile(file, mode);
	}

	/**
	 * Read a little endian 32 bit integer from the file.
	 * @return The interger that has been read.
	 * @throws IOException
	 */
	public int readInt() throws IOException {
		return Integer.reverseBytes(raf.readInt());
	}

	/**
	 * Read a string from the file.
	 * @param length The length of the string to read.
	 * @return The string that has been read.
	 * @throws IOException
	 */
	public String readString(int length) throws IOException {
		byte[] bytes = new byte[length];
		raf.readFully(bytes);
		return new String(bytes, StandardCharsets.US_ASCII);
	}

	/**
	 * Read a NULL (\0x00) terminated string from the file.
	 * @return The string that has been read.
	 * @throws IOException
	 */
	public String readString() throws IOException {
		String s = "";

		byte b = raf.readByte();
		while (b != 0x00) {
			s += new String(new byte[] {b}, StandardCharsets.US_ASCII);
			b = raf.readByte();
		}

		return s;
	}

	/**
	 * Read an arbitrary number of bytes from the file.
	 * @param length The length of bytes to be read.
	 * @return The bytes that have been read.
	 * @throws IOException
	 */
	public byte[] readBytes(int length) throws IOException {
		byte[] bytes = new byte[length];
		raf.readFully(bytes);
		return bytes;
	}

	/**
	 * Get the position of the pointer in the file.
	 * @return The position of the pointer in the file.
	 * @throws IOException
	 */
	public long getPosition() throws IOException {
		return raf.getFilePointer();
	}

	/**
	 * Set the position of the pointer in the file.
	 * @param position The position to set the file pointer to.
	 * @throws IOException
	 */
	public void setPosition(long position) throws IOException {
		raf.seek(position);
	}
}
