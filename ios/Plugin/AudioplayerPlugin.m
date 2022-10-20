#import <Foundation/Foundation.h>
#import <Capacitor/Capacitor.h>

// Define the plugin using the CAP_PLUGIN Macro, and
// each method the plugin supports using the CAP_PLUGIN_METHOD macro.
CAP_PLUGIN(AudioplayerPlugin, "Audioplayer",
            CAP_PLUGIN_METHOD(start, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(pause, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(resume, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(getDuration, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(getCurrentPosition, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(isPaused, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(isPlaying, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(seek, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(stop, CAPPluginReturnPromise);
            CAP_PLUGIN_METHOD(echo, CAPPluginReturnPromise);
)
