package com.appvisor_event.master.modules.Document;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.R;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class DocumentsActivity extends AppActivity implements StickyGridHeadersGridView.OnItemClickListener
{
    private StickyGridHeadersGridView gridView  = null;
    private List<Document.Item>       documents = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.documents);

        reset();
    }

    private void reset()
    {
        resetDocuments();
        resetView();
    }

    private void resetView()
    {
        gridView = (StickyGridHeadersGridView)findViewById(R.id.gridView);
        gridView.setAdapter(new DocumentsAdapter(this, documents, R.layout.documents_grid_header, R.layout.documents_grid_item));
        gridView.setOnItemClickListener(this);
    }

    private void resetDocuments()
    {
        documents = new ArrayList<Document.Item>();

        for (int i = 1; i < 10; i++)
        {
            try {
                JSONObject itemObject = new JSONObject();

                itemObject.put("id",                            String.format("%d", i));
                itemObject.put("name",                          String.format("name%d", i));
                itemObject.put("event_document_category_id",    String.format("%d", (i % 4)));
                itemObject.put("event_document_category_name",  String.format("category_%d", (i % 4)));
                itemObject.put("period_start_date",             String.format("2017-04-%02d 10:00:00", i));
                itemObject.put("period_end_date",               String.format("2017-04-%02d 17:00:00", i));
                itemObject.put("sequence",                      String.format("%d", i));
                itemObject.put("thumbnail_image_path",          String.format("/%d", i));
                itemObject.put("path",                          String.format("/%d", i));

                Document.Item item = Document.newItem(itemObject);

                documents.add(item);
            } catch (JSONException exception) {
                Log.e("tto", "exception: " + exception.getMessage());
            }
        }

        Collections.sort(documents, new Comparator<Document.Item>() {
            @Override
            public int compare(Document.Item itemA, Document.Item itemB)
            {
                int compare = itemA.getCategory().getId().compareTo(itemB.getCategory().getId());
                if (0 == compare)
                {
                    compare = itemA.getSequence().compareTo(itemB.getSequence());
                }
                return compare;
            }
        });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {

    }

    public void onClickButtonBack(View view)
    {
        finish();
    }
}
