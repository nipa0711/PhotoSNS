package net.nipa0711.photosns;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

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

    private static final int REQUEST_TAKE_PHOTO = 1;
    SharedPreferences setting;
    SharedPreferences.Editor editor;

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        imgName = "nipa_" + timeStamp;

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imgName,   //prefix
                ".jpg",          //suffix
                storageDir       //directory
        );

        // Save a file: path for use with ACTION_VIEW intents
        globalVar.photoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final globalVar val = (globalVar) getApplicationContext();

        takeShot = findViewById(R.id.takeShot);
        photoView = findViewById(R.id.photoView);
        writeCover = findViewById(R.id.writeCover);
        favorite = findViewById(R.id.favorite);
        send = findViewById(R.id.send);
        val.tv = findViewById(R.id.tv);
        imageView = findViewById(R.id.imageView);

        setting = getSharedPreferences("setting", 0); // 0은 읽기 쓰기 가능. setting.xml이 생성됨
        editor = setting.edit();

        uploader = val.email;
        editor.putString("uploader", uploader); // Preference

       /* if (setting.contains("uploader") == false) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("사용자 이름을 입력해주세요");
            final EditText input = new EditText(this);
            alert.setView(input);
            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    uploader = input.getText().toString();
                    editor.commit();
                }
            });
            alert.show();
        }         editor.putString("uploader", uploader); // Preference*/


        send.setEnabled(false);

        takeShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    // Create the File where the photo should go
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {

                    }
                    // Continue only if the File was successfully created
                    if (photoFile != null) {
                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA);

                        if(permissionCheck== PackageManager.PERMISSION_DENIED){
                            // 권한 없음
                            ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},0);
                        }else{
                            //권한 있음
                            mImageCaptureUri = FileProvider.getUriForFile(getApplication().getApplicationContext(), "net.nipa0711.photosns.fileprovider", photoFile);

                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
                            startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                            imageView.setTag(globalVar.photoPath + File.separator + imgName);
                        }

                    }
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
                val.select = 0;
                Intent intent = new Intent(MainActivity.this, PhotoLook.class);
                startActivity(intent);
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Toast.makeText(getApplicationContext(), "잠시만 기다려주십시오", Toast.LENGTH_SHORT).show();

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), mImageCaptureUri);
                    String stringPhoto = val.BitMapToString(bitmap);
                    ExifInterface exif = new ExifInterface(globalVar.photoPath); //imgName
                    String metadata = val.ShowExif(exif);

                    String values = setting.getString("uploader", "") + "%" + quote + "%" + metadata + "%" + stringPhoto;

                    // Post HTTP 호출을 담당하는 스레드 실행 (핸들러 객체 전달 필수!)
                    ServerPostComm PostClient = new ServerPostComm(globalVar.url, 0, values, val.hosthandle);
                    PostClient.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        favorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                val.select = 1;
                Intent intent = new Intent(MainActivity.this, PhotoLook.class);
                startActivity(intent);

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_TAKE_PHOTO:
                    try {
                        Bitmap photo;
                        photo = BitmapFactory.decodeFile(globalVar.photoPath);
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