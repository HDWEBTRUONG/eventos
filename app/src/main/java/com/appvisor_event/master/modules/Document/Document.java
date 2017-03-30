package com.appvisor_event.master.modules.Document;

import android.util.Log;

import com.appvisor_event.master.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class Document
{
    public static class Item
    {
        public class Category
        {
            private String id   = null;
            private String name = null;

            public Category(String id, String name)
            {
                this.id   = id;
                this.name = name;
            }

            public String getId()
            {
                return this.id;
            }

            public String getName()
            {
                return this.name;
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
        }

        private String   id                 = null;
        private Category category           = null;
        private String   name               = null;
        private Period   period             = null;
        private String   sequence           = null;
        private String   thumbnailImagePath = null;
        private String   dataPath           = null;

        public Item(JSONObject json) throws JSONException
        {
            this.id                 = json.getString("id");
            this.category           = new Category(json.getString("event_document_category_id"), json.getString("event_document_category_name"));
            this.name               = json.getString("name");
            this.period             = new Period(json.getString("period_start_date"), json.getString("period_end_date"));
            this.sequence           = json.getString("sequence");
            this.thumbnailImagePath = json.getString("thumbnail_image_path");
            this.dataPath           = json.getString("path");
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

        public String toString()
        {
            try {
                JSONObject jsonObject = new JSONObject();

                jsonObject.put("id",                            this.id);
                jsonObject.put("event_document_category_id",    this.category.getId());
                jsonObject.put("event_document_category_name",  this.category.getName());
                jsonObject.put("name",                          this.name);
                jsonObject.put("period_start_date",             this.period.getStartDate());
                jsonObject.put("period_end_date",               this.period.getEndDate());
                jsonObject.put("sequence",                      this.sequence);
                jsonObject.put("thumbnail_image_path",          this.thumbnailImagePath);
                jsonObject.put("path",                          this.dataPath);

                return jsonObject.toString();
            } catch (JSONException exception) {
                Log.e("tto", "exception: " + exception.getMessage());
            }

            return null;
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
}
