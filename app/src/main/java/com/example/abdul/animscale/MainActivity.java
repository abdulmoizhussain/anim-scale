package com.example.abdul.animscale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG_NAME = "console_log_tag";
    TextView tvADS, tvWAS, tvTAS;
    EditText etADS, etWAS, etTAS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //	https://www.dev2qa.com/how-to-grant-write-settings-permission-in-android/
        //	https://stackoverflow.com/questions/13045283/write-secure-settings-permission-error-even-when-added-in-manifest
        //	adb shell pm grant your.package.name android.permission.WRITE_SECURE_SETTINGS
        //	adb shell pm grant com.example.abdul.animscale android.permission.WRITE_SECURE_SETTINGS

        if (android.os.Build.VERSION.SDK_INT > 23) {
            if (Settings.System.canWrite(getApplicationContext()) == Boolean.FALSE) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                startActivity(intent);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SECURE_SETTINGS) == PackageManager.PERMISSION_DENIED) {
            new AlertDialog.Builder(this)
                    .setMessage("You don't have WRITE_SECURE_SETTINGS permission! Run following command:\n\n" +
                            "adb shell pm grant " +
                            getApplicationContext().getPackageName() +
                            " android.permission.WRITE_SECURE_SETTINGS")
                    .show();
        }

//		DevicePolicyManager mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//		ComponentName mAdminName = new ComponentName(this, MainActivity.class);
//
//		if (!mDPM.isAdminActive(mAdminName)) {
//			// try to become active – must happen here in this activity, to get result
//			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
//			intent.putExtra(
//					DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//					"Additional text explaining why this needs to be added."
//			);
//			startActivityForResult(intent, 1);
////			startActivityForResult(intent, 1);
//		} else {
//			// Already is a device administrator, can do security operations now.
//			mDPM.lockNow();
//		}

        tvADS = findViewById(R.id.textView2);
        tvWAS = findViewById(R.id.textView3);
        tvTAS = findViewById(R.id.textView4);
        showAnimationSettings();
//		showAnimationSettings(textView);
//		Settings.Global.putInt(
//				getContentResolver(),
//				Settings.Global.ANIMATOR_DURATION_SCALE,
//				0
//		);
//		Settings.Global.putInt(
//				getContentResolver(),
//				Settings.Global.WINDOW_ANIMATION_SCALE,
//				0
//		);
//		Settings.Global.putInt(
//				getContentResolver(),
//				Settings.Global.TRANSITION_ANIMATION_SCALE,
//				0
//		);

//		Settings.Global.ANIMATOR_DURATION_SCALE;
//		Settings.Global.WINDOW_ANIMATION_SCALE;
//		Settings.Global.TRANSITION_ANIMATION_SCALE;
//		Settings.Global.put
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showAnimationSettings() {
        // Settings.Global.getString
        // Call requires API level 17
        tvADS.setText(getString(R.string.ANIMATOR_DURATION_SCALE));
        tvADS.append(
                getString(Settings.Global.getString(
                        getContentResolver(),
                        Settings.Global.ANIMATOR_DURATION_SCALE
                ))
        );

        tvWAS.setText(getString(R.string.WINDOW_ANIMATION_SCALE));
        tvWAS.append(
                getString(Settings.Global.getString(
                        getContentResolver(),
                        Settings.Global.WINDOW_ANIMATION_SCALE
                ))
        );

        tvTAS.setText(getString(R.string.TRANSITION_ANIMATION_SCALE));
        tvTAS.append(
                getString(Settings.Global.getString(
                        getContentResolver(),
                        Settings.Global.TRANSITION_ANIMATION_SCALE
                ))
        );
    }

    private String getString(String str) {
        return str == null ? "null" : str;
    }

    public void setADS(View v) {
        String tag = v.getTag().toString();
        set(Settings.Global.ANIMATOR_DURATION_SCALE, Integer.parseInt(tag));
    }

    public void setAWS(View v) {
        String tag = v.getTag().toString();
        set(Settings.Global.WINDOW_ANIMATION_SCALE, Integer.parseInt(tag));
    }

    public void setTAS(View v) {
        String tag = v.getTag().toString();
        set(Settings.Global.TRANSITION_ANIMATION_SCALE, Integer.parseInt(tag));
    }

    private void set(String key, int value) {
        try {
            Settings.Global.putInt(getContentResolver(), key, value);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Permission Denial !", Toast.LENGTH_SHORT).show();
        }
        showAnimationSettings();


//		// testing 1 2 3
//		try {
//			Map<String, Command> map = new HashMap<>();
//			map.put("", new Command() {
//				@Override
//				public void command() {
//
//				}
//			});
//
//			Command cmd = map.get("a");
//			if (cmd != null) {
//				cmd.command();
//			}
//
//		} catch (NullPointerException e) {
//			e.printStackTrace();
//		}
    }

    private void CONSOLE_LOG(String string) {
        Log.i(TAG_NAME, string == null ? "null" : string);
    }

    private interface Command {
        void command();
    }
    // testing 1 2 3
}