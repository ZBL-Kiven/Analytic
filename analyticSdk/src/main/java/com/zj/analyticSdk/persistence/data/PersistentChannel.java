package com.zj.analyticSdk.persistence.data;


import android.content.Context;

import com.zj.analyticSdk.persistence.loader.BasePreferenceLoader;

public class PersistentChannel extends BasePreferenceLoader {

    public PersistentChannel(Context context, String name) {

        super(context, name, new PersistentSerializer() {
            @Override
            public String load(String value) {
                return value;
            }

            @Override
            public String save(Object item) {
                return item.toString();
            }

            @Override
            public String create() {
                return null;
            }
        });
    }
}
