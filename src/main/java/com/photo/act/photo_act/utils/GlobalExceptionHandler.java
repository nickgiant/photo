package com.photo.act.photo_act.utils;

import com.photo.act.photo_act.db.RecordService;
import org.springframework.web.bind.annotation.ControllerAdvice;


@ControllerAdvice
public class GlobalExceptionHandler {

    private RecordService recordService;

//    @ExceptionHandler(MultipartException.class)
//    public void handleMultipartException(MaxUploadSizeExceededException ex, RecordService recordService) {
//        this.recordService = recordService;
//        // Custom error response
//        String errorMessage = "Error. handleMultipartException: "+ex.getMessage()+". getMaxUploadSize: "+ex.getMaxUploadSize();
//
//        recordService.logErrorInDb(ex,"","handleMultipartException",1,"","","",errorMessage);
//        //return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(errorMessage);
//    }

//    @ExceptionHandler(MultipartException.class)
//    public ResponseEntity<String> handleMultipartException(MultipartException ex) {
//        String errorMessage = "The uploaded file is too large.";
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage+"  "+ex.getMessage());
//    }

}

