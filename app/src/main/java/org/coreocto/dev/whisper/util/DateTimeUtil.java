package org.coreocto.dev.whisper.util;

import java.util.Date;

/**
 * Created by John on 3/20/2018.
 */

public class DateTimeUtil {
    public static long getCurrentTime() {
        return new Date().getTime();
    }
}
