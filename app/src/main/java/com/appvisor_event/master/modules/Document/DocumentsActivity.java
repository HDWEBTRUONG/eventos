package com.appvisor_event.master.modules.Document;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.R;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class DocumentsActivity extends AppActivity implements StickyGridHeadersGridView.OnItemClickListener
{
    private StickyGridHeadersGridView gridView  = null;
    private Document                  document  = null;
    private List<Document.Item>       documents = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.documents);

        reset();
        loadDocuments();
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
        document = new Document(getApplicationContext());

        documents = document.getDocuments();

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

    private void loadDocuments()
    {
        // apiからゲットする。
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        Document.Item document = documents.get(position);
        Log.d("tto", "clickItem: " + document.toString());
    }

    public void onClickButtonBack(View view)
    {
        finish();
    }
}
