package com.anxpp.one.components.beamazingtoday.ui.animator;
import android.support.annotation.IntDef;

/**
 * Created by anxpp.com on 22.08.16.
 */

@IntDef({AnimationType.ADD, AnimationType.REMOVE, AnimationType.MOVE})
public @interface AnimationType {

    int ADD = 0;

    int REMOVE = 1;

    int MOVE = 2;

}
