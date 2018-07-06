package net.nipa0711.photosns;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Hyunmin on 2015-06-01.
 * Start refactoring 2018-07-06.
 */
public class PhotoShow extends Activity {
    public static Activity photoShowActivity;

    TextView uploader, date, quote, address;

    Button googleMap, deleteItem, addFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_show);

        photoShowActivity = PhotoShow.this;

        final globalVar val = (globalVar) getApplicationContext();

        uploader = (TextView) findViewById(R.id.uploader);
        date = (TextView) findViewById(R.id.date);
        quote = (TextView) findViewById(R.id.quote);
        address = (TextView) findViewById(R.id.address);
        val.imageViewShow = (ImageView) findViewById(R.id.imageViewShow);
        googleMap = (Button) findViewById(R.id.googleMap);
        deleteItem = (Button) findViewById(R.id.deleteItem);
        addFavorite = (Button) findViewById(R.id.addFavorite);

        Intent intent = getIntent();
        uploader.setText(intent.getStringExtra("Uploader"));
        quote.setText(intent.getStringExtra("Quote"));
        date.setText(intent.getStringExtra("Date"));

        if (val.select == 0) {
            ServerPostComm postclient = new ServerPostComm(val.url, 6, val.id, val.hosthandle);
            postclient.start();
        } else {
            deleteItem.setEnabled(false);
            addFavorite.setText("즐겨찾기 삭제");
            final DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);
            mDbOpenHelper.open();
            Cursor mCursor = mDbOpenHelper.getPhoto(val.id);
            mCursor.moveToLast();
            String photo = mCursor.getString(mCursor.getColumnIndex("photo"));
            Bitmap bitmap = val.stringToBitmap(photo);
            val.imageViewShow.setImageBitmap(bitmap);
        }

        if (!val.metadata[2].equals("null") && !val.metadata[4].equals("null")) {
            if (val.metadata[3].equals("N")) {
                val.Latitude = val.convertToDegree(val.metadata[2]);
            } else {
                val.Latitude = 0 - val.convertToDegree(val.metadata[2]);
            }

            if (val.metadata[5].equals("E")) {
                val.Longitude = val.convertToDegree(val.metadata[4]);
            } else {
                val.Longitude = 0 - val.convertToDegree(val.metadata[4]);
            }

            address.setText(val.findAddress(val.Latitude, val.Longitude));
        } else {
            address.setText("GPS가 기록되어 있지 않습니다.");
        }

        googleMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PhotoShow.this, ShowGoogleMap.class);
                startActivity(intent);
            }
        });

        deleteItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerPostComm postclient = new ServerPostComm(val.url, 4, val.id, val.hosthandle);
                postclient.start();
            }
        });

        final DbOpenHelper mDbOpenHelper = new DbOpenHelper(this);


        addFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (val.select == 0) {
                    ListData mData = val.mAdapter.mListData.get(val.listPosition);
                    String thumbnail = val.BitMapToString(val.thumbnailBitmap);
                    mDbOpenHelper.open();
                    Cursor mCursor = mDbOpenHelper.chkID(val.id);
                    if (mCursor.getCount() > 0) {
                        Toast.makeText(getApplicationContext(), "이미 추가된 사진입니다", Toast.LENGTH_SHORT).show();
                    } else {
                        mDbOpenHelper.insert(mData.id, mData.mUploader, mData.mQuote, thumbnail, mData.mDate, mData.metadata, val.originalPhoto);
                        Toast.makeText(getApplicationContext(), "즐겨찾기에 추가되었습니다", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    final DbOpenHelper mDbOpenHelper = new DbOpenHelper(getApplicationContext());
                    mDbOpenHelper.open();
                    mDbOpenHelper.delete(val.id);

                    val.mAdapter.mListData.remove(val.listPosition);
                    val.mAdapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }
}
