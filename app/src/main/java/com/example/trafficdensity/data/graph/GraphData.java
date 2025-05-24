package com.example.trafficdensity.data.graph; // Đảm bảo đúng package của bạn

import com.example.trafficdensity.data.model.Node; // Import Node
import com.example.trafficdensity.data.model.Edge; // Import Edge
import com.example.trafficdensity.CameraInfo; // Import CameraInfo
import com.google.android.gms.maps.model.LatLng; // Import LatLng
import android.location.Location; // Import Location for distance calculation
import android.util.Log; // Import Log
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Locale; // Import Locale

// --- Lớp chứa dữ liệu đồ thị mô phỏng Quận 5 ---
// Dữ liệu này bao gồm các giao lộ chính (có và không có camera) và các cạnh nối chúng.
// Tọa độ là ước lượng cho mục đích minh họa.
public class GraphData {

    private static final String TAG = "GraphData";

    private List<Node> nodes = new ArrayList<>();
    private List<Edge> edges = new ArrayList<>();
    private Map<String, Node> nodeMap = new HashMap<>(); // Map ID -> Node để dễ tra cứu
    private Map<String, List<Edge>> graphEdges = new HashMap<>(); // Map Node ID -> List of outgoing Edges

    // Constructor sẽ xây dựng đồ thị mô phỏng dựa trên CameraInfo và các giao lộ ước lượng
    public GraphData(List<CameraInfo> cameraInfoList) {
        Log.d(TAG, "Building simulated District 5 graph with camera nodes and estimated intersections...");

        // --- 1. Thêm các Node từ danh sách CameraInfo ---
        if (cameraInfoList != null) {
            for (CameraInfo cameraInfo : cameraInfoList) {
                String cameraId = cameraInfo.getId();
                LatLng location = cameraInfo.getLocation();
                if (location != null) {
                    // Tạo Node cho mỗi camera. ID node là ID camera.
                    Node cameraNode = new Node(
                            cameraId,
                            cameraInfo.getName(), // Tên camera làm tên node
                            location.latitude,
                            location.longitude,
                            0.0f, // Mật độ ban đầu (có thể cập nhật sau)
                            true // Đánh dấu là node camera
                    );
                    // Chỉ thêm node nếu ID chưa tồn tại (phòng trường hợp trùng ID camera)
                    if (!nodeMap.containsKey(cameraId)) {
                        nodes.add(cameraNode);
                        nodeMap.put(cameraId, cameraNode);
                        Log.d(TAG, "  Added camera node: " + cameraId + " (" + cameraInfo.getName() + ")");
                    } else {
                        Log.w(TAG, "  Skipping adding camera node with duplicate ID: " + cameraId);
                    }
                } else {
                    Log.w(TAG, "  CameraInfo with ID " + cameraId + " has null location. Skipping.");
                }
            }
        }
        Log.d(TAG, "Added " + (cameraInfoList != null ? cameraInfoList.size() : 0) + " potential camera nodes. Total nodes so far: " + nodes.size());

        // --- 2. Thêm các Node (Giao lộ) ước lượng không có camera trong danh sách ---
        // Sử dụng tọa độ ước lượng cho các giao lộ của các tuyến đường chính
        // Tọa độ này là gần đúng và cần được điều chỉnh dựa trên bản đồ thực tế.
        // Các ID node được đặt tên theo các tuyến đường giao nhau (hoặc mô tả vị trí).
        // Chỉ thêm nếu ID chưa tồn tại (tránh trùng với ID camera nếu có sự trùng hợp ngẫu nhiên)
// Các lời gọi addIntersectionNode() được tạo tự động từ file Location Node.docx
        addIntersectionNode("Nguyễn_Thị_Nhỏ_Nguyễn_Chí_Thanh", "Nguyễn Thị Nhỏ- Nguyễn Chí Thanh", 10.756380, 106.651143);
        addIntersectionNode("Nguyễn_Thị_Nhỏ_Tân_Thành", "Nguyễn Thị Nhỏ-Tân Thành", 10.755275, 106.650992);
        addIntersectionNode("NTN_HBàng", "NTN - HBàng", 10.753520, 106.650820);
        addIntersectionNode("NTN_TCChiếu", "NTN-TCChiếu", 10.752342, 106.650691);
        addIntersectionNode("NTN_TTử", "NTN-TTử", 10.751497, 106.650566);
        addIntersectionNode("NgCThanh_HTQ", "NgCThanh- HTQ", 10.756636, 106.652819);
        addIntersectionNode("HTQ_TT", "HTQ-TT", 10.755537, 106.652903);
        addIntersectionNode("HTQ_HB", "HTQ -HB", 10.753628, 106.652996);
        addIntersectionNode("Võ_Trường_Toản_HB", "Võ Trường Toản - HB", 10.753569, 106.651767);
        addIntersectionNode("Xóm_Vôi_NT", "Xóm Vôi -NT", 10.753056, 106.651746);
        addIntersectionNode("Xóm_Vôi_Trần_Chánh_Chiêu", "Xóm Vôi - Trần Chánh Chiêu", 10.752268, 106.651791);
        addIntersectionNode("Xóm_Vôi_Trang_Tử", "Xóm Vôi -Trang Tử", 10.751494, 106.651806);
        addIntersectionNode("Tạ_Uyên_Nguyễn_Chí_Thanh", "Tạ Uyên - Nguyễn Chí Thanh", 10.756785, 106.653652);
        addIntersectionNode("Tạ_Uyên_Tân_Thành", "Tạ Uyên - Tân Thành", 10.755678, 106.653716);
        addIntersectionNode("Tạ_Uyên_Phạm_Hữu_Chí", "Tạ Uyên - Phạm Hữu Chí", 10.754972, 106.653748);
        addIntersectionNode("Tạ_Uyên_Hồng_Bàng", "Tạ Uyên - Hồng Bàng", 10.753865, 106.653802);
        addIntersectionNode("PG_NT", "PG-NT", 10.752720, 106.652852);
        addIntersectionNode("PG_TCC", "PG-TCC", 10.752172, 106.652741);
        addIntersectionNode("PG_TT", "PG-TT", 10.751607, 106.652588);
        addIntersectionNode("PH_NT", "PH-NT", 10.752790, 106.653523);
        addIntersectionNode("PH_TCC", "PH-TCC", 10.752124, 106.653387);
        addIntersectionNode("PH_Trang_Tử", "PH-Trang Tử", 10.751652, 106.653255);
        addIntersectionNode("DTG_NCT", "DTG-NCT", 10.757012, 106.654541);
        addIntersectionNode("DTG_TT", "DTG-TT", 10.755837, 106.654743);
        addIntersectionNode("DTG_PHC", "DTG-PHC", 10.755191, 106.654848);
        addIntersectionNode("DTG_TH", "DTG-TH", 10.754508, 106.654981);
        addIntersectionNode("DTG_HB", "DTG-HB", 10.754056, 106.655071);
        addIntersectionNode("DTG_NgT", "DTG-NgT", 10.752835, 106.655259);
        addIntersectionNode("DTG_THD", "DTG-THD", 10.752007, 106.655392);
        addIntersectionNode("DTG_HTLO", "DTG-HTLO", 10.751061, 106.655474);
        addIntersectionNode("NNT_LVS", "NNT-LVS", 10.751082, 106.653309);
        addIntersectionNode("NNT_HTLO", "NNT-HTLO", 10.750465, 106.653392);
        addIntersectionNode("NNT_PVK", "NNT-PVK", 10.748933, 106.653767);
        addIntersectionNode("NNT_BS", "NNT-BS", 10.748574, 106.653866);
        addIntersectionNode("NNT_GP", "NNT-GP", 10.747490, 106.654303);
        addIntersectionNode("NNT_VVK", "NNT-VVK", 10.746744, 106.654735);
        addIntersectionNode("TT_LVS", "TT-LVS", 10.751135, 106.653769);
        addIntersectionNode("HL_HB", "HL-HB", 10.753736, 106.654388);
        addIntersectionNode("HL_NT", "HL-NT", 10.752875, 106.654413);
        addIntersectionNode("HL_THD", "HL-THD", 10.751977, 106.654414);
        addIntersectionNode("HL_HTLO", "HL- HTLO", 10.751106, 106.654422);
        addIntersectionNode("GC_HTLO", "GC-HTLO", 10.750751, 106.654526);
        addIntersectionNode("GC_PVK", "GC-PVK", 10.749130, 106.654970);
        addIntersectionNode("GC_BS", "GC-BS", 10.748743, 106.655062);
        addIntersectionNode("GC_GP", "GC-GP", 10.747910, 106.655300);
        addIntersectionNode("GC_VVK", "GC-VVK", 10.747198, 106.655528);
        addIntersectionNode("DNT_NCT", "DNT-NCT", 10.757318, 106.656008);
        addIntersectionNode("DNT_TT", "DNT-TT", 10.756068, 106.656067);
        addIntersectionNode("DNT_PHC", "DNT-PHC", 10.755417, 106.656079);
        addIntersectionNode("DNT_TH", "DNT-TH", 10.754744, 106.656109);
        addIntersectionNode("DNT_HB", "DNT-HB", 10.754250, 106.656125);
        addIntersectionNode("DNT_NT", "DNT-NT", 10.752893, 106.656131);
        addIntersectionNode("DNT_THD", "DNT-THD", 10.751988, 106.656049);
        addIntersectionNode("DNT_HTLO", "DNT-HTLO", 10.751029, 106.655963);
        addIntersectionNode("KB_HTLO", "KB-HTLO", 10.750758, 106.655718);
        addIntersectionNode("KB_PVK", "KB-PVK", 10.749289, 106.655907);
        addIntersectionNode("KB_VVK", "KB-VVK", 10.747780, 106.656543);
        addIntersectionNode("VT_VCH", "VT-VCH", 10.750258, 106.656057);
        addIntersectionNode("VT_PH", "VT-PH", 10.749700, 106.656139);
        addIntersectionNode("VT_THD", "VT-THD", 10.749115, 106.656298);
        addIntersectionNode("VT_VVK", "VT-VVK", 10.747900, 106.656799);
        addIntersectionNode("PCD_NCT", "PCD-NCT", 10.757514, 106.657118);
        addIntersectionNode("PCD_TT", "PCD-TT", 10.756247, 106.657168);
        addIntersectionNode("PCD_PHC", "PCD-PHC", 10.755592, 106.657178);
        addIntersectionNode("PH_HB", "PH-HB", 10.754254, 106.657307);
        addIntersectionNode("PH_LT", "PH-LT", 10.753569, 106.657380);
        addIntersectionNode("PH_NT", "PH-NT", 10.752950, 106.657412);
        addIntersectionNode("PH_THD", "PH-THD", 10.751966, 106.657507);
        addIntersectionNode("PH_HTLO", "PH-HTLO", 10.750930, 106.657408);
        addIntersectionNode("PH_VCH", "PH-VCH", 10.750260, 106.657412);
        addIntersectionNode("PH_THD", "PH-THD", 10.749347, 106.657461);
        addIntersectionNode("PH_VVK", "PH-VVK", 10.748298, 106.657650);
        addIntersectionNode("Nguyễn_Văn_Cừ_Nguyễn_Trãi", "Nguyễn Văn Cừ -Nguyễn Trãi", 10.759393, 106.683999);
        addIntersectionNode("Nguyễn_Văn_Cừ_An_Dương_Vương", "Nguyễn Văn Cừ -An Dương Vương", 10.761153, 106.683287);
        addIntersectionNode("Nguyễn_Văn_Cừ_Hùng_Vương", "Nguyễn Văn Cừ - Hùng Vương", 10.765392, 106.681374);
        addIntersectionNode("Nguyễn_Văn_Cừ_Phan_Văn_Trị", "Nguyễn Văn Cừ - Phan Văn Trị", 10.757955, 106.684545);
        addIntersectionNode("Nguyễn_Văn_Cừ_Trần_Hưng_Đạo", "Nguyễn Văn Cừ -Trần Hưng Đạo", 10.756368, 106.685074);
        addIntersectionNode("Nguyễn_Văn_Cừ_Võ_Văn_Kiệt", "Nguyễn Văn Cừ - Võ Văn Kiệt", 10.754050, 106.686635);
        addIntersectionNode("NgBieu_VVK", "NgBieu - VVK", 10.751950, 106.683855);
        addIntersectionNode("NgBieu_CDat", "NgBieu - CDat", 10.754668, 106.684143);
        addIntersectionNode("NgBieu_THD", "NgBieu - THD", 10.755956, 106.683697);
        addIntersectionNode("NgBieu_PhanVanTri", "NgBieu - PhanVanTri", 10.757360, 106.683173);
        addIntersectionNode("NgBieu_NgTrai", "NgBieu - NgTrai", 10.758932, 106.682609);
        addIntersectionNode("NgTrai_TBTrong", "NgTrai - TBTrong", 10.758314, 106.680747);
        addIntersectionNode("TBTrong_TranPhu", "Trần Bình Trọng - Trần Phú", 10.762590, 106.679082);
        addIntersectionNode("TBTrong_HV", "TBTrong - HV", 10.763634, 106.678649);
        addIntersectionNode("TBTrong_ADV", "Trần Bình Trọng - An Dương Vương", 10.759900, 106.680125);
        addIntersectionNode("HV_LHPhong", "HV - LHPhong", 10.762191, 106.676446);
        addIntersectionNode("LHPhong_TranPhu", "LHPhong - TranPhu", 10.760491, 106.677076);
        addIntersectionNode("LHPhong_ADV", "LHPhong - ADV", 10.758859, 106.677653);
        addIntersectionNode("LHPhong_NgTrai", "LHPhong - NgTrai", 10.757267, 106.678238);
        addIntersectionNode("LHPhong_PVTri", "LHPhong - PVTri", 10.755941, 106.678500);
        addIntersectionNode("LHP_THD", "LHP - THD", 10.754814, 106.678948);
        addIntersectionNode("VVK_HMD", "VVK - HMD", 10.752155, 106.677204);
        addIntersectionNode("HMD_NgThuc", "HMD - NgThuc", 10.753357, 106.677047);
        addIntersectionNode("HMD_THD", "HMD - THD", 10.754158, 106.676849);
        addIntersectionNode("HMD_PVTri", "HMD - PVTri", 10.755339, 106.676525);
        addIntersectionNode("HMD_NgTrai", "HMD - NgTrai", 10.756377, 106.676263);
        addIntersectionNode("HMD_ADV", "HMD - ADV", 10.758125, 106.675743);
        addIntersectionNode("HMD_TPhu", "HMD - TPhu", 10.758862, 106.675537);
        addIntersectionNode("TNTon_HV", "TNTon - HV", 10.761204, 106.674974);
        addIntersectionNode("SVH_HV", "SVH - HV", 10.760166, 106.673288);
        addIntersectionNode("SVH_TP_ADV", "SVH - TP - ADV", 10.757476, 106.673949);
        addIntersectionNode("SVH_NgChiThanh", "SVH - NgChiThanh", 10.760293, 106.671063);
        addIntersectionNode("YK_HV", "YK - HV", 10.759634, 106.672456);
        addIntersectionNode("YK_ADV", "YK - ADV", 10.757344, 106.672986);
        addIntersectionNode("NgTrai_BHNghia", "NgTrai - BHNghia", 10.755520, 106.674277);
        addIntersectionNode("BHNghia_PVTri", "BHNghia - PVTri", 10.754621, 106.674514);
        addIntersectionNode("BHNghia_THD", "BHNghia - THD", 10.753613, 106.674773);
        addIntersectionNode("BHNghia_NgThuc", "BHNghia - NgThuc", 10.752711, 106.675027);
        addIntersectionNode("BHNghia_BachVan", "BHNghia - BachVan", 10.751649, 106.675292);
        addIntersectionNode("BHNghia_DaoTan", "BHNghia - DaoTan", 10.750771, 106.675534);
        addIntersectionNode("TTKhai_VVK", "TTKhai - VVK", 10.749738, 106.675387);
        addIntersectionNode("TTKhai_BachVan", "TTKhai - BachVan", 10.751375, 106.674421);
        addIntersectionNode("TTKhai_NgThuc", "TTKhai - NgThuc", 10.752362, 106.673899);
        addIntersectionNode("TTKhai_THD", "TTKhai - THD", 10.753249, 106.673429);
        addIntersectionNode("NgTrai_TPhu", "NgTrai - TPhu", 10.755063, 106.671965);
        addIntersectionNode("NgDuyDuong_ADV", "NgDuyDuong - ADV", 10.757070, 106.671796);
        addIntersectionNode("NgDuyDuong_HV", "NgDuyDuong - HV", 10.758906, 106.671370);
        addIntersectionNode("NgTrPhuong_HV", "NgTrPhuong - HV", 10.757693, 106.669411);
        addIntersectionNode("NgTrPhuong_ADV", "NgTrPhuong - ADV", 10.756674, 106.669588);
        addIntersectionNode("NgTrPhuong_NgTrai", "NgTrPhuong - NgTrai", 10.754641, 106.669570);
        addIntersectionNode("NgTriPhuong_TPhu", "NgTriPhuong - TPhu", 10.753436, 106.669527);
        addIntersectionNode("NgTriPhuong_THD", "NgTriPhuong - THD", 10.752711, 106.669460);
        addIntersectionNode("NgTriPhuong_VVK", "NgTriPhuong - VVK", 10.750857, 106.669424);
        addIntersectionNode("AB_NgTrai", "AB - NgTrai", 10.754832, 106.670862);
        addIntersectionNode("AB_TPhu", "AB - TPhu", 10.754451, 106.671231);
        addIntersectionNode("AB_THD", "AB - THD", 10.752945, 106.672096);
        addIntersectionNode("AB_BachVan", "AB - BachVan", 10.750982, 106.672968);
        addIntersectionNode("AB_VVK", "AB - VVK", 10.749401, 106.673420);
        addIntersectionNode("NVDung_VVK", "NVDung - VVK", 10.750557, 106.671180);
        addIntersectionNode("NVDung_THD", "NVDung - THD", 10.752826, 106.671110);
        addIntersectionNode("NQ_VVK", "NQ - VVK", 10.750792, 106.666877);
        addIntersectionNode("NQ_THD", "NQ - THD", 10.752460, 106.666757);
        addIntersectionNode("NQ_NgTrai", "NQ - NgTrai", 10.753962, 106.666609);
        addIntersectionNode("NQ_MTT", "NQ - MTT", 10.755139, 106.666454);
        addIntersectionNode("NQ_HV_NGT_ADV", "NQ - HV - NGT - ADV", 10.756212, 106.666227);
        addIntersectionNode("NQ_AnDiem", "NQ - AnDiem", 10.751762, 106.666824);
        addIntersectionNode("NQ_NgChiThanh", "NQ - NgChiThanh", 10.759199, 106.665592);
        addIntersectionNode("Ngã_6_Nguyễn_Tri_Phương", "Ngã 6 Nguyễn Tri Phương", 10.759863, 106.668913);
        addIntersectionNode("NK_NgChiThanh", "NK - NgChiThanh", 10.758635, 106.662749);
        addIntersectionNode("NK_TangBatHo", "NK - TangBatHo", 10.758009, 106.662859);
        addIntersectionNode("NK_BaTrieu", "NK - BaTrieu", 10.757403, 106.662999);
        addIntersectionNode("NK_PHChi", "NK - PHChi", 10.756814, 106.663149);
        addIntersectionNode("NK_HB", "NK - HB", 10.755546, 106.663392);
        addIntersectionNode("HB_TanDa", "HB - TanDa", 10.755483, 106.663793);
        addIntersectionNode("HB_NgTrai", "HB - NgTrai", 10.753553, 106.664182);
        addIntersectionNode("TanDa_THD", "TanDa - THD", 10.752248, 106.664390);
        addIntersectionNode("TanDa_VVK", "TanDa - VVK", 10.750664, 106.664770);
        addIntersectionNode("TanDa_TanHang", "TanDa - TanHang", 10.751542, 106.664533);
        addIntersectionNode("LTK_NgChiThanh", "LTK - NgChiThanh", 10.758372, 106.661468);
        addIntersectionNode("LTK_TangBatHo", "LTK - TangBatHo", 10.757785, 106.661667);
        addIntersectionNode("LTK_BaTieu", "LTK - BaTieu", 10.757161, 106.661857);
        addIntersectionNode("LTK_PHChi", "LTK - PHChi", 10.756639, 106.662009);
        addIntersectionNode("LTK_HB", "LTK - HB", 10.755371, 106.662398);
        addIntersectionNode("PĐTV_NgTrai", "PĐTV - NgTrai", 10.753299, 106.662842);
        addIntersectionNode("PĐTV_THD", "PĐTV - THD", 10.752166, 106.662908);
        addIntersectionNode("TranHoa_THD", "TranHoa - THD", 10.752138, 106.662753);
        addIntersectionNode("TranDien_THD", "TranDien - THD", 10.752077, 106.662380);
        addIntersectionNode("PhamDon_THD", "PhamDon - THD", 10.752187, 106.663292);
        addIntersectionNode("PhamDon_TanHang", "PhamDon - TanHang", 10.751521, 106.663334);
        addIntersectionNode("TQP_NgTrai", "TQP - NgTrai", 10.749661, 106.661570);
        addIntersectionNode("TQP_HTLO", "TQP - HTLO", 10.751137, 106.661477);
        addIntersectionNode("TQP_THD", "TQP - THD", 10.752032, 106.661462);
        addIntersectionNode("TQP_HB", "TQP - HB", 10.755049, 106.661505);
        addIntersectionNode("TQP_PHChi", "TQP - PHChi", 10.756312, 106.661180);
        addIntersectionNode("LNH_HB", "LNH - HB", 10.754952, 106.660344);
        addIntersectionNode("LHN_LaoTu", "LHN - LaoTu", 10.753838, 106.660136);
        addIntersectionNode("LNH_NgTrai", "LNH - NgTrai", 10.753033, 106.659999);
        addIntersectionNode("LNH_THD", "LNH - THD", 10.752019, 106.660041);
        addIntersectionNode("LNH_HTLO", "LNH - HTLO", 10.750541, 106.660324);
        addIntersectionNode("ThuanKieu_NgChiThanh", "ThuanKieu - NgChiThanh", 10.757741, 106.658293);
        addIntersectionNode("ThuanKieu_TanThanh", "ThuanKieu- TanThanh", 10.756445, 106.658372);
        addIntersectionNode("ThuanKieu_PHChi", "ThuanKieu - PHChi", 10.755768, 106.658397);
        addIntersectionNode("ThuanKieu_TanHung", "ThuanKieu - TanHung", 10.755113, 106.658393);
        addIntersectionNode("CVL_HB", "CVL - HB", 10.754541, 106.658402);
        addIntersectionNode("CVL_NgTrai", "CVL - NgTrai", 10.752999, 106.658748);
        addIntersectionNode("CVL_THD", "CVL - THD", 10.752025, 106.658948);
        addIntersectionNode("CVL_HTLO_NguyenThi_MacCuu", "CVL - HTLO - NguyenThi - MacCuu", 10.750867, 106.659203);
        addIntersectionNode("NguyenThi_VVK", "NguyenThi - VVK", 10.748696, 106.658845);
        addIntersectionNode("MacCuu_VVK", "MacCuu-VVK", 10.749053, 106.659865);
        addIntersectionNode("VanKiep_VVK", "VanKiep - VVK", 10.749222, 106.660421);
        addIntersectionNode("HTLO_VVK", "HTLO - VVK", 10.750264, 106.663408);
        addIntersectionNode("HTLO_NgAnKhuong", "HTLO - NgAnKhuong", 10.750624, 106.658410);
        addIntersectionNode("NgAnKkhuong_THDuc", "NgAnKkhuong - THDuc", 10.749405, 106.658304);
        addIntersectionNode("TBTrong_THD", "Trần Bình Trọng - Trần Hưng Đạo", 10.755404, 106.681397);// Tọa độ cuối cùng bị thiếu, đặt 0.0
        addIntersectionNode("TBTrong_VVK", "Trần Bình Trọng - Võ Văn Kiệt", 10.752363, 106.682127);// Tọa độ cuối cùng bị thiếu, đặt 0.0


        Log.d(TAG, "Added estimated intersection nodes. Total nodes: " + nodes.size());


        // --- 3. Tạo các Edge (Đường nối) giữa các Node ---
        // Nối các node giao lộ và các node camera với nhau.
        // Đây là phần cần mô phỏng cấu trúc đường bộ thực tế.
        // Sử dụng khoảng cách đường chim bay giữa các node làm trọng số cạnh.

        // Nối các giao lộ chính và các giao lộ lân cận (mô phỏng đường đi)
        // Các lời gọi addEdge() được tạo tự động từ file Location Node.docx
// Các cạnh được tạo hai chiều (đồ thị vô hướng).

        addEdge("Nguyễn_Thị_Nhỏ_Nguyễn_Chí_Thanh", "Nguyễn_Thị_Nhỏ_Tân_Thành");
        addEdge("Nguyễn_Thị_Nhỏ_Tân_Thành", "NTN_HBàng");
        addEdge("NTN_HBàng", "NTN_TCChiếu");
        addEdge("NTN_TCChiếu", "NTN_TTử");
        addEdge("NTN_TTử", "Xóm_Vôi_Trang_Tử");
        addEdge("NgCThanh_HTQ", "Nguyễn_Thị_Nhỏ_Nguyễn_Chí_Thanh");
        addEdge("NgCThanh_HTQ", "HTQ_TT");
        addEdge("HTQ_TT", "HTQ_HB");
        addEdge("HTQ_TT", "Nguyễn_Thị_Nhỏ_Tân_Thành");
        addEdge("Võ_Trường_Toản_HB", "NTN_HBàng");
        addEdge("HTQ_HB", "Võ_Trường_Toản_HB");
        addEdge("Xóm_Vôi_NT", "Võ_Trường_Toản_HB");
        addEdge("Xóm_Vôi_NT", "NTN_HBàng");
        addEdge("Xóm_Vôi_NT", "PG_NT");
        addEdge("Xóm_Vôi_Trần_Chánh_Chiêu", "Xóm_Vôi_NT");
        addEdge("Xóm_Vôi_Trần_Chánh_Chiêu", "NTN_TCChiếu");
        addEdge("Xóm_Vôi_Trang_Tử", "Xóm_Vôi_Trần_Chánh_Chiêu");
        addEdge("Tạ_Uyên_Nguyễn_Chí_Thanh", "NgCThanh_HTQ");
        addEdge("Tạ_Uyên_Tân_Thành", "Tạ_Uyên_Nguyễn_Chí_Thanh");
        addEdge("Tạ_Uyên_Tân_Thành", "HTQ_TT");
        addEdge("Tạ_Uyên_Tân_Thành", "DTG_TT");
        addEdge("Tạ_Uyên_Phạm_Hữu_Chí", "Tạ_Uyên_Tân_Thành");
        addEdge("Tạ_Uyên_Phạm_Hữu_Chí", "DTG_PHC");
        addEdge("Tạ_Uyên_Phạm_Hữu_Chí", "Tạ_Uyên_Hồng_Bàng");
        addEdge("Tạ_Uyên_Hồng_Bàng", "PH_NT");
        addEdge("Tạ_Uyên_Hồng_Bàng", "HTQ_HB");
        addEdge("Tạ_Uyên_Hồng_Bàng", "DTG_HB");
        addEdge("PG_NT", "HTQ_HB");
        addEdge("PG_TCC", "PG_NT");
        addEdge("PG_TCC", "Xóm_Vôi_Trần_Chánh_Chiêu");
        addEdge("PG_TT", "PG_TCC");
        addEdge("PG_TT", "Xóm_Vôi_Trang_Tử");
        addEdge("PH_NT", "PG_NT");
        addEdge("PH_NT", "PH_TCC");
        addEdge("PH_TCC", "PG_TCC");
        addEdge("PH_TrangTử", "PH_TCC");
        addEdge("PH_TrangTử", "PG_TT");
        addEdge("DTG_NCT", "Tạ_Uyên_Nguyễn_Chí_Thanh");
        addEdge("DTG_TT", "DTG_NCT");
        addEdge("DTG_PHC", "DTG_TT");
        addEdge("DTG_TH", "DTG_PHC");
        addEdge("DTG_HB", "DTG_TH");
        addEdge("DTG_NgT", "DTG_HB");
        addEdge("DTG_THD", "DTG_NgT");
        addEdge("DTG_HTLO", "DTG_THD");
        addEdge("NNT_LVS", "PH_TrangTử");
        addEdge("NNT_HTLO", "NNT_LVS");
        addEdge("NNT_PVK", "NNT_HTLO");
        addEdge("NNT_BS", "NNT_PVK");
        addEdge("NNT_GP", "NNT_BS");
        addEdge("NNT_VVK", "NNT_GP");
        addEdge("TT_LVS", "PH_TrangTử");
        addEdge("TT_LVS", "NNT_LVS");
        addEdge("HL_HB", "Tạ_Uyên_Hồng_Bàng");
        addEdge("HL_HB", "DTG_HB");
        addEdge("HL_NT", "HL_HB");
        addEdge("HL_NT", "PH_NT");
        addEdge("HL_NT", "DTG_NgT");
        addEdge("HL_THD", "HL_NT");
        addEdge("HL_THD", "DTG_THD");
        addEdge("HL_HTLO", "HL_THD");
        addEdge("HL_HTLO", "TT_LVS");
        addEdge("HL_HTLO", "DTG_HTLO");
        addEdge("GC_HTLO", "HL_HTLO");
        addEdge("GC_HTLO", "NNT_HTLO");
        addEdge("GC_PVK", "GC_HTLO");
        addEdge("GC_PVK", "NNT_PVK");
        addEdge("GC_BS", "GC_PVK");
        addEdge("GC_BS", "NNT_BS");
        addEdge("GC_GP", "GC_BS");
        addEdge("GC_GP", "NNT_GP");
        addEdge("GC_VVK", "GC_GP");
        addEdge("GC_VVK", "NNT_VVK");
        addEdge("DNT_NCT", "DTG_NCT");
        addEdge("DNT_TT", "DNT_NCT");
        addEdge("DTG_TT", "DNT_TT");
        addEdge("DNT_PHC", "DNT_TT");
        addEdge("DNT_PHC", "DTG_PHC");
        addEdge("DNT_TH", "DNT_PHC");
        addEdge("DNT_TH", "DTG_TH");
        addEdge("DNT_HB", "DNT_TH");
        addEdge("DNT_HB", "DTG_HB");
        addEdge("DNT_HB", "DTG_HB"); // Cạnh lặp lại, giữ nguyên theo danh sách bạn cung cấp
        addEdge("DNT_NT", "DNT_HB");
        addEdge("DNT_NT", "DTG_NgT");
        addEdge("DNT_THD", "DNT_NT");
        addEdge("DNT_THD", "DTG_THD");
        addEdge("DNT_HTLO", "DNT_THD");
        addEdge("DNT_HTLO", "DTG_HTLO");
        addEdge("KB_HTLO", "GC_HTLO");
        addEdge("KB_PVK", "KB_HTLO");
        addEdge("KB_PVK", "GC_PVK");
        addEdge("KB_VVK", "GC_VVK");
        addEdge("VT_VCH", "PH_VCH");
        addEdge("VT_PH", "VT_VCH");
        addEdge("VT_THD", "VT_PH");
        addEdge("VT_VVK", "VT_THD");
        addEdge("VT_VVK", "KB_VVK");
        addEdge("PCD_NCT", "DNT_NCT");
        addEdge("PCD_TT", "PCD_NCT");
        addEdge("PCD_TT", "DNT_TT");
        addEdge("PCD_PHC", "PCD_TT");
        addEdge("PCD_PHC", "DNT_PHC");
        addEdge("PH_HB", "DNT_HB");
        addEdge("PH_LT", "PH_HB");
        addEdge("PH_NT", "PH_LT");
        addEdge("PH_NT", "DNT_NT");
        addEdge("PH_THD", "PH_NT");
        addEdge("PH_THD", "DNT_THD");
        addEdge("PH_HTLO", "PH_THD");
        addEdge("PH_HTLO", "DNT_HTLO");
        addEdge("PH_VCH", "PH_HTLO");
        addEdge("PH_THD", "PH_VCH");
        addEdge("PH_THD", "VT_THD");
        addEdge("PH_VVK", "PH_THD");
        addEdge("PH_VVK", "VT_VVK");
        addEdge("Nguyễn_Văn_Cừ_Nguyễn_Trãi", "Nguyễn_Văn_Cừ_Phan_Văn_Trị");
        addEdge("Nguyễn_Văn_Cừ_Phan_Văn_Trị", "Nguyễn_Văn_Cừ_Trần_Hưng_Đạo");
        addEdge("Nguyễn_Văn_Cừ_Trần_Hưng_Đạo", "Nguyễn_Văn_Cừ_Võ_Văn_Kiệt");
        addEdge("Nguyễn_Văn_Cừ_Nguyễn_Trãi", "Nguyễn_Văn_Cừ_An_Dương_Vương");
        addEdge("Nguyễn_Văn_Cừ_An_Dương_Vương", "Nguyễn_Văn_Cừ_Hùng_Vương");
        addEdge("NgBieu_VVK", "NgBieu_CDat");
        addEdge("NgBieu_CDat", "NgBieu_THD");
        addEdge("NgBieu_THD", "NgBieu_PhanVanTri");
        addEdge("NgBieu_PhanVanTri", "NgBieu_NgTrai");
        addEdge("TBTrong_HV", "Nguyễn_Văn_Cừ_Hùng_Vương");
        addEdge("TBTrong_TranPhu","Nguyễn_Văn_Cừ_Hùng_Vương");
        addEdge("TBTrong_TranPhu", "TBTrong_ADV");
        addEdge("TBTrong_ADV","Nguyễn_Văn_Cừ_An_Dương_Vương");
        addEdge("TBTrong_ADV", "NgTrai_TBTrong");
        addEdge("TBTrong_THD", "NgTrai_TBTrong");
        addEdge("NgTrai_TBTrong", "NgTrai_BHNghia");
        addEdge("NgTrai_BHNghia", "NgTrai_TPhu");
        addEdge("NgTrai_TPhu", "NgTrai_LHPhong");
        addEdge("NgTrai_LHPhong", "NgTrai_NQ");
        addEdge("NgTrai_NQ", "NgTrai_NgTriPhuong");
        addEdge("NgTrai_NgTriPhuong", "NgTrai_AB");
        addEdge("NgTrai_AB", "NgTrai_LNH");
        addEdge("NgTrai_LNH", "NgTrai_CVL");
        addEdge("NgTrai_CVL", "NgTrai_ThuanKieu");
        addEdge("NgTrai_ThuanKieu", "NgTrai_DNT");
        addEdge("NgTrai_DNT", "NgTrai_PCD");
        addEdge("NgTrai_PCD", "NgTrai_Nguyễn_Văn_Cừ");
        addEdge("TBTrong_TranPhu", "TBTrong_HV");
        addEdge("HV_LHPhong", "HV_TNTon");
        addEdge("HV_TNTon", "HV_SVH");
        addEdge("HV_SVH", "HV_YK");
        addEdge("HV_YK", "HV_NgTrPhuong");
        addEdge("NgTrPhuong_HV", "NgTrPhuong_ADV");
        addEdge("NgTrPhuong_ADV", "NgTrPhuong_NgTrai");
        addEdge("NgTrPhuong_NgTrai", "NgTriPhuong_TPhu");
        addEdge("NgTriPhuong_TPhu", "NgTriPhuong_THD");
        addEdge("NgTriPhuong_THD", "NgTriPhuong_VVK");
        addEdge("LHPhong_TranPhu", "LHPhong_ADV");
        addEdge("LHPhong_ADV", "LHPhong_NgTrai");
        addEdge("LHPhong_NgTrai", "LHPhong_PVTri");
        addEdge("LHPhong_PVTri", "LHP_THD");
        addEdge("LHP_THD", "TBTrong_THD");
        addEdge("VVK_HMD", "VVK_NgBieu");
        addEdge("VVK_NgBieu", "VVK_Nguyễn_Văn_Cừ");
        addEdge("VVK_Nguyễn_Văn_Cừ", "VVK_NQ");
        addEdge("VVK_NQ", "VVK_HTLO");
        addEdge("VVK_HTLO", "VVK_VanKiep");
        addEdge("VVK_VanKiep", "VVK_MacCuu");
        addEdge("VVK_MacCuu", "VVK_NguyenThi");
        addEdge("VVK_NguyenThi", "VVK_NgAnKhuong");
        addEdge("VVK_NgAnKhuong", "VVK_TTKhai");
        addEdge("VVK_TTKhai", "VVK_BHNghia");
        addEdge("VVK_BHNghia", "VVK_HMD"); // Loop back
        addEdge("HMD_NgThuc", "HMD_THD");
        addEdge("HMD_THD", "HMD_PVTri");
        addEdge("HMD_PVTri", "LHPhong_PVTri");

//        addEdge("HMD_PVTri", "HMD_NgTrai");
//        addEdge("HMD_NgTrai", "HMD_ADV");
//        addEdge("HMD_ADV", "HMD_TPhu");
//        addEdge("SVH_TP_ADV", "SVH_NgChiThanh");
//        addEdge("YK_ADV", "YK_HV"); // Assuming ADV and HV are on the same "street" for YK
//        addEdge("BHNghia_PVTri", "BHNghia_THD");
//        addEdge("BHNghia_THD", "BHNghia_NgThuc");
//        addEdge("BHNghia_NgThuc", "BHNghia_BachVan");
//        addEdge("BHNghia_BachVan", "BHNghia_DaoTan");
//        addEdge("TTKhai_BachVan", "TTKhai_NgThuc");
//        addEdge("TTKhai_NgThuc", "TTKhai_THD");
//        addEdge("NgDuyDuong_ADV", "NgDuyDuong_HV");
//        addEdge("AB_NgTrai", "AB_TPhu");
//        addEdge("AB_TPhu", "AB_THD");
//        addEdge("AB_THD", "AB_BachVan");
//        addEdge("AB_BachVan", "AB_VVK");
//        addEdge("NVDung_VVK", "NVDung_THD");
//        addEdge("NQ_THD", "NQ_NgTrai");
//        addEdge("NQ_NgTrai", "NQ_MTT");
//        addEdge("NQ_MTT", "NQ_HV_NGT_ADV");
//        addEdge("NQ_VVK", "NQ_AnDiem");
//        addEdge("Ngã_6_Nguyễn_Tri_Phương", "NK_NgChiThanh");
//        addEdge("NK_NgChiThanh", "NK_TangBatHo");
//        addEdge("NK_TangBatHo", "NK_BaTrieu");
//        addEdge("NK_BaTrieu", "NK_PHChi");
//        addEdge("NK_PHChi", "NK_HB");
//        addEdge("HB_TanDa", "HB_NgTrai");
//        addEdge("TanDa_THD", "TanDa_TanHang");
//        addEdge("TanDa_THD", "HB_NgTrai");
//        addEdge("TanDa_VVK", "TanDa_TanHang");
//        addEdge("LTK_NgChiThanh", "LTK_TangBatHo");
//        addEdge("LTK_TangBatHo", "LTK_BaTieu");
//        addEdge("LTK_BaTieu", "LTK_PHChi");
//        addEdge("LTK_PHChi", "LTK_HB");
//        addEdge("PĐTV_NgTrai", "PĐTV_THD");
//        addEdge("TranHoa_THD", "TranDien_THD");
//        addEdge("PhamDon_THD", "PhamDon_TanHang");
//        addEdge("TQP_NgTrai", "TQP_HTLO");
//        addEdge("TQP_HTLO", "TQP_THD");
//        addEdge("TQP_THD", "TQP_HB");
//        addEdge("TQP_HB", "TQP_PHChi");
//        addEdge("LNH_HB", "LHN_LaoTu");
//        addEdge("LHN_LaoTu", "LNH_NgTrai");
//        addEdge("LNH_NgTrai", "LNH_THD");
//        addEdge("LNH_THD", "LNH_HTLO");
//        addEdge("ThuanKieu_NgChiThanh", "ThuanKieu_TanThanh");
//        addEdge("ThuanKieu_TanThanh", "ThuanKieu_PHChi");
//        addEdge("ThuanKieu_PHChi", "ThuanKieu_TanHung");
//        addEdge("CVL_HB", "CVL_NgTrai");
//        addEdge("CVL_NgTrai", "CVL_THD");
//        addEdge("CVL_THD", "CVL_HTLO_NguyenThi_MacCuu");
//        addEdge("NguyenThi_VVK", "MacCuu_VVK");
//        addEdge("MacCuu_VVK", "VanKiep_VVK");
//        addEdge("HTLO_VVK", "HTLO_NgAnKhuong");
//        addEdge("NgAnKkhuong_THDuc", "NgAnKhuong_VVK");
        // Các lời gọi addEdge() mới, tập trung vào các tuyến đường ngang
// Các cạnh được tạo hai chiều (đồ thị vô hướng).
// Những đường này không trùng lặp với các đường đã trả về trước đó (dựa trên tên đường chính).

//        addEdge("Nguyễn_Thị_Nhỏ_Nguyễn_Chí_Thanh", "NgCThanh_HTQ");
//        addEdge("NgCThanh_HTQ", "Tạ_Uyên_Nguyễn_Chí_Thanh");
//        addEdge("Tạ_Uyên_Nguyễn_Chí_Thanh", "DTG_NCT");
//        addEdge("DTG_NCT", "DNT_NCT");
//        addEdge("DNT_NCT", "PCD_NCT");
//        addEdge("PCD_NCT", "ThuanKieu_NgChiThanh");
//        addEdge("ThuanKieu_NgChiThanh", "NQ_NgChiThanh");
//        addEdge("Xóm_Vôi_NT", "PG_NT");
//        addEdge("PG_NT", "PH_NT");
//        addEdge("PH_NT", "HL_NT");
//        addEdge("HL_NT", "DNT_NT");
//        addEdge("DNT_NT", "PCD_NT");
//        addEdge("PCD_NT", "Nguyễn_Văn_Cừ_Nguyễn_Trãi");
//        addEdge("Nguyễn_Văn_Cừ_Nguyễn_Trãi", "NgBieu_NgTrai");
//        addEdge("NgBieu_NgTrai", "NgTrai_AB");
//        addEdge("NgTrai_AB", "NgTrai_LNH");
//        addEdge("NgTrai_LNH", "NgTrai_CVL");
//        addEdge("NgTrai_CVL", "NgTrai_ThuanKieu");
//        addEdge("NgTrai_ThuanKieu", "NgTrai_DNT");
//        addEdge("NgTrai_DNT", "NgTrai_PCD");
//        addEdge("NgTrai_PCD", "NgTrai_Nguyễn_Văn_Cừ");
//        addEdge("Xóm_Vôi_Trần_Chánh_Chiêu", "PG_TCC");
//        addEdge("PG_TCC", "PH_TCC");
//        addEdge("Xóm_Vôi_Trang_Tử", "PG_TT");
//        addEdge("PG_TT", "PH_Trang_Tử");
//        addEdge("Võ_Trường_Toản_HB", "HTQ_HB");
//        addEdge("HTQ_HB", "Tạ_Uyên_Hồng_Bàng");
//        addEdge("Tạ_Uyên_Hồng_Bàng", "DTG_HB");
//        addEdge("DTG_HB", "HL_HB");
//        addEdge("HL_HB", "DNT_HB");
//        addEdge("DNT_HB", "PH_HB");
//        addEdge("PH_HB", "LNH_HB");
//        addEdge("LNH_HB", "TQP_HB");
//        addEdge("TQP_HB", "CVL_HB");
//        addEdge("Nguyễn_Thị_Nhỏ_Tân_Thành", "Tạ_Uyên_Tân_Thành");
//        addEdge("Tạ_Uyên_Tân_Thành", "DTG_TT");
//        addEdge("DTG_TT", "DNT_TT");
//        addEdge("DNT_TT", "PCD_TT");
//        addEdge("PCD_TT", "ThuanKieu_TanThanh");
//        addEdge("ThuanKieu_TanThanh", "HTQ_TT");
//        addEdge("Tạ_Uyên_Phạm_Hữu_Chí", "DTG_PHC");
//        addEdge("DTG_PHC", "DNT_PHC");
//        addEdge("DNT_PHC", "PCD_PHC");
//        addEdge("PCD_PHC", "ThuanKieu_PHChi");
//        addEdge("ThuanKieu_PHChi", "TQP_PHChi");
//        addEdge("DTG_TH", "DNT_TH");
//        addEdge("DNT_TH", "ThuanKieu_TanHung");
//        addEdge("ThuanKieu_TanHung", "Ngã_6_Nguyễn_Tri_Phương");
//        addEdge("Ngã_6_Nguyễn_Tri_Phương", "NK_HB");
//        addEdge("NK_HB", "HB_TanDa");
//        addEdge("TanDa_TanHang", "PhamDon_TanHang");
//        addEdge("Nguyễn_Văn_Cừ_Phan_Văn_Trị", "NgBieu_PhanVanTri");
//        addEdge("NgBieu_NgTrai", "NgTrai_TBTrong");
//        addEdge("NgBieu_THD", "TBTrong_THD");
//        addEdge( "TBTrong_THD", "TBTrong_VVK");
//        addEdge(  "TBTrong_VVK", "VVK_HMD");
//
//        addEdge("NgTrPhuong_ADV", "NgDuyDuong_ADV");
//        addEdge("NgDuyDuong_ADV", "NQ_HV_NGT_ADV");
//        addEdge("Nguyễn_Văn_Cừ_Hùng_Vương", "HV_NgTrPhuong");
//        addEdge("HV_NgTrPhuong", "NgTrPhuong_HV");
//        addEdge("NgTrPhuong_HV", "NgDuyDuong_HV");
//        addEdge("NgDuyDuong_HV", "NQ_HV_NGT_ADV");
//        addEdge("Nguyễn_Văn_Cừ_Trần_Hưng_Đạo", "NgBieu_THD");
//        addEdge("NgBieu_THD", "NgTriPhuong_THD");
//        addEdge("NgTriPhuong_THD", "TanDa_THD");
//        addEdge("TanDa_THD", "TranHoa_THD");
//        addEdge("TranHoa_THD", "TranDien_THD");
//        addEdge("TranDien_THD", "PhamDon_THD");
//        addEdge("PhamDon_THD", "NgAnKkhuong_THDuc");
//        addEdge("NgAnKkhuong_THDuc", "TQP_THD");
//        addEdge("TQP_THD", "LNH_THD");
//        addEdge("LNH_THD", "CVL_THD");
//        addEdge("DTG_THD", "HL_THD");
//        addEdge("HL_THD", "DNT_THD");
//        addEdge("DNT_THD", "PH_THD");
//        addEdge("PH_THD", "VT_THD");
//        addEdge("Nguyễn_Văn_Cừ_Võ_Văn_Kiệt", "NgBieu_VVK");
//        addEdge("NgBieu_VVK", "TBTrong_VVK");
//        addEdge("VVK_HMD", "TTKhai_VVK");
//        addEdge("AB_VVK", "TTKhai_VVK");
//        addEdge("TTKhai_BachVan", "TTKhai_VVK");
//        addEdge("AB_VVK", "NVDung_VVK");
//        addEdge("NgTriPhuong_VVK", "NVDung_VVK");
//        addEdge("NgTriPhuong_VVK", "NQ_VVK");
//
//        addEdge("TanDa_VVK", "NgAnKhuong_VVK");
//        addEdge("NgAnKhuong_VVK", "NVDung_VVK");
//        addEdge("NVDung_VVK", "VVK_NguyenThi");
//        addEdge("VVK_NguyenThi", "VVK_NgAnKhuong");
//        addEdge("VVK_NgAnKhuong", "VVK_TTKhai");
//        addEdge("VVK_TTKhai", "VVK_BHNghia");
//        addEdge("VVK_BHNghia", "VVK_HMD");
//        addEdge("VVK_HMD", "VVK_NgBieu");
//        addEdge("VVK_HMD", "VVK_NgThuc");
//        addEdge("VVK_HMD", "BHNghia_BachVan"); // This creates a loop back which is fine for a graph
//        addEdge("VVK_NgBieu", "VVK_Nguyễn_Văn_Cừ");
//        addEdge("VVK_Nguyễn_Văn_Cừ", "VVK_NQ");
//        addEdge("VVK_NQ", "VVK_HTLO");
//        addEdge("VVK_HTLO", "VVK_VanKiep");
//        addEdge("VVK_VanKiep", "VVK_MacCuu");
//        addEdge("VVK_MacCuu", "VVK_NguyenThi");
//        addEdge("VVK_NguyenThi", "VVK_NgAnKhuong");
//        addEdge("NNT_HTLO", "HL_HTLO");
//        addEdge("HL_HTLO", "DNT_HTLO");
//        addEdge("DNT_HTLO", "KB_HTLO");
//        addEdge("KB_HTLO", "PH_HTLO");
//        addEdge("PH_HTLO", "VT_VCH");
//        addEdge("VT_VCH", "PH_VCH");
//        addEdge("PH_VCH", "CVL_HTLO_NguyenThi_MacCuu");
//        addEdge("CVL_HTLO_NguyenThi_MacCuu", "TQP_HTLO");
//        addEdge("TQP_HTLO", "LNH_HTLO");
//        addEdge("LNH_HTLO", "HTLO_VVK");
//        addEdge("HTLO_VVK", "GC_HTLO");
//        addEdge("GC_HTLO", "NNT_PVK");
//        addEdge("NNT_PVK", "GC_PVK");
//        addEdge("GC_PVK", "KB_PVK");
//        addEdge("KB_PVK", "NNT_BS");
//        addEdge("NNT_BS", "GC_BS");
//        addEdge("GC_BS", "NNT_GP");
//        addEdge("NNT_GP", "GC_GP");
//        addEdge("GC_GP", "NNT_VVK");
//        addEdge("NNT_VVK", "GC_VVK");
//        addEdge("GC_VVK", "KB_VVK");
//        addEdge("KB_VVK", "VT_VVK");
//        addEdge("VT_VVK", "PH_VVK");
//        addEdge("TanDa_VVK", "NQ_VVK");
//        addEdge("AB_VVK", "VVK_MacCuu");
//        addEdge("VVK_MacCuu", "MacCuu_VVK");
//        addEdge("MacCuu_VVK", "VanKiep_VVK");
//        addEdge("VanKiep_VVK", "HTLO_VVK");
//        addEdge("HTLO_VVK", "NguyenThi_VVK");
//        addEdge("NguyenThi_VVK", "NgAnKhuong_VVK");
//        addEdge("HMD_NgThuc", "BHNghia_NgThuc");
//        addEdge("BHNghia_NgThuc", "TTKhai_NgThuc");
//        addEdge("HMD_THD", "BHNghia_THD");
//        addEdge("BHNghia_THD", "TTKhai_THD");
//        addEdge("HMD_PVTri", "BHNghia_PVTri");
//        addEdge("HMD_NgTrai", "NgTrai_TBTrong");
//        addEdge("NgTrai_TBTrong", "NgTrai_BHNghia");
//        addEdge("NgTrai_BHNghia", "NgTrai_TPhu");
//        addEdge("NgTrai_TPhu", "NgTrai_LHPhong");
//        addEdge("NgTrai_LHPhong", "NgTrai_NQ");
//        addEdge("NgTrai_NQ", "NgTrai_NgTriPhuong");
//        addEdge("NgTrai_NgTriPhuong", "NgTrai_AB");
//        addEdge("NgTrai_AB", "NgTrai_LNH");
//        addEdge("NgTrai_LNH", "NgTrai_CVL");
//        addEdge("NgTrai_CVL", "NgTrai_ThuanKieu");
//        addEdge("NgTrai_ThuanKieu", "NgTrai_DNT");
//        addEdge("NgTrai_DNT", "NgTrai_PCD");
//        addEdge("NgTrai_PCD", "NgTrai_Nguyễn_Văn_Cừ");
//        addEdge("HMD_ADV", "YK_ADV");
//        addEdge("YK_ADV", "NgTrPhuong_ADV");
//        addEdge("NgTrPhuong_ADV", "NgDuyDuong_ADV");
//        addEdge("NgDuyDuong_ADV", "NQ_HV_NGT_ADV");
//        addEdge("HMD_TPhu", "AB_TPhu");
//        addEdge("AB_TPhu", "NgTriPhuong_TPhu");
//        addEdge("LHPhong_TranPhu", "SVH_TP_ADV");
//        addEdge("SVH_TP_ADV", "SVH_NgChiThanh");
//        addEdge("SVH_NgChiThanh", "NQ_NgChiThanh");
//        addEdge("BHNghia_BachVan", "TTKhai_BachVan");
//        addEdge("BHNghia_BachVan", "AB_BachVan");
//        addEdge("NgTriPhuong_TPhu", "LHPhong_PVTri");
//        addEdge("LHPhong_PVTri", "LHP_THD");
//        addEdge("NQ_AnDiem", "NQ_THD");
//        addEdge("LTK_NgChiThanh", "LTK_TangBatHo");
//        addEdge("LTK_TangBatHo", "LTK_BaTieu");
//        addEdge("LTK_BaTieu", "LTK_PHChi");
//        addEdge("LTK_PHChi", "LTK_HB");
//        addEdge("HB_NgTrai", "PĐTV_NgTrai");
//        addEdge("PĐTV_NgTrai", "TQP_NgTrai");
//        addEdge("TQP_NgTrai", "LNH_NgTrai");
//        addEdge("PĐTV_THD", "TranHoa_THD");
//        addEdge("TranHoa_THD", "TranDien_THD");
//        addEdge("TranDien_THD", "PhamDon_THD");
//        addEdge("PhamDon_THD", "NgAnKkhuong_THDuc");
//        addEdge("NgAnKkhuong_THDuc", "TQP_THD");
//        addEdge("TQP_THD", "LNH_THD");
//        addEdge("LNH_THD", "CVL_THD");
//        addEdge("LHN_LaoTu", "LNH_THD"); // Assuming LHN is a variation of LNH
//


        // Nối các node camera với các giao lộ hoặc node camera khác gần nhất
        // Đây là phần phức tạp và cần dữ liệu đường bộ thực tế hơn.
        // Ví dụ đơn giản: Nối mỗi camera với 1-2 node (giao lộ hoặc camera) gần nhất trong đồ thị mô phỏng.
        double maxConnectionDistance = 300; // Bán kính tối đa để nối camera với node gần nhất (mét)
        double minDistanceForSeparateNode = 50; // Khoảng cách tối thiểu để coi là node riêng biệt

        if (cameraInfoList != null) {
            for (CameraInfo cameraInfo : cameraInfoList) {
                Node cameraNode = nodeMap.get(cameraInfo.getId());
                if (cameraNode != null) {
                    List<Node> nearbyNodes = new ArrayList<>();
                    // Tìm các node gần camera này
                    for (Node node : nodes) {
                        // Không nối node camera với chính nó
                        if (!node.getId().equals(cameraNode.getId())) {
                            float[] distResult = new float[1];
                            Location.distanceBetween(cameraNode.getLatitude(), cameraNode.getLongitude(),
                                    node.getLatitude(), node.getLongitude(), distResult);
                            double distance = distResult[0];
                            if (distance <= maxConnectionDistance) {
                                nearbyNodes.add(node);
                            }
                        }
                    }

                    // Nối camera với các node gần nhất (ví dụ: 2 node gần nhất)
                    // Sắp xếp theo khoảng cách
                    nearbyNodes.sort((n1, n2) -> {
                        float[] dist1 = new float[1];
                        Location.distanceBetween(cameraNode.getLatitude(), cameraNode.getLongitude(),
                                n1.getLatitude(), n1.getLongitude(), dist1);
                        float[] dist2 = new float[1];
                        Location.distanceBetween(cameraNode.getLatitude(), cameraNode.getLongitude(),
                                n2.getLatitude(), n2.getLongitude(), dist2);
                        return Float.compare(dist1[0], dist2[0]);
                    });

                    int connectionsMade = 0;
                    for (Node nearbyNode : nearbyNodes) {
                        // Chỉ nối nếu khoảng cách đủ lớn để không phải là cùng 1 điểm
                        float[] distResult = new float[1];
                        Location.distanceBetween(cameraNode.getLatitude(), cameraNode.getLongitude(),
                                nearbyNode.getLatitude(), nearbyNode.getLongitude(), distResult);
                        double distance = distResult[0];

                        if (distance > minDistanceForSeparateNode) {
                            addEdge(cameraNode.getId(), nearbyNode.getId());
                            connectionsMade++;
                            if (connectionsMade >= 2) break; // Giới hạn số lượng kết nối từ mỗi camera
                        } else {
                            // Nếu khoảng cách rất gần, có thể coi camera này nằm tại node đó
                            // và không cần thêm cạnh mới, hoặc thêm cạnh với trọng số rất nhỏ.
                            // Hiện tại, addEdge sẽ kiểm tra và không thêm cạnh trùng.
                            Log.d(TAG, "  Camera " + cameraNode.getId() + " is very close (" + String.format(Locale.US, "%.2f", distance) + "m) to node " + nearbyNode.getId() + ". Assuming they are at the same location or connected directly.");
                        }
                    }
                    if (connectionsMade == 0 && !nearbyNodes.isEmpty()) {
                        // Nếu không tìm được node nào đủ xa để nối (ví dụ: chỉ có 1 node rất gần),
                        // thì có thể nối với node rất gần đó với trọng số nhỏ nếu chưa có cạnh.
                        Node closestNode = nearbyNodes.get(0);
                        addEdge(cameraNode.getId(), closestNode.getId());
                    } else if (connectionsMade == 0) {
                        Log.w(TAG, "  Camera " + cameraNode.getId() + " has no nearby nodes within " + maxConnectionDistance + "m to connect to.");
                    }
                }
            }
        }


        Log.d(TAG, "Finished building simulated District 5 graph with " + edges.size() + " edges.");
    }

