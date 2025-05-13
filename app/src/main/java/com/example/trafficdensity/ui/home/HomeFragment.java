package com.example.trafficdensity.ui.home;

import android.annotation.SuppressLint;

import org.pytorch.Device;
import org.pytorch.Module;
import org.pytorch.PyTorchAndroid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.core.content.ContextCompat;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.trafficdensity.CameraInfo;
import android.Manifest;
import com.example.trafficdensity.R;
import com.example.trafficdensity.databinding.FragmentHomeBinding;
import com.example.trafficdensity.ui.dialog.CameraImageDialogFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;



public class HomeFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap googleMap;

    private Module model;

    private Device device;
    private FragmentHomeBinding binding;
    // TextView vvk1, vvk2; // Khai báo nếu bạn dùng TextView trong Fragment
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String TAG = "HomeFragment"; // Tag cho Log

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Setup map fragment using ChildFragmentManager
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            Log.d(TAG, "Map Fragment found, requesting map asynchronously using getChildFragmentManager().");
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Error: Map Fragment with ID R.id.map not found in layout used by HomeFragment.");
        }

        // findViews(); // Gọi nếu bạn có phương thức này để tìm các View khác

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // --- Phương thức Helper để tạo BitmapDescriptor từ Resource và Resize ---
    private BitmapDescriptor bitmapDescriptorFromResource(int resId, int width, int height) {
        try {
            // Lấy drawable từ resource ID
            android.graphics.drawable.Drawable vectorDrawable = getResources().getDrawable(resId);

            // Tạo Bitmap trống với kích thước mong muốn
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            vectorDrawable.draw(canvas);

            // Tạo BitmapDescriptor từ Bitmap
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        } catch (Exception e) {
            Log.e(TAG, "Error creating BitmapDescriptor from resource: " + e.getMessage());
            return null; // Trả về null nếu có lỗi
        }
    }
    // ------------------------------------------------------------------------


    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        Log.d(TAG, "onMapReady is called in HomeFragment.");

        if (googleMap == null) {
            Log.e(TAG, "Error - GoogleMap is null in onMapReady of HomeFragment!");
            return;
        }

        enableMyLocation();
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        // --- THÊM MARKER CHO 20 CAMERA VÀO BẢN ĐỒ VỚI ICON ĐÃ RESIZE ---

        // Danh sách các vị trí camera (Dữ liệu mẫu)
        List<CameraInfo> cameraList = new ArrayList<>();
        // ... thêm 20 camera vào cameraList như trước ...
        cameraList.add(new CameraInfo("56de42f611f398ec0c481291", "Võ Văn Kiệt - Nguyễn Tri Phương 1", new LatLng(10.7503914, 106.6690747), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481291&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%201&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("56de42f611f398ec0c481297", "Võ Văn Kiệt - Nguyễn Tri Phương 2", new LatLng(10.7504000, 106.6698932), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481297&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%202&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("56de42f611f398ec0c481293", "Võ Văn Kiệt - Hải Thượng Lãn Ông", new LatLng(10.7499589, 106.6630958), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=56de42f611f398ec0c481293&camLocation=V%C3%B5%20V%C4%83n%20Ki%E1%BB%87t%20-%20H%E1%BA%A3i%20Th%C6%B0%E1%BB%A3ng%20L%C3%A3n%20%C3%94ng%201&camMode=camera&videoUrl=http://camera.thongtingiaothong.vn/s/56de42f611f398ec0c481293/index.m3u8"));
        cameraList.add(new CameraInfo("5b632a79fd4edb0019c7dc0f", "Nguyễn Tri Phương - Trần Hưng Đạo", new LatLng(10.7521932, 106.6695217), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b632a79fd4edb0019c7dc0f&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Tr%E1%BA%A7n%20H%C6%B0ng%20%C4%90%E1%BA%A1o&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("662b4efc1afb9c00172d86bc", "Trần Hưng Đạo - Trần Phú", new LatLng(10.7524870, 106.6678037), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4efc1afb9c00172d86bc&camLocation=Tr%E1%BA%A7n%20H%C6%B0ng%20%C4%90%E1%BA%A1o%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5d8cd1f9766c880017188938", "Nguyễn Tri Phương - Trần Phú", new LatLng(10.7536164, 106.6696310), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5d8cd1f9766c880017188938&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5d8cd49f766c880017188944", "Nguyễn Tri Phương - Nguyễn Trãi", new LatLng(10.7546263, 106.6695642), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5d8cd49f766c880017188944&camLocation=Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng%20-%20Nguy%E1%BB%85n%20Tr%C3%A3i&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("66b1c190779f740018673ed4", "Nguyễn Trãi - Trần Phú", new LatLng(10.7549314, 106.6716376), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=66b1c190779f740018673ed4&camLocation=Nguy%E1%BB%85n%20Tr%C3%A3i%20-%20Tr%E1%BA%A7n%20Ph%C3%BA&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5b632b60fd4edb0019c7dc12", "Hồng Bàng - Ngô Quyền 1", new LatLng(10.7556201, 106.6663852), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b632b60fd4edb0019c7dc12&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ng%C3%B4%20Quy%E1%BB%81n%201&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5deb576d1dc17d7c5515ad20", "Hồng Bàng - Ngô Quyền 2", new LatLng(10.7561475, 106.6661542), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad20&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ng%C3%B4%20Quy%E1%BB%81n%202&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("63b3c274bfd3d90017e9ab93", "Hồng Bàng - Phù Đổng Thiên Vương", new LatLng(10.7549775, 106.6625513), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=63b3c274bfd3d90017e9ab93&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ph%C3%B9%20%C4%90%E1%BB%95ng%20Thi%C3%AAn%20V%C6%B0%C6%A0ng&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5b728aafca0577001163ff7e", "Hồng Bàng - Châu Văn Liêm", new LatLng(10.7545545, 106.6583560), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5b728aafca0577001163ff7e&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20Ch%C3%A2u%20V%C4%83n%20Li%C3%AAm&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("662b4e201afb9c00172d85f9", "Hồng Bàng - Tạ Uyên", new LatLng(10.7537439, 106.6537677), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4e201afb9c00172d85f9&camLocation=H%E1%BB%93ng%20B%C3%A0ng%20-%20T%E1%BA%A1%20Uy%C3%AAn&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5deb576d1dc17d7c5515ad21", "Nút giao ngã 6 Nguyễn Tri Phương", new LatLng(10.7600016, 106.6688883), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad21&camLocation=N%C3%BAt%20giao%20Ng%C3%A3%20s%C3%A1u%20Nguy%E1%BB%85n%20Tri%20Ph%C6%B0%C6%A0ng&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("66f126e8538c780017c9362f", "Nguyễn Chí Thanh - Ngô Quyền", new LatLng(10.7592865, 106.6655812), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=649da419a6068200171a6c90&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Ng%C3%B4%20Quy%E1%BB%81n&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("66f126e8538c780017c9362f", "Nguyễn Chí Thanh - Nguyễn Kim", new LatLng(10.7587157, 106.6627702), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=66f126e8538c780017c9362f&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Nguy%E1%BB%85n%20Kim&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("662b4e8e1afb9c00172d865c", "Nguyễn Chí Thanh - Lý Thường Kiệt", new LatLng(10.7584792, 106.6615056), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4e8e1afb9c00172d865c&camLocation=L%C3%BD%20Th%C6%B0%E1%BB%9Dng%20Ki%E1%BB%87t%20-%20Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("662b4ecb1afb9c00172d8692", "Nguyễn Chí Thanh - Thuận Kiều", new LatLng(10.7577917, 106.6582849), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=662b4ecb1afb9c00172d8692&camLocation=Nguy%E1%BB%85n%20Ch%C3%AD%20Thanh%20-%20Thu%E1%BA%ADn%20Ki%E1%BB%83u&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("5deb576d1dc17d7c5515ad1f", "Hùng Vương - Ngô Gia Tự", new LatLng(10.7564805, 106.6666292), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad1f&camLocation=H%C3%B9ng%20V%C6%B0%C6%A0ng%20-%20Ng%C3%B4%20Gia%20T%E1%BB%B1&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));
        cameraList.add(new CameraInfo("662b4de41afb9c00172d85c5", "Hải Thượng Lãn Ông - Châu Văn Liêm", new LatLng(10.7506780, 106.6592465), "https://giaothong.hochiminhcity.gov.vn/expandcameraplayer/?camId=5deb576d1dc17d7c5515ad1f&camLocation=H%C3%B9ng%20V%C6%B0%C6%A0ng%20-%20Ng%C3%B4%20Gia%20T%E1%BB%B1&camMode=camera&videoUrl=https://d2zihajmogu5jn.cloudfront.net/bipbop-advanced/bipbop_16x9_variant.m3u8"));


        // --- TẠO ICON TÙY CHỈNH ĐÃ RESIZE ---
        // Thay R.drawable.ic_camera_marker bằng ID tài nguyên icon của bạn
        int iconResId = R.drawable.ic_camera_marker; // Đảm bảo bạn đã có icon này

        int iconWidth = 30;
        int iconHeight = 30;


        BitmapDescriptor cameraIcon = null;
        try {
            cameraIcon = bitmapDescriptorFromResource(iconResId, iconWidth, iconHeight);
            if (cameraIcon != null) {
                Log.d(TAG, "Custom icon resized and created successfully with size " + iconWidth + "x" + iconHeight);
            } else {
                Log.e(TAG, "bitmapDescriptorFromResource returned null.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error resizing or creating custom icon: " + e.getMessage());
        }


        // --- THÊM MARKER VÀO BẢN ĐỒ VỚI ICON ĐÃ RESIZE ---
        if (cameraIcon != null) {
            for (CameraInfo camera : cameraList) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(camera.getLocation()) // Đặt vị trí marker
                        .title(camera.getName()); // Đặt tiêu đề
                // .snippet(...) // Thông tin phụ

                // Sử dụng icon đã resize
                markerOptions.icon(cameraIcon);

                // Thêm marker vào bản đồ
                Marker addedMarker = googleMap.addMarker(markerOptions);
                if (addedMarker != null) {
                    addedMarker.setTag(camera); // Có thể lưu thông tin CameraInfo vào tag
                    // Log...
                } else {
                    Log.e(TAG, "Failed to add marker for: " + camera.getName());
                }
            }
            Log.d(TAG, "All camera markers added with resized icon.");
        } else {
            Log.e(TAG, "Camera icon is null, using default markers or cannot add markers.");
            // Nếu icon bị lỗi, bạn có thể thêm marker với icon mặc định ở đây
            for (CameraInfo camera : cameraList) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(camera.getLocation())
                        .title(camera.getName());
                // Không gọi .icon() hoặc gọi .icon(BitmapDescriptorFactory.defaultMarker());
                googleMap.addMarker(markerOptions);
            }
            Log.d(TAG, "Added markers with default icons due to custom icon error.");
        }
        // KẾT THÚC THÊM MARKER


        // Di chuyển camera đến trung tâm của khu vực Quận 5
        LatLng quan5Center = new LatLng(10.7590, 106.6650);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quan5Center, 14f));
        Log.d(TAG, "Moved camera to Quan 5 center.");

        googleMap.setTrafficEnabled(true);
        Log.d(TAG, "Traffic layer enabled.");

        // Thiết lập listener khi click vào marker (Tùy chọn)
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Log.d(TAG, "Marker clicked: " + marker.getTitle());

        Object tag = marker.getTag();
        if (tag instanceof CameraInfo) {
            CameraInfo clickedCamera = (CameraInfo) tag;
            Log.d(TAG, "Clicked Camera: " + clickedCamera.getName() + ", Original URL: " + clickedCamera.getVideoUrl());


            String originalPageUrl = clickedCamera.getVideoUrl();

            String cameraIdentifier = null; // Biến để lưu camId hoặc id
            String m3u8StreamUrl = null;   // Biến để lưu giá trị của tham số videoUrl

            try {
                // Bước 1: Phân tích cú pháp URL gốc thành đối tượng Uri
                Uri uri = Uri.parse(originalPageUrl);

                // Bước 2: Lấy giá trị của tham số query 'camId'
                cameraIdentifier = uri.getQueryParameter("camId");

                // Nếu không tìm thấy 'camId', thử lấy giá trị của tham số 'id'
                if (cameraIdentifier == null) {
                    cameraIdentifier = uri.getQueryParameter("id");
                }

                // Log kết quả phân tích để kiểm tra
                Log.d(TAG, "Parsed URL - camId/id: " + cameraIdentifier);

            } catch (Exception e) {
                // Xử lý lỗi nếu quá trình phân tích URL gặp vấn đề (ví dụ: URL không hợp lệ)
                Log.e(TAG, "c: " + e.getMessage());
                // Đặt giá trị null hoặc giá trị mặc định nếu có lỗi
                cameraIdentifier = null;

            }

            CameraImageDialogFragment dialogFragment = CameraImageDialogFragment.newInstance(
                    clickedCamera.getName(), // Tên camera cho tiêu đề dialog
                    // Truyền URL m3u8 đã trích xuất nếu có, ngược lại truyền URL gốc (hoặc null, tùy logic của bạn)
                    cameraIdentifier
            );

            // Hiển thị dialogFragment
            dialogFragment.show(getChildFragmentManager(), "camera_image_dialog");
            Log.d(TAG, "CameraImageDialogFragment shown for ID: " + cameraIdentifier);
            return true;

        } else {
            // Xử lý trường hợp tag không phải là CameraInfo (ví dụ: marker test)
            Log.w(TAG, "Marker tag is not CameraInfo or is null for clicked marker: " + marker.getTitle());
            return false; // Trả về false để hiển thị Info Window mặc định nếu có
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền vị trí đã được cấp thành công
                Log.d(TAG, "Location permission granted by user.");
                // Quyền đã có, bây giờ kiểm tra dịch vụ vị trí và bật My Location
                enableMyLocation(); // Gọi lại phương thức để kiểm tra và bật
            } else {
                // Quyền vị trí bị từ chối
                Log.w(TAG, "Location permission denied by user.");
                // Tùy chọn: Hiển thị thông báo rằng tính năng định vị không dùng được
                // Toast.makeText(requireContext(), "Không thể hiển thị vị trí của bạn trên bản đồ vì quyền bị từ chối.", Toast.LENGTH_LONG).show();
                View rootView = getView(); // Lấy View gốc của Fragment
                if (rootView != null) {
                    Snackbar.make(rootView, "Không thể hiển thị vị trí của bạn. Vui lòng cấp quyền truy cập vị trí.", Snackbar.LENGTH_LONG).show();
                }
            }
        }
        // Xử lý kết quả của các yêu cầu quyền khác nếu có
    }
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // Bước 1: Kiểm tra xem quyền ACCESS_FINE_LOCATION đã được cấp chưa
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            // Quyền đã được cấp -> Tiếp tục kiểm tra trạng thái dịch vụ vị trí hệ thống
            Log.d(TAG, "Location permission already granted.");
            LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

            // Kiểm tra xem bất kỳ nhà cung cấp vị trí nào (GPS, Network) đã được bật chưa
            // isLocationEnabled() là cách mới hơn và tốt hơn cho API 28+
            boolean isLocationServiceEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                    locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            // Đối với API 28+, có thể dùng cách này ngắn gọn hơn:
            // boolean isLocationServiceEnabled = locationManager.isLocationEnabled();

            if (!isLocationServiceEnabled) {
                // Bước 2a: Dịch vụ vị trí hệ thống CHƯA được bật
                Log.w(TAG, "System location services are disabled.");

                // Hiển thị thông báo nhắc nhở người dùng bật vị trí
                View rootView = getView(); // Lấy View gốc của Fragment để hiển thị Snackbar
                if (rootView != null) {
                    Snackbar snackbar = Snackbar.make(rootView, "Để hiển thị vị trí của bạn, vui lòng bật dịch vụ vị trí của thiết bị.", Snackbar.LENGTH_LONG);
                    snackbar.setAction("Cài đặt", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Mở cài đặt vị trí của hệ thống
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivity(intent);
                        }
                    });
                    snackbar.show();
                } else {
                    // Quyền và dịch vụ đã bật
                    if (googleMap != null) {
                        // Bật lớp My Location (chấm xanh)
                        googleMap.setMyLocationEnabled(true);
                        Log.d(TAG, "My Location layer enabled.");

                        // --- ĐẢM BẢO NÚT MY LOCATION ĐƯỢC BẬT (MẶC ĐỊNH LÀ BẬT) ---
                        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
                        Log.d(TAG, "My Location Button explicitly enabled.");
                        // -------------------------------------------------------

                        // ... animate camera if needed (phức tạp hơn, cần theo dõi vị trí) ...

                    } else {
                        Log.e(TAG, "googleMap is null, cannot enable My Location layer/button.");
                    }
                }

                // KHÔNG bật lớp My Location trên bản đồ vì dịch vụ hệ thống chưa sẵn sàng

            } else {
                // Bước 2b: Dịch vụ vị trí hệ thống đã BẬT
                Log.d(TAG, "System location services are enabled.");

                // Quyền đã được cấp VÀ dịch vụ hệ thống đã bật -> Bật lớp My Location trên bản đồ
                if (googleMap != null) {
                    googleMap.setMyLocationEnabled(true);
                    Log.d(TAG, "My Location layer enabled and services are active.");
                    // Tùy chọn: Di chuyển camera đến vị trí hiện tại của người dùng lần đầu
                    // Việc lấy Last Known Location cần thêm LocationProviderClient và check permission lần nữa, phức tạp hơn.
                    // Tuy nhiên, khi setMyLocationEnabled(true), nút "My Location" sẽ xuất hiện và người dùng có thể nhấn vào đó.
                } else {
                    Log.e(TAG, "googleMap is null when trying to enable My Location.");
                }
            }

        } else {
            // Bước 1b: Quyền chưa được cấp -> Yêu cầu người dùng cấp quyền
            Log.w(TAG, "Location permission not granted yet, requesting...");
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private String assetFilePath(Context context, String assetName) throws IOException {
        File file = new File(context.getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = context.getAssets().open(assetName)) {
            try (FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
                fos.flush();
            }
            return file.getAbsolutePath();
        }
    }

    private void loadModel() {
        try {
            // Tên file model trong thư mục assets
            String modelAssetName = "YOLO8n_v1_0001.pt";
             // Tên file model của bạn

            // Lấy đối tượng AssetManager từ Context của Fragment
            AssetManager assetManager = requireContext().getAssets();
            model = PyTorchAndroid.loadModuleFromAsset(assetManager, modelAssetName);
            Log.d(TAG, "Model PyTorch loaded successfully.");

        } catch (Exception e) {
            // Bắt các lỗi khác có thể xảy ra trong quá trình tải model
            Log.e(TAG, "Unexpected error loading PyTorch model", e);
        }
    }
}