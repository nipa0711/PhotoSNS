package net.nipa0711.photosns;

/**
 * Created by Hyunmin on 2015-06-01.
 */

import android.graphics.drawable.Drawable;

import java.text.Collator;
import java.util.Comparator;

public class ListData {
    /**
     * 리스트 정보를 담고 있을 객체 생성
     */
    // 아이콘
    public Drawable mIcon;

    // 문구
    public String mQuote;

    // 날짜
    public String mDate;

    // 업로더
    public String mUploader;

    // metadata
    public String metadata;

    //id
    public String id;

    /**
     * 알파벳 이름으로 정렬
     */
    public static final Comparator<ListData> ALPHA_COMPARATOR = new Comparator<ListData>() {
        private final Collator sCollator = Collator.getInstance();

        @Override
        public int compare(ListData mListDate_1, ListData mListDate_2) {
            return sCollator.compare(mListDate_1.mQuote, mListDate_2.mQuote);
        }
    };
}