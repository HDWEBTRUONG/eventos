package com.appvisor_event.master.modules.ForceUpdate;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Bundle;

import com.appvisor_event.master.Constants;
import com.appvisor_event.master.User;
import com.appvisor_event.master.modules.AppLanguage.AppLanguage;

/**
 * Created by bsfuji on 2017/02/02.
 */

public class ForceUpdate
{
    public interface CheckVersionListener
    {
        void OnSuccess(ForceUpdateApiClient.Response response);
        void OnError(Exception exception);
    }

    static ForceUpdateAlertDialogFragment alertDialogFragment = null;

    static public void checkVersion(Context context, CheckVersionListener listener)
    {
        String language = AppLanguage.isJapanese(context) ? "ja" : "en";

        ForceUpdateApiClient apiClient = new ForceUpdateApiClient(
                Constants.CHECK_VERSION_API_URL,
                User.getAppVersion(context),
                language
        );


        try {
            apiClient.start();
            apiClient.join();

            if (null != listener)
            {
                if (apiClient.hasResponse())
                {
                    listener.OnSuccess(apiClient.getResponse());
                }
            }
        }
        catch (Exception exception) {
            if (null != listener)
            {
                listener.OnError(exception);
            }
        }
    }

    static public boolean isNotSatisfiedVersion(ForceUpdateApiClient.Response response)
    {
        return !response.code.equals("200");
    }

    static public void showAlertViewWithData(FragmentManager fragmentManager, Bundle data)
    {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment prevFragment = fragmentManager.findFragmentByTag(ForceUpdateAlertDialogFragment.class.getName());
        if (null != prevFragment)
        {
            dismissAlertView();
            fragmentTransaction.remove(prevFragment);
        }
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        alertDialogFragment = new ForceUpdateAlertDialogFragment();
        alertDialogFragment.setArguments(data);
        alertDialogFragment.setCancelable(false);
        fragmentTransaction.add(alertDialogFragment, ForceUpdateAlertDialogFragment.class.getName());
        fragmentTransaction.show(alertDialogFragment);

    }

    static public void dismissAlertView()
    {
        if (null == alertDialogFragment)
        {
            return;
        }

        if (null != alertDialogFragment.getFragmentManager())
        {
            alertDialogFragment.dismissAllowingStateLoss();
        }
        alertDialogFragment = null;
    }
}
