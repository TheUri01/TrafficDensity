package com.example.trafficdensity.ui.fullscreen; // Đảm bảo đúng package của bạn

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.example.trafficdensity.util.Constants;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager; // Để ẩn thanh trạng thái
import android.view.View; // Để ẩn thanh điều hướng
import android.widget.TextView; // Import TextView
import android.widget.ImageView; // Import ImageView
import android.content.Intent; // Import Intent
import android.os.Build; // Import android.os.Build
import android.view.Window; // Import android.view.Window
import android.view.WindowInsetsController; // Import android.view.WindowInsetsController
import android.os.Handler; // Import Handler
import android.os.Looper; // Import Looper

import com.bumptech.glide.Glide; // Thư viện tải ảnh
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Để cấu hình cache của Glide
import com.bumptech.glide.request.target.CustomTarget; // Import CustomTarget
import com.bumptech.glide.request.transition.Transition; // Import Transition
import android.graphics.drawable.Drawable; // Import Drawable


// Import key từ DialogFragment
import com.example.trafficdensity.ui.dialog.CameraImageDialogFragment;

import com.example.trafficdensity.R; // Import R class của ứng dụng

import java.io.Serializable;
import java.util.Locale; // Để định dạng số float
import java.util.concurrent.TimeUnit; // Để dễ đọc thời gian
import java.util.ArrayList; // Import ArrayList

// --- Import cho API Call (Vẫn cần để cập nhật mật độ) ---
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService; // Import interface API service
import api.TrafficDensityReading; // Import data class
import api.Detection; // <-- Import lớp Detection
import java.util.List;
// ---------------------------

// --- Import Custom View để vẽ overlay ---

// ---------------------------------------


public class FullScreenImageActivity extends AppCompatActivity {

    private static final String TAG = "FullScreenImageAct";

    // TextViews để hiển thị thông tin
    private TextView textFullscreenCameraName;
    private TextView textFullscreenDensity;
    private TextView textFullscreenSummary;

    // ImageView để hiển thị ảnh
    private ImageView fullScreenImageView;

    // --- OverlayView để vẽ bounding box ---
    private OverlayView fullScreenOverlayView; // <-- Biến cho OverlayView
    // ---------------------------------------


    // --- Biến để lưu trữ dữ liệu nhận được từ Intent ---
    private String cameraName;
    private String cameraId;
    private String currentImageUrl;
    private String baseApiUrl; // Biến để lưu BASE_API_URL nhận từ Intent
    // --- Biến để lưu trữ danh sách detections nhận được từ Intent ---
    private List<Detection> initialDetections = new ArrayList<>(); // <-- Lưu detections ban đầu
    // --------------------------------------------------

    // --- Biến để lưu trữ dữ liệu mật độ và summary mới nhất nhận được từ API ---
    private float currentDensity = 0.0f;
    private String currentSummary = "Đang tải...";
    // --- Biến để lưu trữ danh sách detections mới nhất nhận được từ API (nếu cần cập nhật) ---
    // Nếu API GET trả về detections, bạn có thể cập nhật OverlayView ở đây
    private List<Detection> currentDetections = new ArrayList<>(); // <-- Lưu detections mới nhất
    // -----------------------------------------------------------------------

    // --- Handler và Runnable cho cập nhật định kỳ ---
    // Sử dụng Looper.getMainLooper() để đảm bảo Handler chạy trên Main Thread
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateDensityRunnable;
    private static final long UPDATE_DENSITY_INTERVAL_MS = Constants.UPDATE_DENSITY_INTERVAL_MS; // Cập nhật mỗi 15 giây
    // ----------------------------------------------

    // --- API Service ---
    private TrafficApiService trafficApiService;
    // -------------------

    // --- Kích thước ảnh gốc (cần để scale bounding box) ---
    // LÝ TƯỞNG: Kích thước này nên được backend trả về cùng với detections
    // --- CẬP NHẬT KÍCH THƯỚC ẢNH GỐC ---
    // Giả định kích thước này là cố định cho tất cả ảnh từ backend
    private int originalImageWidth = 512; // <-- CẦN KHỚP VỚI KÍCH THƯỚC ẢNH GỐC THỰC TẾ
    private int originalImageHeight = 288; // <-- CẦN KHỚP VỚI KÍCH THƯỚC ẢNH GỐC THỰC TẾ
    // ----------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate"); // Log onCreate

