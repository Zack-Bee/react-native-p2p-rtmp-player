package com.reactlibrary.rtmp.output;

import java.io.IOException;
import java.io.OutputStream;
import com.reactlibrary.rtmp.io.packets.ContentData;
import com.reactlibrary.rtmp.io.packets.Data;
import com.reactlibrary.rtmp.io.packets.RtmpHeader;
import com.reactlibrary.rtmp.util.L;

/**
 * Simple writer class for dumping raw RTMP audio packets to an OutputStream
 * 
 * This is mostly useful as a test/debugging writer, but some players will be
 * able to play the resulting output if it is e.g. MP3 or AAC audio. In the case
 * of AAC, it is recommended to use the AacWriter instead, as it adds the usually-required
 * ADTS headers to the AAC audio data.
 * 
 * @author francois
 */
public class RawAudioWriter extends RtmpStreamWriter {

    protected OutputStream out;

    protected RawAudioWriter() {
    }

    public RawAudioWriter(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(Data dataPacket) throws IOException {
    }

    @Override
    public void write(ContentData packet) throws IOException {
        if (packet.getHeader().getMessageType() == RtmpHeader.MessageType.AUDIO) {
            packet.writeBody(out);
        }
    }

    @Override
    public void close() {
        super.close();
        try {
            out.close();
        } catch (IOException ex) {
            L.e("Failed to close wrapped OutputStream", ex);
        }
    }
}
