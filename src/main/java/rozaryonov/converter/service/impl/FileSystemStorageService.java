package rozaryonov.converter.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import rozaryonov.converter.exception.StorageException;
import rozaryonov.converter.exception.StorageFileNotFoundException;
import rozaryonov.converter.service.StorageService;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

@Service
public class FileSystemStorageService implements StorageService {

    private final Path rootLocation;

    @Autowired
    public FileSystemStorageService(@Value("${storage.rootLocation}") String rootLocatonString) {
        this.rootLocation = Paths.get(rootLocatonString);
    }

    @Override
    public void store(MultipartFile file, String username) {
        try {
            if (file.isEmpty()) {
                throw new StorageException("Failed to store empty file.");
            }
            Path destinationFile = this.rootLocation
                    .resolve(username)
                    .resolve(Paths.get(file.getOriginalFilename().replaceAll("\\s", "_")))
                    .normalize()
                    .toAbsolutePath();
            if (!Files.exists(destinationFile.getParent())) {
                Files.createDirectories(destinationFile.getParent());
            }
            if (!destinationFile.getParent().equals(this.rootLocation.resolve(username).toAbsolutePath())) {
                // This is a security check
                throw new StorageException(
                        "Cannot store file outside current directory.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, destinationFile,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            throw new StorageException("Failed to store file.", e);
        }
    }

    @Override
    public Stream<Path> loadAll(String username) {
        Path userRootLocation = this.rootLocation.resolve(username);
        if (userRootLocation.toFile().isDirectory()) {
            try {
                return Files.walk(userRootLocation, 1)
                        .filter(path -> !path.equals(userRootLocation))
                        .map(userRootLocation::relativize);
            } catch (IOException e) {
                throw new StorageException("Failed to read stored files", e);
            }
        } else {
            return Stream.empty();
        }
    }

    @Override
    public Path load(String filename, String username) {
        return rootLocation.resolve(username).resolve(filename);
    }

    @Override
    public Resource loadAsResource(String filename, String username) {
        try {
            Path file = load(filename, username);
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
            else {
                throw new StorageFileNotFoundException(
                        "Could not read file: " + filename);

            }
        }
        catch (MalformedURLException e) {
            throw new StorageFileNotFoundException("Could not read file: " + filename, e);
        }
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(rootLocation.toFile());
    }

    @Override
    public void init() {
        try {
            Files.createDirectories(rootLocation);
        }
        catch (IOException e) {
            throw new StorageException("Could not initialize storage", e);
        }
    }
}
