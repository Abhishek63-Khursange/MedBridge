package com.MedBridge.service;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.MedBridge.entity.Appointment;
import com.MedBridge.dao.AppointmentDao;
import org.apache.commons.codec.binary.Hex;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

@Service
public class RazorpayService {

    private final String KEY = "rzp_test_TNpcedZQbcsMrm"; // Replace with your Razorpay Key ID
    private final String SECRET = "MlTwYdCqYFt1QeDtI9P97nWt"; // Replace with Razorpay secret

    @Autowired
    private EmailService emailService;

    @Autowired
    private AppointmentDao appointmentDao;

    public String createOrder(int amount) throws RazorpayException {
        RazorpayClient client = new RazorpayClient(KEY, SECRET);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amount * 100); // Amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", UUID.randomUUID().toString());
        orderRequest.put("payment_capture", 1); // auto-capture

        Order order = client.orders.create(orderRequest);
        return order.toString();
    }

    public boolean verifyPayment(String orderId, String paymentId, String signature, Long appointmentId) {
        try {
            System.out.println("Starting payment verification...");
            System.out.println("Order ID: " + orderId);
            System.out.println("Payment ID: " + paymentId);
            System.out.println("Appointment ID: " + appointmentId);
            
            String data = orderId + "|" + paymentId;
            String generatedSignature = hmacSha256(data, SECRET);
            
            System.out.println("Generated signature: " + generatedSignature);
            System.out.println("Received signature: " + signature);
            System.out.println("Signatures match: " + generatedSignature.equals(signature));

            if (generatedSignature.equals(signature)) {
                // Payment verified successfully - send email notifications
                Appointment appointment = appointmentDao.findById(appointmentId.intValue())
                        .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));
                
                System.out.println("Appointment found: " + appointment.getId());
                System.out.println("Current status: " + appointment.getStatus());
                
                // Update appointment status to confirmed
                appointment.setStatus("CONFIRMED");
                appointmentDao.save(appointment);
                
                System.out.println("Appointment status updated to CONFIRMED");
                
                // Send email notifications asynchronously
                try {
                    emailService.sendPaymentReceiptToPatient(appointmentId, appointment.getPrice(), paymentId);
                    emailService.sendAppointmentConfirmationToDoctor(appointmentId);
                    emailService.sendAppointmentConfirmationToPatient(appointmentId);
                    System.out.println("Email notifications sent successfully");
                } catch (Exception emailException) {
                    System.err.println("Error sending emails: " + emailException.getMessage());
                    emailException.printStackTrace();
                    // Don't fail payment verification if emails fail
                }
                
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error verifying payment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private String hmacSha256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        return Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes()));
    }
}

