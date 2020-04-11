package com.reactlibrary;

import javax.annotation.Nullable;

import java.util.Map;

import android.util.Log;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.common.MapBuilder;


public class PlayerViewManager extends ViewGroupManager<PlayerView> {
    private ReactApplicationContext mCallerContext;

    private static final int COMMAND_PAUSE_ID = 1;
    private static final String COMMAND_PAUSE_NAME = "pause";

    private static final int COMMAND_START_ID = 2;
    private static final String COMMAND_START_NAME = "start";

    private static final int COMMAND_STOP_ID = 3;
    private static final String COMMAND_STOP_NAME = "stop";

    private static final int COMMAND_START_RTMP_ID = 4;
    private static final String COMMAND_START_RTMP_NAME = "startRtmp";

    private static final int COMMAND_START_D2D_ID = 5;
    private static final String COMMAND_START_D2D_NAME = "startD2D";

    private static final int COMMAND_RELEASE_ID = 6;
    private static final String COMMMAND_RELEASE_NAME = "release";

    public PlayerViewManager(ReactApplicationContext context) {
        mCallerContext = context;
    }

    @Override
    public String getName() {
        return "RCTPlayer";
    }

    @Override
    protected PlayerView createViewInstance(ThemedReactContext reactContext) {
        PlayerView view = new PlayerView(reactContext);
        return view;
    }

    @Nullable
    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of(
                // COMMAND_PAUSE_NAME, COMMAND_PAUSE_ID,
                // COMMAND_START_NAME, COMMAND_START_ID,
                // COMMAND_STOP_NAME, COMMAND_STOP_ID,
                COMMAND_START_RTMP_NAME, COMMAND_START_RTMP_ID,
                COMMAND_START_D2D_NAME, COMMAND_START_D2D_ID,
                COMMMAND_RELEASE_NAME, COMMAND_RELEASE_ID
        );
    }


    @Override
    public void receiveCommand(PlayerView player, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            // case COMMAND_PAUSE_ID:
            //     player.pause();
            //     break;
            // case COMMAND_START_ID:
            //     player.start();
            //     break;
            // case COMMAND_STOP_ID:
            //     player.stop();
            //     break;
            case COMMAND_START_RTMP_ID:
                player.startRtmp(args);
                break;
            case COMMAND_START_D2D_ID:
                player.startD2D();
                break;
            case COMMAND_RELEASE_ID:
                player.release();
                break;
        }
    }
}