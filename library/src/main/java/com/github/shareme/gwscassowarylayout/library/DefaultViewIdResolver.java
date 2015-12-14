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

import android.content.Context;

/**
 * DefaultViewIdResolver
 * Created by alex on 06/10/2014.
 */
public class DefaultViewIdResolver implements ViewIdResolver{
    private Context context;
    public DefaultViewIdResolver(Context context) {
        this.context = context;
    }

    @Override
    public int getViewIdByName(String viewName) {
        return context.getResources().getIdentifier(viewName, "id", context.getPackageName());
    }

    @Override
    public String getViewNameById(int id) {
        return context.getResources().getResourceEntryName(id);
    }
}
