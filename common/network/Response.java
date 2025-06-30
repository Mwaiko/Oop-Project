package common.network;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import common.models.Order;
public class Response implements Serializable {
    private static final long serialVersionUID = 1L;

    private String responseId;
    private String originalMessageId;
    private String senderId;
    private String receiverId;
    private String senderType; // "BRANCH", "HEADQUARTER", "CUSTOMER", "SYSTEM"
    private String receiverType;
    private ResponseType responseType;
    private ResponseStatus status;
    private String responseMessage;
    private Object responseData;
    private LocalDateTime timestamp;
    private String formattedTimestamp;
    private boolean success;
    private String errorMessage;
    private String errorCode;

    public enum ResponseType {
        ORDER_PROCESSED,
        ORDER_REJECTED,
        STOCK_UPDATED,
        STOCK_UNAVAILABLE,
        PAYMENT_CONFIRMED,
        PAYMENT_FAILED,
        CUSTOMER_REGISTERED,
        CUSTOMER_REGISTRATION_FAILED,
        REPORT_GENERATED,
        REPORT_FAILED,
        SYSTEM_ACKNOWLEDGMENT,
        ERROR_RESPONSE,
        SUCCESS_RESPONSE,
        INFO_RESPONSE
    }

    public enum ResponseStatus {
        SUCCESS,
        FAILURE,
        PARTIAL_SUCCESS,
        PENDING,
        TIMEOUT,
        ERROR
    }

    public Response() {
        this.timestamp = LocalDateTime.now();
        this.formattedTimestamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.status = ResponseStatus.PENDING;
        this.success = false;
    }

    public Response(String responseId, String originalMessageId, String senderId, String receiverId,
                    String senderType, String receiverType, ResponseType responseType) {
        this();
        this.responseId = responseId;
        this.originalMessageId = originalMessageId;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.senderType = senderType;
        this.receiverType = receiverType;
        this.responseType = responseType;
    }

    public Response(String responseId, String originalMessageId, String senderId, String receiverId,
                    String senderType, String receiverType, ResponseType responseType,
                    boolean success, String responseMessage) {
        this(responseId, originalMessageId, senderId, receiverId, senderType, receiverType, responseType);
        this.success = success;
        this.responseMessage = responseMessage;
        this.status = success ? ResponseStatus.SUCCESS : ResponseStatus.FAILURE;
    }

    public Response(String responseId, String originalMessageId, String senderId, String receiverId,
                    String senderType, String receiverType, ResponseType responseType,
                    boolean success, String responseMessage, Object responseData) {
        this(responseId, originalMessageId, senderId, receiverId, senderType, receiverType,
                responseType, success, responseMessage);
        this.responseData = responseData;
    }

    // Getters
    public String getResponseId() {
        return responseId;
    }

