package com.example.android.wearable.watchface.models;

import android.support.v7.graphics.Palette;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by tyler on 11/2/15.
 */
public class WatchFacePalette {

    @Getter
    @Setter
    private int background;

    @Getter
    @Setter
    private int text;

    public WatchFacePalette(Palette.Swatch sw) {
        background = sw.getRgb();
        text = sw.getTitleTextColor();
    }

}
