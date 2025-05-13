package com.example.trafficdensity.ui.dialog; // Đảm bảo đúng package của bạn

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- Sử dụng ImageView
import android.widget.TextView;
import android.util.Log;
import android.graphics.Point;
import android.view.Display;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide; // Thư viện tải ảnh
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Để cấu hình cache của Glide

import com.example.trafficdensity.R; // Import R class của ứng dụng

import java.util.concurrent.TimeUnit; // Để dễ đọc thời gian

// --- Import cho API Call ---
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService; // Import interface API service
import api.TrafficDensityReading; // Import data class
import java.util.List;
// ---------------------------

// --- Import Activity mới cho ảnh toàn màn hình ---
 // <-- Import Activity mới
// --------------------------------------------------


public class CameraImageDialogFragment extends DialogFragment {

    // Các key cho arguments
    public static final String ARG_CAMERA_NAME = "camera_name";
    public static final String ARG_CAMERA_ID = "camera_id";

    public static final String EXTRA_IMAGE_URL = "extra_image_url";
    public static final String EXTRA_CAMERA_NAME = "extra_camera_name";
    public static final String EXTRA_DENSITY = "extra_density";
    public static final String EXTRA_SUMMARY = "extra_summary";
    public static final String EXTRA_BASE_API_URL = "extra_base_api_url";
    private float currentDensity = 0.0f;
    private String currentSummary = "Đang tải...";

    private long timestamp_api;
    private String cameraName;
    private String cameraId;
    private String currentImageUrl; // Biến để lưu URL ảnh hiện tại

    private ImageView imageCameraFeed; // <-- Sử dụng ImageView
    // --- Thêm TextViews để hiển thị mật độ ---
    private TextView textCameraName; // Đã có ở trên, nhưng đảm bảo khai báo ở đây
    private TextView textCurrentDensity;
    // --- Đổi tên TextView để hiển thị Summary thay vì Maximum ---
    private TextView textSummary; // <-- Sử dụng TextView này cho summary
    // -----------------------------------------

    private Handler handler = new Handler(); // Dùng Handler để chạy Runnable trên Main Thread
    private Runnable updateImageRunnable;
    private Runnable updateDensityRunnable; // Runnable để cập nhật mật độ

