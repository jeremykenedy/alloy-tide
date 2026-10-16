package com.jeremykenedy.alloytide;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import java.util.Random;

public final class AlloySceneView extends GLSurfaceView {
  private final AlloyRenderer renderer;
  private final Handler handler = new Handler(Looper.getMainLooper());
  private boolean running;
  private long previous;
  private final Runnable tick =
      new Runnable() {
        @Override
        public void run() {
          if (!running) return;
          long now = SystemClock.uptimeMillis();
          renderer.seconds += Math.min(.1f, Math.max(0, now - previous) * .001f);
          previous = now;
          requestRender();
          handler.postDelayed(this, 33);
        }
      };

  public AlloySceneView(Context context) {
    super(context);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String[] values = new String[AlloyOptions.SCHEMA.length];
    for (int i = 0; i < values.length; i++)
      values[i] = prefs.getString(AlloyOptions.SCHEMA[i][0], AlloyOptions.SCHEMA[i][2]);
    renderer =
        new AlloyRenderer(
            context,
            new AlloyOptions(values, new Random(), prefs.getBoolean("randomize_all", false)));
    setEGLContextClientVersion(2);
    setEGLConfigChooser(new TileEglConfig());
    setRenderer(renderer);
    setRenderMode(RENDERMODE_WHEN_DIRTY);
  }

  public void start() {
    if (running) return;
    running = true;
    previous = SystemClock.uptimeMillis();
    onResume();
    handler.post(tick);
  }

  public void stop() {
    if (!running) return;
    running = false;
    handler.removeCallbacks(tick);
    onPause();
  }

  public void release() {
    stop();
  }

  @Override
  protected void onDetachedFromWindow() {
    stop();
    super.onDetachedFromWindow();
  }
}