    public String getOriginalMessageId() {
        return originalMessageId;
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

    public ResponseType getResponseType() {
        return responseType;
    }

    public ResponseStatus getStatus() {
        return status;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public Object getResponseData() {
        return responseData;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getFormattedTimestamp() {
        return formattedTimestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    // Setters
    public void setResponseId(String responseId) {
        this.responseId = responseId;
    }

    public void setOriginalMessageId(String originalMessageId) {
        this.originalMessageId = originalMessageId;
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

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public void setStatus(ResponseStatus status) {
        this.status = status;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }

    public void setSuccess(boolean success) {
        this.success = success;
        if (success && this.status == ResponseStatus.PENDING) {
            this.status = ResponseStatus.SUCCESS;
        } else if (!success && this.status == ResponseStatus.PENDING) {
            this.status = ResponseStatus.FAILURE;
        }
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        if (errorMessage != null && !errorMessage.trim().isEmpty()) {
            this.success = false;
            this.status = ResponseStatus.ERROR;
        }
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    // Business Methods
    public boolean isError() {
        return status == ResponseStatus.ERROR || status == ResponseStatus.FAILURE;
    }

    public boolean isPending() {
        return status == ResponseStatus.PENDING;
    }

    public boolean hasError() {
        return errorMessage != null && !errorMessage.trim().isEmpty();
    }

    public boolean hasData() {
        return responseData != null;
    }

    public void markAsSuccess(String message) {
        this.success = true;
        this.status = ResponseStatus.SUCCESS;
        this.responseMessage = message;
        this.errorMessage = null;
        this.errorCode = null;
    }

    public void markAsFailure(String message) {
        this.success = false;
        this.status = ResponseStatus.FAILURE;
        this.responseMessage = message;
    }

    public void markAsError(String errorMessage, String errorCode) {
        this.success = false;
        this.status = ResponseStatus.ERROR;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public void markAsPartialSuccess(String message) {
        this.success = true;
        this.status = ResponseStatus.PARTIAL_SUCCESS;
        this.responseMessage = message;
    }

    public void markAsTimeout(String message) {
        this.success = false;
        this.status = ResponseStatus.TIMEOUT;
        this.responseMessage = message;
    }

    // Utility Methods for creating specific response types
    public static Response createOrderProcessedResponse(String responseId, String originalMessageId,
                                                        String branchId, String customerId, Order processedOrder) {
        Response response = new Response(responseId, originalMessageId, branchId, customerId,
                "BRANCH", "CUSTOMER", ResponseType.ORDER_PROCESSED);

        if (processedOrder.getStatus() == Order.OrderStatus) {
            response.markAsSuccess("Your order has been successfully processed and completed.");
        } else if (processedOrder.getStatus() == Order.OrderStatus) {
            response.markAsPartialSuccess("Your order is being processed.");
        } else {
            response.markAsFailure("Order could not be processed.");
        }

        response.setResponseData(processedOrder);
        return response;
    }

    public static Response createOrderRejectedResponse(String responseId, String originalMessageId,
                                                       String branchId, String customerId, String reason) {
        Response response = new Response(responseId, originalMessageId, branchId, customerId,
                "BRANCH", "CUSTOMER", ResponseType.ORDER_REJECTED);
        response.markAsFailure("Order rejected: " + reason);
        return response;
    }

    public static Response createStockUpdatedResponse(String responseId, String originalMessageId,
                                                      String headquarterId, String branchId,
                                                      String updateMessage, Object stockData) {
        Response response = new Response(responseId, originalMessageId, headquarterId, branchId,
                "HEADQUARTER", "BRANCH", ResponseType.STOCK_UPDATED);
        response.markAsSuccess(updateMessage);
        response.setResponseData(stockData);
        return response;
    }

    public static Response createStockUnavailableResponse(String responseId, String originalMessageId,
                                                          String headquarterId, String branchId, String reason) {
        Response response = new Response(responseId, originalMessageId, headquarterId, branchId,
                "HEADQUARTER", "BRANCH", ResponseType.STOCK_UNAVAILABLE);
        response.markAsFailure("Stock unavailable: " + reason);
        return response;
    }

    public static Response createCustomerRegisteredResponse(String responseId, String originalMessageId,
                                                            String branchId, String customerId, String customer) {
        Response response = new Response(responseId, originalMessageId, branchId, customerId,
                "BRANCH", "CUSTOMER", ResponseType.CUSTOMER_REGISTERED);
        response.markAsSuccess("Customer registration successful.");
        response.setResponseData(customer);
        return response;
    }

    public static Response createRegistrationFailedResponse(String responseId, String originalMessageId,
                                                            String branchId, String customerId, String reason) {
        Response response = new Response(responseId, originalMessageId, branchId, customerId,
                "BRANCH", "CUSTOMER", ResponseType.CUSTOMER_REGISTRATION_FAILED);
        response.markAsFailure("Registration failed: " + reason);
        return response;
    }

    public static Response createReportGeneratedResponse(String responseId, String originalMessageId,
                                                         String branchId, String headquarterId,
                                                         String reportType, Object reportData) {
        Response response = new Response(responseId, originalMessageId, branchId, headquarterId,
                "BRANCH", "HEADQUARTER", ResponseType.REPORT_GENERATED);
        response.markAsSuccess(reportType + " report generated successfully.");
        response.setResponseData(reportData);
        return response;
    }

    public static Response createSystemAcknowledgment(String responseId, String originalMessageId,
                                                      String senderId, String receiverId,
                                                      String senderType, String receiverType) {
        Response response = new Response(responseId, originalMessageId, senderId, receiverId,
                senderType, receiverType, ResponseType.SYSTEM_ACKNOWLEDGMENT);
        response.markAsSuccess("Message received and acknowledged.");
        return response;
    }

    public static Response createErrorResponse(String responseId, String originalMessageId,
                                               String senderId, String receiverId,
                                               String senderType, String receiverType,
                                               String errorMessage, String errorCode) {
        Response response = new Response(responseId, originalMessageId, senderId, receiverId,
                senderType, receiverType, ResponseType.ERROR_RESPONSE);
        response.markAsError(errorMessage, errorCode);
        return response;
    }

    public static Response createSuccessResponse(String responseId, String originalMessageId,
                                                 String senderId, String receiverId,
                                                 String senderType, String receiverType,
                                                 String successMessage, Object data) {
        Response response = new Response(responseId, originalMessageId, senderId, receiverId,
                senderType, receiverType, ResponseType.SUCCESS_RESPONSE);
        response.markAsSuccess(successMessage);
        response.setResponseData(data);
        return response;
    }

    public String getResponseSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Response ID: ").append(responseId != null ? responseId : "N/A").append("\n");
        summary.append("Original Message ID: ").append(originalMessageId != null ? originalMessageId : "N/A").append("\n");
        summary.append("From: ").append(senderId != null ? senderId : "N/A")
                .append(" (").append(senderType != null ? senderType : "N/A").append(")\n");
        summary.append("To: ").append(receiverId != null ? receiverId : "N/A")
                .append(" (").append(receiverType != null ? receiverType : "N/A").append(")\n");
        summary.append("Type: ").append(responseType).append("\n");
        summary.append("Status: ").append(status).append("\n");
        summary.append("Success: ").append(success).append("\n");
        summary.append("Timestamp: ").append(formattedTimestamp != null ? formattedTimestamp : "N/A").append("\n");

        if (responseMessage != null && !responseMessage.trim().isEmpty()) {
            summary.append("Message: ").append(responseMessage).append("\n");
        }

        if (hasError()) {
            summary.append("Error: ").append(errorMessage);
            if (errorCode != null && !errorCode.trim().isEmpty()) {
                summary.append(" (Code: ").append(errorCode).append(")");
            }
            summary.append("\n");
        }

        if (hasData()) {
            summary.append("Has Data: Yes");
        }

        return summary.toString();
    }

    @Override
    public String toString() {
        return String.format("Response{ID='%s', OriginalMsg='%s', Type=%s, Status=%s, Success=%s, From='%s', To='%s', Time='%s'}",
                responseId != null ? responseId : "N/A",
                originalMessageId != null ? originalMessageId : "N/A",
                responseType, status, success,
                senderId != null ? senderId : "N/A",
                receiverId != null ? receiverId : "N/A",
                formattedTimestamp != null ? formattedTimestamp : "N/A");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Response response = (Response) obj;
        return responseId != null ? responseId.equals(response.responseId) : response.responseId == null;
    }

    @Override
    public int hashCode() {
        return responseId != null ? responseId.hashCode() : 0;
    }
}
