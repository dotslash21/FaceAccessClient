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

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceContour;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceLandmark;

import io.github.dotslash21.faclient.utils.GraphicOverlay;
import io.github.dotslash21.faclient.utils.GraphicOverlay.Graphic;

public class FaceIdentificationGraphic extends Graphic {
    private static final float FACE_POSITION_RADIUS = 4.0f;
    private static final float ID_TEXT_SIZE = 30.0f;
    private static final float ID_Y_OFFSET = 80.0f;
    private static final float ID_X_OFFSET = -70.0f;
    private static final float BOX_STROKE_WIDTH = 5.0f;

    private final Paint facePositionPaint;
    private final Paint idPaint;
    private final Paint boxPaint;

    private volatile FirebaseVisionFace firebaseVisionFace;

    public FaceIdentificationGraphic(GraphicOverlay overlay, FirebaseVisionFace face) {
        super(overlay);

        this.firebaseVisionFace = face;
        final int selectedColor = Color.WHITE;

        facePositionPaint = new Paint();
        facePositionPaint.setColor(selectedColor);

        idPaint = new Paint();
        idPaint.setColor(selectedColor);
        idPaint.setTextSize(ID_TEXT_SIZE);

        boxPaint = new Paint();
        boxPaint.setColor(selectedColor);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(BOX_STROKE_WIDTH);
    }

    /**
     * Draws the face annotations for position on the supplied canvas.
     */
    @Override
    public void draw(Canvas canvas) {
        FirebaseVisionFace face = firebaseVisionFace;
        if (face == null) {
            return;
        }

        // Draws a circle at the position of the detected face, with the face's track id below.
        float x = translateX(face.getBoundingBox().centerX());
        float y = translateY(face.getBoundingBox().centerY());
        canvas.drawCircle(x, y, FACE_POSITION_RADIUS, facePositionPaint);
        canvas.drawText("id: " + face.getTrackingId(), x + ID_X_OFFSET, y + ID_Y_OFFSET, idPaint);

        // Draws a bounding box around the face.
        float xOffset = scaleX(face.getBoundingBox().width() / 2.0f);
        float yOffset = scaleY(face.getBoundingBox().height() / 2.0f);
        float left = x - xOffset;
        float top = y - yOffset;
        float right = x + xOffset;
        float bottom = y + yOffset;
        canvas.drawRect(left, top, right, bottom, boxPaint);

        FirebaseVisionFaceContour contour = face.getContour(FirebaseVisionFaceContour.ALL_POINTS);
        for (com.google.firebase.ml.vision.common.FirebaseVisionPoint point : contour.getPoints()) {
            float px = translateX(point.getX());
            float py = translateY(point.getY());
            canvas.drawCircle(px, py, FACE_POSITION_RADIUS, facePositionPaint);
        }

        if (face.getSmilingProbability() >= 0) {
            canvas.drawText(
                    "happiness: " + String.format("%.2f", face.getSmilingProbability()),
                    x + ID_X_OFFSET * 3,
                    y - ID_Y_OFFSET,
                    idPaint);
        }

        if (face.getRightEyeOpenProbability() >= 0) {
            canvas.drawText(
                    "right eye: " + String.format("%.2f", face.getRightEyeOpenProbability()),
                    x - ID_X_OFFSET,
                    y,
                    idPaint);
        }
        if (face.getLeftEyeOpenProbability() >= 0) {
            canvas.drawText(
                    "left eye: " + String.format("%.2f", face.getLeftEyeOpenProbability()),
                    x + ID_X_OFFSET * 6,
                    y,
                    idPaint);
        }
        FirebaseVisionFaceLandmark leftEye = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_EYE);
        if (leftEye != null && leftEye.getPosition() != null) {
            canvas.drawCircle(
                    translateX(leftEye.getPosition().getX()),
                    translateY(leftEye.getPosition().getY()),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
        }
        FirebaseVisionFaceLandmark rightEye = face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_EYE);
        if (rightEye != null && rightEye.getPosition() != null) {
            canvas.drawCircle(
                    translateX(rightEye.getPosition().getX()),
                    translateY(rightEye.getPosition().getY()),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
        }

        FirebaseVisionFaceLandmark leftCheek = face.getLandmark(FirebaseVisionFaceLandmark.LEFT_CHEEK);
        if (leftCheek != null && leftCheek.getPosition() != null) {
            canvas.drawCircle(
                    translateX(leftCheek.getPosition().getX()),
                    translateY(leftCheek.getPosition().getY()),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
        }
        FirebaseVisionFaceLandmark rightCheek =
                face.getLandmark(FirebaseVisionFaceLandmark.RIGHT_CHEEK);
        if (rightCheek != null && rightCheek.getPosition() != null) {
            canvas.drawCircle(
                    translateX(rightCheek.getPosition().getX()),
                    translateY(rightCheek.getPosition().getY()),
                    FACE_POSITION_RADIUS,
                    facePositionPaint);
        }
    }
}
