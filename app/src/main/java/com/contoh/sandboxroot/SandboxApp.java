package com.contoh.sandboxroot;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.core.SettingConfig;
import com.lody.virtual.client.stub.StubApp;

public class SandboxApp extends Application {

    private SettingConfig mConfig = new SettingConfig() {
        @Override
        public String getMainPackageName() {
            return BuildConfig.APPLICATION_ID;
        }

        @Override
        public String getExtPackageName() {
            return "com.contoh.sandboxroot.ext";
        }

        @Override
        public boolean isEnableIORedirect() {
            return true;
        }

        @Override
        public boolean isUseRealDataDir(String packageName) {
            // Gunakan data dir virtual, bukan asli
            return false;
        }

        @Override
        public boolean isOutsidePackage(String packageName) {
            return false;
        }

        @Override
        public boolean isAllowCreateShortcut() {
            return false;
        }

        @Override
        public boolean isAllowCreateLauncherShortcut() {
            return true;
        }

        @Override
        public boolean isAllowCreateActivityShortcut() {
            return false;
        }
    };

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        try {
            VirtualCore.get().startup(base, mConfig);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // Inisialisasi VirtualCore
        VirtualCore.get().initialize(mConfig);

        // Setup fake root
        try {
            FakeRootProvider.getInstance(this).setup();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
