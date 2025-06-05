package com.carshare.rentalsystem.service.aws.s3;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public interface S3StorageService {
    List<String> uploadMedia(Long rentId, List<MultipartFile> files);

    List<String> getMedia(List<String> keys);

    void deleteMedia(List<String> s3Keys);
}
