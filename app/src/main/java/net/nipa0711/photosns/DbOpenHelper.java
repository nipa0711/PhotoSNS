package net.nipa0711.photosns;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Hyunmin on 2015-04-14.
 * Start refactoring 2018-07-06.
 */
public class DbOpenHelper {
    public static final String CREATE_TABLE =
            "create table favoritePhoto ("
                    + "id TEXT NOT NULL, "
                    + "uploader TEXT NOT NULL, "
                    + "quote TEXT NOT NULL, "
                    + "thumbnail TEXT NOT NULL, "
                    + "uploadDate TEXT NOT NULL, "
                    + "metadata TEXT NOT NULL, "
                    + "photo TEXT NOT NULL);";
    private static final String DATABASE_NAME = "favoritePhoto.db";
    private static final String TABLE_NAME = "favoritePhoto";
    private static final int DATABASE_VERSION = 1;
    public static SQLiteDatabase mDB;
    private DatabaseHelper mDBHelper;
    private Context mCtx;

    public DbOpenHelper(Context context) {
        this.mCtx = context;
    }

    public DbOpenHelper open() throws SQLException {
        mDBHelper = new DatabaseHelper(mCtx, DATABASE_NAME, null,
                DATABASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDB.close();
    }

    // Insert DB
    public long insert(String id, String uploader, String quote, String thumbnail, String uploadDate,
                       String metadata, String photo) {
        ContentValues values = new ContentValues();
        values.put("id", id);
        values.put("quote", quote);
        values.put("uploader", uploader);
        values.put("thumbnail", thumbnail);
        values.put("uploadDate", uploadDate);
        values.put("metadata", metadata);
        values.put("photo", photo);
        return mDB.insert(TABLE_NAME, null, values);
    }

    public Cursor getAlmost() {
        Cursor c = mDB.rawQuery("SELECT  id,  quote,  uploader,  thumbnail, uploadDate, metadata FROM'" + TABLE_NAME + "'ORDER BY id DESC", null);
        return c;
    }

    public Cursor getPhoto(String id) {
        Cursor c = mDB.rawQuery("SELECT photo FROM'" + TABLE_NAME + "' WHERE id='" + id + "'", null);
        return c;
    }

    public Cursor chkID(String id) {
        Cursor c = mDB.rawQuery("SELECT photo FROM'" + TABLE_NAME + "' WHERE id='" + id + "'", null);
        return c;
    }

    // Delete ID
    public boolean delete(String id) {
        return mDB.delete(TABLE_NAME, "id=" + id, null) > 0;
    }

    private class DatabaseHelper extends SQLiteOpenHelper {// 생성자

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        // 최초 DB를 만들때 한번만 호출
        @Override

        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }
// 버전이 업데이트 되었을 경우 DB를 다시 생성

        @Override

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }

    }
}
