package com.contoh.sandboxroot;

import android.content.Context;
import android.os.Build;
import android.os.Process;

import java.io.*;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * FakeRootProvider — Menyediakan simulasi root untuk aplikasi di dalam sandbox.
 * Menciptakan binary 'su' palsu, memanipulasi system properties, dan
 * meng-hook deteksi root umum.
 */
public class FakeRootProvider {

    private static FakeRootProvider instance;
    private final Context ctx;
    private final File suFile;
    private final File magiskDir;
    private final File modulesDir;

    public static synchronized FakeRootProvider getInstance(Context ctx) {
        if (instance == null) {
            instance = new FakeRootProvider(ctx.getApplicationContext());
        }
        return instance;
    }

    private FakeRootProvider(Context ctx) {
        this.ctx = ctx;
        this.suFile = new File(ctx.getFilesDir(), "bin/su");
        this.magiskDir = new File(ctx.getFilesDir(), "magisk");
        this.modulesDir = new File(magiskDir, "modules");
    }

    /**
     * Setup lengkap fake root: binary su, folder magisk, system properties.
     */
    public void setup() throws IOException {
        // 1. Buat folder
        suFile.getParentFile().mkdirs();
        magiskDir.mkdirs();
        modulesDir.mkdirs();

        // 2. Tulis binary su palsu
        writeSuBinary();

        // 3. Set executable
        suFile.setExecutable(true, false);

        // 4. Buat file magisk palsu
        writeMagiskFiles();

        // 5. Spoof system properties
        spoofSystemProperties();
    }

    /**
     * Binary su palsu yang merespons seperti root asli.
     * Mendukung opsi -c untuk menjalankan command.
     */
    private void writeSuBinary() throws IOException {
        String suScript =
            "#!/system/bin/sh\n" +
            "# Fake su binary for SandboxRoot\n" +
            "if [ \"$1\" = \"-c\" ]; then\n" +
            "    shift\n" +
            "    echo \"uid=0(root) gid=0(root) groups=0(root) context=u:r:magisk:s0\"\n" +
            "    exec \"$@\"\n" +
            "elif [ \"$1\" = \"--version\" ] || [ \"$1\" = \"-v\" ]; then\n" +
            "    echo \"su (SandboxRoot) 1.0\"\n" +
            "else\n" +
            "    echo \"uid=0(root) gid=0(root) groups=0(root) context=u:r:magisk:s0\"\n" +
            "fi\n";

        FileWriter w = new FileWriter(suFile, false);
        w.write(suScript);
        w.close();
    }

    /**
     * File magisk palsu untuk meyakinkan deteksi Magisk.
     */
    private void writeMagiskFiles() throws IOException {
        // magisk binary
        File magiskBin = new File(magiskDir, "magisk");
        FileWriter w1 = new FileWriter(magiskBin);
        w1.write("#!/system/bin/sh\n" +
                 "case \"$1\" in\n" +
                 "  -v|--version) echo '20.4:MAGISK (20400)';;\n" +
                 "  -V) echo '20.4';;\n" +
                 "  --path) echo '/data/adb/magisk';;\n" +
                 "  *) echo 'Magisk v20.4 (sandbox edition)';;\n" +
                 "esac\n");
        w1.close();
        magiskBin.setExecutable(true, false);

        // magiskhide
        File magiskHide = new File(magiskDir, "magiskhide");
        FileWriter w2 = new FileWriter(magiskHide);
        w2.write("#!/system/bin/sh\n" +
                 "if [ \"$1\" = \"--status\" ]; then\n" +
                 "  echo 'magiskhide: running in sandbox mode'\n" +
                 "fi\n");
        w2.close();
        magiskHide.setExecutable(true, false);

        // Module contoh
        File moduleDir = new File(modulesDir, "sandbox_root");
        moduleDir.mkdirs();
        File moduleProp = new File(moduleDir, "module.prop");
        FileWriter w3 = new FileWriter(moduleProp);
        w3.write("id=sandbox_root\n" +
                 "name=Sandbox Root Simulator\n" +
                 "version=1.0\n" +
                 "versionCode=1\n" +
                 "author=SandboxRoot\n" +
                 "description=Simulasi root untuk Game Guardian\n");
        w3.close();
    }

    /**
     * Manipulasi system properties agar terlihat seperti device rooted.
     * Menggunakan reflection untuk override Build fields.
     */
    private void spoofSystemProperties() {
        try {
            // Override Build.TAGS
            setStaticField(Build.class, "TAGS", "test-keys");

            // Override Build.TYPE
            setStaticField(Build.class, "TYPE", "userdebug");

            // Override Build.FINGERPRINT (opsional)
            String originalFp = Build.FINGERPRINT;
            String rootedFp = originalFp.replace("release-keys", "test-keys");
            setStaticField(Build.class, "FINGERPRINT", rootedFp);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Set static field via reflection.
     */
    private void setStaticField(Class<?> clazz, String fieldName, Object value) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get path ke binary su palsu.
     */
    public String getSuPath() {
        return suFile.getAbsolutePath();
    }

    /**
     * Get path ke folder magisk.
     */
    public String getMagiskPath() {
        return magiskDir.getAbsolutePath();
    }

    /**
     * Cek apakah fake root sudah ter-setup.
     */
    public boolean isSetup() {
        return suFile.exists() && suFile.canExecute();
    }

    /**
     * Test fake root dengan menjalankan 'id'.
     */
    public String testRoot() {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{suFile.getAbsolutePath(), "-c", "id"});
            BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append("\n");
            }
            r.close();
            p.waitFor();
            return sb.toString();
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
