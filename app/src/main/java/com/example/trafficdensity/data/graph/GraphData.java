package com.example.trafficdensity.data.graph;

import com.example.trafficdensity.data.model.Node;
import com.example.trafficdensity.data.model.Edge;
import com.example.trafficdensity.CameraInfo; // Đảm bảo bạn có class CameraInfo này
import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale;

public class GraphData {

    private static final String TAG = "GraphData";

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Map<String, Node> nodeMap = new HashMap<>(); // Sử dụng để tra cứu Node theo ID
    private Map<String, List<Edge>> graphEdges = new HashMap<>(); // Danh sách kề cho đồ thị

    // Constructor sẽ xây dựng đồ thị mô phỏng dựa trên CameraInfo và các giao lộ ước lượng
    public GraphData(List<CameraInfo> cameraInfoList) {
        // Bước 1: Tải tất cả các Node từ danh sách định nghĩa sẵn (Location Node.docx).
        loadNodesFromDocx();

        // Bước 2: Tải và xử lý các Node có camera, ánh xạ với node gần nhất
        mapCameraInfoToNodes(cameraInfoList);

        // Bước 3: Thêm tất cả các cạnh vào đồ thị (sau khi tất cả Node đã được thêm/cập nhật).
        addAllEdges();
    }

    public Map<String, List<Edge>> getGraphEdgesMap() {
        return graphEdges;
    }

