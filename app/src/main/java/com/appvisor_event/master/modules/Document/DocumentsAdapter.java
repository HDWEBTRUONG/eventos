package com.appvisor_event.master.modules.Document;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersSimpleAdapter;

import java.util.Arrays;
import java.util.List;

import static com.appvisor_event.master.R.id.imageView;

/**
 * Created by bsfuji on 2017/03/30.
 */

public class DocumentsAdapter extends BaseAdapter implements StickyGridHeadersSimpleAdapter
{
    private int mHeaderResId;
    private LayoutInflater mInflater;
    private int mItemResId;
    private List<Document.Item> mItems;
    private Context context;
    private Document document;

    public DocumentsAdapter(Context context, List<Document.Item> items, int headerResId, int itemResId)
    {
        init(context, items, headerResId, itemResId);
    }

    public DocumentsAdapter(Context context, Document.Item[] items, int headerResId, int itemResId)
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
        Document.Item item = getItem(position);
        long headerId = Long.valueOf(item.getCategory().getId());

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

        Document.Item item = getItem(position);
        holder.textView.setText(item.getCategory().getName());

        Log.d("tto", "section: " + item.toString() + " position: " + position);

        return convertView;
    }

    @Override
    public Document.Item getItem(int position)
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
            holder.imageView = (ImageView)convertView.findViewById(imageView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (DocumentsAdapter.ViewHolder)convertView.getTag();
        }

        Document.Item item = getItem(position);

        Glide.with(context).load(item.getThumbnailImageUri()).skipMemoryCache(true).into(holder.imageView);

        return convertView;
    }

    private void init(Context context, List<Document.Item> items, int headerResId, int itemResId)
    {
        this.mItems = items;
        this.mHeaderResId = headerResId;
        this.mItemResId = itemResId;
        mInflater = LayoutInflater.from(context);
        this.context = context;
        this.document = new Document(context.getApplicationContext());
    }

    protected class HeaderViewHolder
    {
        public TextView textView;
    }

    protected class ViewHolder{
        public ImageView imageView;
    }
}
