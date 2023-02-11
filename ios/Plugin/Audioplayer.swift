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
    let queue = DispatchQueue(label: "audio_player_queue")
    var audioSessionIsActive = false
    var isLoading = false
    var progressObserver: Any?
    
    init(player:AVPlayer,despPlugin:AudioplayerPlugin) {
        self.player = player
        self.despPlugin = despPlugin
    }

    func setUpAVPlayer(mediaUrl: String) {
        let url = URL(string: mediaUrl)!
        let playerItem = AVPlayerItem(url: url)
        player = AVPlayer(playerItem: playerItem)
    }

    func setUpProgressObserver() {
        progressObserver = player.addPeriodicTimeObserver(forInterval: CMTimeMakeWithSeconds(1, preferredTimescale: 1), queue: DispatchQueue.main) { [weak self] time in
            if (self != nil) {
                self?.despPlugin.onInfo(info: self?.getState() ?? [String: Any]())
            }
            // update the progress bar with the calculated progress value
        }
    }

    private func getState() -> [String: Any] {
        var ret = [String: Any]()
        var isPlaying = false
        var currentPosition = 0
        var totalDuration = 0
        var progress = 0
        
        if player != nil {
            isPlaying = player.isPlaying
            currentPosition = getCurrentPosition()
            totalDuration = getDuration()
            if totalDuration > 0 {
                progress = (currentPosition * 100) / totalDuration
            }
        }
        
        ret["isPlaying"] = isPlaying
        ret["isLoading"] = isLoading
        ret["duration"] = totalDuration
        ret["progress"] = progress
        ret["currentTime"] = currentPosition
        
        return ret
    }
    
    func initAudioSession() {
        guard !audioSessionIsActive else { return }
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, mode: .default)
            try AVAudioSession.sharedInstance().setActive(true)
            audioSessionIsActive = true
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
        var weakSelf = self
        queue.async { [weak self] in
            guard let self = self else { return }
            let playItem = AVPlayerItem(url: URL(string: value)!)
               
            self.player = AVPlayer(playerItem: playItem)
            self.player.automaticallyWaitsToMinimizeStalling = false
            NotificationCenter.default.addObserver(weakSelf, selector:#selector(weakSelf.playerDidFinishPlaying),
                                                  name: NSNotification.Name.AVPlayerItemDidPlayToEndTime, object: self.player.currentItem)
            if #available(iOS 15.0, *) {
                NotificationCenter.default.addObserver(self, selector: #selector(weakSelf.playerStateChanged), name: AVPlayer.rateDidChangeNotification, object: self.player)
            } else {
                self.player.addObserver(self, forKeyPath: "rate", options: NSKeyValueObservingOptions.new, context: nil)
            }

            self.player.play()
            self.setUpProgressObserver()
            return
       }
       
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
        queue.async { [weak self] in
            self?.player.pause()
        }
    }

    public func resume() {
        queue.async { [weak self] in
            self?.player.play()
        }
    }

    public func isPlaying() -> Bool {
        return player.isPlaying
    }

    public func seekTo(value: Int) -> String {
        let time:CMTime = CMTimeMake(value: Int64(value), timescale: 1000)
        
        queue.async { [weak self] in
            self?.player.seek(to: time)
        }
        return "yes"
    }

    public func getDuration() -> Int {
        guard let playerItem = player.currentItem else {
            return .zero
        }
        
        let duration = playerItem.asset.duration
        let seconds = CMTimeGetSeconds(duration)
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

    public func fadeIn(duration: Double) {
        queue.async { [weak self] in
            self?.player.volume = 0.0
            self?.player.play()
            let step = 0.1 / duration
            for i in stride(from: 0.0, to: 1.0, by: step) {
                DispatchQueue.main.asyncAfter(deadline: .now() + (duration * i)) {
                    self?.player.volume = Float(i)
                }
            }
        }
    }

    public func fadeOut(duration: Double) {
        queue.async { [weak self] in
            let step = 0.1 / duration
            for i in stride(from: 1.0, through: 0.0, by: -step) {
                DispatchQueue.main.asyncAfter(deadline: .now() + (duration * (1.0 - i))) {
                    self?.player.volume = Float(i)
                }
            }
            self?.player.pause()
        }
    }

    public func echo(_ value: String) -> String {
        return value
    }
}
