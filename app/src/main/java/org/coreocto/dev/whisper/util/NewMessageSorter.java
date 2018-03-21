package org.coreocto.dev.whisper.util;

import org.coreocto.dev.whisper.bean.NewMessage;

import java.util.Comparator;

/**
 * Created by John on 3/20/2018.
 */

public class NewMessageSorter implements Comparator<NewMessage> {

    @Override
    public int compare(NewMessage o1, NewMessage o2) {
        if (o1 != null && o2 != null) {
            if (o1.getCreateDt() > o2.getCreateDt()) {
                return 1;
            } else if (o1.getCreateDt() < o2.getCreateDt()) {
                return -1;
            }
        }
        return 0;
    }
}
