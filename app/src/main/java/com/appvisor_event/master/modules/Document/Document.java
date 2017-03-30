package com.appvisor_event.master.modules.Document;

import com.appvisor_event.master.Constants;

import java.net.URL;

/**
 * Created by bsfuji on 2017/03/29.
 */

public class Document
{
    public static Boolean isDocumentUrl(URL url)
    {
        return url.getPath().startsWith(String.format("/%s/documents", Constants.Event));
    }
}
