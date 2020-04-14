package com.reactlibrary.rtmp;

import com.reactlibrary.rtmp.util.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.net.InetAddress;

public class SimpleRtmpClient {
    private String host;
    private int port;
    private String appName;
    private String streamName;
    private boolean isBroadcast = true;
    private boolean isRunning = false;
    private PipedOutputStream outputStream;

    public SimpleRtmpClient(String host, int port, String appName, String streamName, boolean isBroadcast) {
        this.host = host;
        this.port = port;
        this.appName = appName;
        this.streamName = streamName;
        this.isBroadcast = isBroadcast;
    }

    public void setOutputStream(PipedOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                SyncRtmpClient mClient = new SyncRtmpClient(host, port, appName, streamName);
                try {
                    mClient.connect();
                    isRunning = true;
                    byte[] buffer = new byte[256 * 1024];
                    DatagramSocket socket = new DatagramSocket();
                    boolean passedHeader = false;
                    while (isRunning) {
                        int dataLength = mClient.readAvData(buffer);

                        // 跳过flv文件头部
                        if (!passedHeader && dataLength != 0) {
                            passedHeader = true;
                            byte[] prevTagSize = new byte[]{0x66, 0x66, 0x66, 0x66};
                            outputStream.write(prevTagSize);
                            continue;
                        }
                        if (dataLength > 0) {
                            outputStream.write(buffer, 0, dataLength);

                            // 这个地方由于udp包65535的限制很容易丢包，在发送端减小视频帧大小
                            if (isBroadcast) {
                                DatagramPacket packet = new DatagramPacket(buffer, dataLength, InetAddress.getByName("192.168.49.255"), 6666);
                                socket.send(packet);
                            }
                        } else if (dataLength < 0) {
                            break;
                        }
                    }

                    outputStream.close();
                    if (isBroadcast) {
                        socket.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void stop() {
        isRunning = false;
    }

    public static void main(String args[]) {
        SyncRtmpClient mClient;
        try {
            // mClient = new SyncRtmpClient("58.200.131.2", 1935, "livetv", "hunantv");
            // mClient = new SyncRtmpClient("127.0.0.1", 1935, "abcs", "obs");
            mClient = new SyncRtmpClient("127.0.0.1", 1935, "video", "dump.flv");
            //mClient = new SyncRtmpClient("ftv.sun0769.com", 1935, "dgrtv1", "mp4:b1");
            L.w("connect start");
            mClient.connect();
        } catch (Exception ex) {
            L.w("play quit");
            return;
        }

        File file = new File("./try.flv");
        FileOutputStream fo = null;
        if (file.exists()) {
            file.delete();
        } else {
            try {
                file.createNewFile();
                fo = new FileOutputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        byte[] buffer = new byte[258 * 1024]; // 32KB的缓存也会因为数据量过大而丢弃数据
        System.out.println("fo == null?: " + fo == null);
        while (true) {
            try {
                //L.w("read begin");
                int n = mClient.readAvData(buffer);
                L.w("read byte " + n);
                if (n > 0) {
                    fo.write(buffer, 0, n);
                } else if (n < 0) {
                    L.e("read failed !!! need reconnect");
                    break;
                }
            } catch (SocketTimeoutException e) {
                L.d("socket timeout read again");
                continue;
            } catch (Exception ex) {
                ex.printStackTrace();
                L.w("read quit" + ex);
                return;
            }
        }
    }
}
