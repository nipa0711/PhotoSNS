package net.nipa0711.photosns;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Hyunmin on 2015-06-07.
 */


public class ListViewAdapter extends BaseAdapter {
    private Context mContext = null;
    public ArrayList<ListData> mListData = new ArrayList<ListData>();

    public ListViewAdapter(Context mContext) {
        super();
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Override
    public Object getItem(int position) {
        return mListData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void addItem(String id, Drawable icon, String mQuote, String mDate, String mUploader, String metadata) {
        ListData addInfo = null;
        addInfo = new ListData();
        addInfo.id = id;
        addInfo.mIcon = icon;
        addInfo.mQuote = mQuote;
        addInfo.mDate = mDate;
        addInfo.mUploader = mUploader;
        addInfo.metadata = metadata;

        mListData.add(addInfo);
    }

    private class ViewHolder {
        public ImageView mIcon;
        public TextView mQuote;
        public TextView mDate;
        public TextView mUploader;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();

            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview_layout, null);

            holder.mIcon = (ImageView) convertView.findViewById(R.id.mImage);
            holder.mQuote = (TextView) convertView.findViewById(R.id.mQuote);
            holder.mDate = (TextView) convertView.findViewById(R.id.mDate);
            holder.mUploader = (TextView) convertView.findViewById(R.id.mUploader);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        ListData mData = mListData.get(position);
        if (mData.mIcon != null) {
            holder.mIcon.setVisibility(View.VISIBLE);
            holder.mIcon.setImageDrawable(mData.mIcon);
        } else {
            holder.mIcon.setVisibility(View.GONE);
        }

        holder.mQuote.setText(mData.mQuote);
        holder.mDate.setText(mData.mDate);
        holder.mUploader.setText(mData.mUploader);

        return convertView;
    }
}