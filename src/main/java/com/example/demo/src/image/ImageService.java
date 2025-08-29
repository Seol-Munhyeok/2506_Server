package com.example.demo.src.image;

import com.example.demo.common.exceptions.BaseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.example.demo.common.response.BaseResponseStatus.*;

@Service
public class ImageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_SIZE = 5 * 1024 * 1024; // 5MB
    private final Path root = Paths.get("uploads");

    public String uploadImage(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new BaseException(IMAGES_EMPTY_FILE);

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase()
                : null;
        if (ext == null || !ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BaseException(POST_IMAGES_INVALID_EXTENSION);
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BaseException(POST_IMAGES_INVALID_SIZE);
        }

        try {
            if (!Files.exists(root)) {
                Files.createDirectories(root);
            }
            String filename = UUID.randomUUID() + "." + ext;
            Path savePath = root.resolve(filename);
            file.transferTo(savePath.toFile());
            return "/uploads/" + filename;
        } catch (IOException e) {
            throw new BaseException(SERVER_ERROR);
        }
    }
}
