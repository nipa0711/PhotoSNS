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
    double Latitude = 0, Longitude = 0;
    String metadata[];
    TextView tv;
    int listPosition;
    ImageView imageViewShow;
    int select = 0;
    static String email;
    static String password;
    static boolean isLoginSuccess = false;


    public Bitmap stringToBitmap(String in) {
        byte[] bytes = Base64.decode(in, Base64.DEFAULT);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 16;
        options.inDither = true;

        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length); //return
        return temp;
    }

    public String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();

        String temp = Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public String ShowExif(ExifInterface exif) {

        String myAttribute;

        myAttribute = getTagString(ExifInterface.TAG_DATETIME, exif);
        myAttribute += getTagString(ExifInterface.TAG_FLASH, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LATITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE, exif);
        myAttribute += getTagString(ExifInterface.TAG_GPS_LONGITUDE_REF, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_LENGTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_IMAGE_WIDTH, exif);
        myAttribute += getTagString(ExifInterface.TAG_MAKE, exif);
        myAttribute += getTagString(ExifInterface.TAG_MODEL, exif);
        myAttribute += getTagString(ExifInterface.TAG_ORIENTATION, exif);
        myAttribute += getTagString(ExifInterface.TAG_WHITE_BALANCE, exif);
        //Log.d("TAG", "===================myAttribute : " + myAttribute);
        return myAttribute;
    }

    private String getTagString(String tag, ExifInterface exif) {
        return (exif.getAttribute(tag) + "|");
    }

    public String[] getMetadata(String METADATA) {
        metadata = new String[20];
        metadata = METADATA.split("\\|");
        return metadata;
    }

    public String findAddress(double lat, double lng) {
        StringBuffer bf = new StringBuffer();
        Geocoder geocoder = new Geocoder(this, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                // 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
                address = geocoder.getFromLocation(lat, lng, 1);
                // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                if (address != null && address.size() > 0) {
                    // 주소
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();

                    // 전송할 주소 데이터 (위도/경도 포함 편집)
                    bf.append(currentLocationAddress);
                }
            }

        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "주소취득 실패"
                    , Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return bf.toString();
    }

    public Float convertToDegree(String stringDMS) {
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    final Handler hosthandle = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

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
                                Bitmap thumbnail = stringToBitmap(countMsg[i + 3]);
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
                    Bitmap bitmap = stringToBitmap(originalPhoto);
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
