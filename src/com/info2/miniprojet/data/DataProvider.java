package com.info2.miniprojet.data;

import java.io.IOException;
import java.util.List;

public interface DataProvider {
    List<String> loadRawLines() throws IOException,InterruptedException;
    String toString();
}
