package com.jeremykenedy.alloytide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

final class AlloyRenderer implements GLSurfaceView.Renderer {
  private static final float[][] METALS = {
    {.68f, .73f, .78f}, {.085f, .105f, .13f}, {.72f, .49f, .20f}, {.77f, .31f, .14f}
  };
  private static final float[][] ACCENTS = {
    {.02f, .3f, 1}, {.04f, 1, .25f}, {.55f, .08f, 1}, {1, .35f, .015f}
  };

  private final Context context;
  private final AlloyOptions options;
  private final float[] projection = new float[16], view = new float[16], vp = new float[16];
  private int program,
      shadowProgram,
      shadowBuffer,
      shadowTexture,
      shadowDepth,
      environmentTexture,
      buffer,
      count;
  private int width, height;
  private final float[] lightVP = new float[16];
  volatile float seconds;

  AlloyRenderer(Context context, AlloyOptions options) {
    this.context = context;
    this.options = options;
  }

  @Override
  public void onSurfaceCreated(GL10 gl, EGLConfig config) {
    program = GLES20.glCreateProgram();
    GLES20.glAttachShader(program, shader(GLES20.GL_VERTEX_SHADER, read("tiles.vert")));
    GLES20.glAttachShader(program, shader(GLES20.GL_FRAGMENT_SHADER, read("tiles.frag")));
    GLES20.glLinkProgram(program);
    int[] status = new int[1];
    GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0);
    if (status[0] == 0) throw new IllegalStateException(GLES20.glGetProgramInfoLog(program));
    shadowProgram = GLES20.glCreateProgram();
    GLES20.glAttachShader(shadowProgram, shader(GLES20.GL_VERTEX_SHADER, read("shadow.vert")));
    GLES20.glAttachShader(shadowProgram, shader(GLES20.GL_FRAGMENT_SHADER, read("shadow.frag")));
    GLES20.glLinkProgram(shadowProgram);
    GLES20.glGetProgramiv(shadowProgram, GLES20.GL_LINK_STATUS, status, 0);
    if (status[0] == 0) throw new IllegalStateException(GLES20.glGetProgramInfoLog(shadowProgram));
    setupShadows();
    loadEnvironment();
    TileMesh mesh = new TileMesh((int) Math.ceil(18 / options.scale));
    count = mesh.vertices.length / 8;
    FloatBuffer data =
        ByteBuffer.allocateDirect(mesh.vertices.length * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer();
    data.put(mesh.vertices).position(0);
    int[] names = new int[1];
    GLES20.glGenBuffers(1, names, 0);
    buffer = names[0];
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    GLES20.glBufferData(
        GLES20.GL_ARRAY_BUFFER, mesh.vertices.length * 4, data, GLES20.GL_STATIC_DRAW);
    GLES20.glEnable(GLES20.GL_DEPTH_TEST);
    GLES20.glEnable(GLES20.GL_CULL_FACE);
  }

  @Override
  public void onSurfaceChanged(GL10 gl, int width, int height) {
    this.width = width;
    this.height = height;
    GLES20.glViewport(0, 0, width, height);
    Matrix.perspectiveM(projection, 0, 43, width / (float) Math.max(1, height), .1f, 100);
  }

