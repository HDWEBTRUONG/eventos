package com.appvisor_event.master.modules.ForceUpdate;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.appvisor_event.master.R;
import com.bumptech.glide.Glide;


/**
 * Created by bsfuji on 2017/02/02.
 */

public class ForceUpdateAlertDialogFragment extends DialogFragment
{
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.force_update_alert_dialog);

        TextView   titleTextView = (TextView)dialog.findViewById(R.id.titleTextView);
        TextView  headerTextView = (TextView)dialog.findViewById(R.id.headerTextView);
        ImageView      imageView = (ImageView)dialog.findViewById(R.id.imageView);
        TextView  footerTextView = (TextView)dialog.findViewById(R.id.footerTextView);
        Button       storeButton = (Button)dialog.findViewById(R.id.storeButton);

        Bundle arguments = getArguments();

        String title = arguments.getString("title");
        if (null != title && !title.isEmpty())
        {
            titleTextView.setText(title);
            titleTextView.setVisibility(View.VISIBLE);
        }

        String header = arguments.getString("header");
        if (null != header && !header.isEmpty())
        {
            headerTextView.setText(header);
            headerTextView.setVisibility(View.VISIBLE);
        }

        String footer = arguments.getString("footer");
        if (null != footer && !footer.isEmpty())
        {
            footerTextView.setText(footer);
            footerTextView.setVisibility(View.VISIBLE);
        }

        String image = arguments.getString("image");
        if (null != image && !image.isEmpty())
        {
            Uri uri = Uri.parse(image);
            Glide.with(this).load(uri).into(imageView);
            imageView.setVisibility(View.VISIBLE);
        }

        final String storeURL = arguments.getString("storeURL");
        storeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStoreURL(storeURL);
            }
        });

        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    private void openStoreURL(String url)
    {
        try
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        }
        catch (Exception exception)
        {
            exception.printStackTrace();
        }
    }
}
