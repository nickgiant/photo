package com.photo.act.photo_act.services;

// https://medium.com/@kouomeukevin/how-to-upload-and-download-image-into-sql-database-with-spring-boot-c849ec5daec6


import com.photo.act.photo_act.views.ImageGalleryView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;
import java.util.zip.DataFormatException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;


@Service
public class ImageService{

    private static final Logger logger = LoggerFactory.getLogger(ImageService.class);

// https://medium.com/@dulanjayasandaruwan1998/uploading-images-in-a-spring-boot-project-a-step-by-step-guide-8a55248ea520
public String uploadImage(MultipartFile file, String uploadDir) {

    String contentType = file.getContentType();
    if (contentType != null && !contentType.equals("image/jpeg") && !contentType.equals("image/png")) {
        logger.error("Only JPEG or PNG images are allowed");
        throw new IllegalArgumentException("Only JPEG or PNG images are allowed");
    }

    // Save the file to the directory
    String filePath = saveImage(file, uploadDir);
    return "Image uploaded successfully: " + filePath;
}

private String saveImage(MultipartFile file, String uploadDir){
    Path uploadPath = Paths.get(uploadDir);
    try {

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String fileName = file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return filePath.toString();
    }catch (IOException e) {
        logger.error(e.getMessage());
        return "Error uploading image "+e.getMessage();
    }
}

    public String copyImage(String fileName, InputStream inputStream, String uploadDir){
        Path uploadPath = Paths.get(uploadDir);
        try {

//            if (!Files.exists(uploadPath)) {
//                Files.createDirectories(uploadPath);
//            }
//
//            String fileName = file.getOriginalFilename();
            Path filePath = uploadPath.resolve(fileName);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            return filePath.toString();
        }catch (IOException e) {
            logger.error(e.getMessage());
            return "Error uploading image "+e.getMessage();
        }
    }

    public String saveImageToStorage(String uploadDirectory, MultipartFile imageFile) throws IOException {
        String uniqueFileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();

        Path uploadPath = Path.of(uploadDirectory);
        Path filePath = uploadPath.resolve(uniqueFileName);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Files.copy(imageFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }

    public byte[] getImage(String imageDirectory, String imageName) throws IOException {
        Path imagePath = Path.of(imageDirectory, imageName);

        if (Files.exists(imagePath)) {
            byte[] imageBytes = Files.readAllBytes(imagePath);
            return imageBytes;
        } else {
            return null; // Handle missing images
        }
    }
}
//@Service
//public class ImageService {
//
//    private final StorageService storageService;
//
//    public ImageService(StorageService storageService) {
//        this.storageService = storageService;
//    }
//
//    public Path saveImage(MultipartFile file, String filename) throws Exception {
//        // TODO: handle upload in a separate directory + convert image to jpeg or webp +
//        // crop images, ...
//        if (!Arrays.asList(ALLOWED_CONTENT_TYPE).contains(file.getContentType()))
//            throw new Exception("Unsupported image content type");
//        if(!Objects.equals(file.getContentType(), JPG_CONTENT_TYPE)) {
////            ConvertImageToJPG(file.getInputStream());
//        }
//
//        return storageService.saveFile(file, filename);
//    }
//}



//@Service
//@RequiredArgsConstructor
//public class ImageService {
//
//   // private final ImageRepository imageRepository;
//
//    public String uploadImage(MultipartFile imageFile) throws IOException {
//        var imageToSave = Image.builder()
//                .name(imageFile.getOriginalFilename())
//                .type(imageFile.getContentType())
//                .imageData(ImageUtils.compressImage(imageFile.getBytes()))
//                .build();
//        imageRepository.save(imageToSave);
//        return "file uploaded successfully : " + imageFile.getOriginalFilename();
//    }
//
//    public byte[] downloadImage(String imageName) {
//        Optional<Image> dbImage = imageRepository.findByName(imageName);
//
//        return dbImage.map(image -> {
//            try {
//                return ImageUtils.decompressImage(image.getImageData());
//            } catch (DataFormatException | IOException exception) {
//                throw new ContextedRuntimeException("Error downloading an image", exception)
//                        .addContextValue("Image ID",  image.getId())
//                        .addContextValue("Image name", imageName);
//            }
//        }).orElse(null);
//    }
//}
