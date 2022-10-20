import Foundation
import UIKit
import AVFoundation

extension AVPlayer {
    var isPlaying: Bool {
        return (rate != 0)
    }
}

@objc public class Audioplayer: NSObject {
   var player:AVPlayer
    var despPlugin:AudioplayerPlugin
    
    init(player:AVPlayer,despPlugin:AudioplayerPlugin) {
        self.player = player
        self.despPlugin = despPlugin
    }
    
    func initAudioSession() {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
        } catch {
            print("Error making audio session active")
        }
    }
    
    @objc func playerDidFinishPlaying(note: NSNotification) {
        despPlugin.onCompleted()
    }
    
    @objc func playerStateChanged(note: NSNotification) {
        if (player.isPlaying) {
            despPlugin.onPlaying()
        } else {
            despPlugin.onPaused()
        }
    }
    
    public func setup(value: AudioplayerPlugin){
        despPlugin = value
    }
   public func start(value: String) -> String {
        let playItem = AVPlayerItem(url: URL(string: value)!)
//        if (player.currentItem != nil) {
//            print("HELLO FROM BEFORE THE FOLD")
//
//            player.pause()
//            player.replaceCurrentItem(with: playItem)
//        } else {
//            print("HELLO FROM AFTER THE FOLD")
            
        player = AVPlayer(playerItem: playItem)
        player.automaticallyWaitsToMinimizeStalling = false
        NotificationCenter.default.addObserver(self, selector:#selector(playerDidFinishPlaying),
                                                   name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: player.currentItem)
        if #available(iOS 15.0, *) {
            NotificationCenter.default.addObserver(self, selector: #selector(playerStateChanged), name: AVPlayer.rateDidChangeNotification, object: player)
            print("RATE SETUP")
        } else {
            player.addObserver(self, forKeyPath: "rate", options: NSKeyValueObservingOptions.new, context: nil)
        }

//        }
        player.play()
        
        return "yes"
    }
    
    public override func observeValue(forKeyPath keyPath: String?, of object: Any?, change: [NSKeyValueChangeKey : Any]?, context: UnsafeMutableRawPointer?) {
        if keyPath == "rate", let player = object as? AVPlayer {
            if (player.isPlaying) {
                despPlugin.onPlaying()
            } else {
                despPlugin.onPaused()
            }
        }
    }
    
    public func pause() {
        player.pause()
    }

    public func resume() {
        player.play()
    }

    public func isPlaying() -> Bool {
        return player.isPlaying
    }

    public func seekTo(value: Int) -> String {
        let time:CMTime = CMTimeMake(value: Int64(value), timescale: 1000)
        player.seek(to: time)
        return "yes"
    }

    public func getDuration() -> Int {
        let duration = player.currentItem?.asset.duration
        let seconds = CMTimeGetSeconds(duration!)
        return Int(seconds * 1000)
    }

    public func getCurrentPosition() -> Int {
        let currentTime = player.currentTime()
        let seconds = CMTimeGetSeconds(currentTime)
        return Int(seconds * 1000)
    }
    
    public func stop() -> String {
        player.pause()
        player.rate=0
        NotificationCenter.default.removeObserver(self)
        return "yes"
    }

    public func echo(_ value: String) -> String {
        return value
    }

}




