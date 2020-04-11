package com.reactlibrary.rtmp.io;

import com.reactlibrary.rtmp.io.packets.RtmpPacket;

/**
 * Handler interface for received RTMP packets
 * @author francois
 */
public interface PacketRxHandler {
    
    public void handleRxPacket(RtmpPacket rtmpPacket);
    
    public void notifyWindowAckRequired(final int numBytesReadThusFar);    
}
