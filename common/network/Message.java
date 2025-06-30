package common.network;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String messageId;
    private String senderId;
    private String receiverId;
    private String senderType; // "BRANCH", "HEADQUARTER", "CUSTOMER", "SYSTEM"
    private String receiverType;
    private MessageType messageType;
    private String subject;
    private String content;
    private Object data; // For sending objects like Order, Drink, etc.
    private LocalDateTime timestamp;
    private String formattedTimestamp;
    private MessageStatus status;
    private MessagePriority priority;

    public enum MessageType {
        ORDER_REQUEST,
        ORDER_CONFIRMATION,
        ORDER_CANCELLATION,
        STOCK_UPDATE,
        STOCK_ALERT,
        STOCK_REQUEST,
        SALES_REPORT,
        INVENTORY_REPORT,
        CUSTOMER_REGISTRATION,
        BRANCH_STATUS_UPDATE,
        SYSTEM_NOTIFICATION,
        ERROR_NOTIFICATION,
        HEARTBEAT,
        ACKNOWLEDGMENT
    }

    public enum MessageStatus {
        PENDING,
        SENT,
        DELIVERED,
        PROCESSED,
        FAILED,
        ACKNOWLEDGED
    }

    public enum MessagePriority {
        LOW,
        NORMAL,
        HIGH,
        CRITICAL
    }

    public Message() {
        this.timestamp = LocalDateTime.now();
        this.formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = MessageStatus.PENDING;
        this.priority = MessagePriority.NORMAL;
    }

    public Message(String messageId, String senderId, String receiverId,
                   String senderType, String receiverType, MessageType messageType) {
        this();
        this.messageId = messageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderType = senderType;
        this.receiverType = receiverType;
        this.messageType = messageType;
    }

    public Message(String messageId, String senderId, String receiverId,
                   String senderType, String receiverType, MessageType messageType,
                   String subject, String content) {
        this(messageId, senderId, receiverId, senderType, receiverType, messageType);
        this.subject = subject;
        this.content = content;
    }

    public Message(String messageId, String senderId, String receiverId,
                   String senderType, String receiverType, MessageType messageType,
                   String subject, String content, Object data) {
        this(messageId, senderId, receiverId, senderType, receiverType, messageType, subject, content);
        this.data = data;
    }

    // Getters
    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public String getSenderType() {
        return senderType;
    }

    public String getReceiverType() {
        return receiverType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getSubject() {
        return subject;
    }

    public String getContent() {
        return content;
    }

    public Object getData() {
        return data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public MessagePriority getPriority() {
        return priority;
    }

    // Setters
    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public void setSenderType(String senderType) {
        this.senderType = senderType;
    }

    public void setReceiverType(String receiverType) {
        this.receiverType = receiverType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setPriority(MessagePriority priority) {
        this.priority = priority;
    }

    // Business Methods
    public boolean isHighPriority() {
        return priority == MessagePriority.HIGH || priority == MessagePriority.CRITICAL;
    }

    public boolean isCritical() {
        return priority == MessagePriority.CRITICAL;
    }

    public boolean isProcessed() {
        return status == MessageStatus.PROCESSED || status == MessageStatus.ACKNOWLEDGED;
    }

    public boolean isFailed() {
        return status == MessageStatus.FAILED;
    }

    public boolean isPending() {
        return status == MessageStatus.PENDING;
    }

    public void markAsSent() {
        this.status = MessageStatus.SENT;
    }

    public void markAsDelivered() {
        this.status = MessageStatus.DELIVERED;
    }

    public void markAsProcessed() {
        this.status = MessageStatus.PROCESSED;
    }

    public void markAsFailed() {
        this.status = MessageStatus.FAILED;
    }

    public void markAsAcknowledged() {
        this.status = MessageStatus.ACKNOWLEDGED;
    }

    // Utility Methods for creating specific message types
    public static Message createOrderRequest(String messageId, String customerId, String branchId, Order order) {
        Message msg = new Message(messageId, customerId, branchId, "CUSTOMER", "BRANCH", MessageType.ORDER_REQUEST);
        msg.setSubject("New Order Request");
        msg.setContent("Customer " + customerId + " has placed a new order.");
        msg.setData(order);
        msg.setPriority(MessagePriority.NORMAL);
        return msg;
    }

    public static Message createStockAlert(String messageId, String branchId, String headquarterId, String alertContent) {
        Message msg = new Message(messageId, branchId, headquarterId, "BRANCH", "HEADQUARTER", MessageType.STOCK_ALERT);
        msg.setSubject("Low Stock Alert");
        msg.setContent(alertContent);
        msg.setPriority(MessagePriority.HIGH);
        return msg;
    }

    public static Message createStockUpdate(String messageId, String headquarterId, String branchId, Object stockData) {
        Message msg = new Message(messageId, headquarterId, branchId, "HEADQUARTER", "BRANCH", MessageType.STOCK_UPDATE);
        msg.setSubject("Stock Replenishment");
        msg.setContent("Stock has been updated from headquarters.");
        msg.setData(stockData);
        msg.setPriority(MessagePriority.NORMAL);
        return msg;
    }

    public static Message createSalesReport(String messageId, String branchId, String headquarterId, String reportContent) {
        Message msg = new Message(messageId, branchId, headquarterId, "BRANCH", "HEADQUARTER", MessageType.SALES_REPORT);
        msg.setSubject("Daily Sales Report");
        msg.setContent(reportContent);
        msg.setPriority(MessagePriority.NORMAL);
        return msg;
    }

    public static Message createOrderConfirmation(String messageId, String branchId, String customerId, Order order) {
        Message msg = new Message(messageId, branchId, customerId, "BRANCH", "CUSTOMER", MessageType.ORDER_CONFIRMATION);
        msg.setSubject("Order Confirmation");
        msg.setContent("Your order has been " +
                (order.getStatus() == Order.OrderStatus.COMPLETED ? "completed" : "processed") + ".");
        msg.setData(order);
        msg.setPriority(MessagePriority.NORMAL);
        return msg;
    }

    public static Message createSystemNotification(String messageId, String senderId, String receiverId,
                                                   String senderType, String receiverType, String notification) {
        Message msg = new Message(messageId, senderId, receiverId, senderType, receiverType, MessageType.SYSTEM_NOTIFICATION);
        msg.setSubject("System Notification");
        msg.setContent(notification);
        msg.setPriority(MessagePriority.NORMAL);
        return msg;
    }

    public static Message createErrorNotification(String messageId, String senderId, String receiverId,
                                                  String senderType, String receiverType, String errorMessage) {
        Message msg = new Message(messageId, senderId, receiverId, senderType, receiverType, MessageType.ERROR_NOTIFICATION);
        msg.setSubject("Error Notification");
        msg.setContent(errorMessage);
        msg.setPriority(MessagePriority.HIGH);
        return msg;
    }

    public static Message createAcknowledgment(String messageId, String senderId, String receiverId,
                                               String senderType, String receiverType, String originalMessageId) {
        Message msg = new Message(messageId, senderId, receiverId, senderType, receiverType, MessageType.ACKNOWLEDGMENT);
        msg.setSubject("Message Acknowledgment");
        msg.setContent("Acknowledgment for message: " + originalMessageId);
        msg.setPriority(MessagePriority.LOW);
        return msg;
    }

    public String getMessageSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Message ID: ").append(messageId != null ? messageId : "N/A").append("\n");
        summary.append("From: ").append(senderId != null ? senderId : "N/A")
                .append(" (").append(senderType != null ? senderType : "N/A").append(")\n");
        summary.append("To: ").append(receiverId != null ? receiverId : "N/A")
                .append(" (").append(receiverType != null ? receiverType : "N/A").append(")\n");
        summary.append("Type: ").append(messageType).append("\n");
        summary.append("Subject: ").append(subject != null ? subject : "N/A").append("\n");
        summary.append("Priority: ").append(priority).append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Timestamp: ").append(formattedTimestamp != null ? formattedTimestamp : "N/A").append("\n");
        if (content != null && !content.trim().isEmpty()) {
            summary.append("Content: ").append(content.length() > 100 ? content.substring(0, 100) + "..." : content);
        }
        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("Message{ID='%s', Type=%s, From='%s', To='%s', Priority=%s, Status=%s, Time='%s'}",
                messageId != null ? messageId : "N/A",
                messageType,
                senderId != null ? senderId : "N/A",
                receiverId != null ? receiverId : "N/A",
                priority, status,
                formattedTimestamp != null ? formattedTimestamp : "N/A");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Message message = (Message) obj;
        return messageId != null ? messageId.equals(message.messageId) : message.messageId == null;
    }

    @Override
    public int hashCode() {
        return messageId != null ? messageId.hashCode() : 0;
    }
}