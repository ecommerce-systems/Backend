package com.creepereye.ecommerce.global.storage;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.stream.Stream;

public interface StorageService {

    void init();

    String store(MultipartFile file);

    Path load(String filename);

    void deleteAll();

}