    /**
     * Phương thức để tải Node từ nội dung file Location Node.docx.
     * Khởi tạo trực tiếp các Node bằng lệnh addNode().
     * Các node này mặc định không có camera (hasCamera = false) ban đầu.
     */
    private void loadNodesFromDocx() {
        Log.d(TAG, "Loading all nodes directly from predefined list (intersection nodes)...");

        // Chuẩn hóa ID Node ngay khi khởi tạo
        // Tên hiển thị (name) có thể giữ nguyên để dễ đọc hoặc bạn có thể chuẩn hóa nó nếu muốn.
        // Mặc định tất cả các node này đều không có camera (false)
        addNode(new Node(normalizeNodeId("Nguyễn Thị Nhỏ- Nguyễn Chí Thanh"), "Nguyễn Thị Nhỏ - Nguyễn Chí Thanh", 10.756380, 106.651143, false));
        addNode(new Node(normalizeNodeId("Nguyễn Thị Nhỏ-Tân Thành"), "Nguyễn Thị Nhỏ - Tân Thành", 10.755275, 106.650992, false));
        addNode(new Node(normalizeNodeId("NTN - HBàng"), "Nguyễn Thị Nhỏ - Hồng Bàng", 10.753520, 106.650820, false));
        addNode(new Node(normalizeNodeId("NTN-TCChiếu"), "Nguyễn Thị Nhỏ - Trần Chánh Chiếu", 10.752342, 106.650691, false));
        addNode(new Node(normalizeNodeId("NTN-TTử"), "Nguyễn Thị Nhỏ - Trang Tử", 10.751497, 106.650566, false));
        addNode(new Node(normalizeNodeId("NgCThanh- HTQ"), "Nguyễn Chí Thanh - Hà Tôn Quyền", 10.756636, 106.652819, false));
        addNode(new Node(normalizeNodeId("HTQ-TT"), "Hà Tôn Quyền - Tân Thành", 10.755537, 106.652903, false));
        addNode(new Node(normalizeNodeId("HTQ -HB"), "Hà Tôn Quyền - Hồng Bàng", 10.753628, 106.652996, false));
        addNode(new Node(normalizeNodeId("Võ Trường Toản - HB"), "Võ Trường Toản - Hồng Bàng", 10.753569, 106.651767, false));
        addNode(new Node(normalizeNodeId("Xóm Vôi -NT"), "Xóm Vôi - Nguyễn Trãi", 10.753056, 106.651746, false));
        addNode(new Node(normalizeNodeId("Xóm Vôi - Trần Chánh Chiêu"), "Xóm Vôi - Trần Chánh Chiêu", 10.752268, 106.651791, false));
        addNode(new Node(normalizeNodeId("Xóm Vôi -Trang Tử"), "Xóm Vôi - Trang Tử", 10.751494, 106.651806, false));
        addNode(new Node(normalizeNodeId("Tạ Uyên - Nguyễn Chí Thanh"), "Tạ Uyên - Nguyễn Chí Thanh", 10.756785, 106.653652, false));
        addNode(new Node(normalizeNodeId("Tạ Uyên - Tân Thành"), "Tạ Uyên - Tân Thành", 10.755678, 106.653716, false));
        addNode(new Node(normalizeNodeId("Tạ Uyên - Phạm Hữu Chí"), "Tạ Uyên - Phạm Hữu Chí", 10.754972, 106.653748, false));
        addNode(new Node(normalizeNodeId("Tạ Uyên - Hồng Bàng"), "Tạ Uyên - Hồng Bàng", 10.753865, 106.653802, false)); // Camera 13 (Hồng Bàng - Tạ Uyên) gần đây
        addNode(new Node(normalizeNodeId("PG-NT"), "Phú Giáo - Nguyễn Trãi", 10.752720, 106.652852, false));
        addNode(new Node(normalizeNodeId("PG-TCC"), "Phú Giáo - Trần Chánh Chiếu", 10.752172, 106.652741, false));
        addNode(new Node(normalizeNodeId("PG-TT"), "Phú Giáo - Trang Tử", 10.751607, 106.652588, false));
        addNode(new Node(normalizeNodeId("PH-NT"), "Phú Hữu - Nguyễn Trãi", 10.752790, 106.653523, false));
        addNode(new Node(normalizeNodeId("PHung-NT"), "Phùng Hưng - Nguyễn Trãi", 10.752914, 106.657413, false));
        addNode(new Node(normalizeNodeId("PH-TCC"), "Phú Hữu - Trần Chánh Chiếu", 10.752124, 106.653387, false));
        addNode(new Node(normalizeNodeId("PH-TrangTử"), "Phú Hữu - Trang Tử", 10.751652, 106.653255, false));
        addNode(new Node(normalizeNodeId("DTG-NCT"), "Dương Tử Giang - Nguyễn Chí Thanh", 10.757012, 106.654541, false));
        addNode(new Node(normalizeNodeId("DTG-TT"), "Dương Tử Giang - Tân Thành", 10.755837, 106.654743, false));
        addNode(new Node(normalizeNodeId("DTG-PHC"), "Dương Tử Giang - Phạm Hữu Chí", 10.755191, 106.654848, false));
        addNode(new Node(normalizeNodeId("DTG-TH"), "Dương Tử Giang - Tân Hưng", 10.754508, 106.654981, false));
        addNode(new Node(normalizeNodeId("DTG-HB"), "Dương Tử Giang - Hồng Bàng", 10.754056, 106.655071, false));
        addNode(new Node(normalizeNodeId("DTG-NgT"), "Dương Tử Giang - Nguyễn Trãi", 10.752835, 106.655259, false));
        addNode(new Node(normalizeNodeId("DTG-THD"), "Dương Tử Giang - Trần Hưng Đạo", 10.752007, 106.655392, false));
        addNode(new Node(normalizeNodeId("DTG-HTLO"), "Dương Tử Giang - Hải Thượng Lãn Ông", 10.751061, 106.655474, false));
        addNode(new Node(normalizeNodeId("NNT-LVS"), "Ngô Nhân Tịnh -Lê Quang Sung", 10.751082, 106.653309, false));
        addNode(new Node(normalizeNodeId("NNT-HTLO"), "Ngô Nhân Tịnh - Hải Thượng Lãn Ông", 10.750465, 106.653392, false));
        addNode(new Node(normalizeNodeId("NNT-PVK"), "Ngô Nhân Tịnh - Phan Văn Khỏe", 10.748933, 106.653767, false));
        addNode(new Node(normalizeNodeId("NNT-BS"), "Ngô Nhân Tịnh - Bãi Sậy", 10.748574, 106.653866, false));
        addNode(new Node(normalizeNodeId("NNT-GP"), "Ngô Nhân Tịnh - Gia Phú", 10.747490, 106.654303, false));
        addNode(new Node(normalizeNodeId("NNT-VVK"), "Ngô Nhân Tịnh - Võ Văn Kiệt", 10.746744, 106.654735, false));
        addNode(new Node(normalizeNodeId("TT-LVS"), "Trang Tử - Lê Quang Sung", 10.751135, 106.653769, false));
        addNode(new Node(normalizeNodeId("HL-HB"), "Học Lạc - Hồng Bàng", 10.753736, 106.654388, false));
        addNode(new Node(normalizeNodeId("HL-NT"), "Học Lạc - NT", 10.752875, 106.654413, false));
        addNode(new Node(normalizeNodeId("HL-THD"), "Học Lạc - Trần Hưng Đạo", 10.751977, 106.654414, false));
        addNode(new Node(normalizeNodeId("HL- HTLO"), "Học Lạc - Hải Thượng Lãn Ông", 10.751106, 106.654422, false));
        addNode(new Node(normalizeNodeId("GC-HTLO"), "Gò Công - Hải Thượng Lãn Ông", 10.750751, 106.654526, false));
        addNode(new Node(normalizeNodeId("GC-PVK"), "Gò Công - Phan Văn Khỏe", 10.749130, 106.654970, false));
        addNode(new Node(normalizeNodeId("GC-BS"), "Gò Công - Bãi Sậy", 10.748743, 106.655062, false));
        addNode(new Node(normalizeNodeId("GC-GP"), "Gò Công - Gia Phú", 10.747910, 106.655300, false));
        addNode(new Node(normalizeNodeId("GC-VVK"), "Gò Công - Võ Văn Kiệt", 10.747198, 106.655528, false));
        addNode(new Node(normalizeNodeId("DNT-NCT"), "Đỗ Ngọc Thạch- Nguyễn Chí Thanh", 10.757318, 106.656008, false));
        addNode(new Node(normalizeNodeId("DNT-TT"), "Đỗ Ngọc Thạch - Tân Thành", 10.756068, 106.656067, false));
        addNode(new Node(normalizeNodeId("DNT-PHC"), "Đỗ Ngọc Thạch - Phạm Hữu Chí", 10.755417, 106.656079, false));
        addNode(new Node(normalizeNodeId("DNT-TH"), "Đỗ Ngọc Thạch - Tân Hưng", 10.754744, 106.656109, false));
        addNode(new Node(normalizeNodeId("DNT-HB"), "Đỗ Ngọc Thạch - Hồng Bàng", 10.754250, 106.656125, false));
        addNode(new Node(normalizeNodeId("DNT-NT"), "Đỗ Ngọc Thạch - Nguyễn Trãi", 10.752893, 106.656131, false));
        addNode(new Node(normalizeNodeId("DNT-THD"), "Đỗ Ngọc Thạch - Trần Hưng Đạo", 10.751988, 106.656049, false));
        addNode(new Node(normalizeNodeId("DNT-HTLO"), "Đỗ Ngọc Thạch - Hải Thượng Lãn Ông", 10.751029, 106.655963, false));
        addNode(new Node(normalizeNodeId("KB-HTLO"), "Kim Biên - Hải Thượng Lãn Ông", 10.750758, 106.655718, false));
        addNode(new Node(normalizeNodeId("KB-PVK"), "Kim Biên - Phan Văn Khỏe", 10.749289, 106.655907, false));
        addNode(new Node(normalizeNodeId("KB-VVK"), "Kim Biên - Võ Văn Kiệt", 10.747780, 106.656543, false));
        addNode(new Node(normalizeNodeId("VT-VCH"), "Vạn Tượng - Vũ Chí Hiếu", 10.750258, 106.656057, false));
        addNode(new Node(normalizeNodeId("VT-PH"), "Vạn Tượng - Phùng Hưng", 10.749700, 106.656139, false));
        addNode(new Node(normalizeNodeId("VT-THD"), "Vạn Tượng - Trần Hưng Đạo", 10.749115, 106.656298, false));
        addNode(new Node(normalizeNodeId("VT-VVK"), "Vạn Tượng - Võ Văn Kiệt", 10.747900, 106.656799, false));
        addNode(new Node(normalizeNodeId("PCD-NCT"), "Phó Cơ Điều-Nguyễn Chí Thanh", 10.757514, 106.657118, false));
        addNode(new Node(normalizeNodeId("PCD-TT"), "Phó Cơ Điều - Tân Thành", 10.756247, 106.657168, false));
        addNode(new Node(normalizeNodeId("PCD-PHC"), "Phó Cơ Điều - Phạm Hữu Chí", 10.755592, 106.657178, false));
        addNode(new Node(normalizeNodeId("PH-HB"), "Phùng Hưng - Hồng Bàng", 10.754254, 106.657307, false));
        addNode(new Node(normalizeNodeId("PH-LT"), "Phùng Hưng - Lão Tử", 10.753569, 106.657380, false));
        addNode(new Node(normalizeNodeId("PH-NT"), "Phùng Hưng - Nguyễn Trãi", 10.752950, 106.657412, false));
        addNode(new Node(normalizeNodeId("PH-THDuc"), "Phùng Hưng - Trịnh Hoài Đức", 10.749347, 106.657461, false));
        addNode(new Node(normalizeNodeId("PH-HTLO"), "Phùng Hưng - Hải Thượng Lãng Ông", 10.750930, 106.657408, false));
        addNode(new Node(normalizeNodeId("PH-VCH"), "Phùng Hưng - Vũ Chí Hiếu", 10.750260, 106.657412, false));
        addNode(new Node(normalizeNodeId("PH-THD"), "Phùng Hưng - Trần Hưng Đạo", 10.751966, 106.657507, false)); // Đổi ID để tránh trùng lặp
        addNode(new Node(normalizeNodeId("PH-VVK"), "Phùng Hưng - Võ Văn Kiệt", 10.748298, 106.657650, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ -Nguyễn Trãi"), "Nguyễn Văn Cừ - Nguyễn Trãi", 10.759393, 106.683999, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ -An Dương Vương"), "Nguyễn Văn Cừ -An Dương Vương", 10.761153, 106.683287, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ - Hùng Vương"), "Nguyễn Văn Cừ - Hùng Vương", 10.765392, 106.681374, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ - Phan Văn Trị"), "Nguyễn Văn Cừ - Phan Văn Trị", 10.757955, 106.684545, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ -Trần Hưng Đạo"), "Nguyễn Văn Cừ -Trần Hưng Đạo", 10.756368, 106.685074, false));
        addNode(new Node(normalizeNodeId("Nguyễn Văn Cừ - Võ Văn Kiệt"), "Nguyễn Văn Cừ - Võ Văn Kiệt", 10.754050, 106.686635, false));
        addNode(new Node(normalizeNodeId("NgBieu - VVK"), "Nguyễn Biểu - Võ Văn Kiệt", 10.751950, 106.683855, false));
        addNode(new Node(normalizeNodeId("NgBieu - CDat"), "Nguyễn Biểu - Cao Đạt", 10.754668, 106.684143, false));
        addNode(new Node(normalizeNodeId("NgBieu - THD"), "Nguyễn Biểu - Trần Hưng Đạo", 10.755956, 106.683697, false));
        addNode(new Node(normalizeNodeId("NgBieu - PhanVanTri"), "Nguyễn Biểu - Phan Văn Trị", 10.757360, 106.683173, false));
        addNode(new Node(normalizeNodeId("NgBieu - NgTrai"), "Nguyễn Biểu - Nguyễn Trãi", 10.758932, 106.682609, false));
        addNode(new Node(normalizeNodeId("NgTrai - TBTrong"), "Nguyễn Trãi - Trần Bình Trọng", 10.758314, 106.680747, false));
        addNode(new Node(normalizeNodeId("TBTrong - ADV"), "Trần Bình Trọng - An Dương Vương",10.759837, 106.680153, false));
        addNode(new Node(normalizeNodeId("TBTrong - THD"), "Trần Bình Trọng - Trần Hưng Đạo",10.755411, 106.681397, false));
        addNode(new Node(normalizeNodeId("TBTrong - VVK"), "Trần Bình Trọng - Võ Văn Kiệt",10.752227, 106.682138, false));

        addNode(new Node(normalizeNodeId("TBTrong - TranPhu"), "Trần Bình Trọng - Trần Phú", 10.762590, 106.679082, false));
        addNode(new Node(normalizeNodeId("TBTrong - HV"), "Trần Bình Trọng - Hùng Vương", 10.763634, 106.678649, false));
        addNode(new Node(normalizeNodeId("HV - LHPhong"), "Hùng Vương - Lê Hồng Phong", 10.762191, 106.676446, false));
        addNode(new Node(normalizeNodeId("LHPhong - TranPhu"), "Lê Hồng Phong - Trần Phú", 10.760491, 106.677076, false));
        addNode(new Node(normalizeNodeId("LHPhong - ADV"), "Lê Hồng Phong - An Dương Vương", 10.758859, 106.677653, false));
        addNode(new Node(normalizeNodeId("LHPhong - NgTrai"), "Lê Hồng Phong - Nguyễn Trãi", 10.757267, 106.678238, false));
        addNode(new Node(normalizeNodeId("LHPhong - PVTri"), "Lê Hồng Phong - Phan Văn Trị", 10.755941, 106.678500, false));
        addNode(new Node(normalizeNodeId("LHP - THD"), "Lê Hồng Phong - Trần Hưng Đạo", 10.754814, 106.678948, false));
        addNode(new Node(normalizeNodeId("VVK - HMD"), "Võ Văn Kiệt - Huỳnh Mẫn Đạt", 10.752155, 106.677204, false));
        addNode(new Node(normalizeNodeId("HMD - NgThuc"), "Huỳnh Mẫn Đạt - Nghĩa Thục", 10.753357, 106.677047, false));
        addNode(new Node(normalizeNodeId("HMD - THD"), "Huỳnh Mẫn Đạt - Trần Hưng Đạo", 10.754158, 106.676849, false));
        addNode(new Node(normalizeNodeId("HMD - PVTri"), "Huỳnh Mẫn Đạt - Phan Văn Trị", 10.755339, 106.676525, false));
        addNode(new Node(normalizeNodeId("HMD - NgTrai"), "Huỳnh Mẫn Đạt - Nguyễn Trãi", 10.756377, 106.676263, false));
        addNode(new Node(normalizeNodeId("HMD - ADV"), "Huỳnh Mẫn Đạt - An Dương Vương", 10.758125, 106.675743, false));
        addNode(new Node(normalizeNodeId("HMD - TPhu"), "Huỳnh Mẫn Đạt - Trần Phú", 10.758862, 106.675537, false));
        addNode(new Node(normalizeNodeId("TNTon - HV"), "Trần Nhân Tôn - Hùng Vương", 10.761204, 106.674974, false));
        addNode(new Node(normalizeNodeId("SVH - HV"), "Sư Vạn Hạnh - Hùng Vương", 10.760166, 106.673288, false));
        addNode(new Node(normalizeNodeId("SVH - TP - ADV"), "Sư Vạn Hạnh - Trần Phú - An Dương Vương", 10.757476, 106.673949, false));
        addNode(new Node(normalizeNodeId("NgDuyDuong - NgChiThanh"), "Nguyễn Duy Dương - Nguyễn Chí Thanh", 10.760293, 106.671063, false));
        addNode(new Node(normalizeNodeId("SVH - NgChiThanh"), "Sư Vạn Hạnh - Nguyễn Chí Thanh", 10.760747, 106.673165, false));
        addNode(new Node(normalizeNodeId("56de42f611f398ec0c481291"), "Võ Văn Kiệt - Nguyễn Tri Phương (1)", 10.7503914, 106.6690747, true));
        addNode(new Node(normalizeNodeId("56de42f611f398ec0c481297"), "Võ Văn Kiệt - Nguyễn Tri Phương (2)", 10.7504000, 106.6698932, true));
        addNode(new Node(normalizeNodeId("56de42f611f398ec0c481293"), "Võ Văn Kiệt - Hải Thượng Lãn Ông", 10.7499589, 106.6630958, true));
        addNode(new Node(normalizeNodeId("5b632a79fd4edb0019c7dc0f"), "Nguyễn Tri Phương - Trần Hưng Đạo", 10.7521932, 106.6695217, true));
        addNode(new Node(normalizeNodeId("662b4efc1afb9c00172d86bc"), "Trần Hưng Đạo - Trần Phú", 10.7524870, 106.6678037, true));
        addNode(new Node(normalizeNodeId("5d8cd1f9766c880017188938"), "Nguyễn Tri Phương - Trần Phú", 10.7536164, 106.6696310, true));
        addNode(new Node(normalizeNodeId("5d8cd49f766c880017188944"), "Nguyễn Tri Phương - Nguyễn Trãi", 10.7546263, 106.6695642, true));
        addNode(new Node(normalizeNodeId("66b1c190779f740018673ed4"), "Nguyễn Trãi - Trần Phú", 10.7549314, 106.6716376, true));
        addNode(new Node(normalizeNodeId("5b632b60fd4edb0019c7dc12"), "Hồng Bàng - Ngô Quyền (1)", 10.7556201, 106.6663852, true));
        addNode(new Node(normalizeNodeId("5deb576d1dc17d7c5515ad20"), "Hồng Bàng - Ngô Quyền (2)", 10.7561475, 106.6661542, true));
        addNode(new Node(normalizeNodeId("63b3c274bfd3d90017e9ab93"), "Hồng Bàng - Phù Đổng Thiên Vương", 10.7549775, 106.6625513, true));
        addNode(new Node(normalizeNodeId("5b728aafca0577001163ff7e"), "Hồng Bàng - Châu Văn Liêm", 10.7545545, 106.6583560, true));
        addNode(new Node(normalizeNodeId("662b4e201afb9c00172d85f9"), "Hồng Bàng - Tạ Uyên", 10.7537439, 106.6537677, true));
        addNode(new Node(normalizeNodeId("5deb576d1dc17d7c5515ad21"), "Nút giao ngã 6 Nguyễn Tri Phương", 10.7600016, 106.6688883, true));
        addNode(new Node(normalizeNodeId("649da419a6068200171a6c90"), "Nguyễn Chí Thanh - Ngô Quyền", 10.7592865, 106.6655812, true));
        addNode(new Node(normalizeNodeId("66f126e8538c7800172d862f"), "Nguyễn Chí Thanh - Nguyễn Kim", 10.7587157, 106.6627702, true));
        addNode(new Node(normalizeNodeId("662b4e8e1afb9c00172d865c"), "Nguyễn Chí Thanh - Lý Thường Kiệt", 10.7584792, 106.6615056, true));
        addNode(new Node(normalizeNodeId("662b4ecb1afb9c00172d8692"), "Nguyễn Chí Thanh - Thuận Kiều", 10.7577917, 106.6582849, true));
        addNode(new Node(normalizeNodeId("5deb576d1dc17d7c5515ad1f"), "Hùng Vương - Ngô Gia Tự", 10.7564805, 106.6666292, true));
        addNode(new Node(normalizeNodeId("662b4de41afb9c00172d85c5"), "Hải Thượng Lãn Ông - Châu Văn Liêm", 10.7506780, 106.6592465, true));
        addNode(new Node(normalizeNodeId("YK - HV"), "Yết Kiêu - Hùng Vương", 10.759634, 106.672456, false));
        addNode(new Node(normalizeNodeId("YK - ADV"), "Yết Kiêu - An Dương Vương", 10.757344, 106.672986, false));
        addNode(new Node(normalizeNodeId("NgTrai - BHNghia"), "Nguyễn Trãi - Bùi Hữu Nghĩa", 10.755520, 106.674277, false));
        addNode(new Node(normalizeNodeId("BHNghia - PVTri"), "Bùi Hữu Nghĩa - Phan Văn Trị", 10.754621, 106.674514, false));
        addNode(new Node(normalizeNodeId("BHNghia - THD"), "Bùi Hữu Nghĩa - Trần Hưng Đạo", 10.753613, 106.674773, false));
        addNode(new Node(normalizeNodeId("BHNghia - NgThuc"), "Bùi Hữu Nghĩa - Nghĩa Thục", 10.752711, 106.675027, false));
        addNode(new Node(normalizeNodeId("BHNghia - BachVan"), "Bùi Hữu Nghĩa - Bạch Vân", 10.751649, 106.675292, false));
        addNode(new Node(normalizeNodeId("BHNghia - DaoTan"), "Bùi Hữu Nghĩa - Đào Tấn", 10.750771, 106.675534, false));
        addNode(new Node(normalizeNodeId("TTKhai - VVK"), "Trần Tuấn Khải - Võ Văn Kiệt", 10.749738, 106.675387, false));
        addNode(new Node(normalizeNodeId("TTKhai - BachVan"), "Trần Tuấn Khải - Bạch Vân", 10.751375, 106.674421, false));
        addNode(new Node(normalizeNodeId("TTKhai - NgThuc"), "Trần Tuấn Khải - Nghĩa Thục", 10.752362, 106.673899, false));
        addNode(new Node(normalizeNodeId("TTKhai - THD"), "Trần Tuấn Khải - Trần Hưng Đạo", 10.753249, 106.673429, false));
        addNode(new Node(normalizeNodeId("NgTrai - TPhu"), "Nguyễn Trãi - Trần Phú", 10.755063, 106.671965, false)); // Camera 8 gần đây
        addNode(new Node(normalizeNodeId("NgDuyDuong - ADV"), "Nguyễn Duy Dương - An Dương Vương", 10.757070, 106.671796, false));
        addNode(new Node(normalizeNodeId("NgDuyDuong - HV"), "Nguyễn Duy Dương - Hùng Vương", 10.758906, 106.671370, false));
        addNode(new Node(normalizeNodeId("NgTrPhuong - HV"), "Nguyễn Tri Phương - Hùng Vương", 10.757693, 106.669411, false));
        addNode(new Node(normalizeNodeId("NgTrPhuong - ADV"), "Nguyễn Tri Phương - An Dương Vương", 10.756674, 106.669588, false));
        addNode(new Node(normalizeNodeId("NgTrPhuong - NgTrai"), "Nguyễn Tri Phương - Nguyễn Trãi", 10.754641, 106.669570, false)); // Camera 7 gần đây
        addNode(new Node(normalizeNodeId("NgTriPhuong - TPhu"), "Nguyễn Tri Phương - Trần Phú", 10.753436, 106.669527, false)); // Camera 6 gần đây
        addNode(new Node(normalizeNodeId("NgTriPhuong - THD"), "Nguyễn Tri Phương - Trần Hưng Đạo", 10.752711, 106.669460, false)); // Camera 4 gần đây
        addNode(new Node(normalizeNodeId("NgTriPhuong - VVK"), "Nguyễn Tri Phương - Võ Văn Kiệt", 10.750857, 106.669424, false)); // Camera 1 và 2 gần đây
        addNode(new Node(normalizeNodeId("AB - NgTrai"), "An Bình - Nguyễn Trãi", 10.754832, 106.670862, false));
        addNode(new Node(normalizeNodeId("AB - TPhu"), "An Bình - Trần Phú", 10.754451, 106.671231, false));
        addNode(new Node(normalizeNodeId("AB - THD"), "An Bình - Trần Hưng Đạo", 10.752945, 106.672096, false));
        addNode(new Node(normalizeNodeId("AB - BachVan"), "An Bình - Bạch Vân", 10.750982, 106.672968, false));
        addNode(new Node(normalizeNodeId("AB - VVK"), "An Bình - Võ Văn Kiệt", 10.749401, 106.673420, false));
        addNode(new Node(normalizeNodeId("NVDung - VVK"), "Nguyễn Văn Đừng - Võ Văn Kiệt", 10.750557, 106.671180, false));
        addNode(new Node(normalizeNodeId("NVDung - THD"), "Nguyễn Văn Đừng - Trần Hưng Đạo", 10.752826, 106.671110, false));
        addNode(new Node(normalizeNodeId("NQ - VVK"), "Ngô Quyền - Võ Văn Kiệt", 10.750792, 106.666877, false));
        addNode(new Node(normalizeNodeId("NQ - THD"), "Ngô Quyền - Trần Hưng Đạo", 10.752460, 106.666757, false)); // Camera 5 gần đây
        addNode(new Node(normalizeNodeId("NQ - NgTrai"), "Ngô Quyền - Nguyễn Trãi", 10.753962, 106.666609, false));
        addNode(new Node(normalizeNodeId("NQ - MTT"), "Ngô Quyền - Mạc Thiên Tích", 10.755139, 106.666454, false));
        addNode(new Node(normalizeNodeId("NQ - HV - NGT - ADV"), "Ngô Quyền - Hùng Vương - Ngô Gia Tự - An Dương Vương", 10.756212, 106.666227, false)); // Camera 9 và 10 gần đây
        addNode(new Node(normalizeNodeId("NQ - AnDiem"), "Ngô Quyền - AnDiem", 10.751762, 106.666824, false));
        addNode(new Node(normalizeNodeId("NQ - NgChiThanh"), "Ngô Quyền - Nguyễn Chí Thanh", 10.759199, 106.665592, false)); // Camera 15 gần đây
        addNode(new Node(normalizeNodeId("Ngã 6 Nguyễn Tri Phương"), "Ngã 6 Nguyễn Tri Phương", 10.759863, 106.668913, false)); // Camera 14 gần đây
        addNode(new Node(normalizeNodeId("NK - NgChiThanh"), "Nguyễn Kim - Nguyễn Chí Thanh", 10.758635, 106.662749, false)); // Camera 16 gần đây
        addNode(new Node(normalizeNodeId("NK - TangBatHo"), "Nguyễn Kim - Tăng Bạt Hổ", 10.758009, 106.662859, false));
        addNode(new Node(normalizeNodeId("NK - BaTrieu"), "Nguyễn Kim - Bà Triệu", 10.757403, 106.662999, false));
        addNode(new Node(normalizeNodeId("NK - PHChi"), "Nguyễn Kim - Phạm Hữu Chí", 10.756814, 106.663149, false));
        addNode(new Node(normalizeNodeId("NK - HB "), "Nguyễn Kim - Hồng Bàng", 10.755546, 106.663392, false));
        addNode(new Node(normalizeNodeId("HB - TanDa"), "Hồng Bàng - Tản Đà", 10.755483, 106.663793, false));
        addNode(new Node(normalizeNodeId("TanDa- NgTrai"), "Tản Đà - Nguyễn Trãi", 10.753553, 106.664182, false));
        addNode(new Node(normalizeNodeId("TanDa - THD"), "Tản Đà - Trần Hưng Đạo", 10.752248, 106.664390, false));
        addNode(new Node(normalizeNodeId("TanDa - VVK"), "Tản Đà - Võ Văn Kiệt", 10.750664, 106.664770, false));
        addNode(new Node(normalizeNodeId("TanDa - TanHang"), "Tản Đà - TanHang", 10.751542, 106.664533, false));
        addNode(new Node(normalizeNodeId("LTK - NgChiThanh"), "Lý Thường Kiệt - Nguyễn Chí Thanh", 10.758372, 106.661468, false)); // Camera 17 gần đây
        addNode(new Node(normalizeNodeId("LTK - TangBatHo"), "Lý Thường Kiệt - Tăng Bạt Hổ", 10.757785, 106.661667, false));
        addNode(new Node(normalizeNodeId("LTK - BaTieu"), "Lý Thường Kiệt - Bà Triệu", 10.757161, 106.661857, false));
        addNode(new Node(normalizeNodeId("LTK - PHChi"), "Lý Thường Kiệt - Phạm Hữu Chí", 10.756639, 106.662009, false));
        addNode(new Node(normalizeNodeId("LTK - HB"), "Lý Thường Kiệt - Hồng Bàng", 10.755371, 106.662398, false)); // Camera 11 (Hồng Bàng - Phù Đổng Thiên Vương) gần đây
        addNode(new Node(normalizeNodeId("PĐTV - NgTrai"), "Phù Đổng Thiên Vương - Nguyễn Trãi", 10.753299, 106.662842, false));
        addNode(new Node(normalizeNodeId("TranHoa - THD"), "Trần Hòa - Trần Hưng Đạo", 10.752138, 106.662753, false));
        addNode(new Node(normalizeNodeId("PhamDon - THD"), "Phạm Đôn - Trần Hưng Đạo", 10.752187, 106.663292, false));
        addNode(new Node(normalizeNodeId("PhamDon - TanHang"), "Phạm Đôn - TanHang", 10.751521, 106.663334, false));
        addNode(new Node(normalizeNodeId("TQP - VVK"), "Triệu Quang Phục - Võ Văn Kiệt", 10.749661, 106.661570, false));
        addNode(new Node(normalizeNodeId("TQP - HTLO"), "Triệu Quang Phục - Hải Thượng Lãn Ông", 10.751137, 106.661477, false));
        addNode(new Node(normalizeNodeId("TQP - THD"), "Triệu Quang Phục - Trần Hưng Đạo", 10.752032, 106.661462, false));
        addNode(new Node(normalizeNodeId("TQP - HB"), "Triệu Quang Phục - Hồng Bàng", 10.755049, 106.661505, false));
        addNode(new Node(normalizeNodeId("TQP - PHChi"), "Triệu Quang Phục - Phạm Hữu Chí", 10.756312, 106.661180, false));
        addNode(new Node(normalizeNodeId("LNH - HB"), "Lương Nhữ Học - Hồng Bàng", 10.754952, 106.660344, false));
        addNode(new Node(normalizeNodeId("LHN - LaoTu"), "Lương Nhữ Học - Lão Tử", 10.753838, 106.660136, false));
        addNode(new Node(normalizeNodeId("LNH - NgTrai"), "Lương Nhữ Học - Nguyễn Trãi", 10.753033, 106.659999, false));
        addNode(new Node(normalizeNodeId("LNH - THD"), "Lương Nhữ Học - Trần Hưng Đạo", 10.752019, 106.660041, false));
        addNode(new Node(normalizeNodeId("LNH - HTLO"), "Lương Nhữ Học - Hải Thượng Lãn Ông", 10.750541, 106.660324, false));
        addNode(new Node(normalizeNodeId("ThuanKieu - NgChiThanh"), "Thuận Kiều - Nguyễn Chí Thanh", 10.757741, 106.658293, false)); // Camera 18 gần đây
        addNode(new Node(normalizeNodeId("ThuanKieu- TanThanh"), "Thuận Kiều- TanThanh", 10.756445, 106.658372, false));
        addNode(new Node(normalizeNodeId("ThuanKieu - PHChi"), "Thuận Kiều - Phạm Hữu Chí", 10.755768, 106.658397, false));
        addNode(new Node(normalizeNodeId("ThuanKieu - TanHung"), "Thuận Kiều - Tân Hưng", 10.755113, 106.658393, false));
        addNode(new Node(normalizeNodeId("CVL - HB"), "Châu Văn Liêm - Hồng Bàng", 10.754541, 106.658402, false)); // Camera 12 (Hồng Bàng - Châu Văn Liêm) gần đây
        addNode(new Node(normalizeNodeId("CVL - NgTrai"), "Châu Văn Liêm - Nguyễn Trãi", 10.752999, 106.658748, false));
        addNode(new Node(normalizeNodeId("CVL - THD"), "Châu Văn Liêm - Trần Hưng Đạo", 10.752025, 106.658948, false));
        addNode(new Node(normalizeNodeId("CVL - HTLO - NguyenThi - MacCuu"), "Châu Văn Liêm - Hải Thượng Lãn Ông - Nguyễn Thi - Mạc Cửu", 10.750867, 106.659203, false));
        addNode(new Node(normalizeNodeId("NguyenThi - VVK"), "Nguyễn Thi - Võ Văn Kiệt", 10.748696, 106.658845, false));
        addNode(new Node(normalizeNodeId("MacCuu-VVK"), "Mạc Cửu - Võ Văn Kiệt", 10.749053, 106.659865, false));
        addNode(new Node(normalizeNodeId("HTLO - NgAnKhuong"), "Hải Thượng Lãn Ông - Nguyễn An Khương", 10.750624, 106.658410, false)); // Camera 20 gần đây
        addNode(new Node(normalizeNodeId("NgAnKhuong - THDuc"), "Nguyễn An Khương - Trịnh Hoài Đức", 10.749405, 106.658304, false));
        addNode(new Node(normalizeNodeId("NgAnKhuong - VVK"), "Nguyễn An Khương - Võ Văn Kiệt", 10.748498, 106.658301, false));
        addNode(new Node(normalizeNodeId("HungVuong - NgoGiaTu"), "Hùng Vương - Ngô Gia Tự", 10.7564805, 106.6666292, false));
        addNode(new Node(normalizeNodeId("HTLO - VVK"), "Hải Thượng Lãng Ông - Võ Văn Kiệt", 10.750182, 106.663420, false)); // Camera 19 (Hùng Vương - Ngô Gia Tự) có ID trùng với NQ - HV - NGT - ADV, sẽ ưu tiên node gần nhất.

        // Để tránh nhầm lẫn, tôi tạo một node mới cho nó nếu nó đủ xa, hoặc nó sẽ được ánh xạ tới NQ - HV - NGT - AD
        Log.d(TAG, "Finished loading nodes from DOCX content. Total initial nodes: " + nodes.size());
    }

