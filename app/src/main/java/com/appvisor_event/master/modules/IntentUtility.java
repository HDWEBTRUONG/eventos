package com.appvisor_event.master.modules;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import java.util.List;

/**
 * Created by bsfuji on 2017/12/15.
 */

public class IntentUtility
{
    public static boolean existsBrowser(Context context, String url)
    {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        List<ResolveInfo> applications = packageManager.queryIntentActivities(intent, 0);
        return (0 < applications.size());
    }
}
