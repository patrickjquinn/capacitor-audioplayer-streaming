import Foundation
import Capacitor
import AVFoundation

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(AudioplayerPlugin)
public class AudioplayerPlugin: CAPPlugin {
     private var implementation :Audioplayer?

    
    @objc func start(_ call: CAPPluginCall)  -> String {
        let value = call.getString("url") ?? ""
        let playItem = AVPlayerItem(url: URL(string: value)!)
        let player = AVPlayer(playerItem: playItem)
        implementation = Audioplayer(player: player, despPlugin: self)
        implementation?.setup(value: self)
        call.resolve([
            "value": implementation?.start(value: value) ?? 0
        ])
        return "yes"
    }
    @objc func onCompleted() {
        notifyListeners("end", data: nil);
    }

    @objc func onPlaying() {
        notifyListeners("play", data: nil);
    }

    @objc func onPaused() {
        notifyListeners("pause", data: nil);
    }
    
    @objc func isPlaying(_ call: CAPPluginCall) {
        call.resolve([
            "value": implementation?.isPlaying() as Any
        ])
    }
    
    @objc func isPaused(_ call: CAPPluginCall) {
        let isPlaying = implementation?.isPlaying() ?? false
        var isPaused = true
        if (isPlaying) {
            isPaused = false
        }
        call.resolve([
            "value": isPaused
        ])
    }

    @objc func pause(_ call: CAPPluginCall) {
        implementation?.pause()
        call.resolve()
    }

    @objc func resume(_ call: CAPPluginCall) {
        implementation?.resume()
        call.resolve()
    }

    @objc func seek(_ call: CAPPluginCall)  -> String {
        let value = call.getInt("seekTo") ?? 0
        call.resolve([
            "value": implementation?.seekTo(value: value) ?? 0
        ])
        return "yes"
    }

    @objc func getDuration(_ call: CAPPluginCall)  -> String {
        call.resolve([
            "value": implementation?.getDuration() ?? 0
        ])
        return "yes"
    }

    @objc func getCurrentPosition(_ call: CAPPluginCall)  -> String {
        call.resolve([
            "value": implementation?.getCurrentPosition() ?? 0
        ])
        return "yes"
    }
 
    @objc func stop(_ call: CAPPluginCall)  -> String {
                call.resolve([
                    "value": implementation?.stop() ?? 0
                ])
          return "yes"
}
        @objc func echo(_ call: CAPPluginCall)  -> String {
        let value = call.getString("value") ?? ""
        call.resolve([
            "value": implementation?.echo(value) ?? 0
        ])
            return "yes"
    }
}

