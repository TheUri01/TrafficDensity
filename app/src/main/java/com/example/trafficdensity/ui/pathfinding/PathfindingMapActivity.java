package com.example.trafficdensity.ui.pathfinding; // Đảm bảo đúng package của bạn

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button; // Import Button if using a regular button
import android.widget.TextView; // Import TextView
import android.widget.Toast; // Import Toast

import com.example.trafficdensity.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton; // Import FloatingActionButton

import com.example.trafficdensity.data.graph.GraphData; // Import GraphData
import com.example.trafficdensity.CameraInfo; // Import CameraInfo
import com.example.trafficdensity.data.model.Node; // Import Node
import com.example.trafficdensity.data.model.Edge; // Import Edge
import com.example.trafficdensity.algorithm.AStarPathfinder; // Import AStarPathfinder

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory; // For custom marker icons
import com.google.android.gms.maps.model.BitmapDescriptor; // Import BitmapDescriptor
import com.google.android.gms.maps.model.CameraPosition; // Import CameraPosition

// --- Imports cho cập nhật mật độ ---
import android.os.Handler; // Import Handler
import android.os.Looper; // Import Looper
import java.util.concurrent.TimeUnit; // Import TimeUnit
// --- Imports cho API Call ---
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService; // Import interface API service
import api.TrafficDensityReading; // Import data class
// --- Import cho Vector Drawable to Bitmap ---
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat; // Sử dụng ContextCompat để lấy Drawable tương thích
// ---------------------------------------------


import java.util.ArrayList;
import java.util.List;
import java.util.Map; // Import Map
import java.util.HashMap; // Import HashMap
import java.util.Locale; // Import Locale


