package com.thouland.plugins.audioplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.getcapacitor.PluginCall;

public class Audioplayer {

    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Context context;
    private boolean isLoading = false;
    public AudioplayerPlugin AudioplayerPlugin;

    public void setup(AudioplayerPlugin AudioplayerPlugin){
        this.AudioplayerPlugin = AudioplayerPlugin;
        this.context = AudioplayerPlugin.getContext();
    }

    private void initMediaPlayer(PluginCall call,String url) {
        try {
            Log.i("dot","initMediaPlayer:"+url);
            isLoading = true;
            if (isPlaying()) {
                mediaPlayer.stop();
            }

            if (mediaPlayer != null) {
                mediaPlayer.release();
                mediaPlayer = null;
            }
//            mediaPlayer.release();
            mediaPlayer = new MediaPlayer();

            mediaPlayer.setAudioAttributes(
                  new AudioAttributes.Builder()
                      .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                      .setUsage(AudioAttributes.USAGE_MEDIA)
                      .build()
            );
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(this.context,Uri.parse(url));
            mediaPlayer.setLooping(false);
            mediaPlayer.prepareAsync();
            isLoading = false;

            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if(!mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                        AudioplayerPlugin.onPlaying();
                    }
                }
            });

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mediaPlayer) {
                    Log.i("dot","onCompletion");
                    if (!isLoading){
                        AudioplayerPlugin.onCompleted();
                    }
                }
            });

            // mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            //     @Override
            //     public boolean onInfo(MediaPlayer mediaPlayer, int i, int i1) {
            //         Log.e("dot","onInfo:"+i);
            //         AudioplayerPlugin.onInfo();
            //         return true;
            //     };
            // });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Void start(PluginCall call,String url) {
        if (!isLoading){
            initMediaPlayer(call,url);
        }
        return null;
    }

    public Void resume() {
        if(!isLoading && !mediaPlayer.isPlaying()){
            mediaPlayer.start();
            this.AudioplayerPlugin.onPlaying();
        }
        return null;
    }

    public Void seekTo(int value) {
        if (!isLoading && mediaPlayer != null) {
            mediaPlayer.seekTo(value);
        }
        return null;
    }

    public int getCurrentPosition() {
        if (!isLoading && mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getCurrentPosition();
        }
        return 0;
    }

    public int getDuration() {
        if (!isLoading && mediaPlayer != null && mediaPlayer.isPlaying()) {
            return mediaPlayer.getDuration();
        }
        return 0;
    }

    public Void pause() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            this.AudioplayerPlugin.onPaused();
        }
        return null;
    }

    public Void stop() {
        if(mediaPlayer.isPlaying()){
            mediaPlayer.reset();
            this.AudioplayerPlugin.onPaused();
        }
        return null;
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        return !mediaPlayer.isPlaying();
    }

    public String echo(String value) {
        return value;
    }
}
