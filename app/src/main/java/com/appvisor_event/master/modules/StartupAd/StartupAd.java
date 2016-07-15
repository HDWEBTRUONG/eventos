package com.appvisor_event.master.modules.StartupAd;

/**
 * Created by bsfuji on 16/07/15.
 */
public class StartupAd
{
    static boolean isAlreadyShown = false;

    static public boolean isAlreadyShown()
    {
        return isAlreadyShown;
    }

    static public void setShown(boolean shown)
    {
        isAlreadyShown = shown;
    }
}
