package com.jeremykenedy.alloytide;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

public final class PreviewActivity extends Activity {
  private AlloySceneView scene;

  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    getWindow()
        .getDecorView()
        .setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
    scene = new AlloySceneView(this);
    setContentView(scene);
  }

  @Override
  protected void onResume() {
    super.onResume();
    if (scene != null) scene.start();
  }

  @Override
  protected void onPause() {
    if (scene != null) scene.stop();
    super.onPause();
  }

  @Override
  protected void onDestroy() {
    if (scene != null) scene.release();
    scene = null;
    super.onDestroy();
  }
}
