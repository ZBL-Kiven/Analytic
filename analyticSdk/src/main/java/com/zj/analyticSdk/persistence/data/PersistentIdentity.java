package com.zj.analyticSdk.persistence.data;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.zj.analyticSdk.CALogs;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@SuppressLint("CommitPrefEdits")
public abstract class PersistentIdentity<T> {

    private static final String TAG = "CCA.PersistentIdentity";
    private final Future<SharedPreferences> loadStoredPreferences;
    private final PersistentSerializer<T> serializer;
    private final String persistentKey;
    private T item;

    PersistentIdentity(final Future<SharedPreferences> loadStoredPreferences, final String persistentKey, final PersistentSerializer<T> serializer) {
        this.loadStoredPreferences = loadStoredPreferences;
        this.serializer = serializer;
        this.persistentKey = persistentKey;
    }

    /**
     * get data
     */
    public T get() {
        if (this.item == null) {
            String data = null;
            synchronized (loadStoredPreferences) {
                try {
                    SharedPreferences sharedPreferences = loadStoredPreferences.get();
                    if (sharedPreferences != null) {
                        data = sharedPreferences.getString(persistentKey, null);
                    }
                } catch (final ExecutionException | InterruptedException e) {
                    CALogs.i(TAG, "Cannot read distinct ids from sharedPreferences.", e);
                }
                if (data == null) {
                    item = serializer.create();
                    commit(item);
                } else {
                    item = serializer.load(data);
                }
            }
        }
        return this.item;
    }

    /**
     * save data
     */
    public void commit(T item) {
        this.item = item;

        synchronized (loadStoredPreferences) {
            SharedPreferences sharedPreferences = null;
            try {
                sharedPreferences = loadStoredPreferences.get();
            } catch (final ExecutionException | InterruptedException e) {
                CALogs.i(TAG, "Cannot read distinct ids from sharedPreferences.", e);
            }

            if (sharedPreferences == null) {
                return;
            }

            final SharedPreferences.Editor editor = sharedPreferences.edit();
            if (this.item == null) {
                this.item = serializer.create();
            }
            editor.putString(persistentKey, serializer.save(this.item));
            editor.apply();
        }
    }

    /**
     * Persistent serializers data
     */
    interface PersistentSerializer<T> {

        T load(final String value);

        String save(T item);

        T create();
    }
}