    // Thời gian cập nhật: 15 giây
    private static final long UPDATE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);

    private static final String TAG = "CameraImageDialog"; // Tag cho Log


    private static final String IMAGE_URL_BASE = "https://giaothong.hochiminhcity.gov.vn/render/ImageHandler.ashx?id=%s&t=%d";


    private static final String BASE_API_URL = "https://eadf-35-247-40-140.ngrok-free.app"; // <-- THAY ĐỔI ĐỊA CHỈ NÀY BẰNG URL NGROK THỰC TẾ

    private TrafficApiService trafficApiService; // Biến để giữ đối tượng service
    // ---------------------


    // Phương thức Factory để tạo instance của DialogFragment
    public static CameraImageDialogFragment newInstance(String cameraName, String cameraId) {
        CameraImageDialogFragment fragment = new CameraImageDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CAMERA_NAME, cameraName);
        args.putString(ARG_CAMERA_ID, cameraId); // Truyền camera ID
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        if (getArguments() != null) {
            cameraName = getArguments().getString(ARG_CAMERA_NAME);
            cameraId = getArguments().getString(ARG_CAMERA_ID); // Lấy camera ID
            Log.d(TAG, "Arguments received: Name=" + cameraName + ", ID=" + cameraId);
        }
        // Optional: Set style for the dialog (e.g., no title)
        // setStyle(DialogFragment.STYLE_NO_TITLE, 0);

        // --- Khởi tạo Retrofit và API Service ---
        // Khởi tạo ở đây hoặc nhận từ Activity/Fragment chứa nó (khởi tạo 1 lần duy nhất trong app tốt hơn)
        // Nếu bạn đã khởi tạo Retrofit ở nơi khác (ví dụ: Application class), hãy truyền instance đó vào đây.
        // Kiểm tra xem BASE_API_URL đã được thay đổi chưa
        if ("YOUR_NGROK_URL_HERE".equals(BASE_API_URL)) {
            Log.e(TAG, "BASE_API_URL is not updated! Please replace 'YOUR_NGROK_URL_HERE' with your actual ngrok URL.");
            // Bạn có thể muốn hiển thị thông báo lỗi cho người dùng hoặc đóng dialog
        }


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL) // Đặt Base URL của API
                .addConverterFactory(GsonConverterFactory.create()) // Thêm converter để xử lý JSON (cần thư viện Gson)
                .build();

        trafficApiService = retrofit.create(TrafficApiService.class); // Tạo instance của API service
        Log.d(TAG, "Retrofit and TrafficApiService initialized in DialogFragment.");
        // ---------------------------------------
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        // Inflate layout cho dialog (sử dụng layout dialog_camera_image.xml đã tạo trước đó)
        View view = inflater.inflate(R.layout.dialog_camera_image, container, false);

        // Tìm Views trong layout
        textCameraName = view.findViewById(R.id.text_camera_name_dialog);
        imageCameraFeed = view.findViewById(R.id.image_camera_feed_dialog); // <-- Find ImageView
        // --- Tìm TextViews mới ---
        textCurrentDensity = view.findViewById(R.id.text_current_density);
        // --- Tìm TextView cho Summary (sử dụng lại ID text_maximum_density) ---
        textSummary = view.findViewById(R.id.text_maximum_density); // <-- Sử dụng lại ID này
        // -------------------------
        Log.d(TAG, "Views found in onCreateView");

        // Hiển thị tên camera
        if (cameraName != null) {
            textCameraName.setText(cameraName);
        } else {
            textCameraName.setText("Thông tin Camera");
        }

        // --- Thiết lập OnClickListener cho ImageView ---
        imageCameraFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ImageView clicked. Opening full screen image.");
                openFullScreenImage(); // Gọi phương thức mở Activity mới
            }
        });
        // ---------------------------------------------


        // Khởi tạo Runnable để cập nhật ảnh
        updateImageRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating image for camera: " + cameraId);
                loadImage(); // Tải ảnh mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian xác định
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };

        // --- Khởi tạo Runnable để cập nhật dữ liệu mật độ ---
        updateDensityRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Fetching density data for camera: " + cameraId);
                fetchDensityData(); // Lấy dữ liệu mật độ mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian xác định
                handler.postDelayed(this, UPDATE_INTERVAL_MS);
            }
        };
        // ----------------------------------------------------

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        // Tải ảnh lần đầu ngay sau khi view được tạo
        loadImage();
        // --- Lấy dữ liệu mật độ lần đầu ngay sau khi view được tạo ---
        fetchDensityData(); // Gọi phương thức lấy dữ liệu mật độ
        // -----------------------------------------------------------
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        Dialog dialog = getDialog();
        if (dialog != null) {
            // Lấy kích thước màn hình
            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenWidth = size.x;
            int screenHeight = size.y;

            // Đặt kích thước cho cửa sổ dialog
            int dialogWidth = (int) (screenWidth * 0.9); // 90% chiều rộng màn hình
            int dialogHeight = (int) (screenHeight * 0.7); // 70% chiều cao màn hình (ví dụ)

            dialog.getWindow().setLayout(dialogWidth, dialogHeight);
        }

        // Bắt đầu chu kỳ cập nhật ảnh định kỳ sau lần tải ảnh đầu tiên trong onViewCreated
        handler.postDelayed(updateImageRunnable, UPDATE_INTERVAL_MS);

        // --- Bắt đầu chu kỳ cập nhật mật độ định kỳ ---
        handler.postDelayed(updateDensityRunnable, UPDATE_INTERVAL_MS);
        // ----------------------------------------------------------
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        // Nếu dialog bị tạm dừng và tiếp tục, đảm bảo hẹn giờ được chạy lại
        // (Kiểm tra thêm để tránh chạy lại nếu đã chạy trong onStart)
        // if (updateImageRunnable != null && !handler.hasCallbacks(updateImageRunnable)) {
        //    handler.postDelayed(updateImageRunnable, UPDATE_IMAGE_INTERVAL_MS);
        // }
        // if (updateDensityRunnable != null && !handler.hasCallbacks(updateDensityRunnable)) {
        //    handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
        // }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Dừng cập nhật khi dialog bị tạm dừng
        handler.removeCallbacks(updateImageRunnable);
        handler.removeCallbacks(updateDensityRunnable); // Dừng cập nhật mật độ
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss");
        // Khi dialog bị đóng, dừng hẹn giờ cập nhật ảnh và mật độ
        handler.removeCallbacks(updateImageRunnable);
        handler.removeCallbacks(updateDensityRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        // Đảm bảo dừng Runnable nếu View bị hủy trước khi dialog bị dismiss
        handler.removeCallbacks(updateImageRunnable);
        handler.removeCallbacks(updateDensityRunnable); // Dừng cập nhật mật độ
        imageCameraFeed = null;
        // Giải phóng TextViews
        textCameraName = null; // Đảm bảo giải phóng
        textCurrentDensity = null;
        textSummary = null; // Giải phóng TextView summary
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // Giải phóng API service nếu cần (tùy cách quản lý lifecycle)
        trafficApiService = null;
    }


    // Phương thức để tải ảnh từ URL và hiển thị lên ImageView
    private void loadImage() {

        if (cameraId != null && imageCameraFeed != null) {
            currentImageUrl = String.format(IMAGE_URL_BASE, cameraId, timestamp_api); // Lưu URL ảnh hiện tại

            Log.d(TAG, "Attempting to load static image from URL: " + currentImageUrl);

            Glide.with(this)
                    .load(currentImageUrl) // Tải ảnh từ URL đã lưu
                    .placeholder(R.drawable.ic_menu_camera)
                    .error(R.drawable.ic_menu_gallery)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Không cache ảnh tĩnh vì nó thay đổi
                    .skipMemoryCache(true)
                    .into(imageCameraFeed); //

        } else {
            Log.e(TAG, "Camera ID is null or image view is null in loadImage().");
            if (imageCameraFeed != null) {
                imageCameraFeed.setImageResource(R.drawable.ic_menu_gallery);
            }
        }
    }

    // --- Phương thức để mở Activity hiển thị ảnh toàn màn hình ---
    private void openFullScreenImage() {
        if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
            Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
            intent.putExtra(EXTRA_IMAGE_URL, currentImageUrl);


            intent.putExtra(EXTRA_CAMERA_NAME, cameraName);
            intent.putExtra(EXTRA_DENSITY, currentDensity);
            intent.putExtra(EXTRA_SUMMARY, currentSummary);
            intent.putExtra(EXTRA_BASE_API_URL, BASE_API_URL);
            // ---------------------------------------------------------

            startActivity(intent);
            Log.d(TAG, "Started FullScreenImageActivity with URL: " + currentImageUrl +
                    ", Name: " + cameraName + ", Density: " + currentDensity + ", Summary: " + currentSummary);
        }
        else {
            Log.w(TAG, "Cannot open full screen image: currentImageUrl is null or empty.");
            // Tùy chọn: Hiển thị thông báo cho người dùng
        }
    }
    // -----------------------------------------------------------



    private void fetchDensityData() {
        // Kiểm tra xem API service và camera ID đã sẵn sàng chưa
        if (trafficApiService == null || cameraId == null) {
            Log.e(TAG, "API service or Camera ID is null, cannot fetch density data.");
            //Hiển thị trạng thái lỗi trên TextViews
            if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi");
            if (textSummary != null) textSummary.setText("Summary: Lỗi"); // Cập nhật TextView summary
            return;
        }
        if ("YOUR_NGROK_URL_HERE".equals(BASE_API_URL)) {
            Log.e(TAG, "BASE_API_URL is not updated! Cannot fetch data.");
            if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi URL");
            if (textSummary != null) textSummary.setText("Summary: Lỗi URL"); // Cập nhật TextView summary
            return;
        }


        // Tạo cuộc gọi API để lấy dữ liệu cho camera ID cụ thể này
        // Sử dụng tham số query 'camera_ids' để chỉ yêu cầu dữ liệu của 1 camera
        Call<List<TrafficDensityReading>> call = trafficApiService.getLatestTrafficDensities(cameraId); // Truyền camera ID

        Log.d(TAG, "Fetching density data for camera ID: " + cameraId + " from API...");


        call.enqueue(new Callback<List<TrafficDensityReading>>() {
            @Override
            public void onResponse(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Response<List<TrafficDensityReading>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficDensityReading> densityReadings = response.body();
                    Log.d(TAG, "API Call Successful. Received " + densityReadings.size() + " readings.");

                    // API trả về một danh sách, nhưng vì chúng ta yêu cầu theo 1 ID,
                    // danh sách này chỉ nên chứa 0 hoặc 1 phần tử.
                    if (!densityReadings.isEmpty()) {
                        TrafficDensityReading reading = densityReadings.get(0);

                        // Log dữ liệu nhận được để debug, bao gồm cả summary
                        Log.d(TAG, "Received data for " + reading.getCameraId() +
                                ": Density=" + reading.getDensity() +
                                ", Summary='" + reading.getSummary() +
                                ", Timestamp= " + reading.getTimestamp() + "'"); // Log summary
                        timestamp_api = reading.getTimestamp();
                        // --- Cập nhật TextViews với dữ liệu mật độ và summary ---
                        if (textCurrentDensity != null) {
                            currentDensity = reading.getDensity();
                            textCurrentDensity.setText(String.format("Mật độ hiện tại: %.2f", reading.getDensity()));
                        }
                        if (textSummary != null) {
                            currentSummary = reading.getSummary();
                            textSummary.setText("Summary: " + reading.getSummary()); // Hiển thị summary
                        }
                        // ---------------------------------------------

                    } else {
                        Log.w(TAG, "API Call Successful but no data received for camera ID: " + cameraId);
                        // Tùy chọn: Hiển thị trạng thái "Không có dữ liệu"
                        if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Không có dữ liệu");
                        if (textSummary != null) textSummary.setText("Summary: Không có dữ liệu"); // Cập nhật TextView summary
                    }

                } else {
                    // API trả về lỗi (ví dụ: 404 Not Found, 500 Internal Server Error)
                    Log.e(TAG, "API Call Failed. Response code: " + response.code() + ", Message: " + response.message());
                    // Tùy chọn: Hiển thị trạng thái lỗi
                    if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi tải (" + response.code() + ")");
                    if (textSummary != null) textSummary.setText("Summary: Lỗi tải (" + response.code() + ")"); // Cập nhật TextView summary
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Throwable t) {
                // Lỗi mạng hoặc lỗi khác (ví dụ: không kết nối được đến server)
                Log.e(TAG, "API Call Failed (Network Error): " + t.getMessage(), t);
                // Tùy chọn: Hiển thị trạng thái lỗi mạng
                if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi mạng");
                if (textSummary != null) textSummary.setText("Summary: Lỗi mạng"); // Cập nhật TextView summary
            }


        });
    }

}
