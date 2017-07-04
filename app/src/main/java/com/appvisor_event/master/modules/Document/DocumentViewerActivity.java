package com.appvisor_event.master.modules.Document;

import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.SeekBar;
import android.widget.TextView;

import com.appvisor_event.master.AppActivity;
import com.appvisor_event.master.R;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener;

import org.json.JSONException;

import java.io.File;

/**
 * Created by bsfuji on 2017/03/31.
 */

public class DocumentViewerActivity extends AppActivity implements OnLoadCompleteListener, OnPageChangeListener
{
    private SeekBar  seekBar                = null;
    private PDFView  pdfView                = null;
    private TextView pageNumberTextView     = null;
    private AlphaAnimation feedInAnimation  = new AlphaAnimation(0, 1);
    private AlphaAnimation feedOutAnimation = new AlphaAnimation(1, 0);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document_viewer);

        pageNumberTextView = (TextView)findViewById(R.id.pageNumberTextView);

        feedInAnimation.setDuration(1000);
        feedInAnimation.setFillAfter(true);
        feedOutAnimation.setStartOffset(1000);
        feedOutAnimation.setDuration(1000);
        feedOutAnimation.setFillAfter(true);

        seekBar = (SeekBar)findViewById(R.id.seekBar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int page, boolean isOnSeek)
            {
                if (isOnSeek)
                {
                    pdfView.jumpTo(seekBar.getProgress());
                }

                pageNumberTextView.setText(String.format("%d / %d", (page + 1), pdfView.getPageCount()));
                pageNumberTextView.startAnimation(feedInAnimation);
                pageNumberTextView.startAnimation(feedOutAnimation);
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
        pdfView.fromFile(file)
                .defaultPage(0)
                .enableAnnotationRendering(true)
                .enableSwipe(true)
                .swipeHorizontal(true)
                .onPageChange(this)
                .onLoad(this)
                .load();
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
        seekBar.setMax(nbPages - 1);
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