    /**
     * Ánh xạ thông tin camera với các Node gần nhất đã tồn tại trong đồ thị.
     * Nếu một Node đã tồn tại và là Node gần nhất với camera, nó sẽ được cập nhật
     * ID và đánh dấu là có camera.
     * Nếu không tìm thấy Node gần nhất, hoặc Node gần nhất đã là camera,
     * một Node mới sẽ được tạo (nhưng điều này nên hạn chế để tránh tạo quá nhiều Node).
     *
     * @param cameraInfoList Danh sách thông tin camera.
     */
    private void mapCameraInfoToNodes(List<CameraInfo> cameraInfoList) {
        if (cameraInfoList == null || cameraInfoList.isEmpty()) {
            Log.w(TAG, "CameraInfo list is null or empty, no camera nodes will be mapped.");
            return;
        }

        Log.d(TAG, "Mapping camera info to nearest existing nodes...");
        for (CameraInfo cameraInfo : cameraInfoList) {
            LatLng cameraLatLng = new LatLng(cameraInfo.getLatitude(), cameraInfo.getLongitude());
            Node closestNode = findClosestNode(cameraLatLng);

            if (closestNode != null) {
                String oldNodeId = closestNode.getId();
                String newCameraNodeId = normalizeNodeId(cameraInfo.getId());

                // Kiểm tra xem ID mới có trùng với một node khác đã tồn tại (không phải chính nó)
                if (nodeMap.containsKey(newCameraNodeId) && !nodeMap.get(newCameraNodeId).equals(closestNode)) {
                    Log.w(TAG, "Conflict: Camera ID " + newCameraNodeId + " already exists as another node. Skipping mapping for camera: " + cameraInfo.getName());
                    continue; // Bỏ qua camera này nếu ID đã có sẵn (tránh ghi đè nhầm)
                }

                // Nếu node này đã có camera, kiểm tra xem có phải là camera hiện tại không
                if (closestNode.hasCamera() && !closestNode.getId().equals(newCameraNodeId)) {
                    Log.w(TAG, "Node " + oldNodeId + " already has camera " + closestNode.getId() + ". Skipping mapping for camera: " + cameraInfo.getName() + " to avoid overwriting.");
                    continue;
                }

                // Xóa node cũ khỏi map để thêm lại với ID mới
                nodeMap.remove(oldNodeId);
                // Cập nhật ID và trạng thái camera
                closestNode.setId(newCameraNodeId);
                closestNode.setHasCamera(true);
                closestNode.setName(cameraInfo.getName()); // Cập nhật tên node theo tên camera cho rõ ràng

                // Thêm lại vào map với ID mới
                nodeMap.put(newCameraNodeId, closestNode);

                Log.d(TAG, String.format(Locale.US, "Mapped camera '%s' (ID: %s) to node '%s' (old ID: %s, new ID: %s) at (%.4f, %.4f).",
                        cameraInfo.getName(), cameraInfo.getId(), closestNode.getName(), oldNodeId, newCameraNodeId,
                        closestNode.getLatitude(), closestNode.getLongitude()));

            } else {
                // Trường hợp này có thể xảy ra nếu camera ở quá xa bất kỳ node nào trong danh sách
                // Hoặc nếu danh sách node ban đầu quá nhỏ.
                Log.w(TAG, "No closest node found for camera: " + cameraInfo.getName() + " at " + cameraLatLng.latitude + "," + cameraLatLng.longitude);
                // Bạn có thể chọn thêm một node mới cho camera này nếu muốn,
                // nhưng hãy cân nhắc xem nó có nên được thêm vào đồ thị chính hay không.
                // Ví dụ: addNode(new Node(normalizeNodeId(cameraInfo.getId()), cameraInfo.getName(), cameraInfo.getLatitude(), cameraInfo.getLongitude(), true));
            }
        }
        Log.d(TAG, "Finished mapping camera info. Total nodes after mapping: " + nodes.size());
    }

