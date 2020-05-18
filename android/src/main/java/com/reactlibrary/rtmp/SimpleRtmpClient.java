package com.reactlibrary.rtmp;

import com.reactlibrary.rtmp.util.L;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;
import java.net.InetAddress;
import java.net.Socket;
import java.net.ServerSocket;

public class SimpleRtmpClient {
    private String host;
    private int port;
    private String appName;
    private String streamName;
    private boolean isBroadcast = true;
    private boolean isRunning = false;
    private PipedOutputStream outputStream;
    private List<Socket> clients = new ArrayList<Socket>();
    private ServerSocket server;
    private SyncRtmpClient mClient;

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
        isRunning = true;
        new Thread() {
            @Override
            public void run() {
                try {
                    mClient = new SyncRtmpClient(host, port, appName, streamName);
                    mClient.connect();
                    byte[] buffer = new byte[256 * 1024];
                    // DatagramSocket socket = new DatagramSocket();
                    boolean passedHeader = false;
                    while (isRunning) {
                        int dataLength = mClient.readAvData(buffer);

                        // 跳过flv文件头部
                        if (!passedHeader && dataLength != 0) {
                            passedHeader = true;
                            continue;
                        }
                        if (dataLength > 0) {
                            outputStream.write(buffer, 0, dataLength);

                            if (isBroadcast) {
                                pollingSend(buffer, 0, dataLength);
                            }
                        } else if (dataLength < 0) {
                            break;
                        }
                    }

                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

        new Thread() {
            @Override
            public void run() {
                try {
                    server = new ServerSocket(5460);
                    Socket client = null;
                    while (isRunning) {
                        client = server.accept();
                        System.out.println("client connect");
                        byte[] hello = new byte[4];
                        OutputStream os = client.getOutputStream();
                        InputStream is = client.getInputStream();
                        is.read(hello);
                        os.write(mClient.getVideoHeadSequence(), 0, mClient.getVideoHeadSequenceSize());
                        os.write(mClient.getAudioHeadSequence(), 0, mClient.getAudioHeadSequenceSize());
                        os.flush();
                        System.out.println("getVideoHeadSequenceSize: "+ mClient.getVideoHeadSequenceSize());
                        clients.add(client);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    private void pollingSend(byte[] buffer, int offset, int length) {
        List<Integer> closedClientIndexList = new ArrayList<>();
        try {
            for (int i = 0; i < clients.size(); i++) {
                Socket client = clients.get(i);
                if (client.isClosed()) {
                    System.out.println("client.isClosed");
                    closedClientIndexList.add(i);
                    continue;
                }
                OutputStream os = client.getOutputStream();
                os.write(buffer, offset, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 清除List中已关闭的socket
        for (int i = closedClientIndexList.size() - 1; i > 0; i--) {
            clients.remove(closedClientIndexList.get(i));
        }
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