public class PathfindingMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private static final String TAG = "PathfindingMapActivity";
    private GoogleMap mMap;
    private GraphData graphData; // Instance của GraphData
    private List<CameraInfo> cameraInfoList; // Danh sách camera để truyền vào GraphData

    private Marker startMarker;
    private Marker endMarker;
    private Node startNode; // Lưu trữ Node object được chọn làm điểm bắt đầu
    private Node endNode;   // Lưu trữ Node object được chọn làm điểm kết thúc

    private Polyline pathPolyline; // Để lưu trữ polyline của đường đi

    private FloatingActionButton fabFindPath; // Nút tìm đường

    // TextViews để hiển thị điểm bắt đầu/kết thúc và kết quả
    private TextView textStartCamera;
    private TextView textEndCamera;
    private TextView textPathResult;

    // --- Cấu hình API ---
    private static final String BASE_API_URL = "http://f418-35-245-28-142.ngrok-free.app/"; // <-- CẬP NHẬT ĐỊA CHỈ NÀY
    private TrafficApiService trafficApiService;
    // ---------------------

    // --- Handler và Runnable cho cập nhật mật độ định kỳ ---
    private Handler handler = new Handler(Looper.getMainLooper()); // Chạy trên Main Thread
    private Runnable updateDensityRunnable;
    private static final long UPDATE_DENSITY_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15); // Cập nhật mỗi 15 giây
    // ------------------------------------------------------

    // Map để lưu trữ các Marker trên bản đồ, ánh xạ Node ID -> Marker
    private Map<String, Marker> nodeMarkers = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pathfinding_map); // Đảm bảo layout file đúng

        // Ánh xạ TextViews
        textStartCamera = findViewById(R.id.text_start_camera);
        textEndCamera = findViewById(R.id.text_end_camera);
        textPathResult = findViewById(R.id.text_path_result);


        // Khởi tạo danh sách camera (sử dụng dummy data tạm thời)
        // Trong ứng dụng thực tế, dữ liệu này có thể đến từ API hoặc database
        cameraInfoList = createDummyCameraList(); // Hàm tạo dummy data

        // --- Khởi tạo GraphData với danh sách camera ---
        // Đây là nơi bạn cần đảm bảo GraphData xử lý việc tạo node duy nhất cho mỗi vị trí.
        // Logic bên trong constructor hoặc một phương thức buildGraph() của GraphData
        // cần kiểm tra trùng lặp vị trí khi thêm camera và các điểm giao lộ khác.
        graphData = new GraphData(cameraInfoList);
        Log.d(TAG, "GraphData initialized with " + (graphData != null ? graphData.getNodes().size() : 0) + " nodes.");
        // ---------------------------------------------


        // Lấy SupportMapFragment và yêu cầu thông báo khi bản đồ sẵn sàng sử dụng.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_pathfinding); // Đảm bảo ID fragment đúng trong layout
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Ánh xạ nút tìm đường
        fabFindPath = findViewById(R.id.button_find_path); // Đảm bảo ID FAB đúng trong layout
        fabFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPath(); // Gọi hàm tìm đường khi nhấn nút
            }
        });

        // Vô hiệu hóa nút tìm đường ban đầu
        fabFindPath.setEnabled(false);

        // --- Khởi tạo Retrofit và API Service ---
        // Kiểm tra xem BASE_API_URL đã được thay đổi chưa
        if ("YOUR_NGROK_URL_OR_IP/".equals(BASE_API_URL)) {
            Log.e(TAG, "BASE_API_URL is not updated! Please replace 'YOUR_NGROK_URL_OR_IP/' with your actual backend URL.");
            Toast.makeText(this, "Lỗi cấu hình API URL.", Toast.LENGTH_LONG).show();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL) // Đặt Base URL của API
                .addConverterFactory(GsonConverterFactory.create()) // Thêm converter để xử lý JSON
                .build();

        trafficApiService = retrofit.create(TrafficApiService.class); // Tạo instance của API service
        Log.d(TAG, "Retrofit and TrafficApiService initialized.");
        // -----------------------------------------------------------------

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
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Cấu hình bản đồ
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL); // Hoặc MAP_TYPE_HYBRID, MAP_TYPE_SATELLITE, MAP_TYPE_TERRAIN
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true); // Cần quyền truy cập vị trí

        // Đặt listener cho sự kiện click vào marker
        mMap.setOnMarkerClickListener(this);

        // --- Vẽ đồ thị lên bản đồ sau khi bản đồ sẵn sàng ---
        // Đảm bảo graphData đã được khởi tạo trong onCreate()
        if (graphData != null) {
            drawGraphOnMap();
        } else {
            Log.e(TAG, "GraphData is null in onMapReady. Cannot draw graph.");
            // Có thể hiển thị thông báo lỗi cho người dùng
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu đồ thị.", Toast.LENGTH_LONG).show();
        }


        // Di chuyển camera đến một vị trí trung tâm Quận 5 (ví dụ: gần Ngã 6 Nguyễn Tri Phương)
        LatLng centerOfDistrict5 = new LatLng(10.7600, 106.6690);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centerOfDistrict5)      // Đặt trung tâm bản đồ
                .zoom(14)           // Đặt mức zoom
                .tilt(45)           // Đặt góc nghiêng (ví dụ: 45 độ)
                .bearing(0)         // Đặt hướng (ví dụ: 0 độ - hướng Bắc)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Log.d(TAG, "Map is ready.");

        // Bắt đầu cập nhật mật độ sau khi bản đồ sẵn sàng và đồ thị đã được xây dựng
        startDensityUpdates();
    }

    // --- Vẽ đồ thị lên bản đồ ---
    private void drawGraphOnMap() {
        if (mMap == null || graphData == null) {
            Log.e(TAG, "Map or GraphData not initialized. Cannot draw graph.");
            return;
        }

        Log.d(TAG, "Drawing graph on map...");

        // Xóa các marker và polyline cũ (nếu có)
        mMap.clear();
        nodeMarkers.clear(); // Clear the marker map as well

        // Vẽ các Edge (đường nối)
        // Duyệt qua danh sách edges từ GraphData
        // Đảm bảo rằng các edges trong graphData được tạo ra từ các node duy nhất.
        for (Edge edge : graphData.getEdges()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(edge.getSource().getLatitude(), edge.getSource().getLongitude()))
                    .add(new LatLng(edge.getDestination().getLatitude(), edge.getDestination().getLongitude()))
                    .width(5) // Độ dày của đường
                    .color(Color.GRAY); // Màu sắc của đường

            mMap.addPolyline(polylineOptions);
        }
        Log.d(TAG, "Drew " + graphData.getEdges().size() + " edges (polylines) on map.");


        // Vẽ các Node (marker)
        // Duyệt qua danh sách nodes từ GraphData
        // Danh sách này phải chứa các node duy nhất cho mỗi vị trí địa lý.
        List<Node> nodesToDraw = graphData.getNodes();
        if (nodesToDraw.isEmpty()) {
            Log.w(TAG, "GraphData contains no nodes to draw.");
            return; // Không có node nào để vẽ
        }

        for (Node node : nodesToDraw) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(node.getLatitude(), node.getLongitude()))
                    .title(node.getName()) // Tên node làm tiêu đề marker
                    .snippet("ID: " + node.getId() + ", Mật độ: " + String.format(Locale.US, "%.2f", node.getDensity())); // Hiển thị ID và mật độ

            // Tùy chỉnh icon cho node (sử dụng hàm getNodeMarkerIcon)
            markerOptions.icon(getNodeMarkerIcon(node));

            // Lưu trữ Node object trong tag của Marker để dễ dàng truy xuất khi click
            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(node); // Gắn Node object vào tag của marker
                // Lưu marker vào map để dễ dàng cập nhật màu sau này
                nodeMarkers.put(node.getId(), marker);
            } else {
                Log.e(TAG, "Failed to add marker for node: " + node.getId());
            }
        }
        Log.d(TAG, "Drew " + nodeMarkers.size() + " nodes (markers) on map.");
    }

    // --- Xử lý khi click vào Marker ---
    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        // Lấy Node object từ tag của marker
        Object tag = marker.getTag();
        if (tag instanceof Node) {
            Node clickedNode = (Node) tag;
            Log.d(TAG, "Clicked on node: " + clickedNode.getName() + " (ID: " + clickedNode.getId() + ")");

            // Chọn điểm bắt đầu hoặc kết thúc
            if (startNode == null) {
                // Chọn điểm bắt đầu
                startNode = clickedNode;
                startMarker = marker;
                startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); // Đổi màu marker bắt đầu
                textStartCamera.setText("Điểm Bắt đầu: " + startNode.getName()); // Cập nhật TextView
                Log.d(TAG, "Start node selected: " + startNode.getName());
            } else if (endNode == null && !clickedNode.getId().equals(startNode.getId())) {
                // Chọn điểm kết thúc (phải khác điểm bắt đầu)
                endNode = clickedNode;
                endMarker = marker;
                endMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)); // Đổi màu marker kết thúc
                textEndCamera.setText("Điểm Kết thúc: " + endNode.getName()); // Cập nhật TextView
                Log.d(TAG, "End node selected: " + endNode.getName());

                // Kích hoạt nút tìm đường khi cả điểm bắt đầu và kết thúc đã được chọn
                fabFindPath.setEnabled(true);
            } else {
                // Nếu đã chọn cả hai điểm, hoặc click lại vào điểm bắt đầu/kết thúc, reset lựa chọn
                resetSelection();
                // Sau khi reset, nếu click vào marker hiện tại, chọn nó làm điểm bắt đầu mới
                // Điều này cho phép người dùng nhanh chóng chọn lại điểm bắt đầu
                startNode = clickedNode;
                startMarker = marker;
                startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); // Đổi màu marker bắt đầu
                textStartCamera.setText("Điểm Bắt đầu: " + startNode.getName()); // Cập nhật TextView
                Log.d(TAG, "Start node selected after reset: " + startNode.getName());
            }

            // Trả về true để báo hiệu rằng chúng ta đã xử lý sự kiện click
            return true;
        }
        // Trả về false để hành vi mặc định xảy ra (hiển thị info window)
        return false;
    }

    // --- Reset lựa chọn điểm bắt đầu và kết thúc ---
    private void resetSelection() {
        Log.d(TAG, "Resetting node selection.");
        if (startMarker != null) {
            // Đặt lại icon mặc định cho marker bắt đầu dựa trên loại node
            Node startNodeFromTag = (Node) startMarker.getTag();
            if (startNodeFromTag != null) {
                startMarker.setIcon(getNodeMarkerIcon(startNodeFromTag)); // Sử dụng hàm lấy icon cho node
            } else {
                // Fallback về màu mặc định nếu tag bị mất
                startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            startMarker = null;
            startNode = null;
            textStartCamera.setText("Điểm Bắt đầu: Đang chọn..."); // Reset TextView
        }
        if (endMarker != null) {
            // Đặt lại icon mặc định cho marker kết thúc dựa trên loại node
            Node endNodeFromTag = (Node) endMarker.getTag();
            if (endNodeFromTag != null) {
                endMarker.setIcon(getNodeMarkerIcon(endNodeFromTag)); // Sử dụng hàm lấy icon cho node
            } else {
                // Fallback về màu mặc định nếu tag bị mất
                endMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            }
            endMarker = null;
            endNode = null;
            textEndCamera.setText("Điểm Kết thúc: Đang chọn..."); // Reset TextView
        }

        // Xóa polyline đường đi cũ (nếu có)
        if (pathPolyline != null) {
            pathPolyline.remove();
            pathPolyline = null;
        }

        // Reset TextView kết quả
        textPathResult.setText("Kết quả: Chưa tìm đường");

        // Vô hiệu hóa nút tìm đường
        fabFindPath.setEnabled(false);
    }

    // --- Hàm tìm đường (sẽ sử dụng AStarPathfinder) ---
    private void findPath() {
        // Kiểm tra lại xem cả hai điểm đã được chọn và graphData đã sẵn sàng chưa
        if (startNode != null && endNode != null && graphData != null) {
            Log.d(TAG, "Finding path from " + startNode.getName() + " to " + endNode.getName());

            // Xóa polyline đường đi cũ (nếu có) trước khi vẽ đường mới
            if (pathPolyline != null) {
                pathPolyline.remove();
                pathPolyline = null;
            }

            // --- Gọi thuật toán A* ---
            // Khởi tạo AStarPathfinder với dữ liệu đồ thị từ GraphData
            // Đảm bảo graphData.getNodeMap() và graphData.getGraphEdgesMap()
            // chứa dữ liệu đồ thị đã được xử lý trùng lặp node.
            AStarPathfinder pathfinder = new AStarPathfinder(graphData.getNodeMap(), graphData.getGraphEdgesMap());

            // Tìm đường đi (truyền Node object)
            List<Node> path = pathfinder.findPath(startNode, endNode);

            // Hiển thị đường đi trên bản đồ
            if (path != null && !path.isEmpty()) {
                Log.d(TAG, "Path found with " + path.size() + " nodes.");
                PolylineOptions pathOptions = new PolylineOptions()
                        .width(10) // Độ dày của đường đi
                        .color(Color.RED); // Màu đỏ cho đường đi

                for (Node node : path) {
                    pathOptions.add(new LatLng(node.getLatitude(), node.getLongitude()));
                }

                pathPolyline = mMap.addPolyline(pathOptions);
                textPathResult.setText("Kết quả: Tìm thấy đường đi (" + path.size() + " điểm)"); // Cập nhật TextView kết quả
            } else {
                Log.w(TAG, "No path found.");
                textPathResult.setText("Kết quả: Không tìm thấy đường đi"); // Cập nhật TextView kết quả
                Toast.makeText(this, "Không tìm thấy đường đi giữa hai điểm này.", Toast.LENGTH_SHORT).show();
            }

            // Sau khi tìm đường, có thể reset lựa chọn hoặc giữ nguyên tùy ý
            // resetSelection(); // Tùy chọn: reset sau khi tìm đường
        } else {
            Log.w(TAG, "Start or end node not selected, or graph data not available.");
            Toast.makeText(this, "Vui lòng chọn điểm bắt đầu và kết thúc.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- Phương thức để lấy BitmapDescriptor cho marker dựa trên Node ---
    // Nếu là node camera, chọn màu theo mật độ. Nếu không, dùng màu mặc định.
    private BitmapDescriptor getNodeMarkerIcon(Node node) {
        if (node == null) return BitmapDescriptorFactory.defaultMarker(); // Xử lý trường hợp null

        if (node.isCameraNode()) {
            // Nếu là node camera, chọn màu theo mật độ
            float density = node.getDensity();
            // Sử dụng ngưỡng mật độ tương tự như Home Fragment
            if (density < 0.3) { // Mật độ thấp (ví dụ: < 30%)
                return bitmapDescriptorFromVector(this, R.drawable.ic_camera_green); // Icon xanh lá
            } else if (density < 0.7) { // Mật độ trung bình (ví dụ: 30% - 70%)
                return bitmapDescriptorFromVector(this, R.drawable.ic_camera_yellow); // Icon vàng
            } else { // Mật độ cao (ví dụ: > 70%)
                return bitmapDescriptorFromVector(this, R.drawable.ic_camera_red); // Icon đỏ
            }
        } else {
            // Nếu không phải node camera, dùng icon mặc định (ví dụ: chấm tròn màu xám)
            // Bạn cần tạo drawable cho icon node mặc định (ví dụ: ic_intersection_grey.xml)
            return bitmapDescriptorFromVector(this, R.drawable.ic_intersection_grey); // <-- CẦN TẠO DRAWABLE NÀY
            // Hoặc sử dụng marker mặc định của Google Maps với màu tùy chỉnh:
            // return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE); // Ví dụ màu xanh lam nhạt
        }
    }
    // ---------------------------------------------------------------------

    // --- HELPER METHOD: Chuyển Vector Drawable Resource sang BitmapDescriptor ---
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
    // --------------------------------------------------------------------------

    // --- Phương thức để cập nhật màu sắc của các marker hiện có khi có dữ liệu mật độ mới ---
    // Phương thức này chỉ cập nhật màu cho các node camera
    private void updateNodeMarkerColors() {
        if (mMap == null || nodeMarkers.isEmpty() || graphData == null || graphData.getNodeMap().isEmpty()) {
            Log.d(TAG, "Cannot update node marker colors: Map, Markers, or NodeMap are empty/null.");
            return;
        }

        Log.d(TAG, "Updating node marker colors based on latest density data...");

        // Lặp qua tất cả các marker đang có trên bản đồ
        for (Map.Entry<String, Marker> entry : nodeMarkers.entrySet()) {
            String nodeId = entry.getKey();
            Marker marker = entry.getValue();

            // Lấy Node tương ứng từ map trong GraphData
            Node node = graphData.getNodeMap().get(nodeId);

            if (node != null) {
                // Chỉ cập nhật màu nếu node là camera node
                if (node.isCameraNode()) {
                    float density = node.getDensity(); // Lấy mật độ mới nhất từ Node

                    // Lấy icon marker phù hợp với mật độ mới
                    BitmapDescriptor newIcon = getNodeMarkerIcon(node); // Sử dụng hàm chọn icon cho node

                    // Cập nhật icon cho marker trên bản đồ
                    if (marker != null && newIcon != null) {
                        marker.setIcon(newIcon);
                        // Cập nhật snippet để hiển thị mật độ mới nhất
                        marker.setSnippet("ID: " + nodeId + ", Mật độ: " + String.format(Locale.US, "%.2f", density));
                        // Log.d(TAG, "Updated marker color and snippet for node ID: " + nodeId + " with density: " + density); // Log này có thể quá nhiều
                    } else if (marker == null) {
                        Log.w(TAG, "Marker is null for node ID: " + nodeId + " when attempting to update color.");
                    } else { // newIcon == null
                        Log.e(TAG, "Failed to create new icon for node ID: " + nodeId + " with density: " + density + ". Drawable conversion failed.");
                    }
                } else {
                    // Node không phải camera, màu marker không thay đổi dựa trên mật độ
                    // Log.d(TAG, "Node " + nodeId + " is not a camera node, skipping color update.");
                }
            } else {
                Log.w(TAG, "Node not found in map for ID: " + nodeId + " when attempting to update color.");
            }
        }
        Log.d(TAG, "Finished updating node marker colors.");
    }
    // --------------------------------------------------------------------------------------

    // --- Phương thức để bắt đầu chu kỳ cập nhật mật độ định kỳ ---
    private void startDensityUpdates() {
        // Chỉ bắt đầu nếu Runnable, API service đã sẵn sàng và đồ thị đã được xây dựng (graphData không null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (updateDensityRunnable != null && trafficApiService != null && graphData != null && !graphData.getNodes().isEmpty() && !handler.hasCallbacks(updateDensityRunnable)) {
                // Lần fetch đầu tiên ngay lập lập tức
                fetchTrafficDensityData();

                // Hẹn giờ cho các lần fetch tiếp theo
                handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
                Log.d(TAG, "Density update runnable started.");
            } else {
                Log.d(TAG, "Density update runnable already scheduled, not ready, or graph not loaded in onStart, skipping postDelayed.");
            }
        }
    }
    // -----------------------------------------------------------

    // --- Phương thức để lấy dữ liệu mật độ giao thông từ API (chỉ mật độ) ---
    private void fetchTrafficDensityData() {
        if (trafficApiService == null || graphData == null || graphData.getNodes().isEmpty()) {
            Log.e(TAG, "API service is null or graph nodes are not loaded, cannot fetch density data.");
            // Toast.makeText(this, "Chưa tải xong thông tin camera.", Toast.LENGTH_SHORT).show(); // Có thể gây spam toast
            return;
        }

        // Lấy danh sách tất cả camera IDs từ các Node trong đồ thị đã được đánh dấu là camera node
        List<String> cameraIds = new ArrayList<>();
        for (Node node : graphData.getNodes()) {
            if (node.isCameraNode()) {
                cameraIds.add(node.getId());
            }
        }

        if (cameraIds.isEmpty()) {
            Log.d(TAG, "No camera nodes found in the graph. Skipping density fetch.");
            return; // Không có camera nào để fetch mật độ
        }

        // Tạo cuộc gọi API để lấy mật độ cho các camera ID này
        // Bạn cần có một endpoint trong backend trả về danh sách TrafficDensityReading dựa trên danh sách ID
        // và một phương thức tương ứng trong TrafficApiService interface.
        // Giả định phương thức đó là getLatestTrafficDensities(String commaSeparatedCameraIds)
        Call<List<TrafficDensityReading>> call = trafficApiService.getLatestTrafficDensities(String.join(",", cameraIds)); // <-- CẦN TRIỂN KHAI PHƯƠNG THỨC NÀY HOẶC DÙNG PHƯƠNG THỨC CŨ NẾU PHÙ HỢP

        Log.d(TAG, "Fetching latest traffic densities from API for " + cameraIds.size() + " cameras.");

        call.enqueue(new Callback<List<TrafficDensityReading>>() {
            @Override
            public void onResponse(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Response<List<TrafficDensityReading>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<TrafficDensityReading> readings = response.body();
                    Log.d(TAG, "API Response Successful. Received " + readings.size() + " density readings.");

                    if (!readings.isEmpty()) {
                        // --- Cập nhật mật độ cho các Node trong đồ thị đã đánh dấu là camera node ---
                        // Logic này dựa trên việc các node trong graphData đã được tạo duy nhất.
                        Map<String, Float> latestDensitiesMap = new HashMap<>();
                        for (TrafficDensityReading reading : readings) {
                            latestDensitiesMap.put(reading.getCameraId(), reading.getDensity());
                        }

                        int updatedCount = 0;
                        // Lặp qua các Node trong đồ thị và cập nhật mật độ nếu nó là camera node
                        // Sử dụng graphData.getNodeMap() để tra cứu Node nhanh hơn
                        for (String cameraId : latestDensitiesMap.keySet()) {
                            Node node = graphData.getNodeMap().get(cameraId);
                            if (node != null && node.isCameraNode()) {
                                node.setDensity(latestDensitiesMap.get(cameraId)); // Cập nhật mật độ
                                updatedCount++;
                            }
                        }
                        Log.d(TAG, "Updated densities for " + updatedCount + " camera nodes in the graph.");

                        // Cập nhật màu sắc các marker trên bản đồ (chỉ các marker của camera node sẽ thay đổi màu)
                        if (mMap != null && !nodeMarkers.isEmpty()) {
                            updateNodeMarkerColors(); // <-- GỌI PHƯƠNG THỨC CẬP NHẬT MÀU MARKER NODE
                        } else {
                            Log.w(TAG, "Map or node markers not ready when attempting to update marker colors after density fetch.");
                        }

                    } else {
                        Log.w(TAG, "API Response body for density is empty.");
                        // Tùy chọn: Xử lý khi không có dữ liệu mật độ trả về
                    }

                } else {
                    Log.e(TAG, "API Call for density Failed. Response code: " + response.code() + ", Message: " + response.message());
                    // Tùy chọn: Xử lý lỗi API mật độ
                    // Toast.makeText(PathfindingMapActivity.this, "Lỗi tải dữ liệu mật độ: " + response.message(), Toast.LENGTH_SHORT).show(); // Có thể gây spam toast
                }
                // Cập nhật trạng thái nút tìm đường sau khi fetch dữ liệu (đảm bảo pathfinder đã init)
                // Mật độ được cập nhật không ảnh hưởng đến việc init pathfinder, chỉ ảnh hưởng đến kết quả tìm đường
                // updateFindButtonState(); // Không cần gọi ở đây vì mật độ không ảnh hưởng đến việc nút có được enable hay không
            }

            @Override
            public void onFailure(@NonNull Call<List<TrafficDensityReading>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Call for density Failed (Network Error): " + t.getMessage(), t);
                // Tùy chọn: Xử lý lỗi mạng mật độ
                // Toast.makeText(PathfindingMapActivity.this, "Lỗi mạng khi tải dữ liệu mật độ.", Toast.LENGTH_SHORT).show(); // Có thể gây spam toast
                // updateFindButtonState(); // Không cần gọi ở đây
            }
        });
    }
    // ----------------------------------------------------------------------------------


    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        // Bắt đầu chu kỳ cập nhật mật độ khi Activity hiển thị
        // Chỉ bắt đầu nếu Runnable, API service đã sẵn sàng và đồ thị đã được xây dựng (graphData không null)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (updateDensityRunnable != null && trafficApiService != null && graphData != null && !graphData.getNodes().isEmpty() && !handler.hasCallbacks(updateDensityRunnable)) {
                startDensityUpdates();
            } else {
                Log.d(TAG, "Density update runnable already scheduled, not ready, or graph not loaded in onStart, skipping postDelayed.");
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        // Dừng cập nhật khi Activity bị tạm dừng
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable stopped in onPause.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        // Giải phóng API service nếu cần
        trafficApiService = null;
        // Giải phóng pathfinder nếu cần
        // pathfinder = null; // Pathfinder được tạo trong findPath(), không cần giải phóng ở đây
        // Giải phóng GraphData và dữ liệu của nó khi Activity bị hủy hoàn toàn
        graphData = null; // Điều này sẽ giúp Garbage Collector giải phóng nodes, edges, nodeMap
    }


    // --- Hàm tạo danh sách CameraInfo dummy ---
    private List<CameraInfo> createDummyCameraList() {
        List<CameraInfo> dummyList = new ArrayList<>();
        dummyList.add(new CameraInfo("56de42f611f398ec0c481291", "Võ Văn Kiệt - Nguyễn Tri Phương 1", new LatLng(10.7503914, 106.6690747), "url1"));
        dummyList.add(new CameraInfo("56de42f611f398ec0c481297", "Võ Văn Kiệt - Nguyễn Tri Phương 2", new LatLng(10.7504000, 106.6698932), "url2"));
        dummyList.add(new CameraInfo("56de42f611f398ec0c481293", "Võ Văn Kiệt - Hải Thượng Lãn Ông", new LatLng(10.7499589, 106.6630958), "url3"));
        dummyList.add(new CameraInfo("5b632a79fd4edb0019c7dc0f", "Nguyễn Tri Phương - Trần Hưng Đạo", new LatLng(10.7521932, 106.6695217), "url4"));
        dummyList.add(new CameraInfo("662b4efc1afb9c00172d86bc", "Trần Hưng Đạo - Trần Phú", new LatLng(10.7524870, 106.6678037), "url5"));
        dummyList.add(new CameraInfo("5d8cd1f9766c880017188938", "Nguyễn Tri Phương - Trần Phú", new LatLng(10.7536164, 106.6696310), "url6"));
        dummyList.add(new CameraInfo("5d8cd49f766c880017188944", "Nguyễn Tri Phương - Nguyễn Trãi", new LatLng(10.7546263, 106.6695642), "url7"));
        dummyList.add(new CameraInfo("66b1c190779f740018673ed4", "Nguyễn Trãi - Trần Phú", new LatLng(10.7549314, 106.6716376), "url8"));
        dummyList.add(new CameraInfo("5b632b60fd4edb0019c7dc12", "Hồng Bàng - Ngô Quyền 1", new LatLng(10.7556201, 106.6663852), "url9"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad20", "Hồng Bàng - Ngô Quyền 2", new LatLng(10.7561475, 106.6661542), "url10"));
        dummyList.add(new CameraInfo("63b3c274bfd3d90017e9ab93", "Hồng Bàng - Phù Đổng Thiên Vương", new LatLng(10.7549775, 106.6625513), "url11"));
        dummyList.add(new CameraInfo("5b728aafca0577001163ff7e", "Hồng Bàng - Châu Văn Liêm", new LatLng(10.7545545, 106.6583560), "url12"));
        dummyList.add(new CameraInfo("662b4e201afb9c00172d85f9", "Hồng Bàng - Tạ Uyên", new LatLng(10.7537439, 106.6537677), "url13"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad21", "Nút giao ngã 6 Nguyễn Tri Phương", new LatLng(10.7600016, 106.6688883), "url14"));
        dummyList.add(new CameraInfo("66f126e8538c780017c9362f", "Nguyễn Chí Thanh - Ngô Quyền", new LatLng(10.7592865, 106.6655812), "url15"));
        dummyList.add(new CameraInfo("66f126e8538c7800172d862f", "Nguyễn Chí Thanh - Nguyễn Kim", new LatLng(10.7587157, 106.6627702), "url16")); // Sửa ID trùng lặp
        dummyList.add(new CameraInfo("662b4e8e1afb9c00172d865c", "Nguyễn Chí Thanh - Lý Thường Kiệt", new LatLng(10.7584792, 106.6615056), "url17"));
        dummyList.add(new CameraInfo("662b4ecb1afb9c00172d8692", "Nguyễn Chí Thanh - Thuận Kiều", new LatLng(10.7577917, 106.6582849), "url18"));
        dummyList.add(new CameraInfo("5deb576d1dc17d7c5515ad1f", "Hùng Vương - Ngô Gia Tự", new LatLng(10.7564805, 106.6666292), "url19"));
        dummyList.add(new CameraInfo("662b4de41afb9c00172d85c5", "Hải Thượng Lãn Ông - Châu Văn Liêm", new LatLng(10.7506780, 106.6592465), "url20"));
        return dummyList;
    }
}
