package com.thouland.plugins.audioplayer;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;

import com.getcapacitor.JSObject;
import com.getcapacitor.PluginCall;

import java.io.IOException;

public class Audioplayer {

    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private Handler progressHandler;
    private Runnable progressRunnable;
    private Context context;
    private boolean isLoading = false;

    private boolean isPaused = true;
    public AudioplayerPlugin AudioplayerPlugin;
    private static final float FADE_STEP = 0.1f;
    private static final int FADE_HANDLER_DELAY = 100;
    
    private Handler mFadeHandler;

    private ProgressUpdateListener progressUpdateListener;

    private float mVolume = 1.0f;
    private boolean mIsFadingOut = false;
    private boolean mIsFadingIn = false;
    private int retryCount = 0;
    private final int MAX_RETRY = 1;
    public void setup(AudioplayerPlugin AudioplayerPlugin){
        this.AudioplayerPlugin = AudioplayerPlugin;
        this.context = AudioplayerPlugin.getContext();
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    if (mediaPlayer != null) {
                        setVolume(0.1f);
                    }
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    if (mediaPlayer != null) {
                        mediaPlayer.setVolume(1.0f, 1.0f);
                        resume();
                    }
                    break;
            }
        }
    };

    private void initMediaPlayer(PluginCall call,String url) throws IOException {
        try {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
            Log.i("dot","initMediaPlayer:"+url);
            isLoading = true;

            if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

//                HandlerThread handlerThread = new HandlerThread("MediaPlayerThread");
//                handlerThread.start();
//                Handler handler = new Handler(handlerThread.getLooper());
//                Context localContext = this.context;
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });

                if (mediaPlayer == null) {
                    mediaPlayer = new MediaPlayer();
                    mediaPlayer.setAudioAttributes(
                            new AudioAttributes.Builder()
                                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(AudioAttributes.USAGE_MEDIA)
                                    .build()
                    );
//                    mediaPlayer.setWakeMode(this.context, PowerManager.PARTIAL_WAKE_LOCK);
                } else {
//                            mediaPlayer.release();
//                            mediaPlayer.stop();
                    mediaPlayer.reset();
                }

                try {
                    mediaPlayer.setDataSource(this.context, Uri.parse(url));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer mp, int what, int extra) {
                        Log.e("RemoteMediaPlayer", "MediaPlayer error: what=" + what + " extra=" + extra);
                        if (retryCount < MAX_RETRY) {
                            retryCount++;
                            try {
                                initMediaPlayer(call, url);
                            } catch (IOException e) {
                                retryCount = 0;
                                AudioplayerPlugin.onCompleted();
                                throw new RuntimeException(e);
                            }
                        } else {
                            retryCount = 0;
                            isLoading = false;
//                                stop();
                            AudioplayerPlugin.onCompleted();
                            // Show error message or take other action as needed
                        }

                        return true;
                    }
                });

                mediaPlayer.setOnInfoListener(new MediaPlayer.OnInfoListener() {
                    @Override
                    public boolean onInfo(MediaPlayer mp, int what, int extra) {
                        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
                            // the media player is paused, do something
                            isLoading = true;
                            AudioplayerPlugin.onInfo(getState());
                            return true;
                        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
                            // the media player has started playing, do something
                            isLoading = false;
                            AudioplayerPlugin.onInfo(getState());
                            return true;
                        }
                        return false;
                    }
                });

                mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        setUpProgressUpdateListener();
                        mp.start();
                        AudioplayerPlugin.onPlaying();
                        isLoading = false;
                    }
                });

                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i("dot","onCompletion");
                        if (!isLoading){
                            isLoading=true;
                            AudioplayerPlugin.onCompleted();
                        }
                    }
                });

                mediaPlayer.prepareAsync();

            } else {
                isLoading = false;
                AudioplayerPlugin.onCompleted();
            }
        } catch (Exception e) {
            AudioplayerPlugin.onCompleted();
            e.printStackTrace();
        }
    }

    public Void start(PluginCall call,String url) throws IOException {
        initMediaPlayer(call,url);
        return null;
    }

    public Void resume() {
        if(this.AudioplayerPlugin != null && mediaPlayer != null && !mediaPlayer.isPlaying()){
            mediaPlayer.start();
            this.AudioplayerPlugin.onPlaying();
        }
        return null;
    }

