package com.widget.socialpalette;


import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class SocialListAdapter extends BaseAdapter {

    private LayoutInflater mInflator;

    private static String TAG = "SocialListAdapter"; 
    private ArrayList<String> mListNames = new ArrayList<String>();
    private Context mContext;


    private static class ViewHolder {
        private static TextView sListItemText;
        private ImageView sImage;
    }


    public SocialListAdapter(Context context, ArrayList<String> listItems) {
        this.mContext = context;
        this.mListNames.clear();
        this.mListNames = listItems;
    }

    public int getCount() {
        if (mListNames != null && !mListNames.isEmpty()) {
            return mListNames.size();
        } else {
            return 0;
        }
    }


    public Object getItem(int arg0) {
        return arg0;
    }

    public long getItemId(int arg0) {
        return arg0;
    }


    public View getView(int pos, View convertView, ViewGroup arg2) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();

            // set the item
            mInflator = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = mInflator.inflate(R.layout.listitem, null);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.sListItemText = (TextView) convertView.findViewById(R.id.type);
        holder.sImage = (ImageView)convertView.findViewById(R.id.myImage);
        Log.v(TAG,"item is"+mListNames.get(pos).toString());
        holder.sListItemText.setText(mListNames.get(pos).toString());
        if(0 == pos) {
        	holder.sImage.setImageResource(R.drawable.facebook);
        } else {
        	holder.sImage.setImageResource(R.drawable.twitter);
        }
        
        return convertView;
    }

}