    // Helper method để thêm một Node giao lộ vào danh sách và map
    private void addIntersectionNode(String id, String name, double latitude, double longitude) {
        if (!nodeMap.containsKey(id)) {
            Node newNode = new Node(id, name, latitude, longitude, 0.0f, false); // false vì đây là node giao lộ
            nodes.add(newNode);
            nodeMap.put(id, newNode);
            Log.d(TAG, "  Added intersection node: " + id + " (" + name + ")");
        } else {
            Log.w(TAG, "  Node with ID " + id + " already exists. Skipping.");
        }
    }

    // Helper method để thêm một cạnh (vô hướng) vào danh sách edges và graphEdges map
    private void addEdge(String sourceId, String destinationId) {
        Node sourceNode = nodeMap.get(sourceId);
        Node destinationNode = nodeMap.get(destinationId);

        if (sourceNode != null && destinationNode != null) {
            // Kiểm tra xem cạnh đã tồn tại chưa (để tránh trùng lặp trong đồ thị vô hướng)
            boolean edgeExists = false;
            if (graphEdges.containsKey(sourceId)) {
                for (Edge existingEdge : graphEdges.get(sourceId)) {
                    if (existingEdge.getDestination().getId().equals(destinationId)) {
                        edgeExists = true;
                        break;
                    }
                }
            }
            // Kiểm tra cạnh ngược lại cũng được (nếu đồ thị vô hướng)
            if (!edgeExists && graphEdges.containsKey(destinationId)) {
                for (Edge existingEdge : graphEdges.get(destinationId)) {
                    if (existingEdge.getDestination().getId().equals(sourceId)) {
                        edgeExists = true;
                        break;
                    }
                }
            }

            if (!edgeExists) {
                // Tính khoảng cách giữa hai node làm trọng số
                float[] distanceResult = new float[1];
                Location.distanceBetween(sourceNode.getLatitude(), sourceNode.getLongitude(),
                        destinationNode.getLatitude(), destinationNode.getLongitude(), distanceResult);
                double weight = distanceResult[0]; // Khoảng cách tính bằng mét

                // Thêm cạnh theo cả hai chiều (vô hướng) vào danh sách edges
                edges.add(new Edge(sourceNode, destinationNode, weight));
                edges.add(new Edge(destinationNode, sourceNode, weight));

                // Thêm vào graphEdges map để tra cứu nhanh
                if (!graphEdges.containsKey(sourceId)) {
                    graphEdges.put(sourceId, new ArrayList<>());
                }
                graphEdges.get(sourceId).add(new Edge(sourceNode, destinationNode, weight));

                if (!graphEdges.containsKey(destinationId)) {
                    graphEdges.put(destinationId, new ArrayList<>());
                }
                graphEdges.get(destinationId).add(new Edge(destinationNode, sourceNode, weight));

                Log.d(TAG, "  Added edge between " + sourceId + " and " + destinationId + " with weight: " + String.format(Locale.US, "%.2f", weight));
            } else {
                // Log.d(TAG, "  Edge between " + sourceId + " and " + destinationId + " already exists. Skipping."); // Log này có thể quá nhiều
            }

        } else {
            Log.w(TAG, "Cannot add edge: Source node " + sourceId + " or destination node " + destinationId + " not found in map.");
        }
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

    // Helper method để lấy danh sách cạnh (cần cho AStarPathfinder)
    public Map<String, List<Edge>> getGraphEdgesMap() {
        return graphEdges;
    }
}
