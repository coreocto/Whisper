package org.coreocto.dev.whisper.bean;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.coreocto.dev.whisper.R;

public class Settings {
    private Settings(Context ctx) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
        this.ctx = ctx;
    }

    //    private static Settings instance = null;
    private SharedPreferences sharedPreferences = null;
    private Context ctx = null;

    public static final String KEY_UI_FONT_SIZE = "ui.fontsize";
    public static final String KEY_STT_SWITCH = "stt.switch";
    public static final String KEY_STT_LANG = "stt.lang";
    public static final String KEY_TTS_SWITCH = "tts.switch";
    public static final String KEY_TTS_LANG = "tts.lang";
    public static final String KEY_FB_ONTOUCH = "fb.type";
    public static final String KEY_FB_VIBRATE_ON_CLICK = "fb.vibrate_on_click";

    public static Settings getInstance(Context ctx) {
//        if (instance == null) {
//            instance = new Settings(ctx);
//        }
//        return instance;
        return new Settings(ctx);
    }

    public String getUiFontSize() {
        return sharedPreferences.getString(KEY_UI_FONT_SIZE, ctx.getString(R.string.opts_values_font_size_default));
    }

    public boolean isSttEnabled() {
        return sharedPreferences.getBoolean(KEY_STT_SWITCH, false);
    }

    public String getSttLang() {
        return sharedPreferences.getString(KEY_STT_LANG, ctx.getString(R.string.opts_values_stt_lang_default));
    }

    public boolean isTtsEnabled() {
        return sharedPreferences.getBoolean(KEY_TTS_SWITCH, false);
    }

    public String getTtsLang() {
        return sharedPreferences.getString(KEY_TTS_LANG, ctx.getString(R.string.opts_values_tts_lang_default));
    }

    public String getFbOnTouch() {
        return sharedPreferences.getString(KEY_FB_ONTOUCH, ctx.getString(R.string.opts_values_fb_type_default));
    }

    public boolean isVibrateOnClickEnabled(){
        return sharedPreferences.getBoolean(KEY_FB_VIBRATE_ON_CLICK, false);
    }
}
