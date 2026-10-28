package com.example.photoboothstudio.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * EmailSender gọi Cloud Function sendOtpEmail để gửi mã OTP 6 số qua Gmail.
 */
public class EmailSender {

    public interface EmailCallback {
        void onSuccess();

        void onError(@NonNull String error);
    }

    private static final String TAG = "EmailSender";
    private static final String CLOUD_FUNCTION_URL = "https://us-central1-photobooth-studio-5bea1.cloudfunctions.net/sendOtpEmail";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private EmailSender() {
    }

    public static void sendOtpEmail(String recipientEmail, @NonNull EmailCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(CLOUD_FUNCTION_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(12000);
                conn.setReadTimeout(12000);

                JSONObject payload = new JSONObject();
                payload.put("email", recipientEmail.trim());

                byte[] body = payload.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(body);
                    os.flush();
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == 200 || responseCode == 201) {
                    Log.d(TAG, "Gửi yêu cầu OTP qua Cloud Function thành công!");
                    mainHandler.post(callback::onSuccess);
                } else {
                    InputStream errStream = conn.getErrorStream();
                    String errText = "";
                    if (errStream != null) {
                        Scanner s = new Scanner(errStream).useDelimiter("\\A");
                        errText = s.hasNext() ? s.next() : "";
                    }
                    Log.e(TAG, "Lỗi HTTP Cloud Function " + responseCode + ": " + errText);
                    final String detailError = errText;
                    mainHandler.post(() -> callback.onError("Lỗi Server (" + responseCode + "): " + (detailError.isEmpty() ? "Gửi thất bại" : detailError)));
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Lỗi kết nối Cloud Function", e);
                mainHandler.post(() -> callback.onError("Không thể kết nối máy chủ gửi thư. Vui lòng kiểm tra lại mạng!"));
            }
        });
    }
}
