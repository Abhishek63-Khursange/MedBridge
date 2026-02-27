package com.MedBridge.service;

import com.MedBridge.entity.Appointment;
import com.MedBridge.entity.User;
import com.MedBridge.dao.UserDao;
import com.MedBridge.dao.DoctorDao;
import com.MedBridge.dao.AppointmentDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserDao userDao;

    @Autowired
    private DoctorDao doctorDao;

    @Autowired
    private AppointmentDao appointmentDao;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    @Async
    public void sendPaymentReceiptToPatient(Long appointmentId, double amount, String paymentId) {
        try {
            Appointment appointment = getAppointmentDetails(appointmentId);
            User patient = userDao.findById(appointment.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            User doctor = userDao.findById(appointment.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            String subject = "Payment Receipt - MedBridge Appointment Booking";
            String body = buildPaymentReceiptEmail(patient, doctor, appointment, amount, paymentId);

            sendEmail(patient.getEmailId(), subject, body);
            System.out.println("Payment receipt sent successfully to: " + patient.getEmailId());
        } catch (Exception e) {
            System.err.println("Error sending payment receipt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentConfirmationToDoctor(Long appointmentId) {
        try {
            Appointment appointment = getAppointmentDetails(appointmentId);
            User patient = userDao.findById(appointment.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            User doctor = userDao.findById(appointment.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            String subject = "New Appointment Scheduled - MedBridge";
            String body = buildDoctorNotificationEmail(patient, doctor, appointment);

            sendEmail(doctor.getEmailId(), subject, body);
            System.out.println("Appointment notification sent successfully to doctor: " + doctor.getEmailId());
        } catch (Exception e) {
            System.err.println("Error sending doctor notification: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Async
    public void sendAppointmentConfirmationToPatient(Long appointmentId) {
        try {
            Appointment appointment = getAppointmentDetails(appointmentId);
            User patient = userDao.findById(appointment.getPatientId())
                    .orElseThrow(() -> new RuntimeException("Patient not found"));
            User doctor = userDao.findById(appointment.getDoctorId())
                    .orElseThrow(() -> new RuntimeException("Doctor not found"));

            String subject = "Appointment Confirmed - MedBridge Healthcare";
            String body = buildPatientConfirmationEmail(patient, doctor, appointment);

            sendEmail(patient.getEmailId(), subject, body);
            System.out.println("Appointment confirmation sent successfully to: " + patient.getEmailId());
        } catch (Exception e) {
            System.err.println("Error sending patient confirmation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String buildPaymentReceiptEmail(User patient, User doctor, Appointment appointment, double amount, String paymentId) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append(",\n\n");
        emailBody.append("Thank you for your payment. Your appointment has been successfully booked with MedBridge Healthcare.\n\n");
        emailBody.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
        emailBody.append("📋 PAYMENT RECEIPT\n");
        emailBody.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
        
        emailBody.append("🏥 DOCTOR DETAILS:\n");
        emailBody.append("   Doctor: Dr. ").append(doctor.getFirstName()).append(" ").append(doctor.getLastName()).append("\n\n");
        
        emailBody.append("📅 APPOINTMENT DETAILS:\n");
        emailBody.append("   Date: ").append(appointment.getAppointmentDate()).append("\n");
        emailBody.append("   Status: ").append(appointment.getStatus()).append("\n\n");
        
        emailBody.append("💳 PAYMENT DETAILS:\n");
        emailBody.append("   Amount Paid: ₹").append(String.format("%.2f", amount)).append("\n");
        emailBody.append("   Payment ID: ").append(paymentId).append("\n");
        emailBody.append("   Payment Status: SUCCESSFUL\n\n");
        
        emailBody.append("👤 PATIENT INFORMATION:\n");
        emailBody.append("   Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
        emailBody.append("   Email: ").append(patient.getEmailId()).append("\n");
        emailBody.append("   Phone: ").append(patient.getContact()).append("\n\n");
        
        emailBody.append("Thank you for choosing MedBridge for your healthcare needs.\n");
        emailBody.append("Best Regards,\n");
        emailBody.append("The MedBridge Healthcare Team");

        return emailBody.toString();
    }

    private String buildDoctorNotificationEmail(User patient, User doctor, Appointment appointment) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear Dr. ").append(doctor.getFirstName()).append(" ").append(doctor.getLastName()).append(",\n\n");
        emailBody.append("You have a new appointment scheduled. Please find the details below:\n\n");
        
        emailBody.append("👤 PATIENT DETAILS:\n");
        emailBody.append("   Name: ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("\n");
        emailBody.append("   Email: ").append(patient.getEmailId()).append("\n");
        emailBody.append("   Phone: ").append(patient.getContact()).append("\n\n");
        
        emailBody.append("📅 APPOINTMENT SCHEDULE:\n");
        emailBody.append("   Date: ").append(appointment.getAppointmentDate()).append("\n");
        emailBody.append("   Status: ").append(appointment.getStatus()).append("\n");
        emailBody.append("   Appointment ID: #").append(appointment.getId()).append("\n\n");
        
        if (appointment.getProblem() != null && !appointment.getProblem().trim().isEmpty()) {
            emailBody.append("🏥 PATIENT'S PROBLEM/COMPLAINT:\n");
            emailBody.append("   ").append(appointment.getProblem()).append("\n\n");
        }
        
        emailBody.append("💰 CONSULTATION FEE: ₹").append(String.format("%.2f", appointment.getPrice())).append("\n\n");
        
        emailBody.append("Thank you for your dedication to patient care.\n\n");
        emailBody.append("Best Regards,\n");
        emailBody.append("MedBridge Healthcare Team");

        return emailBody.toString();
    }

    private String buildPatientConfirmationEmail(User patient, User doctor, Appointment appointment) {
        StringBuilder emailBody = new StringBuilder();
        emailBody.append("Dear ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append(",\n\n");
        emailBody.append("Your appointment has been successfully confirmed! We are pleased to inform you that your booking is complete.\n\n");
        
        emailBody.append("🏥 DOCTOR DETAILS:\n");
        emailBody.append("   Name: Dr. ").append(doctor.getFirstName()).append(" ").append(doctor.getLastName()).append("\n\n");
        
        emailBody.append("📅 APPOINTMENT DETAILS:\n");
        emailBody.append("   Date: ").append(appointment.getAppointmentDate()).append("\n");
        emailBody.append("   Status: ").append(appointment.getStatus()).append("\n");
        emailBody.append("   Appointment ID: #").append(appointment.getId()).append("\n\n");
        
        emailBody.append("💳 PAYMENT INFORMATION:\n");
        emailBody.append("   Consultation Fee: ₹").append(String.format("%.2f", appointment.getPrice())).append("\n");
        emailBody.append("   Payment Status: PAID\n\n");
        
        emailBody.append("We look forward to providing you with the best healthcare service.\n\n");
        emailBody.append("Best Regards,\n");
        emailBody.append("The MedBridge Healthcare Team");

        return emailBody.toString();
    }

    private void sendEmail(String to, String subject, String body) {
        try {
            if (to == null || to.trim().isEmpty()) {
                System.err.println("Email recipient is null or empty");
                return;
            }
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject != null ? subject : "MedBridge Notification");
            message.setText(body != null ? body : "No content available");
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("Error sending email to " + to + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Appointment getAppointmentDetails(Long appointmentId) {
        return appointmentDao.findById(appointmentId.intValue())
                .orElseThrow(() -> new RuntimeException("Appointment not found with ID: " + appointmentId));
    }

    // Existing methods for backward compatibility
    public void sendAppointmentConfirmation(String toEmail, String subject, String fullMessage) {
        sendEmail(toEmail, subject, fullMessage);
    }

    public void AppointmentBooked(String toEmail, String patientName, Appointment appointment, double price) {
        String subject = "Appointment Confirmation - MedBridge";
        String body = "Dear " + patientName + ",\n\n"
                + "We are pleased to inform you that your appointment has been successfully booked with MedBridge Healthcare.\n\n"
                + "📅 Appointment Date: " + appointment.getAppointmentDate() + "\n"
                + "💳 Payment Status: Confirmed\n"
                + "💰 Amount Paid: ₹" + price + "\n"
                + "🩺 Appointment Status: " + appointment.getStatus() + "\n\n"
                + "Thank you for your payment and for choosing MedBridge for your healthcare needs.\n"
                + "You will receive further details or reminders as your appointment date approaches.\n\n"
                + "Wishing you good health,\n"
                + "The MedBridge Healthcare Team";

        sendAppointmentConfirmation(toEmail, subject, body);
    }

    public void sendPrescriptionEmail(String toEmail, String patientName, String prescription, double price) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Your Prescription is Updated");
            message.setText("Dear "+ patientName +",\n\nYour prescription has been updated:\n\nPrescription: " + prescription +
                    "\nEstimated Price: ₹" + price +
                    "\n\nThank you,\nMedBridge Team");

            mailSender.send(message);
            System.out.println("Prescription email sent successfully to: " + toEmail);
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

    public void sendAppointmentScheduledEmail(String toEmail, String Subject, String mesage) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(Subject);
        message.setText(mesage);
        mailSender.send(message);
    }
}

