package com.example.trafficdensity.ui.dialog; // Đảm bảo đúng package của bạn

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView; // <-- Sử dụng ImageView
import android.widget.TextView;
import android.util.Log;
import android.view.WindowManager;
import android.graphics.Point;
import android.view.Display;
import android.content.Intent; // <-- Import Intent

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide; // Thư viện tải ảnh
import com.bumptech.glide.load.engine.DiskCacheStrategy; // Để cấu hình cache của Glide

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.example.trafficdensity.R; // Import R class của ứng dụng
import com.example.trafficdensity.ui.fullscreen.FullScreenImageActivity; // <-- Import FullScreenImageActivity
import com.example.trafficdensity.ui.fullscreen.OverlayView;

// import com.github.chrisbanes.photoview.PhotoView; // <-- Xóa import PhotoView

import java.util.ArrayList;
import java.util.concurrent.TimeUnit; // Để dễ đọc thời gian

// --- Import cho API Call ---
import api.Detection;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService; // Import interface API service
import api.TrafficDensityReading; // Import data class
import java.util.List;
// ---------------------------

import java.util.Locale; // Để định dạng số float

public class CameraImageDialogFragment extends DialogFragment {

    // Các key cho arguments
    public static final String ARG_CAMERA_NAME = "camera_name";
    public static final String ARG_CAMERA_ID = "camera_id";
    // --- Thêm key cho BASE_API_URL để truyền sang FullScreenImageActivity ---
    public static final String EXTRA_BASE_API_URL = "com.example.trafficdensity.BASE_API_URL";
    // --------------------------------------------------------------------


    // Các key cho Intent extras khi mở FullScreenImageActivity
    public static final String EXTRA_CAMERA_ID = "com.example.trafficdensity.CAMERA_ID";
    public static final String EXTRA_IMAGE_URL = "com.example.trafficdensity.IMAGE_URL";
    public static final String EXTRA_CAMERA_NAME = "com.example.trafficdensity.CAMERA_NAME";
    public static final String EXTRA_DENSITY = "com.example.trafficdensity.DENSITY";
    public static final String EXTRA_SUMMARY = "com.example.trafficdensity.SUMMARY";
    public static final String EXTRA_DETECTIONS = "extra_detections";


    private String cameraName;
    private String cameraId;

    private ImageView imageCameraFeed; // <-- Sử dụng ImageView
    private OverlayView overlayView; // <-- Biến cho OverlayView trong dialog

    private TextView textCameraName; // TextView cho tên camera
    private TextView textCurrentDensity;
    private TextView textSummary;
    // --- Thêm biến để lưu đường dẫn ảnh nhận từ API ---
    private String currentImagePath = null;
    // --- Thêm biến để lưu dữ liệu mật độ và summary hiện tại ---
    private float currentDensity = 0.0f;

    private List<Detection> currentDetections = new ArrayList<>();

    private long currentTimestamp = 0;
    private String currentSummary = "Không có thông tin";
    // -------------------------------------------------------

