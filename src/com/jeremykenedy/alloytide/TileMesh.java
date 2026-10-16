package com.jeremykenedy.alloytide;

/** Rounded box surfaces with shared analytic normals at bevel boundaries. */
public final class TileMesh {
  private static final float[] GRID = {-.5f, -.46f, .46f, .5f};
  public final float[] vertices;

  public TileMesh(int radius) {
    if (radius < 0 || radius > 32)
      throw new IllegalArgumentException("Tile radius outside safe range");
    int side = radius * 2 + 1;
    vertices = new float[side * side * 6 * 9 * 6 * 8];
    float[] tile = tileVertices();
    int at = 0;
    for (int y = -radius; y <= radius; y++)
      for (int x = -radius; x <= radius; x++)
        for (int vertex = 0; vertex < tile.length; vertex += 6) {
          System.arraycopy(tile, vertex, vertices, at, 6);
          at += 6;
          vertices[at++] = x;
          vertices[at++] = y;
        }
  }

  private static float[] tileVertices() {
    float[] tile = new float[6 * 9 * 6 * 6];
    int at = 0;
    int[] corners = {0, 1, 2, 2, 1, 3};
    for (int face = 0; face < 6; face++)
      for (int v = 0; v < 3; v++)
        for (int u = 0; u < 3; u++)
          for (int corner : corners) {
            float a = GRID[u + (corner & 1)], b = GRID[v + (corner >> 1)];
            float[] point = face(face, a, b);
            float[] normal = new float[3];
            float length = 0;
            for (int i = 0; i < 3; i++) {
              float inner = Math.max(-.46f, Math.min(.46f, point[i]));
              normal[i] = point[i] - inner;
              length += normal[i] * normal[i];
            }
            length = (float) Math.sqrt(length);
            for (int i = 0; i < 3; i++) normal[i] /= length;
            for (int i = 0; i < 3; i++) {
              float inner = Math.max(-.46f, Math.min(.46f, point[i]));
              tile[at++] = inner + normal[i] * .04f;
            }
            for (float n : normal) tile[at++] = n;
          }
    return tile;
  }

  private static float[] face(int face, float a, float b) {
    switch (face) {
      case 0:
        return new float[] {a, b, .5f};
      case 1:
        return new float[] {-a, b, -.5f};
      case 2:
        return new float[] {.5f, b, -a};
      case 3:
        return new float[] {-.5f, b, a};
      case 4:
        return new float[] {a, .5f, -b};
      default:
        return new float[] {a, -.5f, b};
    }
  }
}
