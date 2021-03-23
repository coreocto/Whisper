package org.coreocto.dev.whisper.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;
import android.speech.SpeechRecognizer;
import android.util.Log;

import org.coreocto.dev.whisper.R;
import org.coreocto.dev.whisper.bean.Settings;
import org.coreocto.dev.whisper.util.HapticUtil;
import org.coreocto.dev.whisper.util.UiUtil;

public class SettingsActivity extends AppCompatPreferenceActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

    private static final String TAG = "SettingsActivity";
    private SwitchPreference mSttSwitch;
    private ListPreference mUiFontSize;
    private SwitchPreference mTtsSwitch;
    private ListPreference mSttLang;
    private ListPreference mTtsLang;
    private ListPreference mFbOnTouch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preference);

        mUiFontSize = (ListPreference) findPreference(Settings.KEY_UI_FONT_SIZE);
        mUiFontSize.setOnPreferenceChangeListener(this);
        mUiFontSize.setOnPreferenceClickListener(this);
        mSttSwitch = (SwitchPreference) findPreference(Settings.KEY_STT_SWITCH);
        mSttSwitch.setOnPreferenceChangeListener(this);
        mSttSwitch.setOnPreferenceClickListener(this);
        mSttLang = (ListPreference) findPreference(Settings.KEY_STT_LANG);
        mSttLang.setOnPreferenceChangeListener(this);
        mSttLang.setOnPreferenceClickListener(this);
        mTtsSwitch = (SwitchPreference) findPreference(Settings.KEY_TTS_SWITCH);
        mTtsSwitch.setOnPreferenceChangeListener(this);
        mTtsSwitch.setOnPreferenceClickListener(this);
        mTtsLang = (ListPreference) findPreference(Settings.KEY_TTS_LANG);
        mTtsLang.setOnPreferenceChangeListener(this);
        mTtsLang.setOnPreferenceClickListener(this);
//        mFbOnTouch = (ListPreference) findPreference(Settings.KEY_FB_ONTOUCH);
//        mFbOnTouch.setOnPreferenceChangeListener(this);
//        mFbOnTouch.setOnPreferenceClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSttSwitch.isChecked()) {
            mSttLang.setEnabled(true);
        } else {
            mSttLang.setEnabled(false);
        }
        if (mTtsSwitch.isChecked()) {
            mTtsLang.setEnabled(true);
        } else {
            mTtsLang.setEnabled(false);
        }
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference: " + key + " changed.");
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object o) {
        String key = preference.getKey();
        if (key.equals(Settings.KEY_UI_FONT_SIZE)) {
            return true;
        } else if (key.equals(Settings.KEY_STT_SWITCH)) {
            if (o instanceof Boolean) {
                Boolean b = (Boolean) o;
                if (b) {
                    if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                        UiUtil.showModalError(this, getString(R.string.speech_not_supported));
                        return false;
                    }
                    mSttLang.setEnabled(b);
                }
                return true;
            }
        } else if (key.equals(Settings.KEY_STT_LANG)) {
            return true;
        } else if (key.equals(Settings.KEY_TTS_SWITCH)) {
            if (o instanceof Boolean) {
                Boolean b = (Boolean) o;
                mTtsLang.setEnabled(b);
                return true;
            }
        } else if (key.equals(Settings.KEY_TTS_LANG)) {
            return true;
        }
        return false;
    }

    private void doVibrate() {
        if (Settings.getInstance(this).isVibrateOnClickEnabled()) {
            HapticUtil.vibrate(this, 100);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        doVibrate();
        return false;
    }
}