        // --- DI CHUYỂN setContentView() LÊN ĐÂY ---
        // Thiết lập layout cho Activity. Nên gọi sớm trong onCreate.
        setContentView(R.layout.activity_full_screen_image);
        // ------------------------------------------

        // --- ĐĂNG LOGIC ẨN UI LÊN HANDLER ---
        // Chạy logic ẩn thanh trạng thái/điều hướng sau một khoảng thời gian ngắn
        // để đảm bảo Window đã được khởi tạo đầy đủ.
        handler.post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Executing UI hiding logic from Handler.");
                Window window = getWindow(); // Lấy Window

                if (window != null) { // Kiểm tra null cho Window
                    window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);

                    // Đối với Android 11 (R) trở lên, sử dụng WindowInsetsController
                    // Sử dụng Build.VERSION_CODES.R để kiểm tra tương thích ngược
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        WindowInsetsController insetsController = window.getInsetsController(); // Lấy InsetsController từ Window
                        if (insetsController != null) { // Kiểm tra null cho InsetsController
                            insetsController.hide(android.view.WindowInsets.Type.statusBars() | android.view.WindowInsets.Type.navigationBars());
                            Log.d(TAG, "System bars hidden using WindowInsetsController.");
                        } else {
                            Log.w(TAG, "WindowInsetsController is null on Android R+ (Handler).");
                        }
                    } else {
                        // Đối với các phiên bản cũ hơn
                        window.getDecorView().setSystemUiVisibility(
                                View.SYSTEM_UI_FLAG_FULLSCREEN
                                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                        Log.d(TAG, "System bars hidden using setSystemUiVisibility.");
                    }
                } else {
                    Log.e(TAG, "Window object is null in Handler for UI hiding!"); // Log lỗi nếu Window là null
                }
            }
        });
        // -------------------------------------


        // --- Các dòng findViewById() ---
        fullScreenImageView = findViewById(R.id.full_screen_image_view);
        fullScreenOverlayView = findViewById(R.id.full_screen_overlay_view); // <-- Find OverlayView
        // Tìm các TextView trong layout
        textFullscreenCameraName = findViewById(R.id.text_fullscreen_camera_name);
        textFullscreenDensity = findViewById(R.id.text_fullscreen_density);
        textFullscreenSummary = findViewById(R.id.text_fullscreen_summary);
        // --------------------------------


        // --- Lấy dữ liệu từ Intent ---
        Intent intent = getIntent();
        currentImageUrl = intent.getStringExtra(CameraImageDialogFragment.EXTRA_IMAGE_URL);
        cameraName = intent.getStringExtra(CameraImageDialogFragment.EXTRA_CAMERA_NAME);
        // Lấy mật độ và summary ban đầu (có thể đã cũ, sẽ được cập nhật bằng API)
        currentDensity = intent.getFloatExtra(CameraImageDialogFragment.EXTRA_DENSITY, 0.0f);
        currentSummary = intent.getStringExtra(CameraImageDialogFragment.EXTRA_SUMMARY);
        // Lấy Camera ID và BASE_API_URL (cần cho cuộc gọi API)
        // Sử dụng ARG_CAMERA_ID đã public
        cameraId = intent.getStringExtra(CameraImageDialogFragment.ARG_CAMERA_ID);
        baseApiUrl = intent.getStringExtra(CameraImageDialogFragment.EXTRA_BASE_API_URL);
        // --- LẤY DANH SÁCH DETECTIONS TỪ INTENT ---
        // Sử dụng getSerializableExtra và cast sang ArrayList<Detection>
        Serializable detectionsExtra = intent.getSerializableExtra(CameraImageDialogFragment.EXTRA_DETECTIONS);
        if (detectionsExtra instanceof ArrayList) {
            initialDetections = (ArrayList<Detection>) detectionsExtra;
            Log.d(TAG, "Received " + initialDetections.size() + " initial detections from Intent.");
        } else {
            Log.w(TAG, "No initial detections received from Intent or data format is incorrect.");
            initialDetections = new ArrayList<>(); // Đảm bảo không null
        }
        currentDetections = initialDetections; // Gán detections ban đầu cho biến currentDetections
        // -----------------------------

        // --- Hiển thị dữ liệu ban đầu lên TextViews ---
        // Dữ liệu này có thể hơi cũ, sẽ được cập nhật sau khi fetch API lần đầu
        if (cameraName != null) {
            textFullscreenCameraName.setText("Camera: " + cameraName);
        } else {
            textFullscreenCameraName.setText("Camera Name: N/A");
        }

        // Định dạng và hiển thị mật độ ban đầu
        textFullscreenDensity.setText(String.format(Locale.US, "Mật độ hiện tại: %.2f", currentDensity));

        // Hiển thị summary ban đầu
        if (currentSummary != null && !currentSummary.isEmpty()) {
            textFullscreenSummary.setText("Summary: " + currentSummary);
        } else {
            textFullscreenSummary.setText("Summary: Không có thông tin");
        }
        // ---------------------------------------------


        // --- Tải ảnh vào ImageView bằng Glide ---
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Log.d(TAG, "Loading full screen image from URL: " + currentImageUrl);
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.ic_menu_camera) // Placeholder khi đang tải
                    .error(R.drawable.ic_menu_gallery) // Ảnh khi lỗi tải
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Không cache ảnh tĩnh vì nó thay đổi
                    .skipMemoryCache(true) // Bỏ qua cache bộ nhớ
                    // --- Sử dụng CustomTarget để lấy kích thước ảnh sau khi tải ---
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            fullScreenImageView.setImageDrawable(resource);
                            Log.d(TAG, "Image loaded successfully into ImageView.");

                            // --- Cập nhật OverlayView với detections và kích thước ảnh gốc ---
                            // Kích thước hiển thị của ImageView chỉ có sau khi layout được đo đạc.
                            // Sử dụng post để chạy sau khi layout hoàn thành.
                            fullScreenImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (fullScreenOverlayView != null) {
                                        // Truyền detections (ban đầu nhận từ Intent)
                                        // và kích thước ảnh gốc đã set.
                                        // OverlayView sẽ tự tính toán scaling dựa trên kích thước hiển thị của nó
                                        // và kích thước ảnh gốc.
                                        Log.d(TAG, "Updating OverlayView after image load (count: " + currentDetections.size() + ") with original size: " + originalImageWidth + "x" + originalImageHeight);
                                        fullScreenOverlayView.setDetections(currentDetections, originalImageWidth, originalImageHeight);
                                        Log.d(TAG, "OverlayView updated after image load.");
                                    } else {
                                        Log.w(TAG, "OverlayView is null when attempting to update after image load.");
                                    }
                                }
                            });
                            // ----------------------------------------------------------
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            Log.d(TAG, "Image load cleared.");
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.e(TAG, "Image load FAILED.");
                            fullScreenImageView.setImageDrawable(errorDrawable);
                        }
                    });
        } else {
            Log.e(TAG, "Image URL is null or empty. Cannot load image.");
            // Hiển thị ảnh lỗi hoặc thông báo
            fullScreenImageView.setImageResource(R.drawable.ic_menu_gallery);
        }
        // ----------------------------------------------------

        // --- Khởi tạo Retrofit và API Service ---
        // Kiểm tra BASE_API_URL trước khi khởi tạo Retrofit
        if (baseApiUrl != null && !baseApiUrl.isEmpty() && !baseApiUrl.equals("YOUR_NGROK_URL_HERE")) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(baseApiUrl) // Đặt Base URL của API từ Intent
                    .addConverterFactory(GsonConverterFactory.create()) // Thêm converter để xử lý JSON (cần thư viện Gson)
                    .build();

            trafficApiService = retrofit.create(TrafficApiService.class); // Tạo instance của API service
            Log.d(TAG, "Retrofit and TrafficApiService initialized in FullScreenImageActivity.");
        } else {
            Log.e(TAG, "BASE_API_URL is null, empty, or not updated in Intent. Cannot initialize API service.");
            // Xử lý trường hợp không có API URL (ví dụ: hiển thị thông báo lỗi)
            if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Lỗi API URL");
            if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Lỗi API URL");
            trafficApiService = null; // Đảm bảo service là null nếu không khởi tạo được
        }
        // ---------------------------------------

        // --- Khởi tạo Runnable để cập nhật dữ liệu mật độ ---
        updateDensityRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Executing updateDensityRunnable. Attempting to fetch density data."); // Log khi Runnable chạy
                fetchDensityData(); // Lấy dữ liệu mật độ mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian xác định
                handler.postDelayed(this, UPDATE_DENSITY_INTERVAL_MS);
                Log.d(TAG, "updateDensityRunnable rescheduled for " + UPDATE_DENSITY_INTERVAL_MS + "ms."); // Log khi Runnable được reschedule
            }
        };
        // ----------------------------------------------------

        // --- Lấy dữ liệu mật độ lần đầu ngay sau khi onCreate hoàn thành ---
        // Điều này đảm bảo dữ liệu hiển thị là mới nhất ngay khi Activity mở
        if (trafficApiService != null && cameraId != null) {
            Log.d(TAG, "Triggering initial density fetch in FullScreenActivity."); // Log khi fetch ban đầu được gọi
            fetchDensityData();
        } else {
            Log.e(TAG, "Cannot trigger initial density fetch: API service or Camera ID is null.");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart"); // Log onStart
        // Bắt đầu chu kỳ cập nhật mật độ định kỳ khi Activity hiển thị
        // Runnable đã được khởi tạo trong onCreate
        if (updateDensityRunnable != null && trafficApiService != null && cameraId != null) {
            // Bắt đầu hẹn giờ sau khoảng thời gian UPDATE_DENSITY_INTERVAL_MS
            // Kiểm tra nếu Runnable đã được post trước đó để tránh double-posting
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (!handler.hasCallbacks(updateDensityRunnable)) {
                    handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
                    Log.d(TAG, "Density update runnable started in onStart."); // Log khi Runnable được start
                } else {
                    Log.d(TAG, "Density update runnable already scheduled, skipping postDelayed in onStart."); // Log nếu đã scheduled
                }
            }
        } else {
            Log.e(TAG, "Cannot start density update runnable in onStart: Runnable, API service, or Camera ID is null.");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume"); // Log onResume
        // Nếu Activity bị tạm dừng và tiếp tục, đảm bảo hẹn giờ được chạy lại
        // (Kiểm tra thêm để tránh chạy lại nếu đã chạy trong onStart)
        // if (updateDensityRunnable != null && trafficApiService != null && cameraId != null && !handler.hasCallbacks(updateDensityRunnable)) {
        //    handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
        //    Log.d(TAG, "Density update runnable resumed in onResume.");
        // }
    }


    @Override
    protected void onPause() {
        super.onPause( );
        Log.d(TAG, "onPause"); // Log onPause
        // Dừng cập nhật khi Activity bị tạm dừng
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable stopped in onPause."); // Log khi Runnable bị stop
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop"); // Log onStop
        // Đảm bảo dừng Runnable khi Activity không còn hiển thị
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable stopped in onStop."); // Log khi Runnable bị stop
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy"); // Log onDestroy
        // Đảm bảo dừng Runnable nếu Activity bị hủy
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable removed in onDestroy."); // Log khi Runnable bị remove
        // Giải phóng API service nếu cần (tùy cách quản lý lifecycle)
        trafficApiService = null;
        fullScreenImageView = null; // Giải phóng ImageView
        fullScreenOverlayView = null; // Giải phóng OverlayView
        // Giải phóng TextViews
        textFullscreenCameraName = null;
        textFullscreenDensity = null;
        textFullscreenSummary = null;
    }


    // --- PHƯƠNG THỨC ĐỂ LẤY DỮ LIỆU MẬT ĐỘ TỪ API ---
    private void fetchDensityData() {
        // Kiểm tra xem API service và camera ID đã sẵn sàng chưa
        if (trafficApiService == null || cameraId == null) {
            Log.e(TAG, "API service or Camera ID is null, cannot fetch density data.");
            // Tùy chọn: Hiển thị trạng thái lỗi trên TextViews
            if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Lỗi");
            if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Lỗi"); // Cập nhật TextView summary
            currentDensity = 0.0f; // Reset giá trị
            currentSummary = "Lỗi tải";
            currentDetections = new ArrayList<>(); // Clear detections on error
            // --- Cập nhật OverlayView khi có lỗi ---
            if (fullScreenOverlayView != null) {
                fullScreenOverlayView.setDetections(currentDetections);
                Log.d(TAG, "Cleared detections in OverlayView due to API service/Camera ID null.");
            } else {
                Log.w(TAG, "OverlayView is null when attempting to clear detections on error.");
            }
            // -------------------------------------
            return;
        }
        if (baseApiUrl == null || baseApiUrl.isEmpty() || baseApiUrl.equals("YOUR_NGROK_URL_HERE")) {
            Log.e(TAG, "BASE_API_URL is null, empty, or not updated! Cannot fetch data.");
            if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Lỗi URL");
            if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Lỗi URL"); // Cập nhật TextView summary
            currentDensity = 0.0f; // Reset giá trị
            currentSummary = "Lỗi URL";
            currentDetections = new ArrayList<>(); // Clear detections on error
            // --- Cập nhật OverlayView khi có lỗi ---
            if (fullScreenOverlayView != null) {
                fullScreenOverlayView.setDetections(currentDetections);
                Log.d(TAG, "Cleared detections in OverlayView due to BASE_API_URL not updated.");
            } else {
                Log.w(TAG, "OverlayView is null when attempting to clear detections on URL error.");
            }
            // -------------------------------------
            return;
        }


        // Tạo cuộc gọi API để lấy dữ liệu cho camera ID cụ cụ thể này
        // Sử dụng tham số query 'camera_ids' để chỉ yêu cầu dữ liệu của 1 camera
        Call<List<TrafficDensityReading>> call = trafficApiService.getLatestTrafficDensities(cameraId); // Truyền camera ID

        Log.d(TAG, "Fetching density data for camera ID: " + cameraId + " from API (FullScreenActivity)..."); // Log khi bắt đầu fetch

        // Thực hiện cuộc gọi API bất đồng bộ
        call.enqueue(new Callback<List<TrafficDensityReading>>() {
            @Override
            public void onResponse(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Response<List<TrafficDensityReading>> response) {
                Log.d(TAG, "API Call Response received (FullScreenActivity)."); // Log khi nhận response
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficDensityReading> densityReadings = response.body();
                    Log.d(TAG, "API Call Successful (FullScreenActivity). Received " + densityReadings.size() + " readings."); // Log thành công

                    // API trả về một danh sách, nhưng vì chúng ta yêu cầu theo 1 ID,
                    // danh sách này chỉ nên chứa 0 hoặc 1 phần tử.
                    if (!densityReadings.isEmpty()) {
                        TrafficDensityReading reading = densityReadings.get(0);
                        // Log dữ liệu nhận được để debug, bao gồm cả summary và detections count
                        Log.d(TAG, "Received data for " + reading.getCameraId() +
                                ": Density=" + reading.getDensity() +
                                ", Summary='" + reading.getSummary() + "'" +
                                ", Detections Count: " + (reading.getDetections() != null ? reading.getDetections().size() : 0)); // Log detections count

                        // --- Log nội dung của detections (chỉ vài mục đầu tiên để tránh log quá dài) ---
                        if (reading.getDetections() != null && !reading.getDetections().isEmpty()) {
                            int logLimit = Math.min(reading.getDetections().size(), 5); // Log tối đa 5 detections
                            for (int i = 0; i < logLimit; i++) {
                                Detection det = reading.getDetections().get(i);
                                Log.d(TAG, "  Detection " + i + ": Box=" + det.getBox() + ", Class=" + det.getClassId());
                            }
                            if (reading.getDetections().size() > logLimit) {
                                Log.d(TAG, "  ... and " + (reading.getDetections().size() - logLimit) + " more detections.");
                            }
                        } else {
                            Log.d(TAG, "  No detections received in the response body.");
                        }
                        // ---------------------------------------------------------------------------


                        // --- Lưu trữ dữ liệu nhận được vào biến thành viên ---
                        currentDensity = reading.getDensity();
                        currentSummary = reading.getSummary();
                        currentDetections = reading.getDetections() != null ? reading.getDetections() : new ArrayList<>(); // <-- LƯU DETECTIONS MỚI NHẤT
                        // --------------------------------------------------

                        // --- Cập nhật TextViews và OverlayView với dữ liệu mới ---
                        if (textFullscreenDensity != null) {
                            // Định dạng số float cho dễ đọc (sử dụng Locale.US để đảm bảo dấu chấm thập phân)
                            textFullscreenDensity.setText(String.format(Locale.US, "Mật độ hiện tại: %.2f", currentDensity));
                        }
                        if (textFullscreenSummary != null) { // Cần thêm TextView cho summary nếu muốn hiển thị
                            String[] temp = currentSummary.split(";");
                            textFullscreenSummary.setText("Số lượng: " + "\n" + temp[0] + "\n" + temp[1] + "\n" + temp[2] + "\n" + temp[3]);
                        }
                        // Cập nhật OverlayView với danh sách detections mới
                        if (fullScreenOverlayView != null) {
                            // Truyền detections mới nhận được và kích thước ảnh gốc đã set.
                            // Kích thước hiển thị của ImageView sẽ được OverlayView tự lấy.
                            Log.d(TAG, "Updating OverlayView with new detections (count: " + currentDetections.size() + ") and original size: " + originalImageWidth + "x" + originalImageHeight);
                            fullScreenOverlayView.setDetections(currentDetections, originalImageWidth, originalImageHeight);
                            Log.d(TAG, "OverlayView updated with new detections.");
                        } else {
                            Log.w(TAG, "OverlayView is null when attempting to update with new detections.");
                        }
                        // ---------------------------------------------

                        // --- Tải ảnh mới nếu image_path thay đổi (Tùy chọn) ---
                        // Nếu backend trả về image_path mới, bạn có thể tải ảnh mới ở đây
                        // String newImageUrl = reading.getImagePath(); // Giả định có getter cho imagePath
                        // if (newImageUrl != null && !newImageUrl.isEmpty() && !newImageUrl.equals(currentImageUrl)) {
                        //     currentImageUrl = newImageUrl;
                        //     loadImage(); // Tải ảnh mới
                        // }
                        // ----------------------------------------------------


                    } else {
                        Log.w(TAG, "API Call Successful but no data received for camera ID: " + cameraId + " (FullScreenActivity)."); // Log không có data
                        // Tùy chọn: Hiển thị trạng thái "Không có dữ liệu"
                        if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Không có dữ liệu");
                        if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Không có dữ liệu"); // Cập nhật TextView summary
                        currentDensity = 0.0f; // Reset giá trị
                        currentSummary = "Không có dữ liệu";
                        currentDetections = new ArrayList<>(); // Clear detections
                        // --- Cập nhật OverlayView khi không có dữ liệu ---
                        if (fullScreenOverlayView != null) {
                            fullScreenOverlayView.setDetections(currentDetections);
                            Log.d(TAG, "Cleared detections in OverlayView due to no data received.");
                        } else {
                            Log.w(TAG, "OverlayView is null when attempting to clear detections (no data).");
                        }
                        // ---------------------------------------------
                    }

                } else {
                    // API trả về lỗi (ví dụ: 404 Not Found, 500 Internal Server Error)
                    Log.e(TAG, "API Call Failed (FullScreenActivity). Response code: " + response.code() + ", Message: " + response.message()); // Log lỗi response
                    // Tùy chọn: Hiển thị trạng thái lỗi
                    if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Lỗi tải (" + response.code() + ")");
                    if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Lỗi tải (" + response.code() + ")"); // Cập nhật TextView summary
                    currentDensity = 0.0f; // Reset giá trị
                    currentSummary = "Lỗi tải";
                    currentDetections = new ArrayList<>(); // Clear detections
                    // --- Cập nhật OverlayView khi API lỗi ---
                    if (fullScreenOverlayView != null) {
                        fullScreenOverlayView.setDetections(currentDetections);
                        Log.d(TAG, "Cleared detections in OverlayView due to API call failed.");
                    } else {
                        Log.w(TAG, "OverlayView is null when attempting to clear detections (API failed).");
                    }
                    // -------------------------------------
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Throwable t) {
                // Lỗi mạng hoặc lỗi khác (ví dụ: không kết nối được đến server)
                Log.e(TAG, "API Call Failed (Network Error - FullScreenActivity): " + t.getMessage(), t); // Log lỗi mạng
                // Tùy chọn: Hiển thị trạng thái lỗi mạng
                if (textFullscreenDensity != null) textFullscreenDensity.setText("Mật độ hiện tại: Lỗi mạng");
                if (textFullscreenSummary != null) textFullscreenSummary.setText("Summary: Lỗi mạng"); // Cập nhật TextView summary
                currentDensity = 0.0f; // Reset giá trị
                currentSummary = "Lỗi mạng";
                currentDetections = new ArrayList<>(); // Clear detections
                // --- Cập nhật OverlayView khi lỗi mạng ---
                if (fullScreenOverlayView != null) {
                    fullScreenOverlayView.setDetections(currentDetections);
                    Log.d(TAG, "Cleared detections due to network error.");
                } else {
                    Log.w(TAG, "OverlayView is null when attempting to clear detections (network error).");
                }
                // -------------------------------------
            }
        });
    }
    // -------------------------------------------------------------

    // Optional: Override onBackPressed để đóng Activity khi nhấn nút Back
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d(TAG, "Back button pressed. Finishing activity."); // Log khi back
        finish(); // Đóng Activity
    }

    // Phương thức tải ảnh (giữ nguyên logic từ DialogFragment)
    private void loadImage() {
        if (currentImageUrl != null && !currentImageUrl.isEmpty() && fullScreenImageView != null) {
            Log.d(TAG, "Attempting to load image: " + currentImageUrl);
            Glide.with(this)
                    .load(currentImageUrl)
                    .placeholder(R.drawable.ic_menu_camera)
                    .error(R.drawable.ic_menu_gallery)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            fullScreenImageView.setImageDrawable(resource);
                            Log.d(TAG, "Image loaded successfully into ImageView.");

                            // Cập nhật OverlayView sau khi ảnh tải xong
                            fullScreenImageView.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (fullScreenOverlayView != null) {
                                        // Sử dụng currentDetections (có thể đã được cập nhật từ API)
                                        Log.d(TAG, "Updating OverlayView after image load (count: " + currentDetections.size() + ") with original size: " + originalImageWidth + "x" + originalImageHeight);
                                        fullScreenOverlayView.setDetections(currentDetections, originalImageWidth, originalImageHeight);
                                        Log.d(TAG, "OverlayView updated after image load.");
                                    } else {
                                        Log.w(TAG, "OverlayView is null when attempting to update after image load.");
                                    }
                                }
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            Log.d(TAG, "Image load cleared.");
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            Log.e(TAG, "Image load FAILED from URL: " + currentImageUrl);
                            fullScreenImageView.setImageDrawable(errorDrawable);
                        }
                    });
        } else {
            Log.e(TAG, "currentImageUrl or ImageView is null/invalid in loadImage().");
            if (fullScreenImageView != null) {
                fullScreenImageView.setImageResource(R.drawable.ic_menu_gallery);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult"); // Log onActivityResult (nếu Activity này nhận kết quả từ Activity khác)
    }
}
