package com.zj.analyticSdk.persistence.data;

import android.content.SharedPreferences;

import com.zj.analyticSdk.persistence.loader.PersistentName;

import java.util.concurrent.Future;

public class PersistentLoginId extends PersistentIdentity<String> {

    public PersistentLoginId(Future<SharedPreferences> loadStoredPreferences) {

        super(loadStoredPreferences, PersistentName.LOGIN_ID, new PersistentSerializer<String>() {
            @Override
            public String load(String value) {
                return value;
            }

            @Override
            public String save(String item) {
                return item;
            }

            @Override
            public String create() {
                return null;
            }
        });
    }
}
