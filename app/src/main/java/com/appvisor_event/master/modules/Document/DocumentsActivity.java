package com.appvisor_event.master.modules.Document;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.Constants;
import com.appvisor_event.master.R;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;
import com.bumptech.glide.Glide;
import com.tonicartos.widget.stickygridheaders.StickyGridHeadersGridView;

import org.json.JSONObject;

import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class DocumentsActivity extends AppActivity implements StickyGridHeadersGridView.OnItemClickListener
{
    private StickyGridHeadersGridView gridView  = null;
    private DocumentsAdapter          adapter   = null;
    private Document                  document  = null;
    private List<Document.Item>       documents = null;
    private ProgressDialog       progressDialog = null;

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
        resetDocumentSequence();
        resetView();
    }

    private void resetImageCache()
    {
        if (!isCachePolicy())
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Glide.get(getApplicationContext()).clearDiskCache();
                }
            }).start();
        }
    }

    private void resetDocuments()
    {
        document  = new Document(getApplicationContext());
        documents = document.getDocuments();
    }

    private void resetDocumentSequence()
    {
        Collections.sort(documents, new Comparator<Document.Item>() {
            @Override
            public int compare(Document.Item itemA, Document.Item itemB)
            {
                int compare = itemA.getCategory().getSequence().compareTo(itemB.getCategory().getSequence());
                if (0 == compare)
                {
                    compare = itemA.getCategory().getId().compareTo(itemB.getCategory().getId());
                    if (0 == compare)
                    {
                        compare = itemA.getSequence().compareTo(itemB.getSequence());
                    }
                }
                return compare;
            }
        });
    }

    private void resetView()
    {
        adapter = new DocumentsAdapter(getApplicationContext(), documents, R.layout.documents_grid_header, R.layout.documents_grid_item);

        gridView = (StickyGridHeadersGridView)findViewById(R.id.gridView);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    private void loadDocuments()
    {
        String language = AppLanguage.isJapanese(getApplicationContext()) ? "ja" : "en";

        DocumentApiClient apiClient = new DocumentApiClient(
                Constants.DOCUMENTS_API_URL,
                language
        );

        try {
            apiClient.start();
            apiClient.join();

            if (apiClient.hasResponse())
            {
                List<JSONObject> items = apiClient.getResponse().getItems();
                for (JSONObject item : items)
                {
                    Document.Item documentItem = new Document.Item(item);
                    if (documents.contains(documentItem))
                    {
                        documents.remove(documentItem);
                        document.saveDocument(documentItem);
                    }
                    documents.add(documentItem);
                }
                resetDocumentSequence();
                resetImageCache();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });
            }
        }
        catch (Exception exception) {}
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        final Document.Item documentItem = documents.get(position);
        if (document.isSavedItem(documentItem))
        {
            showViewer(documentItem);
            return;
        }

        documentItem.downloadData(new Document.Item.OnDownloadListener() {
            @Override
            public void onStartDownload() {
                showDownloadProgress();
            }

            @Override
            public void onDownloadSuccess(String path)
            {
                hideDownloadProgress();

                if (null == path)
                {
                    Toast.makeText(getApplicationContext(), R.string.document_failed_download_message, Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d("tto", "path: " + path);

                File file = new File(path);
                Log.d("tto", "isFile: " + file.isFile());
                Log.d("tto", "canRead: " + file.canRead());
                Log.d("tto", "exists: " + file.exists());
                Log.d("tto", "length: " + file.length());

                if (documentItem.save(file, getFilesDir()))
                {
                    document.saveDocument(documentItem);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.notifyDataSetChanged();
                    }
                });

                showViewer(documentItem);
            }

            @Override
            public void onProgressUpdate(Integer progress)
            {
                updateDownloadProgress(progress);
            }
        });
    }

    private void showDownloadProgress()
    {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.incrementProgressBy(0);
        progressDialog.show();
    }

    private void updateDownloadProgress(Integer progress)
    {
        progressDialog.incrementProgressBy(progress);
    }

    private void hideDownloadProgress()
    {
        progressDialog.incrementProgressBy(0);
        progressDialog.hide();
        progressDialog = null;
    }

    private void showViewer(Document.Item item)
    {
        File file = new File(getFilesDir(), item.savedFileName());
        if (!file.canRead())
        {
            return;
        }

        Intent intent = new Intent(this, DocumentViewerActivity.class);
        intent.putExtra("document", item.toString());
        startActivity(intent);
    }

    public void onClickButtonBack(View view)
    {
        finish();
    }
}
