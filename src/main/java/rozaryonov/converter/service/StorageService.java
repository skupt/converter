package rozaryonov.converter.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();
    void deleteAll();

    void store(MultipartFile file, String username);
    Stream<Path> loadAll(String username);
    Path load(String filename, String username);
    Resource loadAsResource(String filename, String username);



}
