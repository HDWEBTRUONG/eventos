package com.appvisor_event.master.modules.Document;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by bsfuji on 2017/03/30.
 */

public class DocumentsAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter
{
    private int mHeaderResId;
    private LayoutInflater mInflater;
    private int mItemResId;
    private List<Map<String, String>> mItems;

    public DocumentsAdapter(Context context, List<Map<String, String>> items, int headerResId, int itemResId)
    {
        init(context, items, headerResId, itemResId);
    }

    public DocumentsAdapter(Context context, Map<String, String>[] items, int headerResId, int itemResId)
    {
        init(context, Arrays.asList(items), headerResId, itemResId);
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return false;
    }

    @Override
    public int getCount()
    {
        return mItems.size();
    }

    @Override
    public long getHeaderId(int position)
    {
        Map<String, String> item = getItem(position);
        long headerId = Long.valueOf(item.get("section_id"));

        Log.d("tto", "headerId: " + headerId + " item: " + item.toString() + " position: " + position);
        return headerId;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getHeaderView(int position, View convertView, ViewGroup parent)
    {
        DocumentsAdapter.HeaderViewHolder holder;

        if (convertView == null)
        {
            convertView = mInflater.inflate(mHeaderResId, parent, false);
            holder = new DocumentsAdapter.HeaderViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else
        {
            holder = (DocumentsAdapter.HeaderViewHolder)convertView.getTag();
        }

        Map<String, String> item = getItem(position);
        holder.textView.setText(item.get("section"));

        Log.d("tto", "section: " + item.toString() + " position: " + position);

        return convertView;
    }

    @Override
    public Map<String, String> getItem(int position)
    {
        return mItems.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    @SuppressWarnings("unchecked")
    public View getView(int position, View convertView, ViewGroup parent)
    {
        DocumentsAdapter.ViewHolder holder;

        if (convertView == null)
        {
            convertView = mInflater.inflate(mItemResId, parent, false);
            holder = new DocumentsAdapter.ViewHolder();
            holder.textView = (TextView)convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        }
        else
        {
            holder = (DocumentsAdapter.ViewHolder)convertView.getTag();
        }

        Map<String, String> item = getItem(position);
        holder.textView.setText(item.get("name"));

        Log.d("tto", "item: " + item.toString() + " position: " + position);

        return convertView;
    }

    private void init(Context context, List<Map<String, String>> items, int headerResId, int itemResId)
    {
        this.mItems = items;
        this.mHeaderResId = headerResId;
        this.mItemResId = itemResId;
        mInflater = LayoutInflater.from(context);
    }

    protected class HeaderViewHolder
    {
        public TextView textView;
    }

    protected class ViewHolder{
        public TextView textView;
    }
}
