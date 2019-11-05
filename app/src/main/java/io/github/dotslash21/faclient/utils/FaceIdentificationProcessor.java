// Copyright 2018 Google LLC
//
// Modifications copyright 2019 Arunangshu Biswas
//
// The following corresponding commit hashes arranged in chronological order
// documents the modifications done by the modification copyright holder to
// this file.
//
// Commits: bb8203e65edf67b4adcb5fafe58408b992740687
//          f40f14d7d7bbdfa27fd68c2a57816ef24d41187d
//          ffb60eccae0b288560bbcf582c3f436b20dbad20
//          46864dbe985fd97469669b465839c64b05d0becb
//          cc2b1958c69d666aff56d9f65b56743559bebc95
//
// This file contains original code under the Apache License Version 2.0 and
// the modified code under the MIT Licence.
// You may not use this file except in compliance with the License of both
// original and modified code.
// You may obtain a copy of the Apache Version 2.0 License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// and the MIT Licence at
//
//      https://opensource.org/licenses/MIT

package io.github.dotslash21.faclient.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

import io.github.dotslash21.faclient.utils.CameraImageGraphic;
import io.github.dotslash21.faclient.utils.FrameMetadata;
import io.github.dotslash21.faclient.utils.GraphicOverlay;
import io.github.dotslash21.faclient.utils.VisionProcessorBase;
import io.github.dotslash21.faclient.utils.FaceIdentificationGraphic;

public class FaceIdentificationProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {
    private static final String TAG = "FaceIdentificationProc";
    private Context mContext;
    private BackendConnectionManager backendConnectionManager;
    private boolean frameCollectionDone = false;
    private float detectionThreshold;

    private final FirebaseVisionFaceDetector detector;

    public FaceIdentificationProcessor(Context context, BackendConnectionManager backendConnectionManager, float detectionThreshold) {
        this.mContext = context;
        this.detectionThreshold = detectionThreshold;

        this.backendConnectionManager = backendConnectionManager;

        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
                        .setContourMode(FirebaseVisionFaceDetectorOptions.ALL_CONTOURS)
                        .setMinFaceSize(0.7f)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Log.e(TAG, "Exception thrown while trying to close Face Identification Processor: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {
        graphicOverlay.clear();
        if (originalCameraImage != null) {
            CameraImageGraphic imageGraphic = new CameraImageGraphic(graphicOverlay, originalCameraImage);
            graphicOverlay.add(imageGraphic);
        }

        if (faces.size() < 1) {
//            Toast.makeText(this.mContext, "No faces detected!", Toast.LENGTH_SHORT).show();
        } else if (faces.size() > 1) {
//            Toast.makeText(this.mContext, "Multiple faces detected!", Toast.LENGTH_SHORT).show();

            for (int i = 0; i < faces.size(); ++i) {
                FirebaseVisionFace face = faces.get(i);
                FaceIdentificationGraphic faceGraphic = new FaceIdentificationGraphic(graphicOverlay, face);
                graphicOverlay.add(faceGraphic);
            }
        } else {
            FirebaseVisionFace face = faces.get(0);
            FaceIdentificationGraphic faceGraphic = new FaceIdentificationGraphic(graphicOverlay, face);
            graphicOverlay.add(faceGraphic);

            if (!frameCollectionDone) {
                // Get image dimensions
                int origImgHeight = originalCameraImage.getHeight();
                int origImgWidth = originalCameraImage.getWidth();

                // Cuts out the bounding box around the face.
                int width = face.getBoundingBox().width();
                int height = face.getBoundingBox().height();
                int centerX = face.getBoundingBox().centerX();
                int centerY = face.getBoundingBox().centerY();
                int x = centerX - (width / 2);
                int y = centerY - (height / 2);

                if (x >= 0 && y >= 0 && x + width <= origImgWidth && y + height <= origImgHeight) {
                    Log.d(TAG, "CUTOUT: "+x+" "+y+" "+width+" "+height);

                    Bitmap frame = Bitmap.createBitmap(originalCameraImage, x, y, width, height);

                    int result = backendConnectionManager.pushFrame(frame);

                    if (result == 1) {
                        frameCollectionDone = true;
                        backendConnectionManager.authenticateFace(this.mContext, this.detectionThreshold);
                    }
                }
            }
        }

        graphicOverlay.postInvalidate();
    }

    @Override
    protected void onFailure(@NonNull Exception e) {
        Log.e(TAG, "Face identification failed " + e);
    }
}
