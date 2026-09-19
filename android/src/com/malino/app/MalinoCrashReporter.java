package com.malino.app;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Installs a process-wide uncaught-exception handler at process start
 * (ContentProviders are created before the Application and Activity
 * classes), so a Java-layer crash is also written somewhere the user can
 * reach without adb:
 *
 *  - <app external files dir>/malino_crash.txt
 *  - Download/malino_crash.txt (MediaStore, Android 10+; visible in the
 *    system Files/Downloads app, no storage permission required)
 *
 * The previously installed handler is chained afterwards, so the normal
 * crash dialog and process death still happen exactly as before.
 */
public class MalinoCrashReporter extends ContentProvider {

    private static final String FILE_NAME = "malino_crash.txt";

    @Override
    public boolean onCreate() {
        final Context appContext = getContext();
        final Thread.UncaughtExceptionHandler previous =
                Thread.getDefaultUncaughtExceptionHandler();

        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                try {
                    save(appContext, throwable);
                } catch (Throwable ignored) {
                }
                if (previous != null) {
                    previous.uncaughtException(thread, throwable);
                }
            }
        });
        return true;
    }

    private static void save(Context c, Throwable t) {
        StringBuilder sb = new StringBuilder();
        sb.append("Malino crash report\n");
        sb.append("time_ms: ").append(System.currentTimeMillis()).append('\n');
        sb.append("sdk_int: ").append(Build.VERSION.SDK_INT).append('\n');
        sb.append("device: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL).append('\n');
        sb.append(t.getClass().getName()).append(": ").append(t.getMessage()).append('\n');
        for (StackTraceElement f : t.getStackTrace()) {
            sb.append("    at ").append(f).append('\n');
        }
        Throwable cause = t.getCause();
        int depth = 0;
        while (cause != null && depth < 10) {
            sb.append("Caused by: ").append(cause.getClass().getName())
              .append(": ").append(cause.getMessage()).append('\n');
            for (StackTraceElement f : cause.getStackTrace()) {
                sb.append("    at ").append(f).append('\n');
            }
            cause = cause.getCause();
            depth++;
        }
        String text = sb.toString();

        if (c == null) {
            return;
        }

        // 1) App-specific external dir (reachable via adb or a file manager
        //    on Android 10 and below).
        try {
            File dir = c.getExternalFilesDir(null);
            if (dir == null) {
                dir = c.getFilesDir();
            }
            if (dir != null) {
                writeFile(new File(dir, FILE_NAME), text);
            }
        } catch (Throwable ignored) {
        }

        // 2) Public Downloads folder on Android 10+ so the user can open and
        //    share the report without a computer.
        if (Build.VERSION.SDK_INT >= 29) {
            try {
                ContentValues cv = new ContentValues();
                cv.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
                cv.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
                cv.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri uri = c.getContentResolver().insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv);
                if (uri != null) {
                    OutputStream os = c.getContentResolver().openOutputStream(uri);
                    if (os != null) {
                        os.write(text.getBytes(StandardCharsets.UTF_8));
                        os.close();
                    }
                }
            } catch (Throwable ignored) {
            }
        }
    }

    private static void writeFile(File file, String text) throws Exception {
        FileOutputStream out = new FileOutputStream(file, false);
        out.write(text.getBytes(StandardCharsets.UTF_8));
        out.close();
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
