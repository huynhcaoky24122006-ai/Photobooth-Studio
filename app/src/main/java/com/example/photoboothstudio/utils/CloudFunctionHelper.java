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
 * CloudFunctionHelper gọi các Firebase Cloud Functions API trên server.
 */
public class CloudFunctionHelper {

    public interface ApiCallback {
        void onSuccess();

        void onError(@NonNull String error);
    }

    private static final String TAG = "CloudFunctionHelper";
    private static final String SEND_OTP_URL = "https://us-central1-photobooth-studio-5bea1.cloudfunctions.net/sendOtpEmail";

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    private CloudFunctionHelper() {
        // Utility class
    }

    public static void sendOtpEmail(String recipientEmail, @NonNull ApiCallback callback) {
        executor.execute(() -> {
            try {
                URL url = new URL(SEND_OTP_URL);
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
                mainHandler.post(() -> callback.onError("Không thể kết nối máy chủ. Vui lòng kiểm tra lại mạng!"));
            }
        });
    }
}
