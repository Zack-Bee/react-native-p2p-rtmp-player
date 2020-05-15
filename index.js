import {
    requireNativeComponent,
    UIManager,
    findNodeHandle,
    NativeModules
} from 'react-native'
import React, { Component, createRef } from 'react'

const RCTPlayer = requireNativeComponent('RCTPlayer')
const P2pRtmpPlayerModule = NativeModules.P2pRtmpPlayerModule;

class Player extends Component {
    constructor(props) {
        super(props)
        this.RCTPlayerRef = createRef()
    }

    render() {
        return (
            <RCTPlayer {...{...this.props, ref: this.RCTPlayerRef}}/>
        )
    }

    startRtmp({host, port, appName, streamName, isBroadcast}) {
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.RCTPlayerRef.current),
            UIManager.RCTPlayer.Commands.startRtmp,
            [{host, port, appName, streamName, isBroadcast}]
        )
    }

    startD2D() {
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.RCTPlayerRef.current),
            UIManager.RCTPlayer.Commands.startD2D,
            null
        )
    }

    release() {
        UIManager.dispatchViewManagerCommand(
            findNodeHandle(this.RCTPlayerRef.current),
            UIManager.RCTPlayer.Commands.release,
            null
        )
    }
}

export const getP2pMac = P2pRtmpPlayerModule.getP2pMac;

export default Player