//    public Void resume() {
//        int result = audioManager.requestAudioFocus(audioFocusChangeListener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
//        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
//            if (mediaPlayer != null && !isLoading) {
//                mediaPlayer.start();
//                this.AudioplayerPlugin.onPlaying();
//            }
//        }
//        return null;
//    }

    public Void seekTo(int value) {
        if (mediaPlayer != null && !isLoading) {
            mediaPlayer.seekTo(value);
        }
        return null;
    }

    public int getCurrentPosition() {
        if (mediaPlayer == null || isLoading) {
            return 0;
        }
        return mediaPlayer.getCurrentPosition();
    }

    public int getDuration() {
        if (mediaPlayer == null || isLoading) {
            return 0;
        }
        return mediaPlayer.getDuration();
    }

    public Void pause() {
        if(mediaPlayer != null && isLoading == false && mediaPlayer.isPlaying()){
            mediaPlayer.pause();
            this.AudioplayerPlugin.onPaused();
        }
        return null;
    }

    public Void stop() {
        if(mediaPlayer!=null) {
            if(mediaPlayer.isPlaying())
                mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer=null;
        }
        if (audioManager != null) {
            audioManager.abandonAudioFocus(audioFocusChangeListener);
        }

        return null;
    }

    public boolean isPlaying() {
        if (mediaPlayer == null || isLoading == true) {
            return false;
        }
        return mediaPlayer.isPlaying();
    }

    public boolean isPaused() {
        if (mediaPlayer == null || isLoading == true) {
            return true;
        }
        return !mediaPlayer.isPlaying();
    }

    public String echo(String value) {
        return value;
    }

    private void fadeIn(final int duration) {
        if (mediaPlayer == null) return;
        mVolume = 0.0f;
        mIsFadingIn = true;
        mFadeHandler = new Handler();
        mFadeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsFadingIn) return;
                if (mVolume >= 1.0f) {
                    mIsFadingIn = false;
                    mFadeHandler.removeCallbacksAndMessages(null);
                    return;
                }
                mVolume += FADE_STEP;
                setVolume(mVolume);
                mFadeHandler.postDelayed(this, duration * FADE_HANDLER_DELAY);
            }
        }, duration * FADE_HANDLER_DELAY);
    }

    private void fadeOut(final int duration) {
        if (mediaPlayer == null) return;
        mIsFadingOut = true;
        mFadeHandler = new Handler();
        mFadeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!mIsFadingOut) return;
                if (mVolume <= 0.0f) {
                    mIsFadingOut = false;
                    pause();
                    mFadeHandler.removeCallbacksAndMessages(null);
                    return;
                }
                mVolume -= FADE_STEP;
                setVolume(mVolume);
                mFadeHandler.postDelayed(this, duration * FADE_HANDLER_DELAY);
            }
        }, duration * FADE_HANDLER_DELAY);
    }

    private void setVolume(float volume) {
        if (mediaPlayer == null) return;
        mVolume = volume;
        mediaPlayer.setVolume(volume, volume);
    }

    public void setUpProgressUpdateListener() {
        progressUpdateListener = new ProgressUpdateListener();
        mediaPlayer.setOnSeekCompleteListener(progressUpdateListener);
        progressHandler = new Handler();
        progressRunnable = new ProgressRunnable();
        progressHandler.postDelayed(progressRunnable, 1000);
    }

    private class ProgressRunnable implements Runnable {
        @Override
        public void run() {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                isPaused = false;
                JSObject state = getState();
                AudioplayerPlugin.onInfo(state);
            } else if (mediaPlayer != null && isPaused != (!mediaPlayer.isPlaying())) {
                isPaused = true;
                JSObject state = getState();
                AudioplayerPlugin.onInfo(state);
            } else if (mediaPlayer != null && isLoading) {
                isPaused = true;
                JSObject state = getState();
                AudioplayerPlugin.onInfo(state);
            }
            progressHandler.postDelayed(this, 1000);
        }
    }

    private JSObject getState() {
        JSObject ret = new JSObject();
        boolean isPlaying = false;
        int currentPosition = 0;
        int totalDuration = 0;
        int progress = 0;

        if (mediaPlayer != null){
            isPlaying = mediaPlayer.isPlaying();
            currentPosition = getCurrentPosition();
            totalDuration = getDuration();
            if (totalDuration > 0) {
                progress = (currentPosition * 100) / totalDuration;
            }
        }
        ret.put("isPlaying", isPlaying);
        ret.put("isLoading", isLoading);
        ret.put("duration", totalDuration);
        ret.put("progress", progress);
        ret.put("currentTime", currentPosition);

        return ret;
    }

    private class ProgressUpdateListener implements MediaPlayer.OnSeekCompleteListener {
        @Override
        public void onSeekComplete(MediaPlayer mp) {
            // your implementation to update the progress bar or any other UI element
            JSObject state = getState();
            AudioplayerPlugin.onInfo(state);
            // update the progress bar with the calculated progress value
        }
    }
}
