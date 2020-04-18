package com.reactlibrary.d2d;

import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import com.reactlibrary.util.Byte2Hex;

public class D2DServer {
    private PipedOutputStream outputStream;

    public D2DServer(PipedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private boolean isRunning = false;

    public void start() {
        if (isRunning) {
            return;
        }
        this.isRunning = true;
        new Thread() {
            @Override
            public void run() {
                byte[] buf = new byte[256 * 1024];
                try {
                    DatagramSocket datagramSocket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));
                    datagramSocket.setBroadcast(true);
                    while (isRunning) {
                        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                        datagramSocket.receive(datagramPacket);
                        int packetLength = datagramPacket.getLength();
                        outputStream.write(buf, 0, packetLength);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    public void stop() {
        this.isRunning = false;
    }
}