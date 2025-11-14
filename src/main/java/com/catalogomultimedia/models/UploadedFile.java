package com.catalogomultimedia.models;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public interface UploadedFile {
    String getFileName();

    String getWebkitRelativePath();

    InputStream getInputStream() throws IOException;

    byte[] getContent();

    String getContentType();

    long getSize();

    File write(String var1) throws Exception;

    void delete() throws IOException;
}

