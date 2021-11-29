package rozaryonov.converter.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public interface ConverterService {
    List<String> listOfConvertableFiles(String username);
    InputStream getInputStream(String filename, String username);
    OutputStream getOutputStream(String filename, String username);
}
