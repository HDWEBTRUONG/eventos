package com.appvisor_event.master.modules.Document;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.R;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class DocumentsActivity extends AppActivity implements StickyGridHeadersGridView.OnItemClickListener
{
    private StickyGridHeadersGridView gridView = null;
    private List<Map<String, String>> data     = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.documents);

        reset();
    }

    private void reset()
    {
        resetData();
        resetView();
    }

    private void resetView()
    {
        gridView = (StickyGridHeadersGridView)findViewById(R.id.gridView);
        gridView.setAdapter(new DocumentsAdapter(this, data, R.layout.documents_grid_header, R.layout.documents_grid_item));
        gridView.setOnItemClickListener(this);
    }

    private void resetData()
    {
        data = new ArrayList<Map<String, String>>();

        for (int i = 1; i < 10; i++)
        {
            Map<String, String> item = new HashMap<String, String>();

            item.put("id", String.format("%d", i));
            item.put("name", String.format("name_%d", i));
            item.put("section", String.format("section_%d", (i % 4)));
            item.put("section_id", String.format("%d", (i % 4)));

            data.add(item);
        }

        Collections.sort(data, new Comparator<Map<String, String>>() {
            @Override
            public int compare(Map<String, String> itemA, Map<String, String> itemB)
            {
                int compare = itemA.get("section_id").compareTo(itemB.get("section_id"));
                if (0 == compare)
                {
                    compare = itemA.get("id").compareTo(itemB.get("id"));
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
