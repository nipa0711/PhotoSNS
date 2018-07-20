package net.nipa0711.photosns;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.media.ExifInterface;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by Hyunmin on 2015-06-05.
 * Start refactoring 2018-07-06.
 */
public class globalVar extends Application {
    static final String url = "http://192.168.14.30:1234"; // HTTP 서버 url 예시
    static Bitmap thumbnailBitmap;
    static File file = Environment.getExternalStorageDirectory();
    static String path = file.getAbsolutePath();
    static String folder = "/photoSNS/";
    static String photoPath = path + folder;
    String originalPhoto;
    String id;
    public ListView mListView = null;
    public ListViewAdapter mAdapter = null;

    TextView tv;
    int listPosition;
    ImageView imageViewShow;
    int select = 0;
    static String email;
    static String password;
    static boolean isLoginSuccess = false;

    final Handler hosthandle = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            PhotoService ps = new PhotoService();

            // 호출 성공
            if (msg.what == 0) {
                if (msg.arg1 == 0) {
                    Toast.makeText(getApplicationContext(), "전송되었습니다.", Toast.LENGTH_SHORT).show();
                    //photo.delete();
                }
                // command가 2인 경우
                else if (msg.arg1 == 2) {
                    mAdapter = new ListViewAdapter(getApplicationContext());
                    mListView.setAdapter(mAdapter);

                    String countMsg[] = ((String) msg.obj).split("%|\\#");

                    if (countMsg[0].isEmpty()) {
                        Toast.makeText(getApplicationContext(), "등록된 것이 없습니다.", Toast.LENGTH_SHORT).show();
                        PhotoLook look = (PhotoLook) PhotoLook.photoLookActivity;
                        look.finish();
                    } else {
                        for (int i = 0; i < countMsg.length; i++) {
                            if (i == 0 || i % 6 == 0) {
                                Bitmap thumbnail = ps.stringToBitmap(countMsg[i + 3]);
                                Drawable smallPhoto = new BitmapDrawable(getResources(), thumbnail);
                                mAdapter.addItem(countMsg[i], smallPhoto, countMsg[i + 1], countMsg[i + 4], countMsg[i + 2], countMsg[i + 5]); // smallPhoto, quote,UploadDate,uploader,metadata
                            }
                        }
                    }
                } else if (msg.arg1 == 4) {
                    mAdapter.mListData.remove(listPosition);
                    mAdapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                    PhotoShow pl = (PhotoShow) PhotoShow.photoShowActivity;
                    pl.finish();
                } else if (msg.arg1 == 6) {
                    originalPhoto = (String) msg.obj;
                    Bitmap bitmap = ps.stringToBitmap(originalPhoto);
                    imageViewShow.setImageBitmap(bitmap);
                } else if (msg.arg1 == 9) {
                    String countMsg[] = ((String) msg.obj).split("%|\\#");
                    if (countMsg[0].equals("true")) {
                        isLoginSuccess = true;
                    }
                } else if (msg.arg1 == 10) {
                    String countMsg[] = ((String) msg.obj).split("%|\\#");
                    if (countMsg[0].equals("false")) {
                        final globalVar val = (globalVar) getApplicationContext();
                        ServerPostComm PostClient = new ServerPostComm(globalVar.url, 9, email + "%" + password + "#", val.hosthandle);
                        PostClient.start();
                    }
                }else if (msg.arg1 == 11) {
                    String countMsg[] = ((String) msg.obj).split("%|\\#");
                    if (countMsg[0].equals("true")) {
                        isLoginSuccess = true;
                    }
                }

                // 호출 실패
            } else {
                System.out.println("HTTP 통신 오류!");
                Toast.makeText(getApplicationContext(), "HTTP 통신 오류!", Toast.LENGTH_SHORT).show();

                if (msg.arg1 == 2) {
                    PhotoLook pl = (PhotoLook) PhotoLook.photoLookActivity;
                    pl.finish();
                } else if (msg.arg1 == 6) {

                    PhotoShow pl = (PhotoShow) PhotoShow.photoShowActivity;
                    pl.finish();
                }
            }
        }
    };
}
