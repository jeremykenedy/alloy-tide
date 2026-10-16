package com.jeremykenedy.alloytide;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

public final class SettingsActivity extends Activity {
  @Override
  public void onCreate(Bundle state) {
    super.onCreate(state);
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    LinearLayout content = new LinearLayout(this);
    content.setOrientation(LinearLayout.VERTICAL);
    content.setPadding(56, 32, 56, 40);
    TextView title = new TextView(this);
    title.setText("Alloy Tide");
    title.setTextSize(30);
    content.addView(title);
    TextView description = new TextView(this);
    description.setText(
        "Beveled metal tiles rise in continuous waves. Changes apply when the scene next starts.");
    description.setTextSize(17);
    content.addView(description);
    Button preview = new Button(this);
    preview.setText("Preview animation");
    preview.setOnClickListener(
        new android.view.View.OnClickListener() {
          @Override
          public void onClick(android.view.View view) {
            startActivity(new Intent(SettingsActivity.this, PreviewActivity.class));
          }
        });
    content.addView(preview);
    for (String[] spec : AlloyOptions.SCHEMA) {
      TextView label = new TextView(this);
      label.setText(spec[1]);
      label.setTextSize(19);
      label.setPadding(0, 18, 0, 4);
      content.addView(label);
      String[] choices = new String[spec.length - 2];
      System.arraycopy(spec, 3, choices, 0, spec.length - 3);
      choices[choices.length - 1] = "random";
      String[] labels = new String[choices.length];
      for (int i = 0; i < choices.length; i++) {
        String value = choices[i];
        labels[i] = Character.toUpperCase(value.charAt(0)) + value.substring(1);
      }
      Spinner spinner = new Spinner(this);
      ArrayAdapter<String> adapter =
          new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, labels);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      spinner.setAdapter(adapter);
      String current = prefs.getString(spec[0], spec[2]);
      for (int i = 0; i < choices.length; i++)
        if (choices[i].equals(current)) spinner.setSelection(i);
      spinner.setOnItemSelectedListener(
          new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                AdapterView<?> parent, android.view.View view, int position, long id) {
              prefs.edit().putString(spec[0], choices[position]).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
          });
      content.addView(spinner);
    }
    CheckBox random = new CheckBox(this);
    random.setText("Randomize all scene settings on start");
    random.setChecked(prefs.getBoolean("randomize_all", false));
    random.setOnCheckedChangeListener(
        new android.widget.CompoundButton.OnCheckedChangeListener() {
          @Override
          public void onCheckedChanged(android.widget.CompoundButton button, boolean checked) {
            prefs.edit().putBoolean("randomize_all", checked).apply();
          }
        });
    content.addView(random);
    Button credits = new Button(this);
    credits.setText("Credits and licenses");
    credits.setOnClickListener(
        new android.view.View.OnClickListener() {
          @Override
          public void onClick(android.view.View view) {
            showCredits();
          }
        });
    content.addView(credits);
    ScrollView scroll = new ScrollView(this);
    scroll.addView(content);
    setContentView(scroll);
    scroll.setFocusable(false);
    content.setFocusable(false);
    preview.setFocusableInTouchMode(true);
    preview.requestFocus();
  }

  private void showCredits() {
    TextView text = new TextView(this);
    text.setTextSize(16);
    text.setTextIsSelectable(true);
    text.setPadding(32, 24, 32, 24);
    StringBuilder licenses = new StringBuilder();
    for (String file : new String[] {"NOTICE.txt", "Apache-2.0.txt", "CC0-1.0.txt"}) {
      try (java.io.InputStream in = getAssets().open("licenses/" + file)) {
        java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int count;
        while ((count = in.read(buffer)) != -1) bytes.write(buffer, 0, count);
        licenses.append(new String(bytes.toByteArray(), java.nio.charset.StandardCharsets.UTF_8));
        licenses.append("\n\n");
      } catch (java.io.IOException error) {
        licenses.append("Unable to open ").append(file).append("\n");
      }
    }
    text.setText(licenses);
    ScrollView scroll = new ScrollView(this);
    scroll.addView(text);
    new android.app.AlertDialog.Builder(this)
        .setTitle("Credits and licenses")
        .setView(scroll)
        .setPositiveButton("Close", null)
        .show();
  }
}
