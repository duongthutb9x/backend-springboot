package com.project.esavior.controller;

import com.project.esavior.websocket.websocket.DriverWebSocketHandler;
import com.project.esavior.websocket.websocket.CustomerWebSocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/websocket")
public class WebSocketController {

    private final DriverWebSocketHandler driverWebSocketHandler;
    private final CustomerWebSocketHandler customerWebSocketHandler;

    @Autowired
    public WebSocketController(DriverWebSocketHandler driverWebSocketHandler,
                               CustomerWebSocketHandler customerWebSocketHandler) {
        this.driverWebSocketHandler = driverWebSocketHandler;
        this.customerWebSocketHandler = customerWebSocketHandler;
    }

    // API để gửi tin nhắn cho tất cả các tài xế
    @PostMapping("/send/driver")
    public String sendMessageToAllDrivers(@RequestBody Map<String, String> messageBody) {
        String message = messageBody.get("message");
        driverWebSocketHandler.sendToAllClients(message);
        return "Message sent to all drivers!";
    }

    // API để gửi tin nhắn cho tất cả khách hàng
    @PostMapping("/send/customer")
    public String sendMessageToAllCustomers(@RequestBody Map<String, String> messageBody) {
        String message = messageBody.get("message");
        customerWebSocketHandler.sendToAllClients(message);
        return "Message sent to all customers!";
    }
}