    /**
     * Tìm Node gần nhất với một vị trí LatLng cho trước.
     * @param targetLatLng Vị trí mục tiêu.
     * @return Node gần nhất, hoặc null nếu không có node nào.
     */
    private Node findClosestNode(LatLng targetLatLng) {
        Node closestNode = null;
        double minDistance = Double.MAX_VALUE;
        float[] results = new float[1];

        for (Node node : nodes) {
            Location.distanceBetween(targetLatLng.latitude, targetLatLng.longitude,
                    node.getLatitude(), node.getLongitude(), results);
            double distance = results[0];

            // Thử nghiệm với ngưỡng khoảng cách nhỏ hơn, ví dụ 30m, để đảm bảo camera được ánh xạ chính xác hơn
            // Nếu bạn muốn camera có thể ánh xạ đến giao lộ xa hơn, hãy tăng ngưỡng này.
            if (distance < minDistance && distance < 30) { // Giới hạn tìm kiếm trong bán kính 30m
                minDistance = distance;
                closestNode = node;
            }
        }
        // Nếu Node gần nhất vẫn cách xa hơn 30 mét, coi như không tìm thấy
        if (closestNode == null || minDistance > 30) {
            if (closestNode != null) {
                Log.w(TAG, "Closest node for target (" + targetLatLng.latitude + "," + targetLatLng.longitude + ") is too far (" + String.format(Locale.US, "%.2f", minDistance) + "m). No node mapped.");
            } else {
                Log.w(TAG, "No closest node found for target (" + targetLatLng.latitude + "," + targetLatLng.longitude + ").");
            }
            return null;
        }
        return closestNode;
    }


    /**
     * Phương thức để thêm một Node vào đồ thị.
     * Nó kiểm tra sự tồn tại của Node để tránh trùng lặp dựa trên ID đã chuẩn hóa.
     * Nếu ID đã tồn tại, sẽ bỏ qua hoặc cập nhật trạng thái `hasCamera` nếu cần.
     * @param node Node cần thêm.
     */
    public void addNode(Node node) {
        String normalizedId = normalizeNodeId(node.getId());
        if (!nodeMap.containsKey(normalizedId)) {
            Node nodeToAdd = new Node(normalizedId, node.getName(), node.getLatitude(), node.getLongitude(), node.hasCamera());
            nodes.add(nodeToAdd);
            nodeMap.put(normalizedId, nodeToAdd);
            // Log.d(TAG, "Added new node: " + normalizedId); // Giảm log này để tránh spam khi load ban đầu
        } else {
            Node existingNode = nodeMap.get(normalizedId);
            // Nếu node hiện tại không có camera nhưng node mới có, cập nhật trạng thái
            // Hoặc nếu node mới có cùng ID nhưng thông tin hasCamera khác
            if (node.hasCamera() && !existingNode.hasCamera()) {
                existingNode.setHasCamera(true);
                Log.d(TAG, "Updated existing node " + normalizedId + " to hasCamera = true.");
            }
            // Log.d(TAG, "Node " + normalizedId + " already exists. Skipping adding a duplicate."); // Giảm log này
        }
    }

    // Phương thức để thêm một cạnh vào đồ thị (hai chiều)
    // Các ID đầu vào phải là ID đã chuẩn hóa của Node.
    public void addEdge(String sourceId, String destinationId) {
        // ID đã được chuẩn hóa từ đầu trong hàm addNode() và mapCameraInfoToNodes()
        // sourceId và destinationId truyền vào đây phải là ID đã chuẩn hóa
        Node sourceNode = nodeMap.get(sourceId);
        Node destinationNode = nodeMap.get(destinationId);

        if (sourceNode != null && destinationNode != null) {
            boolean edgeExists = false;
            // Kiểm tra xem cạnh đã tồn tại chưa (cả 2 chiều)
            if (graphEdges.containsKey(sourceId)) {
                for (Edge existingEdge : graphEdges.get(sourceId)) {
                    if (existingEdge.getDestination().getId().equals(destinationId)) {
                        edgeExists = true;
                        break;
                    }
                }
            }
            if (!edgeExists && graphEdges.containsKey(destinationId)) {
                for (Edge existingEdge : graphEdges.get(destinationId)) {
                    if (existingEdge.getDestination().getId().equals(sourceId)) {
                        edgeExists = true;
                        break;
                    }
                }
            }


            if (!edgeExists) {
                float[] results = new float[1];
                Location.distanceBetween(sourceNode.getLatitude(), sourceNode.getLongitude(),
                        destinationNode.getLatitude(), destinationNode.getLongitude(),
                        results);
                double weight = results[0];

                // Thêm cạnh từ source đến destination
                edges.add(new Edge(sourceNode, destinationNode, weight));
                if (!graphEdges.containsKey(sourceId)) {
                    graphEdges.put(sourceId, new ArrayList<>());
                }
                graphEdges.get(sourceId).add(new Edge(sourceNode, destinationNode, weight));

                // Thêm cạnh từ destination đến source (đồ thị vô hướng)
                edges.add(new Edge(destinationNode, sourceNode, weight));
                if (!graphEdges.containsKey(destinationId)) {
                    graphEdges.put(destinationId, new ArrayList<>());
                }
                graphEdges.get(destinationId).add(new Edge(destinationNode, sourceNode, weight));

                Log.d(TAG, "  Added edge between " + sourceId + " and " + destinationId + " with weight: " + String.format(Locale.US, "%.2f", weight));
            } else {
                // Log.d(TAG, "  Edge between " + sourceId + " and " + destinationId + " already exists. Skipping."); // Giảm log này
            }

        } else {
            Log.w(TAG, "Cannot add edge: Source node '" + sourceId + "' or destination node '" + destinationId + "' not found in map.");
        }
    }

