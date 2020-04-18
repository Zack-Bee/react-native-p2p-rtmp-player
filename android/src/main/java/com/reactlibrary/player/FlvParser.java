package com.reactlibrary.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.util.Log;

public class FlvParser {
	private static final String TAG = "FlvParser";

	private FileInputStream input;
	private FlvTag tag = new FlvTag(ByteBuffer.allocate(655350));

	private boolean hasVideo;
	private boolean hasAudio;

	private PipedInputStream inputStream;

	private byte[] prevSizeBuffer = new byte[4];

	public void setInputStream(PipedInputStream inputStream) {
		this.inputStream = inputStream;
		tag.data.order(ByteOrder.BIG_ENDIAN);
	}

	public void open(String path) throws IOException {
		input = new FileInputStream(new File(path));
		tag = new FlvTag(ByteBuffer.allocate(655350));
		tag.data.order(ByteOrder.BIG_ENDIAN);

		// read header
		ByteBuffer buf = tag.data;
		input.read(buf.array(), 0, 9);

		if (!isFlv(buf.array())) {
			Log.w(TAG, "Not FLV file.");
			throw new IOException("Not FLV file.");
		}

		int headerSize = buf.getInt(5);
		buf.order(ByteOrder.BIG_ENDIAN);

		hasVideo = (buf.array()[4] & 1) != 0;
		hasAudio = (buf.array()[4] & 4) != 0;

		if (headerSize < 9) {
			Log.w(TAG, "illegal header size.");
			throw new IOException("Not FLV file.");
		}
		if (headerSize > 9) {
			input.read(buf.array(), 0, headerSize - 9); // skip remaining.
		}

		Log.d(TAG, "video: " + hasVideo + " audio:" + hasAudio);
	}

	public void close() {
		if (input != null) {
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			input = null;
		}
		tag = null;
	}

	public int readSizeOfData(byte[] buf, int offset, int size) throws IOException {
		int leftSize = size;
		while (leftSize > 0) {
			int readSize = inputStream.read(buf, size - leftSize + offset, leftSize);
			leftSize -= readSize;
		}
		return leftSize;
	}

	/**
	 * !! function overrides previous FlvTag object (avoid reallocate).
	 */
	public FlvTag parseNextTag() throws IOException {
		ByteBuffer buf = tag.data;

		buf.position(0);
		buf.limit(buf.capacity());

		int sz = readSizeOfData(buf.array(), 0, 11);
		if (sz < 0) {
			throw new IOException();
		}

		int tagType = buf.array()[0] & 0xff;
		int dataSize = buf.getInt(0) & 0xffffff;
		int timestamp = buf.getInt(3) & 0xffffff;

		Log.d(TAG, "tag type: " + tagType + " sz:" + dataSize + " t:" + timestamp);

		sz = readSizeOfData(buf.array(), 0, dataSize);

		if (sz != 0) {
			Log.w(TAG, "read tag data failed.");
			Log.w(TAG, "timestamp: " + timestamp);
			Log.w(TAG, "dataSize: " + dataSize);
			Log.w(TAG, "leftSize: " + sz);
			return null;
		}
		buf.limit(dataSize);

		// read prev tag size
		sz = readSizeOfData(prevSizeBuffer, 0, 4);

		tag.tagType = tagType;
		tag.timestamp = timestamp;
		tag.dataSize = dataSize;
		tag.codec = buf.array()[0] & 0xff;

		return tag;
	}

	private boolean isFlv(byte[] buf) {
		return buf[0] == 'F' && buf[1] == 'L' && buf[2] == 'V' && (buf[3] & 0xff) < 0x10;
	}

}
