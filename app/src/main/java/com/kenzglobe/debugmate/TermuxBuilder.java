package com.kenzglobe.debugmate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class TermuxBuilder {

    public static void buildProject(Context context, File projectDir) {
        try {
            // 1. Generate the bash script
            File scriptFile = new File(projectDir, "build_debugmate.sh");
            FileWriter writer = new FileWriter(scriptFile);
            
            // This is standard bash script using the Termux tools
            writer.write("#!/data/data/com.termux/files/usr/bin/bash\n");
            writer.write("cd \"" + projectDir.getAbsolutePath() + "\"\n");
            writer.write("echo 'Starting DebugMate Build...'\n");
            writer.write("rm -rf build\n");
            writer.write("mkdir -p build/gen build/obj build/apk\n");
            
            // 1. Compile Resources
            writer.write("echo 'Compiling resources...'\n");
            writer.write("aapt2 compile --dir res -o build/res.zip\n");
            writer.write("aapt2 link -o build/app.unaligned.apk -I /data/data/com.termux/files/home/android.jar --manifest AndroidManifest.xml build/res.zip\n");
            
            // 2. Compile Java
            writer.write("echo 'Compiling Java files...'\n");
            writer.write("find src -name \"*.java\" > build/sources.txt\n");
            writer.write("javac -d build/obj -classpath /data/data/com.termux/files/home/android.jar @build/sources.txt\n");
            
            // 3. Dex
            writer.write("echo 'Dexing classes...'\n");
            writer.write("dx --dex --output=build/classes.dex build/obj/\n");
            writer.write("cd build\n");
            writer.write("zip -u app.unaligned.apk classes.dex\n");
            writer.write("cd ..\n");
            
            // 4. Sign APK
            writer.write("echo 'Signing APK...'\n");
            writer.write("apksigner sign --ks /data/data/com.termux/files/home/debug.keystore --ks-pass pass:android --out build/app-release.apk build/app.unaligned.apk\n");
            
            writer.write("echo 'BUILD_SUCCESS: '$(pwd)/build/app-release.apk\n");
            writer.close();

            // 2. Send Intent to Termux
            Intent intent = new Intent();
            intent.setClassName("com.termux", "com.termux.app.RunCommandService");
            intent.setAction("com.termux.RUN_COMMAND");
            
            // Tell Termux to run our script using bash
            intent.putExtra("com.termux.RUN_COMMAND_PATH", "/data/data/com.termux/files/usr/bin/bash");
            intent.putExtra("com.termux.RUN_COMMAND_ARGUMENTS", new String[]{scriptFile.getAbsolutePath()});
            intent.putExtra("com.termux.RUN_COMMAND_WORKDIR", projectDir.getAbsolutePath());
            intent.putExtra("com.termux.RUN_COMMAND_BACKGROUND", false); // Shows the Termux UI

            context.startService(intent);

        } catch (IOException e) {
            AppToast.showError(context, "Failed to create build script: " + e.getMessage());
        }
    }
}