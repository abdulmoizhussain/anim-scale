package com.example.abdul.animscale;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private TextView animatorCurrentValue;
    private TextView windowCurrentValue;
    private TextView transitionCurrentValue;
    private EditText animatorScaleInput;
    private EditText windowScaleInput;
    private EditText transitionScaleInput;
    private TextView permissionSummaryText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        animatorCurrentValue = findViewById(R.id.animatorCurrentValue);
        windowCurrentValue = findViewById(R.id.windowCurrentValue);
        transitionCurrentValue = findViewById(R.id.transitionCurrentValue);
        animatorScaleInput = findViewById(R.id.animatorScaleInput);
        windowScaleInput = findViewById(R.id.windowScaleInput);
        transitionScaleInput = findViewById(R.id.transitionScaleInput);
        permissionSummaryText = findViewById(R.id.permissionSummaryText);

        bindScaleControls(
                R.id.animatorScaleInput,
                R.id.animatorSetButton,
                R.id.animatorDefaultButton,
                R.id.animatorDisableButton,
                Settings.Global.ANIMATOR_DURATION_SCALE
        );
        bindScaleControls(
                R.id.windowScaleInput,
                R.id.windowSetButton,
                R.id.windowDefaultButton,
                R.id.windowDisableButton,
                Settings.Global.WINDOW_ANIMATION_SCALE
        );
        bindScaleControls(
                R.id.transitionScaleInput,
                R.id.transitionSetButton,
                R.id.transitionDefaultButton,
                R.id.transitionDisableButton,
                Settings.Global.TRANSITION_ANIMATION_SCALE
        );

        findViewById(R.id.permissionCheckButton).setOnClickListener(v -> showPermissionStatus());
        findViewById(R.id.adbPermissionHintButton).setOnClickListener(v -> showAdbPermissionHint());

        showAnimationSettings();
        updatePermissionSummary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (permissionSummaryText != null) {
            showAnimationSettings();
            updatePermissionSummary();
        }
    }

    private void bindScaleControls(
            int inputId,
            int applyButtonId,
            int defaultButtonId,
            int disableButtonId,
            String settingKey
    ) {
        EditText input = findViewById(inputId);
        findViewById(applyButtonId).setOnClickListener(v -> applyScaleFromInput(settingKey, input));
        findViewById(defaultButtonId).setOnClickListener(v -> applyScale(settingKey, 1f));
        findViewById(disableButtonId).setOnClickListener(v -> applyScale(settingKey, 0f));
    }

    private boolean hasWriteSecureSettingsPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void updatePermissionSummary() {
        permissionSummaryText.setText(
                hasWriteSecureSettingsPermission()
                        ? R.string.permission_ready_summary
                        : R.string.permission_required_summary
        );
    }

    private String permissionState(boolean granted) {
        return getString(granted ? R.string.permission_granted : R.string.permission_not_granted);
    }

    private void showPermissionStatus() {
        updatePermissionSummary();
        String message = getString(
                R.string.permission_status_message,
                permissionState(hasWriteSecureSettingsPermission())
        );

        new AlertDialog.Builder(this)
                .setTitle(R.string.permission_status_title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setNeutralButton(R.string.adb_permission_hint_button, (dialog, which) -> showAdbPermissionHint())
                .show();
    }

    private void showAdbPermissionHint() {
        String command = getString(R.string.adb_permission_command, getPackageName());

        new AlertDialog.Builder(this)
                .setTitle(R.string.adb_permission_hint_title)
                .setMessage(getString(R.string.adb_permission_hint_message, command))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void showAnimationSettings() {
        showCurrentValue(animatorCurrentValue, animatorScaleInput, Settings.Global.ANIMATOR_DURATION_SCALE);
        showCurrentValue(windowCurrentValue, windowScaleInput, Settings.Global.WINDOW_ANIMATION_SCALE);
        showCurrentValue(transitionCurrentValue, transitionScaleInput, Settings.Global.TRANSITION_ANIMATION_SCALE);
    }

    private void showCurrentValue(TextView view, EditText input, String settingKey) {
        String value = Settings.Global.getString(getContentResolver(), settingKey);
        view.setText(getString(R.string.current_scale_format, value == null ? "—" : value));
        input.setText(value == null ? "" : value);
    }

    private void applyScaleFromInput(String settingKey, EditText input) {
        if (!hasWriteSecureSettingsPermission()) {
            showPermissionDenied();
            return;
        }

        String valueText = input.getText().toString().trim();
        try {
            float value = Float.parseFloat(valueText);
            if (Float.isNaN(value) || Float.isInfinite(value) || value < 0) {
                throw new NumberFormatException("Invalid animation scale");
            }
            if (applyScale(settingKey, value)) {
                input.clearFocus();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.invalid_scale_value, Toast.LENGTH_SHORT).show();
        }
    }

    private boolean applyScale(String settingKey, float value) {
        if (!hasWriteSecureSettingsPermission()) {
            showPermissionDenied();
            return false;
        }

        try {
            if (!Settings.Global.putFloat(getContentResolver(), settingKey, value)) {
                Toast.makeText(this, R.string.setting_update_failed, Toast.LENGTH_SHORT).show();
                return false;
            }
            showAnimationSettings();
            Toast.makeText(this, R.string.scale_updated, Toast.LENGTH_SHORT).show();
            return true;
        } catch (SecurityException e) {
            showPermissionDenied();
            return false;
        } catch (RuntimeException e) {
            Toast.makeText(this, R.string.setting_update_failed, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void showPermissionDenied() {
        updatePermissionSummary();
        Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_SHORT).show();
    }
}
