/*
 * Copyright (C) 2014 Agens AS
 * Modifications Copyright(C) 2015 Fred Grott(GrottWorkShop)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.shareme.gwscassowarylayout.library;

import android.view.View;

/**
 * MeasureSpecUtils
 * Created by alex on 14/11/14.
 */
public class MeasureSpecUtils {
    public static String getModeAsString(int modeOrSpec) {
        String modeAsString;
        int mode = View.MeasureSpec.getMode(modeOrSpec);
        if (mode == View.MeasureSpec.EXACTLY) {
            modeAsString = "EXACTLY";
        } else if (mode == View.MeasureSpec.AT_MOST) {
            modeAsString = "AT_MOST";
        } else if (mode == View.MeasureSpec.UNSPECIFIED) {
            modeAsString = "UNSPECIFIED";
        } else {
            modeAsString = "unknown mode " + modeOrSpec;
        }

        return modeAsString;
    }
}
