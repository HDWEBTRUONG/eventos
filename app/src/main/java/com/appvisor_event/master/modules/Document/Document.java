package com.appvisor_event.master.modules.Document;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.appvisor_event.master.Constants;
import com.appvisor_event.master.modules.Hash.Hash;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import static com.appvisor_event.master.Constants.BASE_URL;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class Document
{
    private Context    context   = null;
    private List<Item> documents = new ArrayList<>();

    public Document(Context context)
    {
        this.context = context;

        loadDocuments();
    }

    public static class Item
    {
        public interface OnDownloadListener
        {
            void onStartDownload();
            void onDownloadSuccess(String path);
            void onProgressUpdate(Integer progress);
        }

        public class Category
        {
            private String id       = null;
            private String name     = null;
            private String sequence = null;

            public Category(String id, String name, String sequence)
            {
                this.id       = id;
                this.name     = name;
                this.sequence = sequence;
            }

            public String getId()
            {
                return this.id;
            }

            public String getName()
            {
                return this.name;
            }

            public String getSequence()
            {
                return this.sequence;
            }
        }

        public class Period
        {
            private String startDate = null;
            private String endDate = null;

            public Period(String startDate, String endDate)
            {
                this.startDate = startDate;
                this.endDate   = endDate;
            }

            public String getStartDate()
            {
                return this.startDate;
            }

            public String getEndDate()
            {
                return this.endDate;
            }

            public boolean isWithin()
            {
                try {
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
                    simpleDateFormat.setTimeZone(TimeZone.getTimeZone("Asia/Tokyo"));

                    Date startDate = simpleDateFormat.parse(this.startDate);
                    Date endDate   = simpleDateFormat.parse(this.endDate);
                    Date nowDate   = new Date();

                    return (nowDate.equals(startDate) || nowDate.equals(endDate) || (nowDate.after(startDate) && nowDate.before(endDate)));
                }
                catch (ParseException exception) {}

                return false;
            }
        }

        private String   id                 = null;
        private Category category           = null;
        private String   name               = null;
        private Period   period             = null;
        private String   sequence           = null;
        private String   thumbnailImagePath = null;
        private String   thumbnailImageHash = null;
        private String   dataPath           = null;
        private String   dataHash           = null;

        public Item(String jsonString) throws JSONException
        {
            init(new JSONObject(jsonString));
        }

        public Item(JSONObject json) throws JSONException
        {
            init(json);
        }

        public String getId()
        {
            return this.id;
        }

        public Category getCategory()
        {
            return this.category;
        }

        public String getName()
        {
            return this.name;
        }

        public Period getPeriod()
        {
            return this.period;
        }

        public String getSequence()
        {
            return this.sequence;
        }

        public String getThumbnailImagePath()
        {
            return this.thumbnailImagePath;
        }

        public String getDataPath()
        {
            return this.dataPath;
        }

        public String getThumbnailImageUri()
        {
            return BASE_URL + this.thumbnailImagePath;
        }

        public String getDataUri()
        {
            return BASE_URL + this.dataPath;
        }

        public boolean isPublic()
        {
            return this.period.isWithin();
        }

        public void downloadData(final OnDownloadListener listener)
        {
            new AsyncTask<Item, Integer, String>() {
                @Override
                protected void onPreExecute()
                {
                    super.onPreExecute();

                    if (null != listener)
                    {
                        listener.onStartDownload();
                    }
                }

                @Override
                protected String doInBackground(Item[] items)
                {
                    String path = null;

                    try {
                        URL url = new URL(items[0].getDataUri());

                        URLConnection connection = url.openConnection();
                        connection.connect();

                        int fileLength = connection.getContentLength();

                        File temporaryFile = File.createTempFile("tmp_", "_document");

                        InputStream input  = new BufferedInputStream(url.openStream());
                        OutputStream output = new FileOutputStream(temporaryFile.getAbsolutePath());

                        byte data[] = new byte[1024];
                        long total = 0;
                        int count;
                        while (-1 != (count = input.read(data)))
                        {
                            total += count;

                            publishProgress((int)(total * 100 / fileLength));
                            output.write(data, 0, count);
                        }

                        output.flush();
                        output.close();
                        input.close();

                        path = temporaryFile.getAbsolutePath();
                    }
                    catch (Exception exception) {
                        Log.e("tto", exception.getMessage());
                    }

                    return path;
                }

                @Override
                protected void onProgressUpdate(Integer[] values)
                {
                    super.onProgressUpdate(values);

                    if (null != listener)
                    {
                        listener.onProgressUpdate(values[0]);
                    }
                }

                @Override
                protected void onPostExecute(String path)
                {
                    super.onPostExecute(path);

                    if (null != listener)
                    {
                        listener.onDownloadSuccess(path);
                    }
                }
            }.execute(this);
        }

        public String savedFileName()
        {
            return String.format("%s_%s", this.id, this.name);
        }

        public boolean save(File file, File saveDirectory)
        {
            return file.renameTo(new File(saveDirectory, savedFileName()));
        }

        public boolean isSaved(Context context)
        {
            File file = new File(context.getFilesDir(), savedFileName());
            if (null == file || !file.canRead())
            {
                return false;
            }

            boolean isModified = isModifiedDataFileWithHash(Hash.md5(file));
            if (isModified)
            {
                remove(context);
                return false;
            }

            return true;
        }

        public boolean isModifiedDataFileWithHash(String hash)
        {
            if (null == hash || null == this.dataHash)
            {
                return true;
            }
            return !hash.equals(this.dataHash);
        }

        public void remove(Context context)
        {
            File file = new File(context.getFilesDir(), savedFileName());
            file.delete();
        }

        public boolean equals(Object object)
        {
            if (null == object)
            {
                return false;
            }

            if (!(object instanceof Item))
            {
                return false;
            }

            return ((Item)object).getId().equals(this.id);
        }

        public String toString()
        {
            try {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("id",                               this.id);
                jsonObject.put("event_document_category_id",       this.category.getId());
                jsonObject.put("event_document_category_name",     this.category.getName());
                jsonObject.put("event_document_category_sequence", this.category.getSequence());
                jsonObject.put("name",                             this.name);
                jsonObject.put("period_start_date",                this.period.getStartDate());
                jsonObject.put("period_end_date",                  this.period.getEndDate());
                jsonObject.put("sequence",                         this.sequence);
                jsonObject.put("thumbnail_image_path",             this.thumbnailImagePath);
                jsonObject.put("thumbnail_image_hash",             this.thumbnailImageHash);
                jsonObject.put("path",                             this.dataPath);
                jsonObject.put("document_hash",                    this.dataHash);

                return jsonObject.toString();
            } catch (JSONException exception) {
                Log.e("tto", "exception: " + exception.getMessage());
            }

            return null;
        }

        private void init(JSONObject json) throws JSONException
        {
            this.id                 = json.getString("id");
            this.category           = new Category(json.getString("event_document_category_id"), json.getString("event_document_category_name"), json.getString("event_document_category_sequence"));
            this.name               = json.getString("name");
            this.period             = new Period(json.getString("period_start_date"), json.getString("period_end_date"));
            this.sequence           = json.getString("sequence");
            this.thumbnailImagePath = json.getString("thumbnail_image_path");
            this.thumbnailImageHash = json.getString("thumbnail_image_hash");
            this.dataPath           = json.getString("path");
            this.dataHash           = json.getString("document_hash");
        }
    }

    public static Item newItem(JSONObject jsonObject) throws JSONException
    {
        return new Item(jsonObject);
    }

    public static Boolean isDocumentUrl(URL url)
    {
        return url.getPath().startsWith(String.format("/%s/documents", Constants.Event));
    }

    public boolean isSavedItem(Item item)
    {
        if (!item.isSaved(context))
        {
            documents.remove(item);
        }
        return documents.contains(item);
    }

    private void loadDocuments()
    {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("Documents", Context.MODE_PRIVATE);
            String items = sharedPreferences.getString("items", "[]");

            JSONArray jsonDocuments = new JSONArray(items);
            for (int i = 0; i < jsonDocuments.length(); i++)
            {
                JSONObject jsonDocument = jsonDocuments.getJSONObject(i);
                Item item = new Item(jsonDocument);
                if (item.isPublic())
                {
                    documents.add(item);
                    continue;
                }

                if (item.isSaved(this.context))
                {
                    item.remove(this.context);
                }
            }

            saveDocuments();
        } catch (JSONException exception) {}
    }

    public List<Item> getDocuments()
    {
        return new ArrayList<>(documents);
    }

    public void saveDocument(Item item)
    {
        if (isSavedItem(item))
        {
            documents.remove(item);
        }
        documents.add(item);

        saveDocuments();
    }

    public void saveDocuments()
    {
        try {
            JSONArray jsonDocuments = new JSONArray();
            for (Item item : documents)
            {
                JSONObject jsonDocument = new JSONObject(item.toString());
                jsonDocuments.put(jsonDocument);
            }

            SharedPreferences sharedPreferences = context.getSharedPreferences("Documents", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("items", jsonDocuments.toString());
            editor.commit();
        } catch (JSONException exception) {}
    }
}
