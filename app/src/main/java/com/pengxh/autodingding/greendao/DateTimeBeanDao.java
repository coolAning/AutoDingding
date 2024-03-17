package com.pengxh.autodingding.greendao;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.pengxh.autodingding.bean.DateTimeBean;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;
import org.greenrobot.greendao.internal.DaoConfig;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.

/**
 * DAO for table "DATE_TIME_BEAN".
 */
public class DateTimeBeanDao extends AbstractDao<DateTimeBean, Long> {

    public static final String TABLENAME = "DATE_TIME_BEAN";

    /**
     * Properties of entity DateTimeBean.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property Uuid = new Property(1, String.class, "uuid", false, "UUID");
        public final static Property Date = new Property(2, String.class, "date", false, "DATE");
        public final static Property Time = new Property(3, String.class, "time", false, "TIME");
        public final static Property WeekDay = new Property(4, String.class, "weekDay", false, "WEEK_DAY");
    }


    public DateTimeBeanDao(DaoConfig config) {
        super(config);
    }

    public DateTimeBeanDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /**
     * Creates the underlying database table.
     */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists ? "IF NOT EXISTS " : "";
        db.execSQL("CREATE TABLE " + constraint + "\"DATE_TIME_BEAN\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT ," + // 0: id
                "\"UUID\" TEXT," + // 1: uuid
                "\"DATE\" TEXT," + // 2: date
                "\"TIME\" TEXT," + // 3: time
                "\"WEEK_DAY\" TEXT);"); // 4: weekDay
    }

    /**
     * Drops the underlying database table.
     */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"DATE_TIME_BEAN\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, DateTimeBean entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String uuid = entity.getUuid();
        if (uuid != null) {
            stmt.bindString(2, uuid);
        }

        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(3, date);
        }

        String time = entity.getTime();
        if (time != null) {
            stmt.bindString(4, time);
        }

        String weekDay = entity.getWeekDay();
        if (weekDay != null) {
            stmt.bindString(5, weekDay);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, DateTimeBean entity) {
        stmt.clearBindings();

        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }

        String uuid = entity.getUuid();
        if (uuid != null) {
            stmt.bindString(2, uuid);
        }

        String date = entity.getDate();
        if (date != null) {
            stmt.bindString(3, date);
        }

        String time = entity.getTime();
        if (time != null) {
            stmt.bindString(4, time);
        }

        String weekDay = entity.getWeekDay();
        if (weekDay != null) {
            stmt.bindString(5, weekDay);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }

    @Override
    public DateTimeBean readEntity(Cursor cursor, int offset) {
        DateTimeBean entity = new DateTimeBean( //
                cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
                cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // uuid
                cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // date
                cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // time
                cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4) // weekDay
        );
        return entity;
    }

    @Override
    public void readEntity(Cursor cursor, DateTimeBean entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setUuid(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setDate(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setTime(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setWeekDay(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
    }

    @Override
    protected final Long updateKeyAfterInsert(DateTimeBean entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }

    @Override
    public Long getKey(DateTimeBean entity) {
        if (entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(DateTimeBean entity) {
        return entity.getId() != null;
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }

}