    private Handler handler = new Handler(); // Dùng Handler để chạy Runnable trên Main Thread
    private Runnable updateImageRunnable;
    // Thời gian cập nhật ảnh: 15 giây
    private static final long UPDATE_IMAGE_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);

    // Thời gian cập nhật dữ liệu mật độ (có thể khác với cập nhật ảnh)
    // Đặt cùng tần suất với script Python gửi dữ liệu (ví dụ: 15 giây)
    private static final long UPDATE_DENSITY_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);


    private static final String TAG = "CameraImageDialog"; // Tag cho Log

    // Base URL để lấy ảnh tĩnh từ backend (giữ nguyên) - KHÔNG DÙNG NỮA ĐỂ TẢI ẢNH CHÍNH
    // private static final String IMAGE_URL_BASE = "https://giaothong.hochiminhcity.gov.vn/render/ImageHandler.ashx?id=%s&t=%d";

    // --- Cấu hình API ---
    // Base URL của API backend của bạn (địa chỉ Flask API)
    // Đảm bảo đây là URL có thể truy cập từ thiết bị Android (IP nội bộ, ngrok URL, Cloud IP/Domain)
    private static final String BASE_API_URL = "http://6666-34-83-127-61.ngrok-free.app/"; // <-- THAY ĐỔI ĐỊA CHỈ NÀY

    private TrafficApiService trafficApiService; // Biến để giữ đối tượng service
    private int originalImageWidth = 512;
    private int originalImageHeight = 288;
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
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL) // Đặt Base URL của API
                .addConverterFactory(GsonConverterFactory.create()) // Thêm converter để xử lý JSON
                .build();

        trafficApiService = retrofit.create(TrafficApiService.class); // Tạo instance của API service
        Log.d(TAG, "Retrofit and TrafficApiService initialized in DialogFragment.");
        // ---------------------------------------
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.dialog_camera_image, container, false);

        // Tìm Views trong layout
        overlayView = view.findViewById(R.id.overlay_view_dialog);
        textCameraName = view.findViewById(R.id.text_camera_name_dialog);
        imageCameraFeed = view.findViewById(R.id.image_camera_feed_dialog);
        textCurrentDensity = view.findViewById(R.id.text_current_density);
        textSummary = view.findViewById(R.id.text_density);
        Log.d(TAG, "Views found in onCreateView");

        // Hiển thị tên camera
        if (cameraName != null) {
            textCameraName.setText(cameraName);
        } else {
            textCameraName.setText("Thông tin Camera");
        }

        // --- Thêm OnClickListener cho ImageView để mở màn hình toàn màn hình ---
        imageCameraFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "ImageView clicked. Attempting to open FullScreenImageActivity.");
                openFullScreenImage();
            }
        });
        // -------------------------------------------------------------------


        // Khởi tạo Runnable để cập nhật ảnh
        updateImageRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Updating image for camera: " + cameraId);
                loadImage(); // Tải ảnh mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian cập nhật ảnh
                handler.postDelayed(this, UPDATE_IMAGE_INTERVAL_MS);
            }
        };

        // --- Khởi tạo Runnable để cập nhật dữ liệu mật độ ---
        // Runnable này sẽ chạy định kỳ để lấy dữ liệu mật độ mới
        Runnable updateDensityRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Fetching density data for camera: " + cameraId);
                fetchDensityData(); // Lấy dữ liệu mật độ mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian cập nhật mật độ
                handler.postDelayed(this, UPDATE_DENSITY_INTERVAL_MS);
            }
        };
        // Bắt đầu Runnable cập nhật mật độ lần đầu ngay sau khi view được tạo
        // và hẹn giờ cho các lần tiếp theo
        handler.postDelayed(updateDensityRunnable, 0); // Chạy ngay lập tức lần đầu


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated");
        // Tải ảnh lần đầu ngay sau khi view được tạo
        // loadImage(); // Không gọi ở đây nữa, sẽ gọi sau khi fetchDensityData lấy được image_path
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

        // Bắt đầu chu kỳ cập nhật ảnh định kỳ
        // updateImageRunnable sẽ được bắt đầu sau khi fetchDensityData thành công lần đầu
        // handler.postDelayed(updateImageRunnable, UPDATE_IMAGE_INTERVAL_MS); // Không gọi ở đây nữa

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
        // Dừng Runnable cập nhật mật độ khi dialog bị tạm dừng
        handler.removeCallbacksAndMessages(null); // Dừng tất cả callbacks và messages
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Log.d(TAG, "onDismiss");
        // Khi dialog bị đóng, dừng hẹn giờ cập nhật ảnh và mật độ
        handler.removeCallbacks(updateImageRunnable);
        overlayView = null;
        // Dừng Runnable cập nhật mật độ khi dialog bị dismiss
        handler.removeCallbacksAndMessages(null); // Dừng tất cả callbacks và messages
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        // Đảm bảo dừng Runnable nếu View bị hủy trước khi dialog bị dismiss
        handler.removeCallbacks(updateImageRunnable);
        // Dừng Runnable cập nhật mật độ khi View bị hủy
        handler.removeCallbacksAndMessages(null); // Dừng tất cả callbacks và messages
        imageCameraFeed = null;
        textCameraName = null;
        overlayView = null;
        textCurrentDensity = null;
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
        // Sử dụng currentImagePath để xây dựng URL đến Flask API
        if (cameraId != null && imageCameraFeed != null && currentImagePath != null) {
            // Xây dựng URL đầy đủ đến endpoint /api/images/<camera_id> của Flask API
            // Giả định BASE_API_URL kết thúc bằng '/', và image_path là chỉ tên file (ví dụ: "camera_id.jpg")
            // HOẶC image_path là đường dẫn đầy đủ trên server (ví dụ: "processed_images/camera_id.jpg")
            // Dựa trên Flask API code, image_path là đường dẫn trên server (ví dụ: "processed_images/camera_id.jpg")
            // Endpoint phục vụ ảnh là /api/images/<camera_id>
            // Vậy URL cần là BASE_API_URL + "api/images/" + cameraId

            String imageUrlToLoad = BASE_API_URL + "api/images/" + cameraId + "_" + currentTimestamp;


            Log.d(TAG, "Attempting to load processed image from URL: " + imageUrlToLoad);

            Glide.with(this)
                    .load(imageUrlToLoad) // Tải ảnh từ URL đã xây dựng
                    .placeholder(R.drawable.ic_menu_camera)
                    .error(R.drawable.ic_menu_gallery)
                    .diskCacheStrategy(DiskCacheStrategy.NONE) // Không cache ảnh tĩnh vì nó thay đổi
                    .skipMemoryCache(true) // Bỏ qua cache bộ nhớ
                    // --- Sử dụng CustomTarget để lấy kích thước ảnh sau khi tải ---
                    .into(new CustomTarget<Drawable>() {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            imageCameraFeed.setImageDrawable(resource);
                            Log.d(TAG, "Image loaded successfully into ImageView from Flask API.");

                            // --- Cập nhật OverlayView với kích thước ảnh gốc ---
                            // Kích thước hiển thị của ImageView chỉ có sau khi layout được đo đạc.
                            // Sử dụng post để chạy sau khi layout hoàn thành.
                            imageCameraFeed.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (overlayView != null) {
                                        // Truyền detections hiện tại (có thể là rỗng hoặc từ fetch đầu tiên)
                                        // và kích thước ảnh gốc đã set.
                                        // OverlayView sẽ tự tính toán scaling dựa trên kích thước hiển thị của nó
                                        // và kích thước ảnh gốc.
                                        Log.d(TAG, "Updating OverlayView after image load (count: " + currentDetections.size() + ") with original size: " + originalImageWidth + "x" + originalImageHeight);
                                        overlayView.setDetections(currentDetections, originalImageWidth, originalImageHeight);
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
                            Log.e(TAG, "Image load FAILED from Flask API URL: " + imageUrlToLoad);
                            imageCameraFeed.setImageDrawable(errorDrawable);
                        }
                    });
        } else {
            Log.e(TAG, "currentImageFilename, ImageView, or BASE_API_URL is null/invalid in loadImage().");
            if (imageCameraFeed != null) {
                imageCameraFeed.setImageResource(R.drawable.ic_menu_gallery);
            }
        }
    }

    // --- PHƯƠNG THỨC ĐỂ LẤY DỮ LIỆU MẬT ĐỘ TỪ API ---
    private void fetchDensityData() {
        // Kiểm tra xem API service và camera ID đã sẵn sàng chưa
        if (trafficApiService == null || cameraId == null) {
            Log.e(TAG, "API service or Camera ID is null, cannot fetch density data.");
            // Tùy chọn: Hiển thị trạng thái lỗi trên TextViews
            if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi");
            if (textSummary != null) textSummary.setText("Summary: Lỗi");
            return;
        }

        // Tạo cuộc gọi API để lấy dữ liệu cho camera ID cụ thể này
        // Sử dụng tham số query 'camera_ids' để chỉ yêu cầu dữ liệu của 1 camera
        Call<List<TrafficDensityReading>> call = trafficApiService.getLatestTrafficDensities(cameraId); // Truyền camera ID

        Log.d(TAG, "Fetching density data for camera ID: " + cameraId + " from API...");

        // Thực hiện cuộc gọi API bất đồng bộ
        call.enqueue(new Callback<List<TrafficDensityReading>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Response<List<TrafficDensityReading>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficDensityReading> densityReadings = response.body();
                    Log.d(TAG, "API Call Successful. Received " + densityReadings.size() + " readings.");

                    // API trả về một danh sách, nhưng vì chúng ta yêu cầu theo 1 ID,
                    // danh sách này chỉ nên chứa 0 hoặc 1 phần tử.
                    if (!densityReadings.isEmpty()) {
                        TrafficDensityReading reading = densityReadings.get(0);
                        Log.d(TAG, "Received data for " + reading.getCameraId() + ": Density=" + reading.getDensity() + ", ImagePath=" + reading.getImagePath() + ", Summary=" + reading.getSummary());

                        // --- Cập nhật biến thành viên với dữ liệu mới ---
                        currentDensity = reading.getDensity();
                        currentSummary = reading.getSummary();
                        currentTimestamp = reading.getTimestamp();
                        currentDetections = reading.getDetections() != null ? reading.getDetections() : new ArrayList<>(); // <-- LƯU DETECTIONS
                        currentImagePath = reading.getImagePath(); // <-- LƯU IMAGE PATH NHẬN ĐƯỢC
                        // ---------------------------------------------

                        // --- Cập nhật TextViews với dữ liệu mật độ ---
                        if (textCurrentDensity != null) {
                            textCurrentDensity.setText(String.format(Locale.US, "Mật độ hiện tại: %.2f", currentDensity)); // Định dạng số float
                        }

                        // Hiển thị summary (tùy chọn)
                         if (textSummary != null) { // Cần thêm TextView cho summary nếu muốn hiển thị
                             String[] temp = currentSummary.split(";");
                            textSummary.setText("Số lượng: " + "\n" + temp[0] + "\n" + temp[1] + "\n" + temp[2] + "\n" + temp[3]);
                         }


                        // ---------------------------------------------

                        // --- Tải ảnh sau khi nhận được image_path ---
                        loadImage(); // Gọi loadImage() sau khi có image_path
                        // -------------------------------------------

                        // --- Bắt đầu chu kỳ cập nhật ảnh định kỳ sau khi fetch thành công lần đầu ---
                        // Kiểm tra để tránh double-posting nếu đã chạy
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            if (updateImageRunnable != null && !handler.hasCallbacks(updateImageRunnable)) {
                                handler.postDelayed(updateImageRunnable, UPDATE_IMAGE_INTERVAL_MS);
                                Log.d(TAG, "Image update runnable scheduled.");
                            }
                        }
                        // -----------------------------------------------------------------------


                    } else {
                        Log.w(TAG, "API Call Successful but no data received for camera ID: " + cameraId);
                        // Tùy chọn: Hiển thị trạng thái "Không có dữ liệu"
                        if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Không có dữ liệu");
                        if (textSummary != null) textSummary.setText("Summary: Không có dữ liệu");
                        // Đặt imagePath về null để loadImage không cố gắng tải ảnh lỗi
                        currentImagePath = null;
                    }

                } else {
                    // API trả về lỗi
                    Log.e(TAG, "API Call Failed. Response code: " + response.code() + ", Message: " + response.message());
                    // Tùy chọn: Hiển thị trạng thái lỗi
                    if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi tải");
                    if (textSummary != null) textSummary.setText("Summary: Lỗi tải");
                    currentImagePath = null; // Đặt imagePath về null khi có lỗi API
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Throwable t) {
                // Lỗi mạng hoặc lỗi khác
                Log.e(TAG, "API Call Failed (Network Error): " + t.getMessage(), t);
                // Tùy chọn: Hiển thị trạng thái lỗi mạng
                if (textCurrentDensity != null) textCurrentDensity.setText("Mật độ hiện tại: Lỗi mạng");
                if (textSummary != null) textSummary.setText("Summary: Lỗi mạng");
                currentImagePath = null; // Đặt imagePath về null khi có lỗi mạng
            }
        });
    }
    // -------------------------------------------------------------

    // --- Phương thức để mở FullScreenImageActivity ---
    private void openFullScreenImage() {
        // Chỉ mở khi có URL ảnh hợp lệ
        if (currentImagePath != null && !currentImagePath.isEmpty()) {
            // Xây dựng URL đầy đủ đến endpoint /api/images/<camera_id> của Flask API
            String fullImageUrl = BASE_API_URL + "api/images/" + cameraId + "_" + currentTimestamp;

            Intent intent = new Intent(getActivity(), FullScreenImageActivity.class);
            intent.putExtra(EXTRA_CAMERA_ID, cameraId);
            intent.putExtra(EXTRA_IMAGE_URL, fullImageUrl); // Truyền URL ảnh
            intent.putExtra(EXTRA_CAMERA_NAME, cameraName); // Truyền tên camera
            intent.putExtra(EXTRA_DENSITY, currentDensity); // Truyền mật độ
            intent.putExtra(EXTRA_SUMMARY, currentSummary); // Truyền summary
            intent.putExtra(EXTRA_BASE_API_URL, BASE_API_URL); // <-- Truyền BASE_API_URL
            intent.putExtra(EXTRA_DETECTIONS, (ArrayList<Detection>) currentDetections); // <-- Truyền danh sách detections (cần cast sang ArrayList)

            startActivity(intent);
            Log.d(TAG, "Started FullScreenImageActivity with URL: " + fullImageUrl);
        } else {
            Log.w(TAG, "Cannot open full screen image: currentImagePath is null or empty.");
            // Tùy chọn: Hiển thị thông báo cho người dùng
            // Toast.makeText(getActivity(), "Không có ảnh để hiển thị.", Toast.LENGTH_SHORT).show();
        }
    }
    // -------------------------------------------------
}
