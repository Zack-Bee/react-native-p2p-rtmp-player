package com.reactlibrary;

import com.reactlibrary.rtmp.SimpleRtmpClient;
import com.reactlibrary.player.FlvPlayer;
import com.reactlibrary.d2d.D2DServer;

import android.content.Context;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.util.Log;
import android.graphics.SurfaceTexture;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;


public class PlayerView extends FrameLayout implements TextureView.SurfaceTextureListener {
    private Surface mSurface;

    private FrameLayout.LayoutParams deflp = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT,
            Gravity.CENTER);

    private SimpleRtmpClient mClient;
    private D2DServer server;
    private FlvPlayer player;

    private PipedInputStream inputStream;
    private PipedOutputStream outputStream;

    private String TEST_FLV = "/sdcard/Download/dump.flv";

    public PlayerView(Context context) {
        super(context);
        TextureView tv = new TextureView(context);
        tv.setSurfaceTextureListener(this);
        tv.setLayoutParams(deflp);
        addView(tv);
        player = new FlvPlayer();
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        player.setSurface(new Surface(surface));
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        player.stop();
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        player.setSurface(new Surface(surface));
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
    }

    public void startRtmp(ReadableArray args) {
        Log.i("ReactNativeJS", "start rtmp");
        ReadableMap map = args.getMap(0);
        String host = map.getString("host");
        int port = map.getInt("port");
        String appName = map.getString("appName");
        String streamName = map.getString("streamName");
        inputStream = new PipedInputStream(256 * 1024);
        outputStream = new PipedOutputStream();
        try {
            inputStream.connect(outputStream);
            mClient = new SimpleRtmpClient(host, port, appName, streamName, true);
            mClient.setOutputStream(outputStream);
            player.setInputStream(inputStream);
            mClient.start();
            player.play();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void startD2D() {
        inputStream = new PipedInputStream(256 * 1024);
        outputStream = new PipedOutputStream();
        try {
            inputStream.connect(outputStream);
            player.setInputStream(inputStream);
            server = new D2DServer(outputStream);
            server.start();
            player.play();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void release() {
        if (server != null) {
            server.stop();
        }
        if (player != null) {
            player.stop();
        }
        if (mClient != null) {
            mClient.stop();
        }
    }
}