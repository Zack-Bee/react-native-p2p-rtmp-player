package com.reactlibrary.d2d;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import com.reactlibrary.util.Byte2Hex;

public class D2DServer {
    private PipedOutputStream outputStream;
    private boolean isConnected = false;
    private Socket client;

    public D2DServer(PipedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    private boolean isRunning = false;

    public void start() {
        if (isRunning) {
            return;
        }
        this.isRunning = true;
        // new Thread() {
        //     @Override
        //     public void run() {
        //         byte[] buf = new byte[256 * 1024];
        //         try {
        //             DatagramSocket datagramSocket = new DatagramSocket(6666, InetAddress.getByName("0.0.0.0"));
        //             datagramSocket.setBroadcast(true);
        //             while (isRunning) {
        //                 DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
        //                 datagramSocket.receive(datagramPacket);
        //                 int packetLength = datagramPacket.getLength();
        //                 outputStream.write(buf, 0, packetLength);
        //             }
        //         } catch (IOException e) {
        //             e.printStackTrace();
        //         }

        //     }
        // }.start();
        new Thread() {
            @Override
            public void run() {
                while (!isConnected) {
                    try {
                        client = new Socket(InetAddress.getByName("192.168.49.1"), 5460);
                        isConnected = true;
                    } catch (IOException err) {
                        isConnected = false;
                        err.printStackTrace();
                        try {
                            sleep(1000);
                        } catch (InterruptedException error) {
                            error.printStackTrace();
                        }
                    }
                }
                try {
                    byte[] buf = new byte[256 * 1024];
                    InputStream is = client.getInputStream();
                    OutputStream os = client.getOutputStream();
                    byte[] hello = new byte[4];
                    os.write(hello);
                    os.flush();
                    while (isRunning) {
                        int dataLength = is.read(buf, 0, 256 * 1024);
                        outputStream.write(buf, 0, dataLength);
                        System.out.println("receive data from groupOwner: " + dataLength);
                    }
                    client.close();
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