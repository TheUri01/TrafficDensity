package com.example.trafficdensity.ui.home; // Đảm bảo đúng package của bạn
import com.example.trafficdensity.util.Constants;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import com.example.trafficdensity.R; // Import R class của ứng dụng
import com.example.trafficdensity.CameraInfo; // Import lớp CameraInfo
import com.example.trafficdensity.ui.dialog.CameraImageDialogFragment; // Import DialogFragment
import android.net.Uri; // Import Uri
import android.widget.Toast; // Import Toast

import java.util.ArrayList; // Import ArrayList
import java.util.HashMap; // Import HashMap
import java.util.List;
import java.util.Map; // Import Map

// --- Import cho API Call ---
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService; // Import interface API service
import api.TrafficDensityReading; // Import data class
// ---------------------------

// --- Import Handler và Runnable cho cập nhật định kỳ ---
import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.TimeUnit;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;
// ------------------------------------------------------


public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private static final String TAG = "HomeFragment";

    // Danh sách các camera (giả định bạn tải từ đâu đó)
    private List<CameraInfo> cameraList = new ArrayList<>(); // Sử dụng ArrayList để khởi tạo rỗng

    // Map để lưu trữ các Marker trên bản đồ, ánh xạ Camera ID -> Marker
    private Map<String, Marker> cameraMarkers = new HashMap<>();

    // --- Map để lưu trữ mật độ hiện tại cho mỗi camera ID ---
    private Map<String, Float> currentDensities = new HashMap<>();
    // -------------------------------------------------------

    // --- Cấu hình API ---
    // Base URL của API backend của bạn (ngrok hoặc IP máy chủ)
    // THAY ĐỔI ĐỊA CHỈ NÀY BẰNG URL NGROK HOẶC ĐỊA CHỈ IP THỰC TẾ CỦA BẠN
    private static final String BASE_API_URL = Constants.API_URL; // <-- CẬP NHẬT ĐỊA CHỈ NÀY

    private TrafficApiService trafficApiService; // Biến để giữ đối tượng service
    // ---------------------

    // --- Handler và Runnable cho cập nhật mật độ định kỳ ---
    private Handler handler = new Handler(Looper.getMainLooper()); // Chạy trên Main Thread
    private Runnable updateDensityRunnable;
    private static final long UPDATE_DENSITY_INTERVAL_MS = Constants.UPDATE_DENSITY_INTERVAL_MS; // Cập nhật mỗi 15 giây
    // ------------------------------------------------------


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error finding map fragment.");
        }

        // --- Khởi tạo Retrofit và API Service ---
        // Kiểm tra xem BASE_API_URL đã được thay đổi chưa
        if ("YOUR_NGROK_URL_OR_IP/".equals(BASE_API_URL)) {
            Log.e(TAG, "BASE_API_URL is not updated! Please replace 'YOUR_NGROK_URL_OR_IP/' with your actual backend URL.");
            // Bạn có thể muốn hiển thị thông báo lỗi cho người dùng
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL) // Đặt Base URL của API
                .addConverterFactory(GsonConverterFactory.create()) // Thêm converter để xử lý JSON
                .build();

        trafficApiService = retrofit.create(TrafficApiService.class); // Tạo instance của API service
        Log.d(TAG, "Retrofit and TrafficApiService initialized in HomeFragment.");
        // ---------------------------------------

        // --- Khởi tạo Runnable để cập nhật mật độ định kỳ ---
        updateDensityRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Executing updateDensityRunnable. Attempting to fetch density data.");
                fetchTrafficDensityData(); // Lấy dữ liệu mật độ mới

                // Hẹn giờ chạy lại Runnable sau khoảng thời gian xác định
                handler.postDelayed(this, UPDATE_DENSITY_INTERVAL_MS);
                Log.d(TAG, "Density update runnable rescheduled for " + UPDATE_DENSITY_INTERVAL_MS + "ms.");
            }
        };
        // ----------------------------------------------------


        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready.");

        // Cấu hình map ban đầu (ví dụ: di chuyển camera đến Quận 5)
        LatLng quan5 = new LatLng(10.7596, 106.6674); // Tọa độ trung tâm Quận 5 (ước lượng)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quan5, 13)); // Zoom level 13

        // Đặt listener cho sự kiện click vào marker
        mMap.setOnMarkerClickListener(this);
        Log.d(TAG, "Marker click listener set.");

        // --- Tải danh sách camera (giả định từ đâu đó) ---
        // Đây là nơi bạn tải danh sách CameraInfo từ nguồn dữ liệu của bạn
        // Ví dụ: từ file JSON trong assets, từ API khác, hoặc hardcode
        cameraList = loadCameraList(); // <-- GỌI HÀM TẢI DANH SÁCH CAMERA CỦA BẠN
        if (cameraList != null && !cameraList.isEmpty()) {
            Log.d(TAG, "Loaded " + cameraList.size() + " cameras.");
            addCameraMarkers(cameraList); // Thêm markers vào bản đồ lần đầu
            // --- Bắt đầu chu kỳ cập nhật mật độ sau khi thêm markers ---
            startDensityUpdates();
            // -------------------------------------------------------
        } else {
            Log.w(TAG, "No cameras loaded or camera list is empty.");
            // Tùy chọn: Hiển thị thông báo cho người dùng
        }

        // --- Tùy chọn: Cấu hình kiểu bản đồ ---
        // try {
        //     // Customise the styling of the base map using a JSON object defined
        //     // in a raw resource file.
        //     boolean success = googleMap.setMapStyle(
        //         MapStyleOptions.loadRawResourceStyle(
        //             requireContext(), R.raw.map_style)); // Tạo file R.raw.map_style.json với style của bạn

        //     if (!success) {
        //         Log.e(TAG, "Style parsing failed.");
        //     }
        // } catch (Resources.NotFoundException e) {
        //     Log.e(TAG, "Can't find style. Error: ", e);
        // }
        // --------------------------------------
    }

    // --- Phương thức giả định để tải danh sách CameraInfo ---
    // THAY THẾ BẰNG LOGIC TẢI DANH SÁCH CAMERA THỰC TẾ CỦA BẠN
    public List<CameraInfo> loadCameraList() {
        List<CameraInfo> dummyList = new ArrayList<>();
        // Thêm các CameraInfo giả định hoặc tải từ nguồn thực tế
        // Đảm bảo camera ID trong đây khớp với camera ID trong dữ liệu API
        // ... thêm các camera khác ...
        dummyList.add(new CameraInfo("56de42f611f398ec0c481291", "Võ Văn Kiệt - Nguyễn Tri Phương 1", new LatLng(10.7503914, 106.6690747), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481291&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%201&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("56de42f611f398ec0c481297", "Võ Văn Kiệt - Nguyễn Tri Phương 2", new LatLng(10.7504000, 106.6698932), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481297&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%202&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("56de42f611f398ec0c481293", "Võ Văn Kiệt - Hải Thượng Lãn Ông", new LatLng(10.7499589, 106.6630958), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481293&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20H%E1%BA%A3i%20Th%C6%B0%E1%BB%A3ng%20L%C3%A3n%20%C3%94ng%201&camMode=camera&videoUrl=http://camera.thongtingiaothong.vn/s/56de42f611f398ec0c481293/index.m3u8"));
        dummyList.add(new CameraInfo("5b632a79fd4edb0019c7dc0f", "Nguyễn Tri Phương - Trần Hưng Đạo", new LatLng(10.7521932, 106.6695217), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b632a79fd4edb0019c7dc0f&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Tr%E1%BA%A7n%20H%C6%B0ng%20%C4%90%E1%BA%A1o&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("662b4efc1afb9c00172d86bc", "Trần Hưng Đạo - Trần Phú", new LatLng(10.7524870, 106.6678037), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4efc1afb9c00172d86bc&camLocation=Tr%E1%BA%A7n%20H%C6%B0ng%20%C4%90%E1%BA%A1o%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5d8cd1f9766c880017188938", "Nguyễn Tri Phương - Trần Phú", new LatLng(10.7536164, 106.6696310), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5d8cd1f9766c880017188938&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5d8cd49f766c880017188944", "Nguyễn Tri Phương - Nguyễn Trãi", new LatLng(10.7546263, 106.6695642), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5d8cd49f766c880017188944&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Nguy%E1%BB%85n%20Tr%C3%A3i&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("66b1c190779f740018673ed4", "Nguyễn Trãi - Trần Phú", new LatLng(10.7549314, 106.6716376), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=66b1c190779f740018673ed4&camLocation=Nguy%E1%BB%85n%20Tr%C3%A3i%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5b632b60fd4edb0019c7dc12", "Hồng Bàng - Ngô Quyền 1", new LatLng(10.7556201, 106.6663852), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b632b60fd4edb0019c7dc12&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ng%C3%B4%20Quy%E1%BB%81n%201&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad20", "Hồng Bàng - Ngô Quyền 2", new LatLng(10.7561475, 106.6661542), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad20&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ng%C3%B4%20Quy%E1%BB%81n%202&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("63b3c274bfd3d90017e9ab93", "Hồng Bàng - Phù Đổng Thiên Vương", new LatLng(10.7549775, 106.6625513), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=63b3c274bfd3d90017e9ab93&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ph%C3%B9%20%C4%90%E1%BB%95ng%20Thi%C3%AAn%20V%C6%B0%C6%A0ng&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5b728aafca0577001163ff7e", "Hồng Bàng - Châu Văn Liêm", new LatLng(10.7545545, 106.6583560), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b728aafca0577001163ff7e&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ch%C3%A2u%20V%C4%83n%20Li%C3%AAm&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("662b4e201afb9c00172d85f9", "Hồng Bàng - Tạ Uyên", new LatLng(10.7537439, 106.6537677), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4e201afb9c00172d85f9&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20T%E1%BA%A1%20Uy%C3%AAn&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad21", "Nút giao ngã 6 Nguyễn Tri Phương", new LatLng(10.7600016, 106.6688883), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad21&camLocation=N%C3%BAt%20giao%20Ng%C3%A3%20s%C3%A1u%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("649da419a6068200171a6c90", "Nguyễn Chí Thanh - Ngô Quyền", new LatLng(10.7592865, 106.6655812), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=649da419a6068200171a6c90&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Ng%C3%B4%20Quy%E1%BB%81n&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("66f126e8538c780017c9362f", "Nguyễn Chí Thanh - Nguyễn Kim", new LatLng(10.7587157, 106.6627702), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=66f126e8538c780017c9362f&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Nguy%E1%BB%85n%20Kim&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("662b4e8e1afb9c00172d865c", "Nguyễn Chí Thanh - Lý Thường Kiệt", new LatLng(10.7584792, 106.6615056), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4e8e1afb9c00172d865c&camLocation=L%C3%BD%20Th%C6%B0%E1%BB%9Dng%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("662b4ecb1afb9c00172d8692", "Nguyễn Chí Thanh - Thuận Kiều", new LatLng(10.7577917, 106.6582849), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4ecb1afb9c00172d8692&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Thu%E1%BA%ADn%20Ki%E1%BB%83u&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad1f", "Hùng Vương - Ngô Gia Tự", new LatLng(10.7564805, 106.6666292), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad1f&camLocation=H%C3%B9ng%20V%C6%B0%C6%A0ng%20-%20Ng%C3%B4%20Gia%20T%E1%BB%B1&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        dummyList.add(new CameraInfo("662b4de41afb9c00172d85c5", "Hải Thượng Lãn Ông - Châu Văn Liêm", new LatLng(10.7506780, 106.6592465), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad1f&camLocation=H%C3%B9ng%20V%C6%B0%C6%A0ng%20-%20Ng%C3%B4%20Gia%20T%E1%BB%B1&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        return dummyList;
    }
    // -------------------------------------------------------


    // Phương thức để thêm các marker camera lên bản đồ lần đầu
    private void addCameraMarkers(List<CameraInfo> cameraList) {
        if (mMap == null || cameraList == null) return;

        // Xóa các marker cũ nếu có
        for (Marker marker : cameraMarkers.values()) {
            marker.remove();
        }
        cameraMarkers.clear(); // Xóa khỏi map

        for (CameraInfo camera : cameraList) {
            LatLng cameraLocation = new LatLng(camera.getLocation().latitude, camera.getLocation().longitude);

            // --- Lấy mật độ hiện tại để chọn màu marker ---
            // Mặc định là 0.0 nếu chưa có dữ liệu mật độ
            float density = currentDensities.get(camera.getId()) != null ? currentDensities.get(camera.getId()) : 0.0f;
            Log.d(TAG, "Adding marker for " + camera.getName() + " (ID: " + camera.getId() + ") with initial density: " + density);

            // Chọn icon dựa trên ngưỡng mật độ
            BitmapDescriptor markerIcon = getMarkerIconForDensity(density);

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(cameraLocation)
                    .title(camera.getName())
                    .icon(markerIcon); // Sử dụng icon đã chọn

            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(camera); // Lưu CameraInfo vào tag của marker
                cameraMarkers.put(camera.getId(), marker); // Lưu marker vào map với Camera ID làm key
                Log.d(TAG, "Marker added for " + camera.getName() + ". Stored in map with ID: " + camera.getId());
            } else {
                Log.e(TAG, "Failed to add marker for " + camera.getName());
            }
        }
    }

    // --- Phương thức để lấy BitmapDescriptor cho marker dựa trên mật độ ---
    private BitmapDescriptor getMarkerIconForDensity(float density) {
        int drawableId;
        if (density < 0.3) { // Mật độ thấp (ví dụ: < 30%)
            drawableId = R.drawable.ic_camera_green; // Icon xanh lá
        } else if (density < 0.7) { // Mật độ trung bình (ví dụ: 30% - 70%)
            drawableId = R.drawable.ic_camera_yellow; // Icon vàng
        } else { // Mật độ cao (ví dụ: > 70%)
            drawableId = R.drawable.ic_camera_red; // Icon đỏ
        }

        // Chuyển drawable resource ID sang BitmapDescriptor
        return bitmapDescriptorFromVector(requireContext(), drawableId);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(android.content.Context context, int vectorDrawableResourceId) {
        // Lấy Drawable từ resource ID
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        if (vectorDrawable == null) {
            Log.e(TAG, "Could not find drawable resource: " + vectorDrawableResourceId);
            return BitmapDescriptorFactory.defaultMarker(); // Trả về marker mặc định nếu không tìm thấy drawable
        }

        // Tạo Bitmap có kích thước của Drawable
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888);

        // Vẽ Drawable lên Bitmap
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);

        // Tạo BitmapDescriptor từ Bitmap
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    // ---------------------------------------------------------------------

    // --- Phương thức để cập nhật màu sắc của các marker hiện có khi có dữ liệu mật độ mới ---
    private void updateMarkerColors() {
        if (mMap == null || cameraMarkers.isEmpty() || currentDensities.isEmpty()) {
            Log.d(TAG, "Cannot update marker colors: Map, Markers, or Densities are empty/null.");
            return;
        }

        Log.d(TAG, "Updating marker colors based on latest density data...");

        // Lặp qua tất cả các marker đang có trên bản đồ
        for (Map.Entry<String, Marker> entry : cameraMarkers.entrySet()) {
            String cameraId = entry.getKey();
            Marker marker = entry.getValue();

            // Lấy mật độ mới nhất cho camera này từ Map currentDensities
            float density = currentDensities.get(cameraId) != null ? currentDensities.get(cameraId) : 0.0f;

            // Lấy icon marker phù hợp với mật độ mới (đã sử dụng helper chuyển đổi sang Bitmap)
            BitmapDescriptor newIcon = getMarkerIconForDensity(density);

            // Cập nhật icon cho marker trên bản đồ
            if (marker != null && newIcon != null) { // Kiểm tra newIcon != null
                marker.setIcon(newIcon);
                // Log.d(TAG, "Updated marker color for ID: " + cameraId + " with density: " + density); // Log này có thể quá nhiều
            } else if (marker == null) {
                Log.w(TAG, "Marker is null for ID: " + cameraId + " when attempting to update color.");
            } else { // newIcon == null
                Log.e(TAG, "Failed to create new icon for ID: " + cameraId + " with density: " + density + ". Drawable conversion failed.");
                // Tùy chọn: Đặt lại icon mặc định hoặc giữ nguyên icon cũ
                // marker.setIcon(BitmapDescriptorFactory.defaultMarker());
            }
        }
        Log.d(TAG, "Finished updating marker colors.");
    }
    // --------------------------------------------------------------------------------------


    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d(TAG, "Marker clicked: " + marker.getTitle());

        Object tag = marker.getTag();
        if (tag instanceof CameraInfo) {
            CameraInfo clickedCamera = (CameraInfo) tag;
            Log.d(TAG, "Clicked Camera: " + clickedCamera.getName() + ", Original URL: " + clickedCamera.getVideoUrl());

            // --- CODE JAVA PHÂN TÍCH URL BẰNG android.net.Uri ---
            String originalPageUrl = clickedCamera.getVideoUrl(); // Lấy URL gốc từ CameraInfo
            String cameraIdentifier = null; // Biến để lưu camId hoặc id
            try {
                Uri uri = Uri.parse(originalPageUrl);
                cameraIdentifier = uri.getQueryParameter("camId");
                if (cameraIdentifier == null) {
                    cameraIdentifier = uri.getQueryParameter("id");
                }
                Log.d(TAG, "Parsed URL - cameraIdentifier: " + cameraIdentifier);
            } catch (Exception e) {
                Log.e(TAG, "Error parsing URL with Android Uri: " + e.getMessage());
                cameraIdentifier = null;
            }
            // --- KẾT THÚC CODE JAVA PHÂN TÍCH URL ---


            // --- Hiển thị CameraImageDialogFragment ---
            // Kiểm tra xem chúng ta có trích xuất được cameraIdentifier không
            if (cameraIdentifier != null) {
                // Tạo instance của DialogFragment và truyền cameraIdentifier
                CameraImageDialogFragment dialogFragment = CameraImageDialogFragment.newInstance(
                        clickedCamera.getName(), // Tên camera cho tiêu đề dialog
                        cameraIdentifier // Truyền camera ID đã trích xuất
                );

                // Hiển thị dialogFragment
                dialogFragment.show(getChildFragmentManager(), "camera_image_dialog");
                Log.d(TAG, "CameraImageDialogFragment shown for ID: " + cameraIdentifier);

                // Trả về true để Google Maps biết bạn đã xử lý sự kiện click
                return true;

            } else {
                // Trường hợp không trích xuất được camera ID từ URL
                Log.e(TAG, "Could not extract camera ID from URL: " + originalPageUrl);
                // Tùy chọn: Hiển thị thông báo lỗi cho người dùng
                Toast.makeText(requireContext(), "Không lấy được thông tin camera.", Toast.LENGTH_SHORT).show();

                // Trả về false để cho phép Info Window mặc định (nếu có) hiện ra
                return false;
            }
        } else {
            // Xử lý trường hợp tag không phải là CameraInfo (ví dụ: marker test)
            Log.w(TAG, "Marker tag is not CameraInfo or is null for clicked marker: " + marker.getTitle());
            return false; // Trả về false để hiển thị Info Window mặc định nếu có
        }
    }

    // --- Phương thức để bắt đầu chu kỳ cập nhật mật độ ---
    private void startDensityUpdates() {
        // Kiểm tra xem Runnable và API service đã sẵn sàng chưa
        if (updateDensityRunnable != null && trafficApiService != null && cameraList != null && !cameraList.isEmpty()) {
            // Lấy danh sách camera IDs để gửi lên API
            List<String> cameraIds = new ArrayList<>();
            for (CameraInfo camera : cameraList) {
                cameraIds.add(camera.getId());
            }
            // Tạo chuỗi camera_ids phân tách bằng dấu phẩy
            String cameraIdsString = String.join(",", cameraIds);

            // Lần fetch đầu tiên ngay lập tức
            fetchTrafficDensityData();

            // Hẹn giờ cho các lần fetch tiếp theo
            handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
            Log.d(TAG, "Density update runnable started.");
        } else {
            Log.w(TAG, "Cannot start density updates: Runnable, API service, or camera list is null/empty.");
        }
    }
    // ---------------------------------------------------

    // --- Phương thức để lấy dữ liệu mật độ giao thông từ API ---
    private void fetchTrafficDensityData() {
        if (trafficApiService == null || cameraList == null || cameraList.isEmpty()) {
            Log.e(TAG, "API service or camera list is null/empty, cannot fetch density data.");
            return;
        }

        // Lấy danh sách camera IDs để gửi lên API
        List<String> cameraIds = new ArrayList<>();
        for (CameraInfo camera : cameraList) {
            cameraIds.add(camera.getId());
        }
        // Tạo chuỗi camera_ids phân tách bằng dấu phẩy
        String cameraIdsString = String.join(",", cameraIds);

        // Tạo cuộc gọi API
        Call<List<TrafficDensityReading>> call = trafficApiService.getLatestTrafficDensities(cameraIdsString);

        Log.d(TAG, "Fetching latest traffic densities from API for IDs: " + cameraIdsString);

        call.enqueue(new Callback<List<TrafficDensityReading>>() {
            @Override
            public void onResponse(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Response<List<TrafficDensityReading>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficDensityReading> readings = response.body();
                    Log.d(TAG, "API Response Successful. Received " + readings.size() + " readings.");

                    // --- Cập nhật Map currentDensities và màu sắc marker ---
                    if (!readings.isEmpty()) {
                        // Xóa dữ liệu cũ trong Map mật độ
                        currentDensities.clear();
                        // Cập nhật Map mật độ với dữ liệu mới
                        for (TrafficDensityReading reading : readings) {
                            currentDensities.put(reading.getCameraId(), reading.getDensity());
                            // Log.d(TAG, "Updated density for ID: " + reading.getCameraId() + " to " + reading.getDensity()); // Log này có thể quá nhiều
                        }
                        // Cập nhật màu sắc các marker trên bản đồ
                        updateMarkerColors();
                    } else {
                        Log.w(TAG, "API Response body is empty.");
                        // Tùy chọn: Xử lý khi không có dữ liệu trả về (ví dụ: giữ nguyên màu marker cũ hoặc đặt về màu mặc định)
                    }
                    // -------------------------------------------------------

                } else {
                    Log.e(TAG, "API Call Failed. Response code: " + response.code() + ", Message: " + response.message());
                    // Tùy chọn: Xử lý lỗi API (ví dụ: hiển thị thông báo, giữ nguyên màu marker)
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Call Failed (Network Error): " + t.getMessage(), t);
                // Tùy chọn: Xử lý lỗi mạng (ví dụ: hiển thị thông báo, giữ nguyên màu marker)
            }
        });
    }
    // ---------------------------------------------------------------------

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        // Bắt đầu chu kỳ cập nhật mật độ khi Fragment hiển thị
        // Kiểm tra nếu Runnable đã được post trước đó để tránh double-posting
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (updateDensityRunnable != null && !handler.hasCallbacks(updateDensityRunnable)) {
                // Nếu cameraList đã được tải trong onCreate/onViewCreated, bắt đầu cập nhật
                if (cameraList != null && !cameraList.isEmpty()) {
                    startDensityUpdates();
                } else {
                    Log.w(TAG, "Camera list is null/empty in onStart, cannot start density updates yet.");
                }
            } else {
                Log.d(TAG, "Density update runnable already scheduled or not ready in onStart, skipping postDelayed.");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Dừng cập nhật khi Fragment bị tạm dừng
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable stopped in onPause.");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView");
        // Đảm bảo dừng Runnable nếu View bị hủy
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable removed in onDestroyView.");
        mMap = null; // Giải phóng GoogleMap
        cameraMarkers.clear(); // Xóa tham chiếu đến markers
        currentDensities.clear(); // Xóa dữ liệu mật độ
        // Không giải phóng cameraList ở đây nếu bạn muốn giữ lại dữ liệu này
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // Giải phóng API service nếu cần
        trafficApiService = null;
    }
}
