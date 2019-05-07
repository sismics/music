package com.sismics.music.db;

import android.content.Context;

import com.esotericsoftware.kryo.serializers.CompatibleFieldSerializer;
import com.snappydb.DB;
import com.snappydb.SnappyDB;
import com.snappydb.SnappydbException;

/**
 * Snappy DB.
 *
 * @author bgamard
 * @author jtremeaux
 */
public class Db {
    private static volatile DB singleton = null;

    public static DB db(Context context) throws SnappydbException {
        if (singleton == null || !singleton.isOpen()) {
            synchronized (SnappyDB.class) {
                if (singleton == null || !singleton.isOpen()) {
                    singleton = new SnappyDB.Builder(context).build();
                    singleton.getKryoInstance().setDefaultSerializer(CompatibleFieldSerializer.class);
                }
            }
        }
        return singleton;
    }
}
