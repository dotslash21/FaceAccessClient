// Copyright 2018 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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

    private final FirebaseVisionFaceDetector detector;

    public FaceIdentificationProcessor(Context context, BackendConnectionManager backendConnectionManager) {
        this.mContext = context;

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
                // Cuts out the bounding box around the face.
                int width = face.getBoundingBox().width();
                int height = face.getBoundingBox().height();
                int centerX = face.getBoundingBox().centerX();
                int centerY = face.getBoundingBox().centerY();
                int x = centerX - (width / 2);
                int y = centerY - (height / 2);

                Log.d(TAG, "CUTOUT: "+x+" "+y+" "+width+" "+height);

                Bitmap frame = Bitmap.createBitmap(originalCameraImage, x, y, width, height);

                int result = backendConnectionManager.pushFrame(frame);

                if (result == 1) {
                    frameCollectionDone = true;
                    backendConnectionManager.authenticateFace(this.mContext);
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
