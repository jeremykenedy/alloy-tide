package com.jeremykenedy.alloytide;

import java.util.Random;

public final class AlloyOptions {
  public static final String[][] SCHEMA = {
    {"finish", "Metal finish", "silver", "silver", "graphite", "champagne", "copper"},
    {"mode", "Scene", "metal", "metal", "neon"},
    {"accent", "Light color", "blue", "blue", "green", "violet", "amber"},
    {"speed", "Wave speed", "gentle", "slow", "gentle", "flowing"},
    {"scale", "Tile size", "medium", "large", "medium", "small"},
    {"depth", "Extrusion", "sculpted", "subtle", "sculpted", "dramatic"},
    {"roughness", "Surface", "satin", "polished", "satin", "brushed"},
    {"brightness", "Lighting", "balanced", "dim", "balanced", "bright"},
    {"camera", "Camera travel", "gentle", "gentle", "wide"}
  };
  public final int finish, accent;
  public final float speed, scale, depth, roughness, brightness, travel;
  public final boolean neon;

  public AlloyOptions(String[] values, Random random, boolean randomize) {
    int[] choice = new int[SCHEMA.length];
    for (int i = 0; i < choice.length; i++)
      choice[i] = index(i, randomize ? "random" : values[i], random);
    finish = choice[0];
    neon = choice[1] == 1;
    accent = choice[2];
    speed = new float[] {.18f, .38f, .7f}[choice[3]];
    scale = new float[] {1.4f, 1f, .72f}[choice[4]];
    depth = new float[] {.65f, 1.65f, 2.8f}[choice[5]];
    roughness = new float[] {.17f, .32f, .57f}[choice[6]];
    brightness = new float[] {.65f, 1f, 1.3f}[choice[7]];
    travel = choice[8] == 0 ? .15f : .55f;
  }

  private static int index(int row, String value, Random random) {
    String[] spec = SCHEMA[row];
    if ("random".equals(value)) return random.nextInt(spec.length - 3);
    for (int i = 3; i < spec.length; i++) if (spec[i].equals(value)) return i - 3;
    return java.util.Arrays.asList(spec).subList(3, spec.length).indexOf(spec[2]);
  }

  public static int row(String key) {
    for (int i = 0; i < SCHEMA.length; i++) if (SCHEMA[i][0].equals(key)) return i;
    return -1;
  }

  public static boolean supported(String key, String value) {
    if ("randomize_all".equals(key)) return "true".equals(value) || "false".equals(value);
    int row = row(key);
    if (row < 0 || value == null) return false;
    if ("random".equals(value)) return true;
    for (int i = 3; i < SCHEMA[row].length; i++) if (SCHEMA[row][i].equals(value)) return true;
    return false;
  }
}
