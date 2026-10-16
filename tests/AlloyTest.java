package com.jeremykenedy.alloytide;

import java.util.Arrays;
import java.util.Random;

public final class AlloyTest {
  private static void check(boolean condition) {
    if (!condition) throw new AssertionError();
  }

  public static void main(String[] args) {
    String[] values = new String[AlloyOptions.SCHEMA.length];
    for (int row = 0; row < values.length; row++) values[row] = AlloyOptions.SCHEMA[row][2];
    AlloyOptions defaults = new AlloyOptions(values, new Random(1), false);
    check(defaults.finish == 0 && !defaults.neon && defaults.scale == 1 && defaults.travel == .15f);
    check(!AlloyOptions.supported(null, "silver"));
    check(!AlloyOptions.supported("finish", null));
    check(!AlloyOptions.supported("finish", "invalid"));
    check(!AlloyOptions.supported("randomize_all", "random"));
    check(AlloyOptions.supported("randomize_all", "true"));
    check(AlloyOptions.supported("randomize_all", "false"));
    for (int row = 0; row < values.length; row++) {
      String[] spec = AlloyOptions.SCHEMA[row];
      check(AlloyOptions.row(spec[0]) == row);
      for (int option = 3; option < spec.length; option++) {
        values[row] = spec[option];
        check(AlloyOptions.supported(spec[0], values[row]));
        AlloyOptions resolved = new AlloyOptions(values, new Random(1), false);
        check(resolved.depth > 0 && resolved.speed > 0 && resolved.roughness > 0);
      }
      values[row] = null;
      AlloyOptions fallback = new AlloyOptions(values, new Random(1), false);
      check(fallback.speed > 0);
      values[row] = spec[2];
      check(AlloyOptions.supported(spec[0], "random"));
    }
    Arrays.fill(values, "random");
    boolean neon = false, metal = false, wide = false;
    for (int seed = 0; seed < 100; seed++) {
      AlloyOptions random = new AlloyOptions(values, new Random(seed), seed % 2 == 0);
      neon |= random.neon;
      metal |= !random.neon;
      wide |= random.travel > .15f;
      check(random.finish >= 0 && random.finish < 4 && random.accent >= 0 && random.accent < 4);
    }
    check(neon && metal && wide);
    TileMesh single = new TileMesh(0);
    check(single.vertices.length == 6 * 9 * 6 * 8);
    for (int radius : new int[] {0, 1}) {
      TileMesh mesh = new TileMesh(radius);
      check(mesh.vertices.length == (radius * 2 + 1) * (radius * 2 + 1) * single.vertices.length);
      for (int i = 0; i < mesh.vertices.length; i += 8) {
        double length = 0;
        for (int n = 0; n < 3; n++) {
          check(Float.isFinite(mesh.vertices[i + n]));
          check(Math.abs(mesh.vertices[i + n]) <= .50001f);
          length += mesh.vertices[i + 3 + n] * mesh.vertices[i + 3 + n];
        }
        check(Math.abs(length - 1) < .00001);
        check(Math.abs(mesh.vertices[i + 6]) <= radius && Math.abs(mesh.vertices[i + 7]) <= radius);
      }
      for (int i = 0; i < mesh.vertices.length; i += 24) {
        float[] a = Arrays.copyOfRange(mesh.vertices, i, i + 3);
        float[] b = Arrays.copyOfRange(mesh.vertices, i + 8, i + 11);
        float[] c = Arrays.copyOfRange(mesh.vertices, i + 16, i + 19);
        float[] ab = {b[0] - a[0], b[1] - a[1], b[2] - a[2]};
        float[] ac = {c[0] - a[0], c[1] - a[1], c[2] - a[2]};
        float[] cross = {
          ab[1] * ac[2] - ab[2] * ac[1],
          ab[2] * ac[0] - ab[0] * ac[2],
          ab[0] * ac[1] - ab[1] * ac[0]
        };
        float dot = 0;
        for (int n = 0; n < 3; n++) dot += cross[n] * mesh.vertices[i + 3 + n];
        check(dot > .00001f);
      }
    }
    for (int radius : new int[] {-1, 33}) {
      try {
        new TileMesh(radius);
        throw new AssertionError("Unsafe mesh radius accepted");
      } catch (IllegalArgumentException expected) {
      }
    }
    System.out.println("Alloy settings and rounded mesh behavior passed");
  }
}
