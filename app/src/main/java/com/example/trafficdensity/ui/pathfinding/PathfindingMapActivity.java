package com.example.trafficdensity.ui.pathfinding;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.trafficdensity.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.example.trafficdensity.data.graph.GraphData;
import com.example.trafficdensity.CameraInfo;
import com.example.trafficdensity.data.model.Node;
import com.example.trafficdensity.data.model.Edge;
import com.example.trafficdensity.algorithm.AStarPathfinder;
import com.example.trafficdensity.algorithm.DensityPropagator;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import android.location.Location;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.TimeUnit;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import api.TrafficApiService;
import com.example.trafficdensity.api.PathfindingCameraDensity;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Locale;


public class PathfindingMapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private static final String TAG = "PathfindingMapActivity";
    private GoogleMap mMap;
    private GraphData graphData;
    private List<CameraInfo> cameraInfoList;

    private Marker startMarker;
    private Marker endMarker;
    private Node startNode;
    private Node endNode;

    private Polyline pathPolyline;
    private List<Polyline> graphPolylines = new ArrayList<>();

    private FloatingActionButton fabFindPath;
    private FloatingActionButton fabResetMap;

    private TextView textStartName;
    private TextView textStartDensity;
    private TextView textEndName;
    private TextView textEndDensity;
    private TextView textPathResult;

    private static final String BASE_API_URL = "https://571f-34-134-173-55.ngrok-free.app/"; // Vui lòng cập nhật URL này
    private TrafficApiService trafficApiService;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable updateDensityRunnable;
    private static final long UPDATE_DENSITY_INTERVAL_MS = TimeUnit.SECONDS.toMillis(15);

    private Map<String, Marker> nodeMarkers = new HashMap<>();
    private DensityPropagator densityPropagator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pathfinding_map);

        initializeCameraList();

        textStartName = findViewById(R.id.text_start_name);
        textStartDensity = findViewById(R.id.text_start_density);
        textEndName = findViewById(R.id.text_end_name);
        textEndDensity = findViewById(R.id.text_end_density);
        textPathResult = findViewById(R.id.text_path_result);

        graphData = new GraphData(cameraInfoList);
        Log.d(TAG, "GraphData initialized with " + graphData.getNodes().size() + " nodes and " + graphData.getEdges().size() + " edges.");

        densityPropagator = new DensityPropagator();
        Log.d(TAG, "DensityPropagator initialized.");

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_pathfinding);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map Fragment is null. Check layout file and ID: map_pathfinding.");
            Toast.makeText(this, "Lỗi: Không tìm thấy Map Fragment.", Toast.LENGTH_LONG).show();
        }

        fabFindPath = findViewById(R.id.button_find_path);
        fabFindPath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                findPath();
            }
        });
        fabFindPath.setEnabled(false);

        fabResetMap = findViewById(R.id.fab_reset_map);
        if (fabResetMap != null) {
            fabResetMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    resetMapAndSelection();
                }
            });
        }

        if ("https://571f-34-134-173-55.ngrok-free.app/".equals(BASE_API_URL)) {
            Log.w(TAG, "BASE_API_URL is the default Ngrok URL. Please update it with your actual backend URL!");
            Toast.makeText(this, "Cảnh báo: API URL đang dùng mặc định. Vui lòng cập nhật!", Toast.LENGTH_LONG).show();
        }

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        trafficApiService = retrofit.create(TrafficApiService.class);
        Log.d(TAG, "Retrofit and TrafficApiService initialized.");

        updateDensityRunnable = new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "Executing updateDensityRunnable. Attempting to fetch density data.");
                fetchTrafficDensityData();
                handler.postDelayed(this, UPDATE_DENSITY_INTERVAL_MS);
                Log.d(TAG, "Density update runnable rescheduled for " + UPDATE_DENSITY_INTERVAL_MS + "ms.");
            }
        };
    }

    private void initializeCameraList() {
        cameraInfoList = new ArrayList<>();
        cameraInfoList.add(new CameraInfo("56de42f611f398ec0c481291", "Võ Văn Kiệt - Nguyễn Tri Phương (1)", new LatLng(10.7503914, 106.6690747), "url1"));
        cameraInfoList.add(new CameraInfo("56de42f611f398ec0c481297", "Võ Văn Kiệt - Nguyễn Tri Phương (2)", new LatLng(10.7503914, 106.6690747), "url2"));
        cameraInfoList.add(new CameraInfo("56de42f611f398ec0c481293", "Võ Văn Kiệt - Hải Thượng Lãn Ông", new LatLng(10.7499589, 106.6630958), "url3"));
        cameraInfoList.add(new CameraInfo("5b632a79fd4edb0019c7dc0f", "Nguyễn Tri Phương - Trần Hưng Đạo", new LatLng(10.7521932, 106.6695217), "url4"));
        cameraInfoList.add(new CameraInfo("662b4efc1afb9c00172d86bc", "Trần Hưng Đạo - Trần Phú", new LatLng(10.7524870, 106.6678037), "url5"));
        cameraInfoList.add(new CameraInfo("5d8cd1f9766c880017188938", "Nguyễn Tri Phương - Trần Phú", new LatLng(10.7536164, 106.6696310), "url6"));
        cameraInfoList.add(new CameraInfo("5d8cd49f766c880017188944", "Nguyễn Tri Phương - Nguyễn Trãi", new LatLng(10.7546263, 106.6695642), "url7"));
        cameraInfoList.add(new CameraInfo("66b1c190779f740018673ed4", "Nguyễn Trãi - Trần Phú", new LatLng(10.7549314, 106.6716376), "url8"));
        cameraInfoList.add(new CameraInfo("5b632b60fd4edb0019c7dc12", "Hồng Bàng - Ngô Quyền (1)", new LatLng(10.7556201, 106.6663852), "url9"));
        cameraInfoList.add(new CameraInfo("5deb576d1dc17d7c5515ad20", "Hồng Bàng - Ngô Quyền (2)", new LatLng(10.7556201, 106.6663852), "url10"));
        cameraInfoList.add(new CameraInfo("63b3c274bfd3d90017e9ab93", "Hồng Bàng - Phù Đổng Thiên Vương", new LatLng(10.7549775, 106.6625513), "url11"));
        cameraInfoList.add(new CameraInfo("5b728aafca0577001163ff7e", "Hồng Bàng - Châu Văn Liêm", new LatLng(10.7545545, 106.6583560), "url12"));
        cameraInfoList.add(new CameraInfo("662b4e201afb9c00172d85f9", "Hồng Bàng - Tạ Uyên", new LatLng(10.7537439, 106.6537677), "url13"));
        cameraInfoList.add(new CameraInfo("5deb576d1dc17d7c5515ad21", "Nút giao ngã 6 Nguyễn Tri Phương", new LatLng(10.7600016, 106.6688883), "url14"));
        cameraInfoList.add(new CameraInfo("66f126e8538c780017c9362f", "Nguyễn Chí Thanh - Ngô Quyền", new LatLng(10.7592865, 106.6655812), "url15"));
        cameraInfoList.add(new CameraInfo("66f126e8538c7800172d862f", "Nguyễn Chí Thanh - Nguyễn Kim", new LatLng(10.7587157, 106.6627702), "url16"));
        cameraInfoList.add(new CameraInfo("662b4e8e1afb9c00172d865c", "Nguyễn Chí Thanh - Lý Thường Kiệt", new LatLng(10.7584792, 106.6615056), "url17"));
        cameraInfoList.add(new CameraInfo("662b4ecb1afb9c00172d8692", "Nguyễn Chí Thanh - Thuận Kiều", new LatLng(10.7577917, 106.6582849), "url18"));
        cameraInfoList.add(new CameraInfo("5deb576d1dc17d7c5515ad1f", "Hùng Vương - Ngô Gia Tự", new LatLng(10.7564805, 106.6666292), "url19"));
        cameraInfoList.add(new CameraInfo("662b4de41afb9c00172d85c5", "Hải Thượng Lãn Ông - Châu Văn Liêm", new LatLng(10.7506780, 106.6592465), "url20"));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        mMap.setOnMarkerClickListener(this);
        mMap.setOnMapClickListener(this);

        if (graphData != null) {
            drawGraphOnMap();
        } else {
            Log.e(TAG, "GraphData is null in onMapReady. Cannot draw graph.");
            Toast.makeText(this, "Lỗi khởi tạo dữ liệu đồ thị.", Toast.LENGTH_LONG).show();
        }

        LatLng centerOfDistrict5 = new LatLng(10.7600, 106.6690);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(centerOfDistrict5)
                .zoom(14)
                .tilt(45)
                .bearing(0)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        Log.d(TAG, "Map is ready.");
        startDensityUpdates();
    }

    private void drawGraphOnMap() {
        if (mMap == null || graphData == null) {
            Log.e(TAG, "Map or GraphData not initialized. Cannot draw graph.");
            return;
        }

        Log.d(TAG, "Drawing graph on map...");

        mMap.clear();
        nodeMarkers.clear();
        graphPolylines.clear();

        for (Edge edge : graphData.getEdges()) {
            PolylineOptions polylineOptions = new PolylineOptions()
                    .add(new LatLng(edge.getSource().getLatitude(), edge.getSource().getLongitude()))
                    .add(new LatLng(edge.getDestination().getLatitude(), edge.getDestination().getLongitude()))
                    .width(5)
                    .color(Color.GRAY);

            graphPolylines.add(mMap.addPolyline(polylineOptions));
        }
        Log.d(TAG, "Drew " + graphData.getEdges().size() + " edges (polylines) on map.");

        List<Node> nodesToDraw = graphData.getNodes();
        if (nodesToDraw.isEmpty()) {
            Log.w(TAG, "GraphData contains no nodes to draw.");
            return;
        }

        for (Node node : nodesToDraw) {
            // Lấy Node object mới nhất từ graphData.nodeMap
            Node latestNode = graphData.getNodeMap().get(node.getId());
            if (latestNode == null) {
                // Should not happen if graphData.getNodes() returns nodes from nodeMap
                latestNode = node; // Fallback to original node if not found
            }

            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(latestNode.getLatitude(), latestNode.getLongitude()))
                    .title(latestNode.getName())
                    // Cập nhật snippet với density mới nhất
                    .snippet(String.format(Locale.US, "ID: %s, Mật độ: %.2f", latestNode.getId(), latestNode.getDensity()));

            markerOptions.icon(getNodeMarkerIcon(latestNode)); // Sử dụng density mới nhất để chọn icon

            Marker marker = mMap.addMarker(markerOptions);
            if (marker != null) {
                marker.setTag(latestNode); // Đảm bảo marker tag là Node object mới nhất
                nodeMarkers.put(latestNode.getId(), marker);
            } else {
                Log.e(TAG, "Failed to add marker for node: " + latestNode.getId());
            }
        }
        Log.d(TAG, "Drew " + nodeMarkers.size() + " nodes (markers) on map.");

        // Cập nhật lại start/end marker nếu chúng đã được chọn trước đó
        if (startNode != null && nodeMarkers.containsKey(startNode.getId())) {
            startMarker = nodeMarkers.get(startNode.getId());
            startNode = (Node) startMarker.getTag(); // Lấy node đã cập nhật từ tag
            if (startMarker != null) startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        } else {
            startNode = null;
            startMarker = null;
        }
        if (endNode != null && nodeMarkers.containsKey(endNode.getId())) {
            endMarker = nodeMarkers.get(endNode.getId());
            endNode = (Node) endMarker.getTag(); // Lấy node đã cập nhật từ tag
            if (endMarker != null) endMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
        } else {
            endNode = null;
            endMarker = null;
        }

        if (pathPolyline != null) {
            Log.d(TAG, "Path polyline was removed during full redraw. User needs to find path again.");
            pathPolyline = null;
        }
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        Object tag = marker.getTag();
        if (tag instanceof Node) {
            Node originalTaggedNode = (Node) tag;
            Node clickedNode = graphData.getNodeMap().get(originalTaggedNode.getId());
            if (clickedNode == null) {
                Log.e(TAG, "Clicked node ID not found in graphData.nodeMap: " + originalTaggedNode.getId() + ". Using original tag node.");
                clickedNode = originalTaggedNode;
            }

            Log.d(TAG, "Clicked on node: " + clickedNode.getName() + " (ID: " + clickedNode.getId() + ", Density: " + String.format(Locale.US, "%.2f", clickedNode.getDensity()) + ")");

            // Cập nhật lại icon của các marker cũ (nếu có)
            // Đảm bảo rằng startMarker và endMarker luôn trỏ đến marker đúng trên bản đồ
            // và icon của chúng được cập nhật lại theo trạng thái (start/end) hoặc density
            if (startMarker != null && !startMarker.equals(marker)) {
                Node oldStartNode = (Node) startMarker.getTag();
                if (oldStartNode != null) startMarker.setIcon(getNodeMarkerIcon(oldStartNode)); // Reset màu về density
            }
            if (endMarker != null && !endMarker.equals(marker)) {
                Node oldEndNode = (Node) endMarker.getTag();
                if (oldEndNode != null) endMarker.setIcon(getNodeMarkerIcon(oldEndNode)); // Reset màu về density
            }


            if (startNode == null) {
                // Trường hợp 1: Chưa có điểm bắt đầu, đặt clickedNode làm điểm bắt đầu
                startNode = clickedNode;
                startMarker = marker;
                startMarker.setTag(clickedNode); // Cập nhật tag của marker được chọn
                startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)); // Đặt màu xanh cho start
                updateStartInfoUI();
            } else if (endNode == null && !clickedNode.getId().equals(startNode.getId())) {
                // Trường hợp 2: Đã có điểm bắt đầu, chưa có điểm kết thúc, đặt clickedNode làm điểm kết thúc
                endNode = clickedNode;
                endMarker = marker;
                endMarker.setTag(clickedNode); // Cập nhật tag của marker được chọn
                endMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)); // Đặt màu cam cho end
                updateEndInfoUI();
                fabFindPath.setEnabled(true);
            } else if (clickedNode.getId().equals(startNode.getId())) {
                // Trường hợp 3: Click lại vào điểm bắt đầu hiện tại
                // Không làm gì hoặc có thể reset chỉ điểm kết thúc nếu muốn chọn lại đường đi
                Log.d(TAG, "Clicked on current start node. No change in start node.");
                if (endNode != null) {
                    resetEndSelection(); // Chỉ reset điểm kết thúc và đường đi
                }
            } else if (clickedNode.getId().equals(endNode != null ? endNode.getId() : null)) {
                // Trường hợp 4: Click lại vào điểm kết thúc hiện tại
                Log.d(TAG, "Clicked on current end node. No change in end node.");
                // Có thể reset chỉ điểm kết thúc nếu muốn chọn lại đích
                resetEndSelection();
            } else {
                // Trường hợp 5: Đã có cả start và end, click vào một node mới (không phải start/end hiện tại)
                // Reset toàn bộ lựa chọn và đặt node mới làm điểm bắt đầu
                Log.d(TAG, "Clicked on a new node after both start and end were selected. Resetting selection.");
                resetSelection(); // Reset cả startNode và endNode
                startNode = clickedNode;
                startMarker = marker;
                startMarker.setTag(clickedNode); // Cập nhật tag của marker được chọn
                startMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                updateStartInfoUI();
            }
            // Hiển thị toast với mật độ mới nhất
            Toast.makeText(this, String.format(Locale.US, "Node: %s\nMật độ: %.2f", clickedNode.getName(), clickedNode.getDensity()), Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        if (startNode != null || endNode != null) {
            resetSelection();
            Toast.makeText(this, "Lựa chọn điểm đã được reset.", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateStartInfoUI() {
        if (startNode != null) {
            textStartName.setText(startNode.getName());
            textStartDensity.setText(String.format(Locale.US, "Mật độ: %.2f", startNode.getDensity()));
        } else {
            textStartName.setText("Chọn điểm bắt đầu...");
            textStartDensity.setText("Mật độ: N/A");
        }
        textPathResult.setText("Kết quả: Chưa tìm đường");
    }

    private void updateEndInfoUI() {
        if (endNode != null) {
            textEndName.setText(endNode.getName());
            textEndDensity.setText(String.format(Locale.US, "Mật độ: %.2f", endNode.getDensity()));
        } else {
            textEndName.setText("Chọn điểm kết thúc...");
            textEndDensity.setText("Mật độ: N/A");
        }
    }

    private void resetSelection() {
        Log.d(TAG, "Resetting node selection.");
        if (startMarker != null) {
            Node startNodeFromTag = (Node) startMarker.getTag();
            if (startNodeFromTag != null) {
                // Lấy Node mới nhất từ graphData để cập nhật icon đúng màu density
                Node latestStartNode = graphData.getNodeMap().get(startNodeFromTag.getId());
                if (latestStartNode != null) {
                    startMarker.setIcon(getNodeMarkerIcon(latestStartNode));
                } else {
                    startMarker.setIcon(BitmapDescriptorFactory.defaultMarker()); // Fallback
                }
            }
            startMarker = null;
            startNode = null;
            updateStartInfoUI();
        }
        if (endMarker != null) {
            Node endNodeFromTag = (Node) endMarker.getTag();
            if (endNodeFromTag != null) {
                // Lấy Node mới nhất từ graphData để cập nhật icon đúng màu density
                Node latestEndNode = graphData.getNodeMap().get(endNodeFromTag.getId());
                if (latestEndNode != null) {
                    endMarker.setIcon(getNodeMarkerIcon(latestEndNode));
                } else {
                    endMarker.setIcon(BitmapDescriptorFactory.defaultMarker()); // Fallback
                }
            }
            endMarker = null;
            endNode = null;
            updateEndInfoUI();
        }

        if (pathPolyline != null) {
            pathPolyline.remove();
            pathPolyline = null;
        }

        textPathResult.setText("Kết quả: Chưa tìm đường");
        fabFindPath.setEnabled(false);
    }

    private void resetMapAndSelection() {
        Log.d(TAG, "Resetting map and selection.");
        resetSelection();
        drawGraphOnMap(); // Vẽ lại đồ thị để đảm bảo các marker có màu đúng
        Toast.makeText(this, "Bản đồ và lựa chọn đã được đặt lại.", Toast.LENGTH_SHORT).show();
    }
    private void resetEndSelection() {
        Log.d(TAG, "Resetting end node selection and path.");
        if (endMarker != null) {
            Node endNodeFromTag = (Node) endMarker.getTag();
            if (endNodeFromTag != null) {
                // Lấy Node mới nhất từ graphData để cập nhật icon đúng màu density
                Node latestEndNode = graphData.getNodeMap().get(endNodeFromTag.getId());
                if (latestEndNode != null) {
                    endMarker.setIcon(getNodeMarkerIcon(latestEndNode));
                } else {
                    endMarker.setIcon(BitmapDescriptorFactory.defaultMarker()); // Fallback
                }
            }
            endMarker = null;
            endNode = null;
            updateEndInfoUI();
        }

        if (pathPolyline != null) {
            pathPolyline.remove();
            pathPolyline = null;
        }
        fabFindPath.setEnabled(false);
        textPathResult.setText("Kết quả: Chưa tìm đường"); // Reset kết quả đường đi
    }

    private void findPath() {
        if (startNode != null && endNode != null && graphData != null) {
            Log.d(TAG, "Finding path from " + startNode.getName() + " to " + endNode.getName());

            if (pathPolyline != null) {
                pathPolyline.remove();
                pathPolyline = null;
            }

            AStarPathfinder pathfinder = new AStarPathfinder(graphData.getNodeMap(), graphData.getGraphEdgesMap());
            List<Node> path = pathfinder.findPath(startNode, endNode);

            if (path != null && !path.isEmpty()) {
                Log.d(TAG, "Path found with " + path.size() + " nodes.");
                PolylineOptions pathOptions = new PolylineOptions()
                        .width(10)
                        .color(Color.RED);

                StringBuilder pathNames = new StringBuilder("Đường đi: ");
                double totalDistance = 0;
                double totalDensityCost = 0; // Đây là tổng chi phí mật độ (dựa trên weightedCost của A*)

                for (int i = 0; i < path.size(); i++) {
                    Node currentNode = path.get(i);
                    pathOptions.add(new LatLng(currentNode.getLatitude(), currentNode.getLongitude()));
                    pathNames.append(currentNode.getName());

                    if (i < path.size() - 1) {
                        Node nextNode = path.get(i + 1);
                        pathNames.append(" -> ");

                        // Tính toán khoảng cách địa lý cho từng phân đoạn
                        float[] distanceResults = new float[1];
                        Location.distanceBetween(currentNode.getLatitude(), currentNode.getLongitude(),
                                nextNode.getLatitude(), nextNode.getLongitude(), distanceResults);
                        totalDistance += distanceResults[0];

                        // Lấy cạnh giữa currentNode và nextNode để tính chi phí mật độ đã dùng trong A*
                        // Cần tìm cạnh cụ thể nếu muốn totalDensityCost phản ánh chính xác gScore của A*
                        Edge segmentEdge = findEdge(currentNode, nextNode);
                        if (segmentEdge != null) {
                            totalDensityCost += segmentEdge.getWeightedCost(); // Cộng dồn chi phí đã có trọng số density
                        } else {
                            Log.w(TAG, "Edge not found for path segment: " + currentNode.getName() + " -> " + nextNode.getName());
                            // Fallback to simple density cost if edge not found
                            totalDensityCost += (currentNode.getDensity() + nextNode.getDensity()) / 2.0 * distanceResults[0];
                        }
                    }
                }

                pathPolyline = mMap.addPolyline(pathOptions);
                textPathResult.setText(String.format(Locale.US, "Đường đi: %s\nKhoảng cách: %.2f m\nTổng chi phí mật độ: %.2f",
                        pathNames.toString(), totalDistance, totalDensityCost));
                Toast.makeText(this, "Đường đi đã được tìm thấy!", Toast.LENGTH_LONG).show();

                if (path.size() > 1) {
                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                    for (Node node : path) {
                        builder.include(new LatLng(node.getLatitude(), node.getLongitude()));
                    }
                    final LatLngBounds bounds = builder.build();
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
                }
            } else {
                Log.w(TAG, "No path found.");
                textPathResult.setText("Kết quả: Không tìm thấy đường đi");
                Toast.makeText(this, "Không tìm thấy đường đi giữa hai điểm này.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Start or end node not selected, or graph data not available.");
            Toast.makeText(this, "Vui lòng chọn điểm bắt đầu và kết thúc.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Helper method to find an edge between two nodes.
     * @param source The source node.
     * @param destination The destination node.
     * @return The Edge object if found, null otherwise.
     */
    private Edge findEdge(Node source, Node destination) {
        List<Edge> edges = graphData.getGraphEdgesMap().get(source.getId());
        if (edges != null) {
            for (Edge edge : edges) {
                if (edge.getDestination().getId().equals(destination.getId())) {
                    return edge;
                }
            }
        }
        return null;
    }

    private BitmapDescriptor getNodeMarkerIcon(Node node) {
        if (node == null) return BitmapDescriptorFactory.defaultMarker();

        float density = (float) node.getDensity();
        int drawableResId;

        if (node.isCameraNode()) {
            if (density < 0.3) {
                drawableResId = R.drawable.ic_camera_green;
            } else if (density < 0.7) {
                drawableResId = R.drawable.ic_camera_yellow;
            } else {
                drawableResId = R.drawable.ic_camera_red;
            }
        } else {
            // Đối với các node giao lộ không phải camera, chúng ta sử dụng density đã lan truyền
            // và áp dụng cùng logic màu sắc.
            if (density < 0.3) {
                drawableResId = R.drawable.ic_intersection_grey; // Giả sử bạn có icon riêng cho giao lộ
            } else if (density < 0.7) {
                drawableResId = R.drawable.ic_intersection_grey;
            } else {
                drawableResId = R.drawable.ic_intersection_grey;
            }
            // Nếu bạn không có icon riêng cho giao lộ, bạn có thể dùng chung với camera
            // drawableResId = R.drawable.ic_camera_green; // hoặc yellow/red
        }
        return bitmapDescriptorFromVector(this, drawableResId);
    }

    private BitmapDescriptor bitmapDescriptorFromVector(android.content.Context context, int vectorDrawableResourceId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorDrawableResourceId);
        if (vectorDrawable == null) {
            Log.e(TAG, "Could not find drawable resource: " + vectorDrawableResourceId);
            return BitmapDescriptorFactory.defaultMarker();
        }
        int width = vectorDrawable.getIntrinsicWidth();
        int height = vectorDrawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void updateNodeMarkerColors() {
        if (mMap == null || nodeMarkers.isEmpty() || graphData == null || graphData.getNodeMap().isEmpty()) {
            Log.d(TAG, "Cannot update node marker colors: Map, Markers, or NodeMap are empty/null.");
            return;
        }

        Log.d(TAG, "Updating node marker colors based on latest density data...");

        for (Node node : graphData.getNodeMap().values()) {
            Marker marker = nodeMarkers.get(node.getId());

            if (marker != null) {
                if ((startMarker != null && marker.equals(startMarker)) ||
                        (endMarker != null && marker.equals(endMarker))) {
                    marker.setTag(node);
                } else {
                    BitmapDescriptor newIcon = getNodeMarkerIcon(node);
                    if (newIcon != null) {
                        marker.setIcon(newIcon);
                    } else {
                        Log.e(TAG, "Failed to create new icon for node ID: " + node.getId() + " with density: " + node.getDensity());
                    }
                    marker.setTag(node);
                }
                marker.setSnippet(String.format(Locale.US, "ID: %s, Mật độ: %.2f", node.getId(), node.getDensity()));
            } else {
                Log.w(TAG, "Marker not found in nodeMarkers for node ID: " + node.getId() + ". This node might not have a marker or map was cleared.");
            }
        }

        // CẬP NHẬT startNode và endNode với dữ liệu mới nhất
        if (startNode != null) {
            Node updatedStartNode = graphData.getNodeMap().get(startNode.getId());
            if (updatedStartNode != null) {
                startNode = updatedStartNode;
                // Chỉ cập nhật UI density, KHÔNG reset path result
                textStartDensity.setText(String.format(Locale.US, "Mật độ: %.2f", startNode.getDensity()));
            }
        }
        if (endNode != null) {
            Node updatedEndNode = graphData.getNodeMap().get(endNode.getId());
            if (updatedEndNode != null) {
                endNode = updatedEndNode;
                // Chỉ cập nhật UI density, KHÔNG reset path result
                textEndDensity.setText(String.format(Locale.US, "Mật độ: %.2f", endNode.getDensity()));
            }
        }

        Log.d(TAG, "Finished updating node marker colors and UI info.");
    }

    private void startDensityUpdates() {
        if (updateDensityRunnable != null && trafficApiService != null && graphData != null && !graphData.getNodes().isEmpty()) {
            fetchTrafficDensityData();
            handler.postDelayed(updateDensityRunnable, UPDATE_DENSITY_INTERVAL_MS);
            Log.d(TAG, "Density update runnable started and scheduled.");
        } else {
            Log.d(TAG, "Density update runnable already scheduled, not ready, or graph not loaded. Skipping initial postDelayed.");
        }
    }

    private void fetchTrafficDensityData() {
        if (trafficApiService == null || graphData == null || graphData.getNodeMap().isEmpty()) {
            Log.e(TAG, "API service is null or graph nodes are not loaded, cannot fetch density data.");
            return;
        }

        List<String> cameraIds = new ArrayList<>();
        for (Node node : graphData.getNodes()) {
            if (node.isCameraNode()) {
                cameraIds.add(node.getId());
            }
        }

        if (cameraIds.isEmpty()) {
            Log.d(TAG, "No camera nodes found in the graph. Skipping density fetch, but propagating with default values (0.0).");
            densityPropagator.propagateDensity(graphData.getNodeMap(), graphData.getGraphEdgesMap());
            if (mMap != null && !nodeMarkers.isEmpty()) {
                updateNodeMarkerColors();
            }
            return;
        }

        List<String> limitedCameraIds = cameraIds.subList(0, Math.min(cameraIds.size(), 20));
        String cameraIdsString = String.join(",", limitedCameraIds);
        Log.d(TAG, "Requesting densities for " + limitedCameraIds.size() + " cameras: " + cameraIdsString);


        Call<List<PathfindingCameraDensity>> call = trafficApiService.getLatestTrafficDensitiesForPathfinding(cameraIdsString);

        call.enqueue(new Callback<List<PathfindingCameraDensity>>() {
            @Override
            public void onResponse(@NonNull Call<List<PathfindingCameraDensity>> call, @NonNull Response<List<PathfindingCameraDensity>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<PathfindingCameraDensity> readings = response.body();
                    Log.d(TAG, "API Response Successful. Received " + readings.size() + " density readings.");

                    if (!readings.isEmpty()) {
                        int updatedCount = 0;
                        for (PathfindingCameraDensity reading : readings) {
                            // CHUẨN HÓA ID TỪ API TRƯỚC KHI TRA CỨU TRONG nodeMap
                            // Giả sử getCameraId() trả về ID kỹ thuật (ví dụ: _id từ MongoDB)
                            // Nếu nó trả về tên giao lộ, bạn cần normalizeNodeId(reading.getCameraId())
                            // Vì cameraInfoList của bạn dùng ID kỹ thuật, nên chỉ cần lấy ID trực tiếp.
                            // Tuy nhiên, nếu cameraInfoList sử dụng tên giao lộ, thì cần normalize.
                            // Dựa vào các CameraInfo bạn cung cấp (dạng "56de42f611f398ec0c481291"),
                            // có vẻ nó là ID kỹ thuật.

                            // Kiểm tra lại: nếu CameraInfo.getId() của bạn là tên giao lộ
                            // (ví dụ: "Nguyễn Chí Thanh - Nguyễn Kim")
                            // thì cần normalize nó khi khởi tạo CameraInfo
                            // hoặc normalize reading.getCameraId() ở đây.
                            // Dựa trên CameraInfo bạn cung cấp, có vẻ ID là dạng kỹ thuật và không cần normalize
                            // nếu API cũng trả về dạng đó.

                            // Tuy nhiên, để đảm bảo an toàn, hãy luôn chuẩn hóa ID khi truy xuất
                            // nếu có khả năng format khác nhau giữa API và cách lưu trữ.
                            // Nếu camera.getId() trong CameraInfo đã là dạng chuẩn hóa,
                            // và API.getCameraId() cũng là dạng đó, thì normalizeNodeId là không cần thiết.
                            // Nhưng để khắc phục lỗi "Density: 0.00" mà bạn gặp,
                            // có lẽ có sự không khớp trong ID.

                            // Tốt nhất là chắc chắn ID được chuẩn hóa khi thêm vào nodeMap và khi tra cứu
                            // Cách thêm cameraInfoList vào GraphData:
                            // graphData.loadCameraNodes(cameraInfoList); // Hàm này có normalizeNodeId cho CameraInfo.getId()
                            // Vậy, khi lấy từ API, nếu reading.getCameraId() KHÔNG phải ID đã chuẩn hóa,
                            // thì phải chuẩn hóa nó.

                            // Dựa vào log "Updated camera node Nguyễn Chí Thanh - Nguyễn Kim with Density: 0.00"
                            // có thể thấy node.getName() được dùng, và API trả về camera ID.
                            // Vấn đề có thể là ID trong `reading.getCameraId()` không khớp với key trong `nodeMap`
                            // sau khi `graphData.loadCameraNodes` đã chuẩn hóa ID.

                            String apiCameraId = reading.getCameraId(); // Lấy ID từ API
                            Log.d(TAG, "API ID : " + apiCameraId + " Density: " + reading.getDensity());

                            Node node = graphData.getNodeMap().get(apiCameraId);

                            if (node != null && node.isCameraNode()) {
                                node.setDensity(reading.getDensity());
                                updatedCount++;
                                Log.d(TAG, "SUCCESS: Updated camera node " + node.getName() + " (ID: " + node.getId() + ") with Density: " + String.format(Locale.US, "%.2f", node.getDensity()));
                            } else {
                                Log.w(TAG, "WARNING: API returned density for unknown or non-camera node ID (after normalization): " + apiCameraId + " (Original API ID: " + apiCameraId + ")");
                            }
                        }
                        Log.d(TAG, "Updated densities for " + updatedCount + " camera nodes from API.");

                        densityPropagator.propagateDensity(graphData.getNodeMap(), graphData.getGraphEdgesMap());

                        if (mMap != null && !nodeMarkers.isEmpty()) {
                            updateNodeMarkerColors(); // Gọi để cập nhật màu marker và snippet
                        } else {
                            Log.w(TAG, "Map or node markers not ready when attempting to update marker colors after density fetch/propagation.");
                        }

                    } else {
                        Log.w(TAG, "API Response body for density is empty. Propagating with default values (0.0).");
                        densityPropagator.propagateDensity(graphData.getNodeMap(), graphData.getGraphEdgesMap());
                        if (mMap != null && !nodeMarkers.isEmpty()) {
                            updateNodeMarkerColors();
                        }
                    }

                } else {
                    Log.e(TAG, "API Call for density Failed. Response code: " + response.code() + ", Message: " + response.message());
                    densityPropagator.propagateDensity(graphData.getNodeMap(), graphData.getGraphEdgesMap());
                    if (mMap != null && !nodeMarkers.isEmpty()) {
                        updateNodeMarkerColors();
                    }}
            }

            @Override
            public void onFailure(@NonNull Call<List<PathfindingCameraDensity>> call, @NonNull Throwable t) {
                Log.e(TAG, "API Call for density Failed (Network Error): " + t.getMessage(), t);
                densityPropagator.propagateDensity(graphData.getNodeMap(), graphData.getGraphEdgesMap());
                if (mMap != null && !nodeMarkers.isEmpty()) {
                    updateNodeMarkerColors();
                }}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        startDensityUpdates();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        handler.removeCallbacks(updateDensityRunnable);
        Log.d(TAG, "Density update runnable stopped in onPause.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        trafficApiService = null;
    }
}