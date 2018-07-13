package net.nipa0711.photosns;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * Created by Hyunmin on 2015-06-01.
 * Start refactoring 2018-07-06.
 */
public class PhotoLook extends Activity {
    public static Activity photoLookActivity;
    String _id, uploader, quote, thumbImg, uploadDate, metadata;
    String tempMeta[];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_look);

        photoLookActivity = PhotoLook.this;
        final globalVar val = (globalVar) getApplicationContext();

        val.mListView = findViewById(R.id.mList);
        tempMeta = new String[20];

        // Post HTTP 호출을 담당하는 스레드 실행 (핸들러 객체 전달 필수!)
        if (val.select == 0) {
            ServerPostComm postclient = new ServerPostComm(globalVar.url, 2, "", val.hosthandle);
            postclient.start();
        } else {
            final DbOpenHelper mDbOpenHelper = new DbOpenHelper(getApplicationContext());
            mDbOpenHelper.open();

            val.mAdapter = new ListViewAdapter(getApplicationContext());
            val.mListView.setAdapter(val.mAdapter);

            Cursor mCursor = mDbOpenHelper.getAlmost();

            if (mCursor.getCount() < 1) {
                finish();
            }

            for (int i = 0; i < mCursor.getCount(); i++) {
                if (mCursor.moveToNext()) {

                    _id = mCursor.getString(mCursor.getColumnIndex("id"));
                    uploader = mCursor.getString(mCursor.getColumnIndex("uploader"));
                    quote = mCursor.getString(mCursor.getColumnIndex("quote"));
                    thumbImg = mCursor.getString(mCursor.getColumnIndex("thumbnail"));
                    uploadDate = mCursor.getString(mCursor.getColumnIndex("uploadDate"));
                    metadata = mCursor.getString(mCursor.getColumnIndex("metadata"));

                    tempMeta = val.getMetadata(metadata);
                    System.arraycopy(tempMeta, 0, val.metadata, 0, tempMeta.length);

                    Bitmap thumbnail = val.stringToBitmap(thumbImg);
                    Drawable smallPhoto = new BitmapDrawable(getResources(), thumbnail);
                    val.mAdapter.addItem(_id, smallPhoto, quote, uploadDate, uploader, tempMeta[0]);
                    val.id = _id;
                }
            }
            mCursor.close();
        }

        val.mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListData mData = val.mAdapter.mListData.get(position);

                Intent intent = new Intent(PhotoLook.this, PhotoShow.class);

                if (val.select == 0) {
                    globalVar.thumbnailBitmap = globalVar.drawableToBitmap(mData.mIcon);
                    tempMeta = val.getMetadata(mData.metadata);
                    val.id = mData.id;
                    val.listPosition = position;

                    intent.putExtra("Quote", mData.mQuote);
                    intent.putExtra("Date", tempMeta[0]);
                    intent.putExtra("Uploader", mData.mUploader);
                    startActivity(intent);
                } else {
                    val.listPosition = position;
                    intent.putExtra("Quote", quote);
                    intent.putExtra("Date", tempMeta[0]);
                    intent.putExtra("Uploader", uploader);
                    startActivity(intent);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        final globalVar val = (globalVar) getApplicationContext();
        if (val.select != 0) {
            final DbOpenHelper mDbOpenHelper = new DbOpenHelper(getApplicationContext());
            mDbOpenHelper.open();

            Cursor mCursor = mDbOpenHelper.getAlmost();

            if (mCursor.getCount() < 1) {
                finish();
            }
        }
    }
}
