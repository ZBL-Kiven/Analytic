package com.zj.analyticSdk.persistence.data;

import android.content.SharedPreferences;

import com.zj.analyticSdk.persistence.loader.PersistentName;

import java.util.UUID;
import java.util.concurrent.Future;

public class PersistentDeviceId extends PersistentIdentity<String> {

    public PersistentDeviceId(Future<SharedPreferences> loadStoredPreferences) {

        super(loadStoredPreferences, PersistentName.DEVICE_ID, new PersistentSerializer<String>() {
            @Override
            public String load(String value) {
                return value;
            }

            @Override
            public String save(String item) {
                return item == null ? create() : item;
            }

            @Override
            public String create() {
                return UUID.randomUUID().toString();
            }
        });
    }
}
