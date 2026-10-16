package com.jeremykenedy.alloytide;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.preference.PreferenceManager;

public final class SettingsProvider extends ContentProvider {
  @Override
  public boolean onCreate() {
    return true;
  }

  private void check(Uri uri) {
    if (!"com.jeremykenedy.alloytide.settings".equals(uri.getAuthority())
        || !("/schema".equals(uri.getPath()) || "/settings".equals(uri.getPath())))
      throw new IllegalArgumentException("Unknown settings URI");
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
    check(uri);
    boolean schema = "/schema".equals(uri.getPath());
    MatrixCursor cursor =
        new MatrixCursor(
            schema
                ? new String[] {"key", "title", "type", "default", "choices", "randomAllowed"}
                : new String[] {"key", "value"});
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
    for (String[] spec : AlloyOptions.SCHEMA) {
      if (schema) {
        StringBuilder choices = new StringBuilder();
        for (int i = 3; i < spec.length; i++) choices.append(spec[i]).append('|');
        cursor.addRow(new Object[] {spec[0], spec[1], "choice", spec[2], choices + "random", true});
      } else cursor.addRow(new Object[] {spec[0], prefs.getString(spec[0], spec[2])});
    }
    cursor.addRow(
        schema
            ? new Object[] {
              "randomize_all", "Randomize all on start", "boolean", "false", "false|true", false
            }
            : new Object[] {
              "randomize_all", Boolean.toString(prefs.getBoolean("randomize_all", false))
            });
    return cursor;
  }

  @Override
  public int update(Uri uri, ContentValues values, String where, String[] args) {
    check(uri);
    if (!"/settings".equals(uri.getPath()) || values == null)
      throw new IllegalArgumentException("Settings update required");
    String key = values.getAsString("key"), value = values.getAsString("value");
    if (!AlloyOptions.supported(key, value))
      throw new IllegalArgumentException("Unsupported setting value");
    SharedPreferences.Editor editor =
        PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
    if ("randomize_all".equals(key)) editor.putBoolean(key, Boolean.parseBoolean(value));
    else editor.putString(key, value);
    editor.apply();
    return 1;
  }

  @Override
  public String getType(Uri uri) {
    check(uri);
    return "vnd.android.cursor.dir/vnd.alloytide.setting.v1";
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    throw new UnsupportedOperationException("Insert unsupported");
  }

  @Override
  public int delete(Uri uri, String where, String[] args) {
    throw new UnsupportedOperationException("Delete unsupported");
  }
}
