package com.contoh.sandboxroot;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.remote.InstalledAppInfo;

import java.util.List;

public class MainActivity extends Activity {

    private LinearLayout container;
    private TextView statusText;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);

        ScrollView scroll = new ScrollView(this);
        container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setBackgroundColor(Color.parseColor("#0D1117"));
        container.setPadding(40, 60, 40, 40);
        scroll.addView(container);
        setContentView(scroll);

        // Header
        TextView title = new TextView(this);
        title.setText("SandboxRoot");
        title.setTextColor(Color.parseColor("#58A6FF"));
        title.setTextSize(28f);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        container.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Virtual Engine + Fake Root\nuntuk Game Guardian");
        subtitle.setTextColor(Color.parseColor("#8B949E"));
        subtitle.setTextSize(13f);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 30);
        container.addView(subtitle);

        // Status
        statusText = new TextView(this);
        statusText.setTextColor(Color.parseColor("#7EE787"));
        statusText.setTextSize(12f);
        statusText.setPadding(20, 20, 20, 20);
        statusText.setBackgroundColor(Color.parseColor("#161B22"));
        container.addView(statusText);
        updateStatus();

        // Tombol Test Fake Root
        addButton("🔓 Test Fake Root", "#238636", new View.OnClickListener() {
            public void onClick(View v) { testFakeRoot(); }
        });

        // Tombol Install APK ke Sandbox
        addButton("📥 Install APK ke Sandbox", "#1F6FEB", new View.OnClickListener() {
            public void onClick(View v) { installApkDialog(); }
        });

        // Tombol List App di Sandbox
        addButton("📱 List App di Sandbox", "#8957E5", new View.OnClickListener() {
            public void onClick(View v) { listSandboxApps(); }
        });

        // Tombol Buka Game Guardian
        addButton("🎮 Buka Game Guardian", "#DA3633", new View.OnClickListener() {
            public void onClick(View v) { launchGameGuardian(); }
        });
    }

    private void addButton(String text, String color, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(14f);
        b.setBackgroundColor(Color.parseColor(color));
        b.setTextColor(Color.WHITE);
        b.setPadding(20, 20, 20, 20);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 8, 0, 8);
        b.setLayoutParams(lp);
        b.setOnClickListener(listener);
        container.addView(b);
    }

    private void updateStatus() {
        FakeRootProvider root = FakeRootProvider.getInstance(this);
        StringBuilder sb = new StringBuilder();
        sb.append("Fake Root: ").append(root.isSetup() ? "✓ Aktif" : "✗ Belum").append("\n");
        sb.append("su path  : ").append(root.getSuPath()).append("\n");
        sb.append("Magisk   : ").append(root.getMagiskPath()).append("\n");
        sb.append("VirtualCore: ").append(VirtualCore.get().isMainProcess() ? "Main" : "Sub").append("\n");
        statusText.setText(sb.toString());
    }

    private void testFakeRoot() {
        FakeRootProvider root = FakeRootProvider.getInstance(this);
        String result = root.testRoot();

        new AlertDialog.Builder(this)
            .setTitle("Test Fake Root")
            .setMessage("Output `su -c id`:\n\n" + result +
                       "\n\nJika muncul 'uid=0(root)', fake root berfungsi.")
            .setPositiveButton("OK", null)
            .show();
    }

    private void installApkDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Install APK ke Sandbox")
            .setMessage("Letakkan file APK di /sdcard/Download/ lalu\n" +
                       "gunakan tombol di bawah untuk install.\n\n" +
                       "Contoh: /sdcard/Download/gameguardian.apk")
            .setPositiveButton("OK", null)
            .show();
    }

    private void listSandboxApps() {
        try {
            List<InstalledAppInfo> apps = VirtualCore.get().getInstalledApps(0);
            StringBuilder sb = new StringBuilder();
            sb.append("Total: ").append(apps.size()).append(" app\n\n");
            for (InstalledAppInfo info : apps) {
                sb.append("📦 ").append(info.packageName).append("\n");
            }
            if (apps.isEmpty()) {
                sb.append("(Belum ada app di sandbox)");
            }

            new AlertDialog.Builder(this)
                .setTitle("App di Sandbox")
                .setMessage(sb.toString())
                .setPositiveButton("OK", null)
                .show();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void launchGameGuardian() {
        String ggPackage = "com.gameguardian";
        try {
            if (VirtualCore.get().isAppInstalled(ggPackage)) {
                VActivityManager.get().launchApp(0, ggPackage);
                Toast.makeText(this, "Membuka Game Guardian...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Game Guardian belum diinstall di sandbox", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
