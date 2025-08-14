package com.example.testaddtoapp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.ViewGroup
import androidx.activity.ComponentActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.flutter.embedding.android.ExclusiveAppComponent
import io.flutter.embedding.android.FlutterView
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.FlutterEngineGroup
import io.flutter.plugin.common.MethodChannel

class FlutterViewEngines(private val context: Context) {
  private var activity: ComponentActivity? = null
  // There is one "engine group" for the whole app, so that the shared library is loaded
  // once and they share the same heap space and garbage collector.
  private val engineGroup = FlutterEngineGroup(context)
  // Each engine in the group has its own runtime state and widget tree.
  private var engines = mutableMapOf<String, FlutterViewEngine>()
  private var engineMethodChannels = mutableMapOf<String, MethodChannel>()

  // Channel for initial setup (Native -> Flutter)
  //private val FLUTTER_SETUP_CHANNEL_NAME = "com.example.testaddtoapp/setup"
  // Channel for ongoing communication (Flutter -> Native)
  private val SCROLL_CHANNEL_NAME = "com.example.testaddtoapp/scrollposition"

  fun attachToActivity(activity: ComponentActivity) {
    this.activity = activity
  }

  /**
   * Creates a new FlutterEngine instance and cache it with provided key or get the existing engine
   * if it's already created.
   *
   * @return the new or existing FlutterEngine.
   */
  fun createAndRunEngine(key: String, dartEntrypointArgs: List<String>): FlutterViewEngine {
    if (engines.containsKey(key)) {
      return engines[key]!!
    }

    val options =
      FlutterEngineGroup.Options(context).apply {
        automaticallyRegisterPlugins = false
        this.dartEntrypointArgs = dartEntrypointArgs
      }

    val engine = engineGroup.createAndRunEngine(options)
    FlutterEngineCache.getInstance().put(key, engine)
    val flutterViewEngine = FlutterViewEngine(engine)
    flutterViewEngine.attachToActivity(activity!!)
    engines[key] = flutterViewEngine

    // 1. Set up the MethodChannel for Flutter -> Native communication (e.g., scroll events)
    // This channel will be used by Flutter to send messages *to* this specific engine's native side.
    val scrollMethodChannel = MethodChannel(engine.dartExecutor.binaryMessenger, SCROLL_CHANNEL_NAME)
    scrollMethodChannel.setMethodCallHandler { call, result ->
      // The 'instanceId' here is known because this handler is specific to this engine.
      Log.d("FlutterViewEngines", "ScrollChannel: Method call '${call.method}' from Flutter instance '$key'")

      // You could also extract instanceId if Flutter sends it redundantly, but here we already know it.
      // val flutterSentInstanceId = call.argument<String>("instanceId")

      if (call.method == "scrollStateChanged") {
        val state = call.argument<String>("state")
        Log.i("FlutterViewEngines", "Instance '$key' scroll state: $state")
        // TODO: Relay this information to MainActivity or another relevant component if needed
        // For example, using a listener pattern or a shared ViewModel
        MainActivity.staticHandleScrollState(key, state ?: "unknown")

        result.success(null)
      } else {
        result.notImplemented()
      }
    }
    engineMethodChannels[key] = scrollMethodChannel // Store for potential cleanup

    return flutterViewEngine
  }
}

class FlutterViewEngine constructor(val engine: FlutterEngine) :
  DefaultLifecycleObserver, ExclusiveAppComponent<Activity> {
  private var activity: ComponentActivity? = null
  private var flutterView: FlutterView? = null

  fun attachToActivity(activity: ComponentActivity) {
    this.activity = activity
  }

  fun detachActivity() {
    if (flutterView != null) {
      unhookActivityAndView()
    }
    activity = null
  }

  fun attachFlutterView(flutterView: FlutterView) {
    this.flutterView = flutterView
    hookActivityAndView()
  }

  fun detachFlutterView(flutterViewContainer: ViewGroup) {
    flutterViewContainer.removeView(flutterView)
    unhookActivityAndView()
    flutterView = null
  }

  fun destroy() {
    detachActivity()
    engine.destroy()
  }

  override fun onResume(owner: LifecycleOwner) {
    if (activity != null) {
      engine.lifecycleChannel.appIsResumed()
    }
  }

  override fun onPause(owner: LifecycleOwner) {
    if (activity != null) {
      engine.lifecycleChannel.appIsInactive()
    }
  }

  override fun onStop(owner: LifecycleOwner) {
    if (activity != null) {
      engine.lifecycleChannel.appIsPaused()
    }
  }

  override fun onDestroy(owner: LifecycleOwner) {
    destroy()
  }

  override fun detachFromFlutterEngine() {
    // Do nothing here
  }

  override fun getAppComponent(): Activity {
    return activity!!
  }

  private fun hookActivityAndView() {
    // Assert state.
    activity!!.let { activity ->
      flutterView!!.let { flutterView ->
        engine.activityControlSurface.attachToActivity(this, activity.lifecycle)
        flutterView.attachToFlutterEngine(engine)
        activity.lifecycle.addObserver(this)
      }
    }
  }

  private fun unhookActivityAndView() {
    // Stop reacting to activity events.
    activity!!.lifecycle.removeObserver(this)

    // Plugins are no longer attached to an activity.
    engine.activityControlSurface.detachFromActivity()

    // Set Flutter's application state to detached.
    engine.lifecycleChannel.appIsDetached()

    // Detach rendering pipeline.
    flutterView!!.detachFromFlutterEngine()
  }
}
