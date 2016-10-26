package com.appvisor_event.master;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by BraveSoft on 16/10/19.
 */
public class ShareDetailUtils {

    //画像保存
    private static final String TAG = "ImageManager";
    private static final String APPLICATION_NAME = "PATOM";
    private static final Uri IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private static final String PATH = Environment.getExternalStorageDirectory().toString() + "/" + APPLICATION_NAME;

    private final static int INS_ID = 2;
    private final static int FACEBOOK_ID = 1;
    private final static int TWITTER_ID = 0;
    private final static String[] sharePackages = {"com.twitter.android",  "com.facebook.katana","com.instagram.android" };

    // Twitter
    public static void shareTwitter(Activity activity, String title, String subtitle, String media, String href, String imagUrl) {

        Uri imageUrl =Uri.fromFile(new File(imagUrl));

        String msg = "";
        if (title != null) {
            msg = title + " ";
        }
        if (subtitle != null) {
            msg += subtitle + " ";
        }
        if (media != null) {
            msg += media + " ";
        }
        if (href != null) {
            msg += href + " ";
        }
        msg += "#appVersion_Event";

        if (isShareAppInstall(TWITTER_ID, activity)) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.setPackage(sharePackages[TWITTER_ID]);
            intent.setType("image/*");
            if (imageUrl != null) {
                intent.putExtra(Intent.EXTRA_STREAM, imageUrl);
            }
            intent.putExtra(Intent.EXTRA_TEXT, msg);
            activity.startActivity(intent);
        } else {
            shareAppDl(TWITTER_ID, activity);
        }
    }

    /**
     * facebook
     * https://developers.facebook.com/policy/ 2014/09/30確認
     * ポリシー上 Facebook公式アプリにインテントを飛ばす際は、アプリ側から投稿の文字等はアプリ側から設定出来ない
     * 以下のソースは、Facebook公式アプリに共有URLしたいURLを飛ばしている それならIntentから送れる
     */
    public static void shareFaceBook(Activity activity, String title, String subtitle, String media, String href, String imagUrl) {
        Uri imageUrl=Uri.fromFile(new File(imagUrl));
        String msg = "";
        if (title != null) {
            msg = title + " ";
        }
        if (subtitle != null) {
            msg += subtitle + " ";
        }
        if (media != null) {
            msg += media + " ";
        }
        if (href != null) {
            msg += href + " ";
        }
        msg += "#appVersion_Event";
        if (isShareAppInstall(FACEBOOK_ID, activity)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage(sharePackages[FACEBOOK_ID]);
            shareIntent.setType("image/*");
            if (msg != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
            }
            if (imageUrl != null) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUrl);
            }
            activity.startActivity(shareIntent);
        } else {
            shareAppDl(FACEBOOK_ID, activity);
        }
    }

    public static void shareInstgram(Activity activity, String title, String subtitle, String media, String href, String imagUrl) {
        Uri imageUrl=Uri.fromFile(new File(imagUrl));
        String msg = "";
        if (title != null) {
            msg = title + " ";
        }
        if (subtitle != null) {
            msg += subtitle + " ";
        }
        if (media != null) {
            msg += media + " ";
        }
        if (href != null) {
            msg += href + " ";
        }
        msg += "#appVersion_Event";
        if (isShareAppInstall(INS_ID, activity)) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setPackage(sharePackages[INS_ID]);
            shareIntent.setType("image/*");
            if (msg != null) {
                shareIntent.putExtra(Intent.EXTRA_TEXT, msg);
            }
            if (imageUrl != null) {
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUrl);
            }
            activity.startActivity(shareIntent);
        } else {
            shareAppDl(INS_ID, activity);
        }
    }


    // アプリがインストールされているかチェック
    private static Boolean isShareAppInstall(int shareId, Activity activity) {
        try {
            PackageManager pm = activity.getPackageManager();
            pm.getApplicationInfo(sharePackages[shareId], PackageManager.GET_META_DATA);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    // アプリが無かったのでGooglePalyに飛ばす
    private static void shareAppDl(int shareId, Activity activity) {
        Uri uri = Uri.parse("market://details?id=" + sharePackages[shareId]);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        activity.startActivity(intent);
    }



    public static Uri addImageAsApplication(ContentResolver cr, Bitmap bitmap) {
        long dateTaken = System.currentTimeMillis();
        String name = createName(dateTaken) + ".jpg";
        return addImageAsApplication(cr, name, dateTaken, PATH, name, bitmap, null);
    }

    private static String createName(long dateTaken) {
        return DateFormat.format("yyyy-MM-dd_kk.mm.ss", dateTaken).toString();
    }

    public static Uri addImageAsApplication(ContentResolver cr, String name,
                                            long dateTaken, String directory,
                                            String filename, Bitmap source, byte[] jpegData) {
        OutputStream outputStream = null;
        String filePath = directory + "/" + filename;
        try {
            File dir = new File(directory);
            if (!dir.exists()) {
                dir.mkdirs();
                Log.d(TAG, dir.toString() + " create");
            }
            File file = new File(directory, filename);
            if (file.createNewFile()) {
                outputStream = new FileOutputStream(file);
                if (source != null) {
                    source.compress(Bitmap.CompressFormat.JPEG, 75, outputStream);
                } else {
                    outputStream.write(jpegData);
                }
            }

        } catch (FileNotFoundException ex) {
            Log.w(TAG, ex);
            return null;
        } catch (IOException ex) {
            Log.w(TAG, ex);
            return null;
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (Throwable t) {
                }
            }
        }
        ContentValues values = new ContentValues(7);
        values.put(MediaStore.Images.Media.TITLE, name);
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/*");
        values.put(MediaStore.Images.Media.DATA, filePath);
        return cr.insert(IMAGE_URI, values);
    }
}
