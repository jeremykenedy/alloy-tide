package com.jeremykenedy.alloytide;

import android.opengl.GLSurfaceView;
import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

/** Prefer hardware edge antialiasing and retain support for TVs without multisampling. */
final class TileEglConfig implements GLSurfaceView.EGLConfigChooser {
  @Override
  public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
    for (int samples : new int[] {4, 2, 0}) {
      int[] attributes = {
        EGL10.EGL_RED_SIZE, 8,
        EGL10.EGL_GREEN_SIZE, 8,
        EGL10.EGL_BLUE_SIZE, 8,
        EGL10.EGL_ALPHA_SIZE, 0,
        EGL10.EGL_DEPTH_SIZE, 16,
        EGL10.EGL_RENDERABLE_TYPE, 4,
        EGL10.EGL_SAMPLE_BUFFERS, samples == 0 ? 0 : 1,
        EGL10.EGL_SAMPLES, samples,
        EGL10.EGL_NONE
      };
      EGLConfig[] configs = new EGLConfig[1];
      int[] count = new int[1];
      if (egl.eglChooseConfig(display, attributes, configs, 1, count) && count[0] > 0)
        return configs[0];
    }
    throw new IllegalStateException("No RGB depth configuration is available");
  }
}
