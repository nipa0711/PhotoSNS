package net.nipa0711.photosns;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Hyunmin on 2015-06-01.
 * Start refactoring 2018-07-06.
 */

public class MainActivity extends Activity {

    Button takeShot, photoView, writeCover, send, favorite;
    ImageView imageView;
    String imgName, quote, uploader;
    Uri mImageCaptureUri;

    private static final int CAMERA_IMAGE_REQUEST = 1;
    SharedPreferences setting;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final globalVar val = (globalVar) getApplicationContext();

        takeShot = (Button) findViewById(R.id.takeShot);
        photoView = (Button) findViewById(R.id.photoView);
        writeCover = (Button) findViewById(R.id.writeCover);
        favorite = (Button) findViewById(R.id.favorite);
        send = (Button) findViewById(R.id.send);
        val.tv = (TextView) findViewById(R.id.tv);
        imageView = (ImageView) findViewById(R.id.imageView);

        setting = getSharedPreferences("setting", 0); // 0은 읽기 쓰기 가능. setting.xml이 생성됨
        editor = setting.edit();

        if (setting.contains("uploader") == false) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("사용자 이름을 입력해주세요");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    uploader = input.getText().toString();
                    editor.putString("uploader", uploader); // Preference
                    editor.commit();
                }
            });
            alert.show();
        }

        send.setEnabled(false);


        takeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file_path;
                try {
                    file_path = new File(val.photoPath);
                    if (!file_path.isDirectory()) {
                        file_path.mkdirs();
                    }

                    long time = System.currentTimeMillis();
                    SimpleDateFormat takenTime = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String photoTakenTime = takenTime.format(new Date(time));
                    imgName = "nipa_" + photoTakenTime + ".jpg";

                    val.photo = new File(val.photoPath, imgName);
                    mImageCaptureUri = Uri.fromFile(val.photo);
                    imageView.setTag(val.photoPath + File.separator + imgName);
                    Intent takenPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    takenPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                    startActivityForResult(takenPhotoIntent, CAMERA_IMAGE_REQUEST);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        writeCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder alert = new AlertDialog.Builder(v.getContext());
                alert.setTitle("문구를 입력해주세요");
                final EditText input = new EditText(v.getContext());
                alert.setView(input);
                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        quote = input.getText().toString();
                        val.tv.setText(quote);
                    }
                });
                alert.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Canceled.
                            }
                        });
                alert.show();
            }
        });

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                val.select=0;
                Intent intent = new Intent(MainActivity.this, PhotoLook.class);
                startActivity(intent);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "잠시만 기다려주십시오", Toast.LENGTH_SHORT).show();
                    DisplayMetrics outMetrics = new DisplayMetrics();
                    getWindowManager().getDefaultDisplay().getMetrics(outMetrics);

                    int currentMyPhonePX = outMetrics.densityDpi; // 1dp에 해당하는 픽셀

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                    String stringPhoto = val.BitMapToString(bitmap);
                    ExifInterface exif = new ExifInterface(val.photoPath + imgName);
                    String metadata = val.ShowExif(exif);

                    int viewDefault = currentMyPhonePX * 100; // 100dp
                    float width = bitmap.getWidth();
                    float height = bitmap.getHeight();

                    if (height > viewDefault) // 높이가 기준점보다 크다면
                    {
                        float percente = (float) (height / 100);
                        float scale = (float) (viewDefault / percente);
                        width *= (scale / 100);
                        height *= (scale / 100);
                    }

                    Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, (int) width, (int) height, true);
                    String smallPhoto = val.BitMapToString(thumbnail);

                    String values = setting.getString("uploader", "") + "%" + quote + "%" + smallPhoto + "%" + metadata + "%" + stringPhoto;


                    // Post HTTP 호출을 담당하는 스레드 실행 (핸들러 객체 전달 필수!)
                    ServerPostComm postclient = new ServerPostComm(val.url, 0, values, val.hosthandle);
                    postclient.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                val.select=1;
                Intent intent = new Intent(MainActivity.this, PhotoLook.class);
                startActivity(intent);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final globalVar val = (globalVar) getApplicationContext();

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case CAMERA_IMAGE_REQUEST:
                    try {
                        Bitmap photo;
                        photo = BitmapFactory.decodeFile(val.photoPath + imgName);
                        imageView.setImageBitmap(photo);
                        send.setEnabled(true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    Toast.makeText(this, "ERROR!", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }
}
