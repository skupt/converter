package rozaryonov.converter.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import rozaryonov.converter.service.ConverterService;
import rozaryonov.converter.util.XlsPdfConverter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FileConverterService implements ConverterService {

    private final Path rootLocation;

    @Autowired
    public FileConverterService(@Value("${storage.rootLocation}") String rootLocatonString) {
        this.rootLocation = Paths.get(rootLocatonString);
    }

    @Override
    public List<String> listOfConvertableFiles(String username) {
        Path userRootLocation = rootLocation.resolve(Paths.get(username));
        List<String> fileNames= new ArrayList<>();
        try {
            fileNames = Files.walk(userRootLocation, 1)
                    .filter(p->!p.toFile().isDirectory())
                    .map(p->p.getFileName())
                    .filter(p->{
                        String filename = p.toString();
                        int lastInd = filename.lastIndexOf(".");
                        if (lastInd == -1) return false;
                        String fileType = filename.substring(lastInd+1);
                        boolean support = XlsPdfConverter.SupportedTypes.checkSupport(fileType);
                        return support;
                    })
                    .map(p->p.toString())
                    .collect(Collectors.toList())
            ;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileNames;
    }

    @Override
    public InputStream getInputStream(String filename, String username) {
        Path fileToConvertPath = rootLocation.resolve(Paths.get(username, filename));
        System.out.println(fileToConvertPath);
        File inputFile = fileToConvertPath.toFile();
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream(String filename, String username) {
        int lastInd = filename.lastIndexOf(".");
        String newFileName = filename.substring(0, lastInd).concat(".pdf");
        Path newPdfFile = rootLocation.resolve(Paths.get(username, newFileName));
        System.out.println(newPdfFile);
        File outputFile = newPdfFile.toFile();
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return outputStream;
    }
}
