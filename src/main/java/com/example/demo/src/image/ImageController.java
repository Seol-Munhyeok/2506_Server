package com.example.demo.src.image;

import com.example.demo.common.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "image", description = "이미지 업로드 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/app/images")
public class ImageController {

    private final ImageService imageService;

    @Operation(summary = "이미지 업로드", description = "이미지를 업로드하고 URL을 반환합니다.")
    @PostMapping("")
    public BaseResponse<String> uploadImage(@RequestParam("image") MultipartFile image) {
        return new BaseResponse<>(imageService.uploadImage(image));
    }
}

