package com.reactlibrary.util;

import java.io.IOException;
import java.io.PipedInputStream;

public class PipedInputStreamPrinter {
    private PipedInputStream inputStream;
    private boolean isRunning = false;

    public PipedInputStreamPrinter(PipedInputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void start() {
        new Thread() {
            @Override
            public void run() {
                isRunning = true;
                byte[] buf = new byte[1024];
                try {
                    while (isRunning) {
                        inputStream.read(buf);
                        System.out.println(Byte2Hex.bytesToHex(buf));
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