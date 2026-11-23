package com.kenzglobe.debugmate;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class ProjectStatsManager {

    private static final String PREFS_NAME = "ProjectStatsCache";
    private static final long CACHE_EXPIRY_MS = 5 * 60 * 1000; // 5 minutes

    // 🛑 THE BLOCKLIST: Folders and files to ignore
    private static final Set<String> IGNORED_NAMES = new HashSet<>(Arrays.asList(
            ".git", ".gradle", ".idea", ".vscode", // IDE & VCS folders
            "build", "bin", "obj", "out", "dist",  // Compiled output
            "node_modules", "venv", ".env",        // Dependencies & Environments
            "__pycache__", ".pytest_cache",        // Python caches
            ".DS_Store", "Thumbs.db"               // OS junk files
    ));

    public interface StatsCallback {
        void onStatsCalculated(ProjectStats stats);
    }

    public static void getStats(final Context context, final String projectPath, final boolean forceRefresh, final StatsCallback callback) {
        final SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        final String keyPrefix = "stats_v2_" + projectPath.hashCode() + "_"; // Note: bumped to v2 to invalidate old bad caches

        // 1. CHECK CACHE
        if (!forceRefresh) {
            long cachedTime = prefs.getLong(keyPrefix + "time", 0);
            if (System.currentTimeMillis() - cachedTime < CACHE_EXPIRY_MS) {
                ProjectStats cachedStats = new ProjectStats();
                cachedStats.fileCount = prefs.getInt(keyPrefix + "files", 0);
                cachedStats.folderCount = prefs.getInt(keyPrefix + "folders", 0);
                cachedStats.totalSize = prefs.getLong(keyPrefix + "size", 0);
                cachedStats.lastModified = prefs.getLong(keyPrefix + "modified", 0);
                
                callback.onStatsCalculated(cachedStats);
                return; 
            }
        }

        // 2. CALCULATE IN BACKGROUND
        final Handler mainHandler = new Handler(Looper.getMainLooper());
        
        new Thread(new Runnable() {
            @Override
            public void run() {
                final ProjectStats newStats = calculateFolderStats(new File(projectPath));

                prefs.edit()
                    .putInt(keyPrefix + "files", newStats.fileCount)
                    .putInt(keyPrefix + "folders", newStats.folderCount)
                    .putLong(keyPrefix + "size", newStats.totalSize)
                    .putLong(keyPrefix + "modified", newStats.lastModified)
                    .putLong(keyPrefix + "time", System.currentTimeMillis())
                    .apply();

                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        callback.onStatsCalculated(newStats);
                    }
                });
            }
        }).start();
    }

    private static ProjectStats calculateFolderStats(File dir) {
        ProjectStats stats = new ProjectStats();
        if (dir == null || !dir.exists()) return stats;

        stats.lastModified = dir.lastModified();
        File[] files = dir.listFiles();
        
        if (files != null) {
            for (File f : files) {
                // 🛑 CHECK IF THE FILE/FOLDER IS IN THE BLOCKLIST
                if (IGNORED_NAMES.contains(f.getName())) {
                    continue; // Skip it completely!
                }

                if (f.isDirectory()) {
                    stats.folderCount++;
                    ProjectStats subStats = calculateFolderStats(f);
                    stats.fileCount += subStats.fileCount;
                    stats.folderCount += subStats.folderCount;
                    stats.totalSize += subStats.totalSize;
                    if (subStats.lastModified > stats.lastModified) {
                        stats.lastModified = subStats.lastModified;
                    }
                } else {
                    stats.fileCount++;
                    stats.totalSize += f.length();
                    if (f.lastModified() > stats.lastModified) {
                        stats.lastModified = f.lastModified();
                    }
                }
            }
        }
        return stats;
    }
    
    public static void clearCache(Context context, String projectPath) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String keyPrefix = "stats_v2_" + projectPath.hashCode() + "_";
        prefs.edit().remove(keyPrefix + "time").apply(); // Removing the time forces a recalculation
    }
}
