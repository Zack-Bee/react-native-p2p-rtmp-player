package com.reactlibrary.rtmp.io;

import java.io.IOException;
import java.io.InputStream;
import com.reactlibrary.rtmp.io.packets.Abort;
import com.reactlibrary.rtmp.io.packets.Audio;
import com.reactlibrary.rtmp.io.packets.Command;
import com.reactlibrary.rtmp.io.packets.Data;
import com.reactlibrary.rtmp.io.packets.RtmpHeader;
import com.reactlibrary.rtmp.io.packets.RtmpPacket;
import com.reactlibrary.rtmp.io.packets.SetChunkSize;
import com.reactlibrary.rtmp.io.packets.SetPeerBandwidth;
import com.reactlibrary.rtmp.io.packets.UserControl;
import com.reactlibrary.rtmp.io.packets.Video;
import com.reactlibrary.rtmp.io.packets.WindowAckSize;
import com.reactlibrary.rtmp.util.L;

/**
 *
 * @author francois
 */
public class RtmpDecoder {

    private RtmpSessionInfo rtmpSessionInfo;

    private RtmpHeader firstHeaderOfBigData;

    public RtmpDecoder(RtmpSessionInfo rtmpSessionInfo) {
        this.rtmpSessionInfo = rtmpSessionInfo;
    }

    public RtmpPacket readPacket(InputStream in) throws IOException {

        L.d("\n====  readPacket(): called =====");
        RtmpHeader header = RtmpHeader.readHeader(in, rtmpSessionInfo);
        RtmpPacket rtmpPacket;
        L.d("readPacket(): header.messageType: " + header.getMessageType());

        ChunkStreamInfo chunkStreamInfo = rtmpSessionInfo.getChunkStreamInfo(header.getChunkStreamId());

        chunkStreamInfo.setPrevHeaderRx(header);
        System.out.println("packet length: " + header.getPacketLength());
        System.out.println("chunk size: " + rtmpSessionInfo.getChunkSize());
        if (header.getPacketLength() > rtmpSessionInfo.getChunkSize()) {
            System.out.println("readPacket(): packet size (" + header.getPacketLength() + ") is bigger than chunk size (" + rtmpSessionInfo.getChunkSize() + "); storing chunk data");
            // This packet consists of more than one chunk; store the chunks in the chunk stream until everything is read
            if (!chunkStreamInfo.storePacketChunk(in, rtmpSessionInfo.getChunkSize())) {
                if (firstHeaderOfBigData == null) {
                    firstHeaderOfBigData = header;
                }
                L.d(" readPacket(): returning null because of incomplete packet");
                return null; // packet is not yet complete
            } else {

                // use first header as the result header of big packet
                header = firstHeaderOfBigData;
                firstHeaderOfBigData = null;
                rtmpSessionInfo.getChunkStreamInfo(header.getChunkStreamId()).setPrevHeaderRx(header);
                L.d(" readPacket(): stored chunks complete packet; reading packet");
                in = chunkStreamInfo.getStoredPacketInputStream();
            }
        } else {
            firstHeaderOfBigData = null;
            L.d("readPacket(): packet size (" + header.getPacketLength() + ") is LESS than chunk size (" + rtmpSessionInfo.getChunkSize() + "); reading packet fully");
        }

        switch (header.getMessageType()) {

            case SET_CHUNK_SIZE: {
                SetChunkSize setChunkSize = new SetChunkSize(header);
                setChunkSize.readBody(in);
                L.d("readPacket(): Setting chunk size to: " + setChunkSize.getChunkSize());
                rtmpSessionInfo.setChunkSize(setChunkSize.getChunkSize());
                return null;
            }
            case ABORT:
                rtmpPacket = new Abort(header);
                break;
            case USER_CONTROL_MESSAGE:
                rtmpPacket = new UserControl(header);
                break;
            case WINDOW_ACKNOWLEDGEMENT_SIZE:
                rtmpPacket = new WindowAckSize(header);
                break;
            case SET_PEER_BANDWIDTH:
                rtmpPacket = new SetPeerBandwidth(header);
                break;
            case AUDIO:
                rtmpPacket = new Audio(header);
                break;
            case VIDEO:
                rtmpPacket = new Video(header);
                break;
            case COMMAND_AMF0:
                rtmpPacket = new Command(header);
                break;
            case DATA_AMF0:
                rtmpPacket = new Data(header);
                break;
            default:
                throw new IOException("No packet body implementation for message type: " + header.getMessageType());
        }
        rtmpPacket.readBody(in);
        return rtmpPacket;
    }
}