  @Override
  public void onDrawFrame(GL10 gl) {
    float t = seconds * options.speed;
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowBuffer);
    GLES20.glViewport(0, 0, 2048, 2048);
    GLES20.glClearColor(1, 1, 1, 1);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    GLES20.glUseProgram(shadowProgram);
    GLES20.glUniformMatrix4fv(
        GLES20.glGetUniformLocation(shadowProgram, "uVP"), 1, false, lightVP, 0);
    GLES20.glUniform1f(GLES20.glGetUniformLocation(shadowProgram, "uTime"), t);
    GLES20.glUniform1f(GLES20.glGetUniformLocation(shadowProgram, "uScale"), options.scale);
    GLES20.glUniform1f(GLES20.glGetUniformLocation(shadowProgram, "uDepth"), options.depth);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    int position = GLES20.glGetAttribLocation(shadowProgram, "aPosition");
    int cell = GLES20.glGetAttribLocation(shadowProgram, "aCell");
    GLES20.glEnableVertexAttribArray(position);
    GLES20.glVertexAttribPointer(position, 3, GLES20.GL_FLOAT, false, 32, 0);
    GLES20.glEnableVertexAttribArray(cell);
    GLES20.glVertexAttribPointer(cell, 2, GLES20.GL_FLOAT, false, 32, 24);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count);
    GLES20.glDisableVertexAttribArray(position);
    GLES20.glDisableVertexAttribArray(cell);
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    GLES20.glViewport(0, 0, width, height);
    float[] accent = ACCENTS[options.accent];
    float glow = options.neon ? .12f : .008f;
    GLES20.glClearColor(accent[0] * glow, accent[1] * glow, accent[2] * glow, 1);
    GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
    GLES20.glUseProgram(program);
    float eyeX =
        (options.neon ? 1.2f : 3.5f) + (float) Math.sin(seconds * .025) * options.travel * 4;
    float eyeY =
        (options.neon ? -.8f : -7.0f) + (float) Math.cos(seconds * .021) * options.travel * 2;
    Matrix.setLookAtM(view, 0, eyeX, eyeY, 11.5f, 0, 0, 0, 0, 1, 0);
    Matrix.multiplyMM(vp, 0, projection, 0, view, 0);
    GLES20.glUniformMatrix4fv(location("uVP"), 1, false, vp, 0);
    GLES20.glUniformMatrix4fv(location("uLightVP"), 1, false, lightVP, 0);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowTexture);
    GLES20.glUniform1i(location("uShadow"), 0);
    GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, environmentTexture);
    GLES20.glUniform1i(location("uEnvironment"), 1);
    GLES20.glUniform3f(location("uEye"), eyeX, eyeY, 11.5f);
    GLES20.glUniform1f(location("uTime"), t);
    GLES20.glUniform1f(location("uGlowPhase"), (float) ((t * .42) % (Math.PI * 2)));
    GLES20.glUniform1f(location("uTileScale"), options.scale);
    GLES20.glUniform1f(location("uScale"), options.scale);
    GLES20.glUniform1f(location("uDepth"), options.depth);
    GLES20.glUniform1f(location("uRoughness"), options.roughness);
    GLES20.glUniform1f(location("uBrightness"), options.brightness);
    GLES20.glUniform1f(location("uNeon"), options.neon ? 1 : 0);
    GLES20.glUniform3fv(location("uMetal"), 1, METALS[options.finish], 0);
    GLES20.glUniform3fv(location("uAccent"), 1, ACCENTS[options.accent], 0);
    GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffer);
    attribute("aPosition", 3, 0);
    attribute("aNormal", 3, 12);
    attribute("aCell", 2, 24);
    GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, count);
  }

  private void loadEnvironment() {
    try (InputStream in = context.getAssets().open("studio-rgbm.png")) {
      BitmapFactory.Options decode = new BitmapFactory.Options();
      decode.inPremultiplied = false;
      Bitmap bitmap = BitmapFactory.decodeStream(in, null, decode);
      if (bitmap == null) throw new IllegalStateException("Studio environment unavailable");
      int[] ids = new int[1];
      GLES20.glGenTextures(1, ids, 0);
      environmentTexture = ids[0];
      GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
      GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, environmentTexture);
      GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);
      bitmap.recycle();
      GLES20.glTexParameteri(
          GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
      GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
      GLES20.glTexParameteri(
          GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
      GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);
    } catch (java.io.IOException error) {
      throw new IllegalStateException(error);
    }
  }

  private void setupShadows() {
    int[] ids = new int[1];
    GLES20.glGenTextures(1, ids, 0);
    shadowTexture = ids[0];
    GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowTexture);
    GLES20.glTexImage2D(
        GLES20.GL_TEXTURE_2D,
        0,
        GLES20.GL_RGBA,
        2048,
        2048,
        0,
        GLES20.GL_RGBA,
        GLES20.GL_UNSIGNED_BYTE,
        null);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
    GLES20.glGenRenderbuffers(1, ids, 0);
    shadowDepth = ids[0];
    GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, shadowDepth);
    GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, 2048, 2048);
    GLES20.glGenFramebuffers(1, ids, 0);
    shadowBuffer = ids[0];
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowBuffer);
    GLES20.glFramebufferTexture2D(
        GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, shadowTexture, 0);
    GLES20.glFramebufferRenderbuffer(
        GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_RENDERBUFFER, shadowDepth);
    if (GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER) != GLES20.GL_FRAMEBUFFER_COMPLETE)
      throw new IllegalStateException("Shadow framebuffer unavailable");
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    float[] lightProjection = new float[16], lightView = new float[16];
    Matrix.orthoM(lightProjection, 0, -23, 23, -23, 23, 1, 70);
    Matrix.setLookAtM(lightView, 0, -12, -8, 24, 0, 0, 0, 0, 1, 0);
    Matrix.multiplyMM(lightVP, 0, lightProjection, 0, lightView, 0);
  }

  private int location(String name) {
    return GLES20.glGetUniformLocation(program, name);
  }

  private void attribute(String name, int size, int offset) {
    int loc = GLES20.glGetAttribLocation(program, name);
    GLES20.glEnableVertexAttribArray(loc);
    GLES20.glVertexAttribPointer(loc, size, GLES20.GL_FLOAT, false, 32, offset);
  }

  private int shader(int kind, String text) {
    int shader = GLES20.glCreateShader(kind);
    GLES20.glShaderSource(shader, text);
    GLES20.glCompileShader(shader);
    int[] ok = new int[1];
    GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, ok, 0);
    if (ok[0] == 0) throw new IllegalStateException(GLES20.glGetShaderInfoLog(shader));
    return shader;
  }

  private String read(String name) {
    try (InputStream in = context.getAssets().open(name);
        ByteArrayOutputStream out = new ByteArrayOutputStream()) {
      byte[] data = new byte[4096];
      int n;
      while ((n = in.read(data)) >= 0) out.write(data, 0, n);
      return out.toString("UTF-8");
    } catch (java.io.IOException error) {
      throw new IllegalStateException(error);
    }
  }
}
