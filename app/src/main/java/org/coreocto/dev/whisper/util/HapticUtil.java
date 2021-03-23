package org.coreocto.dev.whisper.util;

import android.content.Context;
import android.os.Vibrator;

public class HapticUtil {
    public static void vibrate(Context ctx, long milliseconds){
        Vibrator v = (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(milliseconds);
    }
}
