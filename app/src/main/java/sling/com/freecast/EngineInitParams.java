package sling.com.freecast;

import android.app.UiModeManager;
import android.content.Context;
import android.content.res.Configuration;
import android.location.Location;
import android.os.Build;
import android.provider.Settings;

import com.slingmedia.slingPlayer.slingClient.SlingClient;
import com.slingmedia.slingPlayer.slingClient.SlingInitParams;

/**
 * Created by la.Abitha.Madhavan on 7/17/2017.
 */

public class EngineInitParams implements SlingInitParams {
    private static final String CONFIG_PRODUCT_VERSION = BuildConfig.CONFIG_PRODUCT_VERSION;
    private static final String CONFIG_PRODUCT_NAME = BuildConfig.CONFIG_PRODUCT_NAME;

    private Context mContext;

    public boolean isTV() {
        boolean isTV = false;
        if(mContext != null) {
            UiModeManager uiModeManager = (UiModeManager) mContext.getSystemService(Context.UI_MODE_SERVICE);
            if (uiModeManager.getCurrentModeType() == Configuration.UI_MODE_TYPE_TELEVISION) {
                isTV = true;
            }
        }
        return isTV;
    }

    EngineInitParams(Context context) {
        mContext = context;
    }

    @Override
    public String getConfigProductName() {
        if(isTV()) {
            return "slingclientsdk-android-10ft";
        }
        else {
            return "slingclientsdk-android-phone";
        }
    }

    @Override
    public String getConfigProductVersion() {
        return SlingClient.getVersion();
    }

    @Override
    public Context getApplicationContext() {
        return mContext;
    }

    @Override
    public String getDeviceId() {
        return Settings.Secure
                .getString(getApplicationContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
    }

    @Override
    public String getDeviceName() {
        return "TestDevice";
    }

    @Override
    public String getClientAnalyticsId() {
        return "FreeCastDemo test app";
    }

    @Override
    public String getAppConfigProductName() {
        return "slingtvapp-android-phone";
    }

    @Override
    public String getAppConfigProductVersion() {
        return "0.0.1";
    }

    @Override
    public String getAppAccountUniqueId() {
        return "TestUniqueUserGuid";
    }

    @Override
    public String getClientDeviceVersion() {
        return Build.VERSION.RELEASE;
    }

    @Override
    public Location getDeviceLocation() {
        return new Location("");
    }

    @Override
    public String getApplicationVersion() {
        return "0.0.1";
    }

    @Override
    public void setAppInstanceSessionId(String s) {

    }

    @Override
    public String getAppInstanceSessionId() {
        return "";
    }
}
