package com.reactlibrary;

import java.net.NetworkInterface;
import java.lang.StringBuilder;
import java.util.Collections;
import java.util.List;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Promise;

public class P2pRtmpPlayerModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public P2pRtmpPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
    }

    @Override
    public String getName() {
        return "P2pRtmpPlayerModule";
    }

    @ReactMethod
    public void sampleMethod(String stringArgument, int numberArgument, Callback callback) {
        // TODO: Implement some actually useful functionality
        callback.invoke("Received numberArgument: " + numberArgument + " stringArgument: " + stringArgument);
    }

    @ReactMethod
    public void getP2pMac(final Promise promise) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            StringBuilder strBuilder = new StringBuilder();
            for (NetworkInterface ntwInterface : interfaces) {

                if (ntwInterface.getName().equalsIgnoreCase("p2p0")) {
                    byte[] byteMac = ntwInterface.getHardwareAddress();
                    if (byteMac==null) {
                        promise.reject("mac is null");
                        return;
                    }
                    for (int i = 0; i < byteMac.length; i++) {
                        strBuilder.append(String.format("%02X:", byteMac[i]));
                    }

                    if (strBuilder.length()>0){
                        strBuilder.deleteCharAt(strBuilder.length()-1);
                    }

                }
            }
            promise.resolve(strBuilder.toString());
        } catch (Exception e) {
        }
    }
}
