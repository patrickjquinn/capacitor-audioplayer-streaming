package com.thouland.plugins.audioplayer;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.IOException;

@CapacitorPlugin(name = "Audioplayer")
public class AudioplayerPlugin extends Plugin implements onCompletedListener {

    private Audioplayer implementation = new Audioplayer();

    @PluginMethod
    public Void start(PluginCall call) throws IOException {
        String value = call.getString("url");
        JSObject ret = new JSObject();
        implementation.setup(this);
        ret.put("value", implementation.start(call,value));
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void seek(PluginCall call) {
        JSObject ret = new JSObject();
        int value = call.getInt("seekTo");
        ret.put("value",  implementation.seekTo(value));
        call.resolve(ret);
        return null;
    }


    @PluginMethod
    public Void pause(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.pause());
        call.resolve(ret);
        return null;
    }
    
    @PluginMethod
    public Void resume(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.resume());
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void stop(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.stop());
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void getDuration(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.getDuration());
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void getCurrentPosition(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.getCurrentPosition());
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void isPlaying(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isPlaying());
        call.resolve(ret);
        return null;
    }

    @PluginMethod
    public Void isPaused(PluginCall call) {
        JSObject ret = new JSObject();
        ret.put("value", implementation.isPaused());
        call.resolve(ret);
        return null;
    }


    @PluginMethod
    public Void echo(PluginCall call) {
        String value = call.getString("value");

        JSObject ret = new JSObject();
        ret.put("value", implementation.echo(value));
        call.resolve(ret);
        return null;
    }

    @Override
    public void onCompleted() {
        notifyListeners("end", null);
    }

    public void onInfo(JSObject state) {
        notifyListeners("info", state);
    }

    public void onPlaying() {
        notifyListeners("play", null);
    }

    public void onPaused() {
        notifyListeners("pause", null);
    }


}
