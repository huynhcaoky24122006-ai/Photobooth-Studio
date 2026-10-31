package com.example.photoboothstudio.features.editor;

import android.view.MotionEvent;
import android.view.View;

/**
 * StickerTouchListener - cho phép kéo, xoay và phóng to/thu nhỏ sticker
 * bằng 1 hoặc 2 ngón tay.
 */
public class StickerTouchListener implements View.OnTouchListener {

    private static final float MIN_SCALE = 0.25f;
    private static final float MAX_SCALE = 4.0f;

    private final View target;

    private float lastRawX;
    private float lastRawY;
    private float lastDistance;
    private float lastAngle;
    private float lastMidX;
    private float lastMidY;
    private float accumulatedScale = 1f;
    private float accumulatedRotation = 0f;

    public StickerTouchListener(View target) {
        this.target = target;
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                target.bringToFront();
                target.setAlpha(0.85f);
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                lastDistance = 0f;
                return true;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    lastDistance = distance(event);
                    lastAngle = angle(event);
                    lastMidX = midX(event);
                    lastMidY = midY(event);
                }
                return true;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    handleTwoFingerMove(event);
                } else {
                    handleOneFingerMove(event);
                }
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                // Chuyển về thao tác 1 ngón mà không bị nhảy vị trí.
                lastRawX = event.getRawX();
                lastRawY = event.getRawY();
                lastDistance = 0f;
                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                target.setAlpha(1f);
                lastDistance = 0f;
                return true;

            default:
                return false;
        }
    }

    private void handleOneFingerMove(MotionEvent event) {
        float rawX = event.getRawX();
        float rawY = event.getRawY();
        target.setTranslationX(target.getTranslationX() + (rawX - lastRawX));
        target.setTranslationY(target.getTranslationY() + (rawY - lastRawY));
        lastRawX = rawX;
        lastRawY = rawY;
    }

    private void handleTwoFingerMove(MotionEvent event) {
        float distance = distance(event);
        float angle = angle(event);
        float midX = midX(event);
        float midY = midY(event);

        if (lastDistance > 0f) {
            float factor = distance / lastDistance;
            accumulatedScale = clamp(accumulatedScale * factor, MIN_SCALE, MAX_SCALE);
            target.setScaleX(accumulatedScale);
            target.setScaleY(accumulatedScale);

            float deltaAngle = angle - lastAngle;
            while (deltaAngle > 180f) {
                deltaAngle -= 360f;
            }
            while (deltaAngle < -180f) {
                deltaAngle += 360f;
            }
            accumulatedRotation += deltaAngle;
            target.setRotation(accumulatedRotation);

            target.setTranslationX(target.getTranslationX() + (midX - lastMidX));
            target.setTranslationY(target.getTranslationY() + (midY - lastMidY));
        }

        lastDistance = distance;
        lastAngle = angle;
        lastMidX = midX;
        lastMidY = midY;
    }

    private static float distance(MotionEvent event) {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);
        return (float) Math.hypot(dx, dy);
    }

    private static float angle(MotionEvent event) {
        float dx = event.getX(1) - event.getX(0);
        float dy = event.getY(1) - event.getY(0);
        return (float) Math.toDegrees(Math.atan2(dy, dx));
    }

    private static float midX(MotionEvent event) {
        // getRawX(int) chỉ có từ API 29, nên quy đổi pointer 1 về toạ độ raw từ pointer 0.
        return event.getRawX() + (event.getX(1) - event.getX(0)) / 2f;
    }

    private static float midY(MotionEvent event) {
        return event.getRawY() + (event.getY(1) - event.getY(0)) / 2f;
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
}
