package com.appvisor_event.master.modules.Document;

import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.R;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnLoadCompleteListener;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

import org.json.JSONException;

import java.io.File;

/**
 * Created by bsfuji on 2017/03/31.
 */

public class DocumentViewerActivity extends AppActivity implements OnLoadCompleteListener, OnPageChangeListener
{
    private SeekBar seekBar = null;
    private PDFView pdfView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b)
            {
                pdfView.jumpTo(seekBar.getProgress());

                Toast.makeText(DocumentViewerActivity.this, String.format("%d / %d", seekBar.getProgress(), pdfView.getPageCount()), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar)
            {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar)
            {
            }
        });


        Document.Item document = getDocument();
        File file = new File(getFilesDir(), document.savedFileName());
        if (!file.canRead())
        {
            finish();
        }

        TextView titleTextView = (TextView)findViewById(R.id.titleTextView);
        titleTextView.setText(document.getName());

        pdfView = (PDFView)findViewById(R.id.pdfview);
        pdfView.fromFile(file).onLoad(this).onPageChange(this).load();
    }

    private Document.Item getDocument()
    {
        try {
            String document = getIntent().getStringExtra("document");
            return new Document.Item(document);
        } catch (JSONException exception) {}

        return null;
    }

    @Override
    public void loadComplete(int nbPages)
    {
        seekBar.setMax(nbPages);
    }

    @Override
    public void onPageChanged(int page, int pageCount)
    {
        seekBar.setProgress(page);
    }

    public void onClickButtonBack(View view)
    {
        finish();
    }
}
