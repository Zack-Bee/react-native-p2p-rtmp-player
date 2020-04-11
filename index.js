import {
    requireNativeComponent,
    UIManager,
    findNodeHandle
} from 'react-native';
import { Component, createRef } from 'react'

const RCTPlayer = requireNativeComponent('RCTPlayer')

class Player extends Component {
    constructor(props) {
        super(props)
    }
    RCTPlayerRef = createRef()

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

export default Player;
