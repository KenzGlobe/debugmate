package com.kenzglobe.debugmate;

import java.io.File;
import java.io.IOException;

public interface ProjectTemplate {
    void generate(File projectFolder, ProjectConfig config) throws IOException;
}