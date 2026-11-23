package com.kenzglobe.debugmate;

import java.io.File;

public class Template_AndroidEmpty implements ProjectTemplate {

    @Override
    public void generate(File root, ProjectConfig config) throws java.io.IOException {
        String projectName = config.projectName;
        
        // Use the package name from the config. Fallback if empty.
        String packageName = config.packageName;
        if (packageName == null || packageName.trim().isEmpty()) {
            packageName = "com.example." + projectName.toLowerCase().replace(" ", "").replace("-", "");
        }
        
        String packagePath = packageName.replace(".", "/");

        // 1. Root Level Files
        TemplateUtils.writeFile(root, ".gitignore",
            "*.iml\n.gradle\n/local.properties\n/.idea\n.DS_Store\n/build\n/captures\n.externalNativeBuild\n.cxx\nlocal.properties\n"
        );

        TemplateUtils.writeFile(root, "build.gradle",
            "// Top-level build file where you can add configuration options common to all sub-projects/modules.\n" +
            "plugins {\n" +
            "    id 'com.android.application' version '8.1.0' apply false\n" +
            "}\n"
        );

        TemplateUtils.writeFile(root, "settings.gradle",
            "pluginManagement {\n" +
            "    repositories {\n" +
            "        google()\n" +
            "        mavenCentral()\n" +
            "        gradlePluginPortal()\n" +
            "    }\n" +
            "}\n" +
            "dependencyResolutionManagement {\n" +
            "    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)\n" +
            "    repositories {\n" +
            "        google()\n" +
            "        mavenCentral()\n" +
            "    }\n" +
            "}\n" +
            "rootProject.name = \"" + projectName + "\"\n" +
            "include ':app'\n"
        );

        TemplateUtils.writeFile(root, "gradle.properties",
            "org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8\n" +
            "android.useAndroidX=true\n" +
            "android.nonTransitiveRClass=true\n"
        );

        // 2. App Module Level Files
        File appDir = TemplateUtils.createDirectory(root, "app");

        TemplateUtils.writeFile(appDir, "build.gradle",
            "plugins {\n" +
            "    id 'com.android.application'\n" +
            "}\n\n" +
            "android {\n" +
            "    namespace '" + packageName + "'\n" +
            "    compileSdk 34\n\n" +
            "    defaultConfig {\n" +
            "        applicationId \"" + packageName + "\"\n" +
            "        minSdk 24\n" +
            "        targetSdk 34\n" +
            "        versionCode 1\n" +
            "        versionName \"1.0\"\n" +
            "    }\n\n" +
            "    buildTypes {\n" +
            "        release {\n" +
            "            minifyEnabled false\n" +
            "            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'\n" +
            "        }\n" +
            "    }\n" +
            "    compileOptions {\n" +
            "        sourceCompatibility JavaVersion.VERSION_1_8\n" +
            "        targetCompatibility JavaVersion.VERSION_1_8\n" +
            "    }\n" +
            "}\n\n" +
            "dependencies {\n" +
            "    implementation 'androidx.appcompat:appcompat:1.6.1'\n" +
            "    implementation 'com.google.android.material:material:1.9.0'\n" +
            "    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'\n" +
            "}\n"
        );

        TemplateUtils.writeFile(appDir, "proguard-rules.pro",
            "# Add project specific ProGuard rules here.\n" +
            "# By default, the flags in this file are appended to flags specified\n" +
            "# in /path/to/android-sdk/tools/proguard/proguard-android.txt\n"
        );

        // 3. Source Files (src/main/...)
        File srcMain = TemplateUtils.createDirectory(appDir, "src/main");
        
        TemplateUtils.writeFile(srcMain, "AndroidManifest.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<manifest xmlns:android=\"http://schemas.android.com/apk/res/android\">\n\n" +
            "    <application\n" +
            "        android:allowBackup=\"true\"\n" +
            "        android:label=\"@string/app_name\"\n" +
            "        android:supportsRtl=\"true\"\n" +
            "        android:theme=\"@style/Theme." + projectName.replace(" ", "") + "\">\n" +
            "        <activity\n" +
            "            android:name=\".MainActivity\"\n" +
            "            android:exported=\"true\">\n" +
            "            <intent-filter>\n" +
            "                <action android:name=\"android.intent.action.MAIN\" />\n" +
            "                <category android:name=\"android.intent.category.LAUNCHER\" />\n" +
            "            </intent-filter>\n" +
            "        </activity>\n" +
            "    </application>\n" +
            "</manifest>\n"
        );

        // Java Source
        File javaDir = TemplateUtils.createDirectory(srcMain, "java");
        File packageDir = new File(javaDir, packagePath);
        packageDir.mkdirs(); // Create the deeply nested package folders

        TemplateUtils.writeFile(packageDir, "MainActivity.java",
            "package " + packageName + ";\n\n" +
            "import androidx.appcompat.app.AppCompatActivity;\n" +
            "import android.os.Bundle;\n\n" +
            "public class MainActivity extends AppCompatActivity {\n\n" +
            "    @Override\n" +
            "    protected void onCreate(Bundle savedInstanceState) {\n" +
            "        super.onCreate(savedInstanceState);\n" +
            "        setContentView(R.layout.activity_main);\n" +
            "    }\n" +
            "}\n"
        );

        // 4. Resources
        File resDir = TemplateUtils.createDirectory(srcMain, "res");
        
        File layoutDir = TemplateUtils.createDirectory(resDir, "layout");
        TemplateUtils.writeFile(layoutDir, "activity_main.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<androidx.constraintlayout.widget.ConstraintLayout xmlns:android=\"http://schemas.android.com/apk/res/android\"\n" +
            "    xmlns:app=\"http://schemas.android.com/apk/res-auto\"\n" +
            "    xmlns:tools=\"http://schemas.android.com/tools\"\n" +
            "    android:layout_width=\"match_parent\"\n" +
            "    android:layout_height=\"match_parent\"\n" +
            "    tools:context=\".MainActivity\">\n\n" +
            "    <TextView\n" +
            "        android:layout_width=\"wrap_content\"\n" +
            "        android:layout_height=\"wrap_content\"\n" +
            "        android:text=\"Hello World!\"\n" +
            "        app:layout_constraintBottom_toBottomOf=\"parent\"\n" +
            "        app:layout_constraintEnd_toEndOf=\"parent\"\n" +
            "        app:layout_constraintStart_toStartOf=\"parent\"\n" +
            "        app:layout_constraintTop_toTopOf=\"parent\" />\n\n" +
            "</androidx.constraintlayout.widget.ConstraintLayout>\n"
        );

        File valuesDir = TemplateUtils.createDirectory(resDir, "values");
        
        TemplateUtils.writeFile(valuesDir, "colors.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "    <color name=\"purple_200\">#FFBB86FC</color>\n" +
            "    <color name=\"purple_500\">#FF6200EE</color>\n" +
            "    <color name=\"purple_700\">#FF3700B3</color>\n" +
            "    <color name=\"teal_200\">#FF03DAC5</color>\n" +
            "    <color name=\"teal_700\">#FF018786</color>\n" +
            "    <color name=\"black\">#FF000000</color>\n" +
            "    <color name=\"white\">#FFFFFFFF</color>\n" +
            "</resources>\n"
        );

        TemplateUtils.writeFile(valuesDir, "strings.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "    <string name=\"app_name\">" + projectName + "</string>\n" +
            "</resources>\n"
        );

        TemplateUtils.writeFile(valuesDir, "themes.xml",
            "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources xmlns:tools=\"http://schemas.android.com/tools\">\n" +
            "    <!-- Base application theme. -->\n" +
            "    <style name=\"Theme." + projectName.replace(" ", "") + "\" parent=\"Theme.MaterialComponents.DayNight.DarkActionBar\">\n" +
            "        <!-- Primary brand color. -->\n" +
            "        <item name=\"colorPrimary\">@color/purple_500</item>\n" +
            "        <item name=\"colorPrimaryVariant\">@color/purple_700</item>\n" +
            "        <item name=\"colorOnPrimary\">@color/white</item>\n" +
            "        <!-- Secondary brand color. -->\n" +
            "        <item name=\"colorSecondary\">@color/teal_200</item>\n" +
            "        <item name=\"colorSecondaryVariant\">@color/teal_700</item>\n" +
            "        <item name=\"colorOnSecondary\">@color/black</item>\n" +
            "    </style>\n" +
            "</resources>\n"
        );
        
        // Add a basic README for the project
        TemplateUtils.writeFile(root, "README.md", 
            "# " + projectName + "\n\nGenerated by DebugMate.\n"
        );
    }
}
