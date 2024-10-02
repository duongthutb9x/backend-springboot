package com.project.esavior.websocket.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler extends TextWebSocketHandler {

    // Lưu session của cả tài xế và khách hàng theo ID chung (room ID)
    private final Map<Integer, Map<String, WebSocketSession>> roomSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Lấy query params từ URL
        Map<String, String> params = getQueryParams(session.getUri().getQuery());

        Integer roomId = Integer.parseInt(params.get("id"));  // ID chung cho cả tài xế và khách hàng
        String role = params.get("role");  // "driver" hoặc "customer"

        if (roomId == null || role == null) {
            sendErrorResponse(session, "Invalid ID or role");
            return;
        }

        // Lưu session theo roomId và vai trò của người dùng
        roomSessions.putIfAbsent(roomId, new HashMap<>());  // Tạo một HashMap mới nếu roomId chưa tồn tại
        roomSessions.get(roomId).put(role, session);  // Lưu session của driver hoặc customer theo role

        // Gửi thông báo kết nối thành công và phòng đã được tạo
        Map<String, Object> response = new HashMap<>();
        response.put("type", "connected");
        response.put("id", roomId);
        response.put("role", role);
        response.put("message", "Phòng đã được tạo và kết nối thành công.");
        session.sendMessage(new TextMessage(response.toString()));

        System.out.println("Kết nối và tạo phòng thành công với ID chung: " + roomId + " và vai trò: " + role);
    }


    private Map<String, String> getQueryParams(String query) {
        Map<String, String> params = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] keyValue = pair.split("=");
            params.put(keyValue[0], keyValue[1]);
        }
        return params;
    }

    private void sendErrorResponse(WebSocketSession session, String errorMessage) throws Exception {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "error");
        response.put("message", errorMessage);
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> data = parseJson(payload);  // Tạo hàm parse JSON

        String messageType = (String) data.get("type");
        if (messageType == null) {
            sendErrorResponse(session, "Unknown message type");
            return;
        }

        switch (messageType) {
            case "send_message":
                handleMessage(session, data);
                break;
            default:
                sendErrorResponse(session, "Invalid message type");
        }
    }

    private Map<String, Object> parseJson(String payload) throws Exception {
        // Chuyển payload từ String sang Map để xử lý JSON
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(payload, Map.class);
    }

    private void handleMessage(WebSocketSession session, Map<String, Object> data) throws Exception {
        Integer roomId = (Integer) data.get("id");  // ID chung cho cả tài xế và khách hàng
        String message = (String) data.get("message");
        String role = (String) data.get("role");    // Vai trò người gửi (tài xế hoặc khách hàng)

        if (roomId == null || message == null || role == null) {
            sendErrorResponse(session, "Missing required fields");
            return;
        }

        // Xác định người nhận dựa trên role
        String recipientRole = role.equals("driver") ? "customer" : "driver";
        WebSocketSession recipientSession = roomSessions.get(roomId).get(recipientRole);  // Lấy session của người nhận

        if (recipientSession != null && recipientSession.isOpen()) {
            // Gửi tin nhắn cho người còn lại
            Map<String, Object> response = new HashMap<>();
            response.put("type", "message");
            response.put("from", role);  // Ai là người gửi (tài xế hoặc khách hàng)
            response.put("message", message);
            recipientSession.sendMessage(new TextMessage(response.toString()));
            System.out.println("Gửi tin nhắn từ " + role + " trong room với ID: " + roomId);
        } else {
            sendErrorResponse(session, "Recipient not connected or session closed");
        }
    }

}
