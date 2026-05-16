package org.ifpe.services;

import java.nio.file.Path;

public interface DownloadService {
    long download(int quantity);
    Path getOutputDirectory();
}
