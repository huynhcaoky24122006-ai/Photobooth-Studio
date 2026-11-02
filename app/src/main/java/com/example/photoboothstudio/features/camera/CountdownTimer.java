package com.example.photoboothstudio.features.camera;

/**
 * CountdownTimer - bộ đếm ngược dùng chung cho màn hình chụp ảnh.
 * Bọc android.os.CountDownTimer và trả về số giây còn lại dưới dạng số nguyên.
 */
public class CountdownTimer extends android.os.CountDownTimer {

    public interface Callback {
        /**
         * @param secondsRemaining số giây còn lại (1, 2, 3...)
         */
        void onTick(int secondsRemaining);

        void onFinishCountdown();
    }

    private final Callback callback;

    public CountdownTimer(long totalMillis, long intervalMillis, Callback callback) {
        super(totalMillis, intervalMillis);
        this.callback = callback;
    }

    @Override
    public void onTick(long millisUntilFinished) {
        if (callback != null) {
            callback.onTick((int) Math.ceil(millisUntilFinished / 1000.0));
        }
    }

    @Override
    public void onFinish() {
        if (callback != null) {
            callback.onFinishCountdown();
        }
    }
}