    /**
     * Chuẩn hóa ID của Node bằng cách:
     * 1. Cắt bỏ khoảng trắng ở đầu và cuối chuỗi (trim).
     * 2. Thay thế tất cả dấu gạch ngang '-' bằng dấu gạch dưới '_'.
     * 3. Thay thế tất cả khoảng trắng ' ' bằng dấu gạch dưới '_'.
     * 4. Thay thế nhiều dấu gạch dưới liên tiếp '__' bằng một dấu gạch dưới duy nhất '_'.
     * 5. Chuyển đổi thành chữ thường (để đảm bảo tính nhất quán ID).
     *
     * @param originalId ID Node ban đầu.
     * @return ID Node đã được chuẩn hóa.
     */
    public String normalizeNodeId(String originalId) {
        if (originalId == null) {
            return null;
        }
        String normalized = originalId.trim()
                .replace('-', '_')
                .replace(' ', '_')
                .toLowerCase(Locale.ROOT); // Thêm chuyển đổi sang chữ thường
        normalized = normalized.replaceAll("__+", "_"); // Thay thế nhiều dấu gạch dưới thành một
        return normalized;
    }

    // Phương thức để thêm tất cả các cạnh đã định nghĩa
    public void addAllEdges() {
        Log.d(TAG, "Adding all predefined edges with normalized ID format...");

        // Các ID trong addEdge() phải là ID đã được normalize
        addEdge(normalizeNodeId("Nguyễn Thị Nhỏ- Nguyễn Chí Thanh"), normalizeNodeId("Nguyễn Thị Nhỏ-Tân Thành"));
        addEdge(normalizeNodeId("PG-TT"),normalizeNodeId("PH-TrangTử"));
        addEdge(normalizeNodeId("NNT-LVS"),normalizeNodeId("PH-TrangTử"));
        addEdge(normalizeNodeId("TT-LVS"),normalizeNodeId("PH-TrangTử"));

        addEdge(normalizeNodeId("Nguyễn Thị Nhỏ-Tân Thành"), normalizeNodeId("NTN - HBàng"));
        addEdge(normalizeNodeId("NTN - HBàng"), normalizeNodeId("NTN-TCChiếu"));
        addEdge(normalizeNodeId("NTN-TCChiếu"), normalizeNodeId("NTN-TTử"));
        addEdge(normalizeNodeId("NTN-TTử"), normalizeNodeId("Xóm Vôi -Trang Tử"));
        addEdge(normalizeNodeId("NgCThanh- HTQ"), normalizeNodeId("Nguyễn Thị Nhỏ- Nguyễn Chí Thanh"));
        addEdge(normalizeNodeId("NgCThanh- HTQ"), normalizeNodeId("HTQ-TT"));
        addEdge(normalizeNodeId("HTQ-TT"), normalizeNodeId("HTQ -HB"));
        addEdge(normalizeNodeId("HTQ-TT"), normalizeNodeId("Nguyễn Thị Nhỏ-Tân Thành"));
        addEdge(normalizeNodeId("Võ Trường Toản - HB"), normalizeNodeId("NTN - HBàng"));
        addEdge(normalizeNodeId("HTQ -HB"), normalizeNodeId("Võ Trường Toản - HB"));
        addEdge(normalizeNodeId("Xóm Vôi -NT"), normalizeNodeId("Võ Trường Toản - HB"));
        addEdge(normalizeNodeId("Xóm Vôi -NT"), normalizeNodeId("NTN - HBàng"));
        addEdge(normalizeNodeId("Xóm Vôi -NT"), normalizeNodeId("PG-NT"));
        addEdge(normalizeNodeId("Xóm Vôi - Trần Chánh Chiêu"), normalizeNodeId("Xóm Vôi -NT"));
        addEdge(normalizeNodeId("Xóm Vôi - Trần Chánh Chiêu"), normalizeNodeId("NTN-TCChiếu"));
        addEdge(normalizeNodeId("Xóm Vôi -Trang Tử"), normalizeNodeId("Xóm Vôi - Trần Chánh Chiêu"));
        addEdge(normalizeNodeId("Tạ Uyên - Nguyễn Chí Thanh"), normalizeNodeId("NgCThanh- HTQ"));
        addEdge(normalizeNodeId("Tạ Uyên - Tân Thành"), normalizeNodeId("Tạ Uyên - Nguyễn Chí Thanh"));
        addEdge(normalizeNodeId("Tạ Uyên - Tân Thành"), normalizeNodeId("HTQ-TT"));
        addEdge(normalizeNodeId("Tạ Uyên - Tân Thành"), normalizeNodeId("DTG-TT"));
        addEdge(normalizeNodeId("Tạ Uyên - Phạm Hữu Chí"), normalizeNodeId("Tạ Uyên - Tân Thành"));
        addEdge(normalizeNodeId("Tạ Uyên - Phạm Hữu Chí"), normalizeNodeId("DTG-PHC"));
        addEdge(normalizeNodeId("Tạ Uyên - Phạm Hữu Chí"), normalizeNodeId("Tạ Uyên - Hồng Bàng"));
        addEdge(normalizeNodeId("Tạ Uyên - Hồng Bàng"), normalizeNodeId("PH-NT"));
        addEdge(normalizeNodeId("662b4e201afb9c00172d85f9"), normalizeNodeId("Tạ Uyên - Hồng Bàng")); // Camera 13 (Hồng Bàng - Tạ Uyên) -> Tạ Uyên - Hồng Bàng
        addEdge(normalizeNodeId("662b4e201afb9c00172d85f9"), normalizeNodeId("HTQ -HB")); // Camera 13 (Hồng Bàng - Tạ Uyên) -> HTQ - HB

        addEdge(normalizeNodeId("PG-NT"), normalizeNodeId("HTQ -HB"));
        addEdge(normalizeNodeId("PG-TCC"), normalizeNodeId("PG-NT"));
        addEdge(normalizeNodeId("PG-TCC"), normalizeNodeId("Xóm Vôi - Trần Chánh Chiêu"));
        addEdge(normalizeNodeId("PG-TT"), normalizeNodeId("PG-TCC"));
        addEdge(normalizeNodeId("PG-TT"), normalizeNodeId("Xóm Vôi -Trang Tử"));
        addEdge(normalizeNodeId("PH-NT"), normalizeNodeId("PG-NT"));
        addEdge(normalizeNodeId("PHung-NT"),normalizeNodeId("PH-THD"));
        addEdge(normalizeNodeId("PH-NT"), normalizeNodeId("PH-TCC"));
        addEdge(normalizeNodeId("PH-TCC"), normalizeNodeId("PG-TCC"));
        addEdge(normalizeNodeId("PH-TrangTử"), normalizeNodeId("PH-TCC"));
        addEdge(normalizeNodeId("PH-TrangTử"), normalizeNodeId("PG-TT"));
        addEdge(normalizeNodeId("DTG-NCT"), normalizeNodeId("Tạ Uyên - Nguyễn Chí Thanh"));
        addEdge(normalizeNodeId("DTG-TT"), normalizeNodeId("DTG-NCT"));
        addEdge(normalizeNodeId("DTG-PHC"), normalizeNodeId("DTG-TT"));
        addEdge(normalizeNodeId("DTG-TH"), normalizeNodeId("DTG-PHC"));
        addEdge(normalizeNodeId("DTG-HB"), normalizeNodeId("DTG-TH"));
        addEdge(normalizeNodeId("DTG-NgT"), normalizeNodeId("DTG-HB"));
        addEdge(normalizeNodeId("DTG-THD"), normalizeNodeId("DTG-NgT"));
        addEdge(normalizeNodeId("DTG-HTLO"), normalizeNodeId("DTG-THD"));
        addEdge(normalizeNodeId("NNT-LVS"), normalizeNodeId("PH-TrangTử"));
        addEdge(normalizeNodeId("NNT-HTLO"), normalizeNodeId("NNT-LVS"));
        addEdge(normalizeNodeId("NNT-PVK"), normalizeNodeId("NNT-HTLO"));
        addEdge(normalizeNodeId("NNT-BS"), normalizeNodeId("NNT-PVK"));
        addEdge(normalizeNodeId("NNT-GP"), normalizeNodeId("NNT-BS"));
        addEdge(normalizeNodeId("NNT-VVK"), normalizeNodeId("NNT-GP"));
        addEdge(normalizeNodeId("TT-LVS"), normalizeNodeId("PH-TrangTử"));
        addEdge(normalizeNodeId("TT-LVS"), normalizeNodeId("NNT-LVS"));
        addEdge(normalizeNodeId("HL-HB"), normalizeNodeId("Tạ Uyên - Hồng Bàng"));
        addEdge(normalizeNodeId("HL-HB"), normalizeNodeId("DTG-HB"));
        addEdge(normalizeNodeId("HL-NT"), normalizeNodeId("HL-HB"));
        addEdge(normalizeNodeId("HL-NT"), normalizeNodeId("PH-NT"));
        addEdge(normalizeNodeId("HL-NT"), normalizeNodeId("DTG-NgT"));
        addEdge(normalizeNodeId("HL-THD"), normalizeNodeId("HL-NT"));
        addEdge(normalizeNodeId("HL-THD"), normalizeNodeId("DTG-THD"));
        addEdge(normalizeNodeId("HL- HTLO"), normalizeNodeId("HL-THD"));
        addEdge(normalizeNodeId("HL- HTLO"), normalizeNodeId("TT-LVS"));
        addEdge(normalizeNodeId("HL- HTLO"), normalizeNodeId("DTG-HTLO"));
        addEdge(normalizeNodeId("GC-HTLO"), normalizeNodeId("HL- HTLO"));
        addEdge(normalizeNodeId("GC-HTLO"), normalizeNodeId("NNT-HTLO"));
        addEdge(normalizeNodeId("GC-PVK"), normalizeNodeId("GC-HTLO"));
        addEdge(normalizeNodeId("GC-PVK"), normalizeNodeId("NNT-PVK"));
        addEdge(normalizeNodeId("GC-BS"), normalizeNodeId("GC-PVK"));
        addEdge(normalizeNodeId("GC-BS"), normalizeNodeId("NNT-BS"));
        addEdge(normalizeNodeId("GC-GP"), normalizeNodeId("GC-BS"));
        addEdge(normalizeNodeId("GC-GP"), normalizeNodeId("NNT-GP"));
        addEdge(normalizeNodeId("GC-VVK"), normalizeNodeId("GC-GP"));
        addEdge(normalizeNodeId("GC-VVK"), normalizeNodeId("NNT-VVK"));
        addEdge(normalizeNodeId("DNT-NCT"), normalizeNodeId("DTG-NCT"));
        addEdge(normalizeNodeId("DNT-TT"), normalizeNodeId("DNT-NCT"));
        addEdge(normalizeNodeId("DTG-TT"), normalizeNodeId("DNT-TT"));
        addEdge(normalizeNodeId("DNT-PHC"), normalizeNodeId("DNT-TT"));
        addEdge(normalizeNodeId("DNT-PHC"), normalizeNodeId("DTG-PHC"));
        addEdge(normalizeNodeId("DNT-TH"), normalizeNodeId("DNT-PHC"));
        addEdge(normalizeNodeId("DNT-TH"), normalizeNodeId("DTG-TH"));
        addEdge(normalizeNodeId("DNT-HB"), normalizeNodeId("DNT-TH"));
        addEdge(normalizeNodeId("DNT-HB"), normalizeNodeId("DTG-HB"));
        addEdge(normalizeNodeId("DNT-NT"), normalizeNodeId("DNT-HB"));
        addEdge(normalizeNodeId("DNT-NT"), normalizeNodeId("PHung-NT"));
        addEdge(normalizeNodeId("DNT-NT"), normalizeNodeId("DTG-NgT"));
        addEdge(normalizeNodeId("DNT-THD"), normalizeNodeId("DNT-NT"));
        addEdge(normalizeNodeId("DNT-THD"), normalizeNodeId("PH-THD"));
        addEdge(normalizeNodeId("DNT-THD"), normalizeNodeId("DTG-THD"));
        addEdge(normalizeNodeId("DNT-HTLO"), normalizeNodeId("DNT-THD"));
        addEdge(normalizeNodeId("DNT-HTLO"), normalizeNodeId("DTG-HTLO"));
        addEdge(normalizeNodeId("KB-HTLO"), normalizeNodeId("GC-HTLO"));
        addEdge(normalizeNodeId("KB-PVK"), normalizeNodeId("KB-HTLO"));
        addEdge(normalizeNodeId("PH-HTLO"), normalizeNodeId("KB-HTLO"));
        addEdge(normalizeNodeId("KB-PVK"), normalizeNodeId("VT-THD"));
        addEdge(normalizeNodeId("KB-PVK"), normalizeNodeId("GC-PVK"));
        addEdge(normalizeNodeId("KB-VVK"), normalizeNodeId("GC-VVK"));
        addEdge(normalizeNodeId("VT-VCH"), normalizeNodeId("PH-VCH"));
        addEdge(normalizeNodeId("VT-VCH"), normalizeNodeId("DNT-HTLO"));
        addEdge(normalizeNodeId("VT-PH"), normalizeNodeId("VT-VCH"));
        addEdge(normalizeNodeId("VT-THD"), normalizeNodeId("VT-PH"));
        addEdge(normalizeNodeId("VT-VVK"), normalizeNodeId("VT-THD"));
        addEdge(normalizeNodeId("VT-VVK"), normalizeNodeId("KB-VVK"));
        addEdge(normalizeNodeId("PCD-NCT"), normalizeNodeId("DNT-NCT"));
        addEdge(normalizeNodeId("PCD-NCT"), normalizeNodeId("ThuanKieu - NgChiThanh"));
        addEdge(normalizeNodeId("PCD-TT"), normalizeNodeId("PCD-NCT"));
        addEdge(normalizeNodeId("PCD-TT"), normalizeNodeId("DNT-TT"));
        addEdge(normalizeNodeId("PCD-TT"), normalizeNodeId("ThuanKieu - TanThanh"));
        addEdge(normalizeNodeId("PCD-PHC"), normalizeNodeId("PCD-TT"));
        addEdge(normalizeNodeId("PCD-PHC"), normalizeNodeId("DNT-PHC"));
        addEdge(normalizeNodeId("PH-HB"), normalizeNodeId("DNT-HB"));
        addEdge(normalizeNodeId("PH-LT"), normalizeNodeId("PH-HB"));
        addEdge(normalizeNodeId("PH-LT"), normalizeNodeId("PHung-NT"));
        addEdge(normalizeNodeId("PH-THD"), normalizeNodeId("PHung-NT"));
        addEdge(normalizeNodeId("CVL-NgTrai"), normalizeNodeId("PHung-NT"));
        addEdge(normalizeNodeId("CVL-THD"), normalizeNodeId("PH-THD"));
        addEdge(normalizeNodeId("PH-THD"), normalizeNodeId("DNT-THD"));
        addEdge(normalizeNodeId("PH-HTLO"), normalizeNodeId("PH-THDuc"));
        addEdge(normalizeNodeId("PH-HTLO"), normalizeNodeId("PH-THD"));
        addEdge(normalizeNodeId("PH-HTLO"), normalizeNodeId("HTLO - NgAnKhuong"));
        addEdge(normalizeNodeId("PH-HTLO"), normalizeNodeId("DNT-HTLO"));
        addEdge(normalizeNodeId("PH-VCH"), normalizeNodeId("PH-THDuc"));
        addEdge(normalizeNodeId("PH-VCH"), normalizeNodeId("PH-HTLO"));
        addEdge(normalizeNodeId("PH-VVK"), normalizeNodeId("PH-THDuc"));
        addEdge(normalizeNodeId("PH-VVK"), normalizeNodeId("VT-VVK"));
        addEdge(normalizeNodeId("PH-VVK"), normalizeNodeId("NgAnKhuong - VVK"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ -Nguyễn Trãi"), normalizeNodeId("Nguyễn Văn Cừ -An Dương Vương"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ -Nguyễn Trãi"), normalizeNodeId("NgBieu - NgTrai"));

        addEdge(normalizeNodeId("Nguyễn Văn Cừ -An Dương Vương"), normalizeNodeId("Nguyễn Văn Cừ - Hùng Vương"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ - Hùng Vương"), normalizeNodeId("Nguyễn Văn Cừ - Phan Văn Trị"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ - Hùng Vương"), normalizeNodeId("TBTrong - TranPhu"));

        addEdge(normalizeNodeId("Nguyễn Văn Cừ - Phan Văn Trị"), normalizeNodeId("Nguyễn Văn Cừ -Trần Hưng Đạo"));
        addEdge(normalizeNodeId("NgBieu - THD"), normalizeNodeId("Nguyễn Văn Cừ -Trần Hưng Đạo"));
        addEdge(normalizeNodeId("NgBieu - THD"), normalizeNodeId("TBTrong - THD"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ - Phan Văn Trị"), normalizeNodeId("NgBieu - PhanVanTri"));
        addEdge(normalizeNodeId("Nguyễn Văn Cừ -Trần Hưng Đạo"), normalizeNodeId("Nguyễn Văn Cừ - Võ Văn Kiệt"));
        addEdge(normalizeNodeId("NgBieu - VVK"), normalizeNodeId("Nguyễn Văn Cừ - Võ Văn Kiệt"));
        addEdge(normalizeNodeId("NgBieu - VVK"), normalizeNodeId("NgBieu - CDat"));
        addEdge(normalizeNodeId("NgBieu - CDat"), normalizeNodeId("NgBieu - THD"));
        addEdge(normalizeNodeId("NgBieu - THD"), normalizeNodeId("NgBieu - PhanVanTri"));
        addEdge(normalizeNodeId("NgBieu - PhanVanTri"), normalizeNodeId("NgBieu - NgTrai"));
        addEdge(normalizeNodeId("NgBieu - NgTrai"), normalizeNodeId("NgTrai - TBTrong"));
        addEdge(normalizeNodeId("NgTrai - TBTrong"), normalizeNodeId("TBTrong - ADV"));
        addEdge(normalizeNodeId("NgTrai - TBTrong"), normalizeNodeId("TBTrong - THD"));
        addEdge(normalizeNodeId("NgTrai - TBTrong"), normalizeNodeId("LHPhong - NgTrai"));
        addEdge(normalizeNodeId("TBTrong - ADV"), normalizeNodeId("TBTrong - TranPhu"));
        addEdge(normalizeNodeId("TBTrong - TranPhu"), normalizeNodeId("TBTrong - HV"));
        addEdge(normalizeNodeId("TBTrong - TranPhu"), normalizeNodeId("LHPhong - TranPhu"));
        addEdge(normalizeNodeId("TBTrong - HV"), normalizeNodeId("HV - LHPhong"));
        addEdge(normalizeNodeId("TBTrong - HV"), normalizeNodeId("Nguyễn Văn Cừ - Hùng Vương"));
        addEdge(normalizeNodeId("TBTrong - ADV"), normalizeNodeId("Nguyễn Văn Cừ -An Dương Vương"));
        addEdge(normalizeNodeId("TBTrong - ADV"), normalizeNodeId("LHPhong - ADV"));
        addEdge(normalizeNodeId("TBTrong - VVK"), normalizeNodeId("NgBieu - VVK"));
        addEdge(normalizeNodeId("TBTrong - VVK"), normalizeNodeId("TBTrong - THD"));
        addEdge(normalizeNodeId("TBTrong - VVK"), normalizeNodeId("VVK - HMD"));
        addEdge(normalizeNodeId("TBTrong - THD"), normalizeNodeId("LHP - THD"));


        addEdge(normalizeNodeId("HV - LHPhong"), normalizeNodeId("LHPhong - TranPhu"));
        addEdge(normalizeNodeId("HV - LHPhong"), normalizeNodeId("TNTon - HV"));
        addEdge(normalizeNodeId("LHPhong - TranPhu"), normalizeNodeId("LHPhong - ADV"));
        addEdge(normalizeNodeId("LHPhong - TranPhu"), normalizeNodeId("HMD - TPhu"));
        addEdge(normalizeNodeId("LHPhong - ADV"), normalizeNodeId("LHPhong - NgTrai"));
        addEdge(normalizeNodeId("LHPhong - ADV"), normalizeNodeId("HMD - ADV"));
        addEdge(normalizeNodeId("LHPhong - NgTrai"), normalizeNodeId("LHPhong - PVTri"));
        addEdge(normalizeNodeId("LHPhong - NgTrai"), normalizeNodeId("HMD - NgTrai"));
        addEdge(normalizeNodeId("LHPhong - PVTri"), normalizeNodeId("LHP - THD"));
        addEdge(normalizeNodeId("LHPhong - PVTri"), normalizeNodeId("HMD - PVTri"));
        addEdge(normalizeNodeId("LHP - THD"), normalizeNodeId("HMD - THD"));
        addEdge(normalizeNodeId("VVK - HMD"), normalizeNodeId("HMD - NgThuc"));
        addEdge(normalizeNodeId("VVK - HMD"), normalizeNodeId("TTKhai - VVK"));
        addEdge(normalizeNodeId("HMD - NgThuc"), normalizeNodeId("HMD - THD"));
        addEdge(normalizeNodeId("HMD - THD"), normalizeNodeId("HMD - PVTri"));
        addEdge(normalizeNodeId("HMD - PVTri"), normalizeNodeId("HMD - NgTrai"));
        addEdge(normalizeNodeId("NgTrai - BHNghia"), normalizeNodeId("HMD - NgTrai"));
        addEdge(normalizeNodeId("HMD - NgTrai"), normalizeNodeId("HMD - ADV"));
        addEdge(normalizeNodeId("HMD - ADV"), normalizeNodeId("HMD - TPhu"));
        addEdge(normalizeNodeId("HMD - ADV"), normalizeNodeId("SVH - TP - ADV"));
        addEdge(normalizeNodeId("HMD - TPhu"), normalizeNodeId("TNTon - HV"));
        addEdge(normalizeNodeId("HMD - TPhu"), normalizeNodeId("SVH - TP - ADV"));
        addEdge(normalizeNodeId("TNTon - HV"), normalizeNodeId("SVH - HV"));
        addEdge(normalizeNodeId("SVH - HV"), normalizeNodeId("SVH - TP - ADV"));

        addEdge(normalizeNodeId("NgDuyDuong - HV"), normalizeNodeId("NgDuyDuong - NgChiThanh"));
        addEdge(normalizeNodeId("SVH - NgChiThanh"), normalizeNodeId("NgDuyDuong - NgChiThanh"));
        addEdge(normalizeNodeId("NgDuyDuong - NgChiThanh"), normalizeNodeId("YK - HV"));
        addEdge(normalizeNodeId("YK - HV"), normalizeNodeId("NgDuyDuong - HV"));
        addEdge(normalizeNodeId("YK - HV"), normalizeNodeId("YK - ADV"));
        addEdge(normalizeNodeId("SVH - TP - ADV"), normalizeNodeId("YK - ADV"));
        addEdge(normalizeNodeId("NgDuyDuong - ADV"), normalizeNodeId("YK - ADV"));
        addEdge(normalizeNodeId("YK - HV"), normalizeNodeId("SVH - HV"));
        addEdge(normalizeNodeId("SVH - NgChiThanh"), normalizeNodeId("SVH - HV"));

        addEdge(normalizeNodeId("NgTrai - TPhu"), normalizeNodeId("NgTrai - BHNghia"));
        addEdge(normalizeNodeId("66b1c190779f740018673ed4"), normalizeNodeId("NgTrai - TPhu")); // Camera 8 (Nguyễn Trãi - Trần Phú)
        addEdge(normalizeNodeId("NgTrai - BHNghia"), normalizeNodeId("BHNghia - PVTri"));
        addEdge(normalizeNodeId("BHNghia - PVTri"), normalizeNodeId("BHNghia - THD"));
        addEdge(normalizeNodeId("BHNghia - PVTri"), normalizeNodeId("HMD - PVTri"));
        addEdge(normalizeNodeId("BHNghia - THD"), normalizeNodeId("BHNghia - NgThuc"));
        addEdge(normalizeNodeId("BHNghia - THD"), normalizeNodeId("HMD - THD"));
        addEdge(normalizeNodeId("BHNghia - THD"), normalizeNodeId("TTKhai - THD"));
        addEdge(normalizeNodeId("BHNghia - NgThuc"), normalizeNodeId("BHNghia - BachVan"));
        addEdge(normalizeNodeId("BHNghia - NgThuc"), normalizeNodeId("TTKhai - NgThuc"));
        addEdge(normalizeNodeId("BHNghia - BachVan"), normalizeNodeId("BHNghia - DaoTan"));
        addEdge(normalizeNodeId("BHNghia - BachVan"), normalizeNodeId("VVK - HMD"));
        addEdge(normalizeNodeId("BHNghia - BachVan"), normalizeNodeId("TTKhai - BachVan"));

        addEdge(normalizeNodeId("BHNghia - DaoTan"), normalizeNodeId("TTKhai - VVK"));
        addEdge(normalizeNodeId("TTKhai - VVK"), normalizeNodeId("TTKhai - BachVan"));
        addEdge(normalizeNodeId("TTKhai - BachVan"), normalizeNodeId("TTKhai - NgThuc"));
        addEdge(normalizeNodeId("TTKhai - NgThuc"), normalizeNodeId("TTKhai - THD"));
        addEdge(normalizeNodeId("TTKhai - THD"), normalizeNodeId("NgTrai - TPhu"));
        addEdge(normalizeNodeId("NgTrai - TPhu"), normalizeNodeId("NgDuyDuong - ADV"));
        addEdge(normalizeNodeId("NgTrai - TPhu"), normalizeNodeId("SVH - TP - ADV"));
        addEdge(normalizeNodeId("NgDuyDuong - ADV"), normalizeNodeId("NgDuyDuong - HV"));
        addEdge(normalizeNodeId("NgDuyDuong - ADV"), normalizeNodeId("NgTrPhuong - ADV"));
        addEdge(normalizeNodeId("NgDuyDuong - HV"), normalizeNodeId("NgTrPhuong - HV"));
        addEdge(normalizeNodeId("NgTrPhuong - HV"), normalizeNodeId("Ngã 6 Nguyễn Tri Phương")); // Sửa lỗi rỗng và kết nối
        addEdge(normalizeNodeId("NgTrPhuong - ADV"), normalizeNodeId("NgTrPhuong - NgTrai"));
        addEdge(normalizeNodeId("NgTrPhuong - ADV"), normalizeNodeId("NgDuyDuong - ADV"));
        addEdge(normalizeNodeId("5d8cd49f766c880017188944"), normalizeNodeId("NgTrPhuong - NgTrai")); // Camera 7 (Nguyễn Tri Phương - Nguyễn Trãi)
        addEdge(normalizeNodeId("NgTrPhuong - NgTrai"), normalizeNodeId("NgTriPhuong - TPhu"));
        addEdge(normalizeNodeId("5d8cd1f9766c880017188938"), normalizeNodeId("NgTriPhuong - TPhu")); // Camera 6 (Nguyễn Tri Phương - Trần Phú)
        addEdge(normalizeNodeId("NgTriPhuong - TPhu"), normalizeNodeId("NgTriPhuong - THD"));
        addEdge(normalizeNodeId("NgTriPhuong - TPhu"), normalizeNodeId("662b4efc1afb9c00172d86bc"));
        addEdge(normalizeNodeId("NgTriPhuong - TPhu"), normalizeNodeId("AB - TPhu")); // Thay vì TPhu - THD (chưa có)
        addEdge(normalizeNodeId("NgTriPhuong - THD"), normalizeNodeId("NgTriPhuong - VVK"));

        addEdge(normalizeNodeId("NgTriPhuong - THD"), normalizeNodeId("662b4efc1afb9c00172d86bc"));

        addEdge(normalizeNodeId("5b632a79fd4edb0019c7dc0f"), normalizeNodeId("NgTriPhuong - THD")); // Camera 4 (Nguyễn Tri Phương - Trần Hưng Đạo)
        addEdge(normalizeNodeId("NgTriPhuong - THD"), normalizeNodeId("NVDung - THD"));
        addEdge(normalizeNodeId("56de42f611f398ec0c481291"), normalizeNodeId("NgTriPhuong - VVK")); // Camera 1 (Võ Văn Kiệt - Nguyễn Tri Phương 1)
        addEdge(normalizeNodeId("56de42f611f398ec0c481297"), normalizeNodeId("NgTriPhuong - VVK")); // Camera 2 (Võ Văn Kiệt - Nguyễn Tri Phương 2)
        addEdge(normalizeNodeId("NgTriPhuong - VVK"), normalizeNodeId("NVDung - VVK"));
        addEdge(normalizeNodeId("AB - NgTrai"), normalizeNodeId("AB - TPhu"));
        addEdge(normalizeNodeId("AB - NgTrai"), normalizeNodeId("NgTrai - TPhu"));
        addEdge(normalizeNodeId("AB - NgTrai"), normalizeNodeId("NgTrPhuong - NgTrai"));
        addEdge(normalizeNodeId("NQ - NgTrai"), normalizeNodeId("NgTrPhuong - NgTrai"));
        addEdge(normalizeNodeId("AB - TPhu"), normalizeNodeId("AB - THD"));
        addEdge(normalizeNodeId("AB - TPhu"), normalizeNodeId("NgTrai - TPhu"));
        addEdge(normalizeNodeId("AB - TPhu"), normalizeNodeId("NgTriPhuong - TPhu"));
        addEdge(normalizeNodeId("AB - THD"), normalizeNodeId("AB - BachVan"));
        addEdge(normalizeNodeId("AB - THD"), normalizeNodeId("TTKhai - THD"));
        addEdge(normalizeNodeId("AB - THD"), normalizeNodeId("NVDung - THD"));
        addEdge(normalizeNodeId("AB - BachVan"), normalizeNodeId("AB - VVK"));
        addEdge(normalizeNodeId("AB - BachVan"), normalizeNodeId("TTKhai - BachVan"));
        addEdge(normalizeNodeId("AB - VVK"), normalizeNodeId("NVDung - VVK"));
        addEdge(normalizeNodeId("AB - VVK"), normalizeNodeId("TTKhai - VVK"));
        addEdge(normalizeNodeId("NVDung - VVK"), normalizeNodeId("NVDung - THD"));
        addEdge(normalizeNodeId("NgTriPhuong - VVK"), normalizeNodeId("NQ - VVK")); // Kết nối NQ - VVK với NgTriPhuong - VVK
        addEdge(normalizeNodeId("NQ - VVK"), normalizeNodeId("NQ - THD"));
        addEdge(normalizeNodeId("NQ - VVK"), normalizeNodeId("TanDa - VVK"));
        addEdge(normalizeNodeId("662b4efc1afb9c00172d86bc"), normalizeNodeId("NQ - THD")); // Camera 5 (Trần Hưng Đạo - Trần Phú) -> NQ - THD
        addEdge(normalizeNodeId("NQ - THD"), normalizeNodeId("NQ - NgTrai"));
        addEdge(normalizeNodeId("NQ - THD"), normalizeNodeId("TanDa - THD"));
        addEdge(normalizeNodeId("NQ - NgTrai"), normalizeNodeId("NQ - MTT"));
        addEdge(normalizeNodeId("NQ - NgTrai"), normalizeNodeId("TanDa - NgTrai"));
        addEdge(normalizeNodeId("NQ - MTT"), normalizeNodeId("NQ - HV - NGT - ADV"));
        addEdge(normalizeNodeId("5b632b60fd4edb0019c7dc12"), normalizeNodeId("NQ - HV - NGT - ADV")); // Camera 9 (Hồng Bàng - Ngô Quyền 1)
        addEdge(normalizeNodeId("5deb576d1dc17d7c5515ad20"), normalizeNodeId("NQ - HV - NGT - ADV")); // Camera 10 (Hồng Bàng - Ngô Quyền 2)
        addEdge(normalizeNodeId("5deb576d1dc17d7c5515ad1f"), normalizeNodeId("NQ - HV - NGT - ADV")); // Camera 19 (Hùng Vương - Ngô Gia Tự)
        addEdge(normalizeNodeId("NQ - HV - NGT - ADV"), normalizeNodeId("NQ - NgChiThanh"));
        addEdge(normalizeNodeId("649da419a6068200171a6c90"), normalizeNodeId("NQ - NgChiThanh")); // Camera 15 (Nguyễn Chí Thanh - Ngô Quyền)
        addEdge(normalizeNodeId("NQ - NgChiThanh"), normalizeNodeId("Ngã 6 Nguyễn Tri Phương"));
        addEdge(normalizeNodeId("5deb576d1dc17d7c5515ad21"), normalizeNodeId("Ngã 6 Nguyễn Tri Phương")); // Camera 14 (Nút giao ngã 6 Nguyễn Tri Phương)
        addEdge(normalizeNodeId("Ngã 6 Nguyễn Tri Phương"), normalizeNodeId("NK - NgChiThanh"));
        addEdge(normalizeNodeId("66f126e8538c7800172d862f"), normalizeNodeId("NK - NgChiThanh")); // Camera 16 (Nguyễn Chí Thanh - Nguyễn Kim)


        addEdge(normalizeNodeId("662b4ecb1afb9c00172d8692"), normalizeNodeId("662b4e8e1afb9c00172d865c"));
        addEdge(normalizeNodeId("NK - NgChiThanh"), normalizeNodeId("NK - TangBatHo"));
        addEdge(normalizeNodeId("NK - NgChiThanh"), normalizeNodeId("LTK - NgChiThanh"));
        addEdge(normalizeNodeId("NK - TangBatHo"), normalizeNodeId("NK - BaTrieu"));
        addEdge(normalizeNodeId("NK - TangBatHo"), normalizeNodeId("LTK - TangBatHo"));
        addEdge(normalizeNodeId("NK - BaTrieu"), normalizeNodeId("NK - PHChi"));
        addEdge(normalizeNodeId("NK - BaTrieu"), normalizeNodeId("LTK - BaTieu"));
        addEdge(normalizeNodeId("NK - PHChi"), normalizeNodeId("NK - HB "));
        addEdge(normalizeNodeId("NK - PHChi"), normalizeNodeId("LTK - PHChi"));
        addEdge(normalizeNodeId("NK - HB "), normalizeNodeId("HB - TanDa"));
        addEdge(normalizeNodeId("NK - HB "), normalizeNodeId("LTK - HB"));
        addEdge(normalizeNodeId("HB - TanDa"), normalizeNodeId("TanDa - NgTrai"));
        addEdge(normalizeNodeId("TanDa - NgTrai"), normalizeNodeId("TanDa - THD"));
        addEdge(normalizeNodeId("TanDa - NgTrai"), normalizeNodeId("PĐTV - NgTrai"));
        addEdge(normalizeNodeId("TanDa - THD"), normalizeNodeId("TanDa - VVK"));
        addEdge(normalizeNodeId("TanDa - THD"), normalizeNodeId("PhamDon - THD"));
        addEdge(normalizeNodeId("TanDa - VVK"), normalizeNodeId("TanDa - TanHang"));

        addEdge(normalizeNodeId("PhamDon - TanHang"), normalizeNodeId("TanDa - TanHang"));
        addEdge(normalizeNodeId("PhamDon - TanHang"), normalizeNodeId("TQP - HTLO"));
        addEdge(normalizeNodeId("TanDa - VVK"), normalizeNodeId("HTLO - VVK"));
        addEdge(normalizeNodeId("LTK - NgChiThanh"), normalizeNodeId("LTK - TangBatHo"));
        addEdge(normalizeNodeId("662b4e8e1afb9c00172d865c"), normalizeNodeId("LTK - NgChiThanh")); // Camera 17 (Nguyễn Chí Thanh - Lý Thường Kiệt)
        addEdge(normalizeNodeId("LTK - TangBatHo"), normalizeNodeId("LTK - BaTieu"));
        addEdge(normalizeNodeId("LTK - BaTieu"), normalizeNodeId("LTK - PHChi"));
        addEdge(normalizeNodeId("LTK - PHChi"), normalizeNodeId("LTK - HB"));
        addEdge(normalizeNodeId("63b3c274bfd3d90017e9ab93"), normalizeNodeId("LTK - HB")); // Camera 11 (Hồng Bàng - Phù Đổng Thiên Vương)
        addEdge(normalizeNodeId("LTK - HB"), normalizeNodeId("PĐTV - NgTrai"));
        addEdge(normalizeNodeId("PĐTV - NgTrai"), normalizeNodeId("TranHoa - THD"));
        addEdge(normalizeNodeId("TranHoa - THD"), normalizeNodeId("PhamDon - THD"));
        addEdge(normalizeNodeId("PhamDon - THD"), normalizeNodeId("PhamDon - TanHang"));
        addEdge(normalizeNodeId("PhamDon - TanHang"), normalizeNodeId("TQP - NgTrai"));
        addEdge(normalizeNodeId("TQP - VVK"), normalizeNodeId("TQP - HTLO"));
        addEdge(normalizeNodeId("TQP - VVK"), normalizeNodeId("MacCuu - VVK"));
        addEdge(normalizeNodeId("TQP - VVK"), normalizeNodeId("HTLO - VVK"));
        addEdge(normalizeNodeId("TQP - HTLO"), normalizeNodeId("TQP - THD"));
        addEdge(normalizeNodeId("TQP - THD"), normalizeNodeId("TQP - HB"));
        addEdge(normalizeNodeId("TQP - THD"), normalizeNodeId("LNH - THD"));
        addEdge(normalizeNodeId("CVL - THD"), normalizeNodeId("LNH - THD"));
        addEdge(normalizeNodeId("TQP - THD"), normalizeNodeId("TranHoa - THD"));
        addEdge(normalizeNodeId("TQP - HB"), normalizeNodeId("TQP - PHChi"));
        addEdge(normalizeNodeId("ThuanKieu - PHChi"), normalizeNodeId("TQP - PHChi"));
        addEdge(normalizeNodeId("TQP - HB"), normalizeNodeId("LNH - HB"));
        addEdge(normalizeNodeId("NK - HB "), normalizeNodeId("LTK - HB"));
        addEdge(normalizeNodeId("NK - HB "), normalizeNodeId("NQ - HV - NGT - ADV"));
        addEdge(normalizeNodeId("Ngã 6 Nguyễn Tri Phương"), normalizeNodeId("NQ - HV - NGT - ADV"));
        addEdge(normalizeNodeId("Ngã 6 Nguyễn Tri Phương"), normalizeNodeId("NgDuyDuong - NgChiThanh"));
        addEdge(normalizeNodeId("NgTrPhuong - HV"), normalizeNodeId("NQ - HV - NGT - ADV"));
        addEdge(normalizeNodeId("NgTrPhuong - ADV"), normalizeNodeId("NQ - HV - NGT - ADV"));
        addEdge(normalizeNodeId("NgTrPhuong - ADV"), normalizeNodeId("NgTrPhuong - HV"));
        addEdge(normalizeNodeId("NgTrPhuong - HV"), normalizeNodeId("Ngã 6 Nguyễn Tri Phương"));
        addEdge(normalizeNodeId("TQP - HB"), normalizeNodeId("LTK - HB"));
        addEdge(normalizeNodeId("LNH - HB"), normalizeNodeId("LHN - LaoTu"));
        addEdge(normalizeNodeId("LNH - HB"), normalizeNodeId("5b728aafca0577001163ff7e")); // Camera 12 (Hồng Bàng - Châu Văn Liêm)
        addEdge(normalizeNodeId("5b728aafca0577001163ff7e"), normalizeNodeId("ThuanKieu - TanHung"));
        addEdge(normalizeNodeId("5b728aafca0577001163ff7e"), normalizeNodeId("PH - HB"));
        addEdge(normalizeNodeId("5b728aafca0577001163ff7e"), normalizeNodeId("CVL - NgTrai")); // Thêm cạnh từ camera 12 tới CVL - HB

        addEdge(normalizeNodeId("LHN - LaoTu"), normalizeNodeId("LNH - NgTrai"));
        addEdge(normalizeNodeId("LNH - NgTrai"), normalizeNodeId("LNH - THD"));
        addEdge(normalizeNodeId("LNH - NgTrai"), normalizeNodeId("PĐTV - NgTrai"));
        addEdge(normalizeNodeId("LNH - NgTrai"), normalizeNodeId("CVL - NgTrai"));
        addEdge(normalizeNodeId("LNH - THD"), normalizeNodeId("LNH - HTLO"));
        addEdge(normalizeNodeId("ThuanKieu - NgChiThanh"), normalizeNodeId("ThuanKieu- TanThanh"));
        addEdge(normalizeNodeId("662b4ecb1afb9c00172d8692"), normalizeNodeId("ThuanKieu - NgChiThanh")); // Camera 18 (Nguyễn Chí Thanh - Thuận Kiều)
        addEdge(normalizeNodeId("ThuanKieu- TanThanh"), normalizeNodeId("ThuanKieu - PHChi"));
        addEdge(normalizeNodeId("ThuanKieu - PHChi"), normalizeNodeId("ThuanKieu - TanHung"));
        addEdge(normalizeNodeId("ThuanKieu - PHChi"), normalizeNodeId("PCD-PHC"));
        addEdge(normalizeNodeId("ThuanKieu - TanHung"), normalizeNodeId("CVL - HB"));
        addEdge(normalizeNodeId("CVL - HB"), normalizeNodeId("CVL - NgTrai"));
        addEdge(normalizeNodeId("CVL - NgTrai"), normalizeNodeId("CVL - THD"));
        addEdge(normalizeNodeId("CVL - THD"), normalizeNodeId("CVL - HTLO - NguyenThi - MacCuu"));
        addEdge(normalizeNodeId("CVL - HTLO - NguyenThi - MacCuu"), normalizeNodeId("NguyenThi - VVK"));
        addEdge(normalizeNodeId("NguyenThi - VVK"), normalizeNodeId("NgAnKhuong-VVK"));
        addEdge(normalizeNodeId("NgAnKhuong - THDuc"), normalizeNodeId("NgAnKhuong-VVK"));
        addEdge(normalizeNodeId("NguyenThi - VVK"), normalizeNodeId("MacCuu-VVK"));
        addEdge(normalizeNodeId("TQP - HTLO"), normalizeNodeId("HTLO - VVK"));
        addEdge(normalizeNodeId("TQP - HTLO"), normalizeNodeId("LNH - HTLO"));
        addEdge(normalizeNodeId("56de42f611f398ec0c481293"), normalizeNodeId("HTLO - VVK")); // Camera 3 (Võ Văn Kiệt - Hải Thượng Lãn Ông)
        addEdge(normalizeNodeId("PH-THDuc"), normalizeNodeId("VT-THD"));
        addEdge(normalizeNodeId("PH-THDuc"), normalizeNodeId("NgAnKhuong - THDuc"));
        addEdge(normalizeNodeId("662b4de41afb9c00172d85c5"), normalizeNodeId("CVL - HTLO")); // Camera 20 (Hải Thượng Lãn Ông - Châu Văn Liêm)
        addEdge(normalizeNodeId("662b4de41afb9c00172d85c5"), normalizeNodeId("LNH - HTLO")); // Camera 20 (Hải Thượng Lãn Ông - Châu Văn Liêm)
        addEdge(normalizeNodeId("662b4de41afb9c00172d85c5"), normalizeNodeId("CVL - HTLO - NguyenThi - MacCuu")); // Camera 20 (Hải Thượng Lãn Ông - Châu Văn Liêm)

        addEdge(normalizeNodeId("HTLO - NgAnKhuong"), normalizeNodeId("CVL - HTLO - NguyenThi - MacCuu"));
        addEdge(normalizeNodeId("HTLO - NgAnKhuong"), normalizeNodeId("NgAnKhuong - THDuc"));
        addEdge(normalizeNodeId("NgAnKhuong - THDuc"), normalizeNodeId("NgAnKhuong_VVK"));

        Log.d(TAG, "Finished adding all predefined edges. Total edges: " + edges.size());
    }

    // Getters cho danh sách nodes và edges
    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Map<String, Node> getNodeMap() {
        return nodeMap;
    }

    // Helper method để lấy danh sách các cạnh xuất phát từ một Node cụ thể
    public List<Edge> getEdgesFromNode(String nodeId) {
        return graphEdges.getOrDefault(normalizeNodeId(nodeId), new ArrayList<>());
    }
}