package com.appvisor_event.master.modules;

import java.util.ArrayList;

/**
 * Created by bsfuji on 15/07/27.
 */
public class JavascriptManager
{
    private static JavascriptManager sharedInstance = new JavascriptManager();

    private JavascriptManager() {}

    private ArrayList<JavascriptHandlerInterface> handlers = new ArrayList<JavascriptHandlerInterface>();

    public static JavascriptManager getInstance()
    {
        return sharedInstance;
    }

    public JavascriptManager addHandler(JavascriptHandlerInterface handler)
    {
        this.handlers.add(handler);

        return this;
    }

    public boolean onJsAlert(String alert)
    {
        for (JavascriptHandlerInterface handler : this.handlers)
        {
            if (handler.onJsAlert(alert))
            {
                return true;
            }
        }

        return false;
    }

    public interface JavascriptHandlerInterface
    {
        public boolean onJsAlert(String alertString);
    }
}
