package com.MedBridge.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.MedBridge.dto.AppointmentUpdateDto;
import com.MedBridge.exception.ResourceNotFoundException;
import com.MedBridge.service.EmailService;
import io.swagger.annotations.ApiOperation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.MedBridge.dto.AppointmentResponseDto;
import com.MedBridge.dto.CommanApiResponse;
import com.MedBridge.dto.UpdateAppointmentRequest;
import com.MedBridge.entity.Appointment;
import com.MedBridge.entity.User;
import com.MedBridge.exception.AppointmentNotFoundException;
import com.MedBridge.service.AppointmentService;
import com.MedBridge.service.UserService;
import com.MedBridge.utility.Constants.AppointmentStatus;
import com.MedBridge.utility.Constants.ResponseCode;
import com.MedBridge.dto.ApiResponse;
import com.MedBridge.service.VideoConsultationService.*;


@RestController
@RequestMapping("api/appointment/")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = {"http://localhost:5173" , "http://localhost:3000"})
public class AppointmentController {

	Logger LOG = LoggerFactory.getLogger(AppointmentController.class);

	@Autowired
	private EmailService emailService;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private UserService userService;

	@PostMapping("patient/add")
	@ApiOperation(value = "Api to add patient appointment")
	public ResponseEntity<?> addAppointment(@RequestBody Appointment appointment) {
		LOG.info("Recieved request to add patient appointment");

		CommanApiResponse response = new CommanApiResponse();

		if (appointment == null) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to add patient appointment");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		if (appointment.getPatientId() == 0) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to add patient appointment");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		// Validate appointment date is not in the past
		if (appointment.getAppointmentDate() != null) {
			try {
				LocalDate appointmentDate = LocalDate.parse(appointment.getAppointmentDate());
				LocalDate today = LocalDate.now();
				
				if (appointmentDate.isBefore(today)) {
					response.setResponseCode(ResponseCode.FAILED.value());
					response.setResponseMessage("Appointment date cannot be in the past");
					return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
				}
			} catch (Exception e) {
				response.setResponseCode(ResponseCode.FAILED.value());
				response.setResponseMessage("Invalid appointment date format");
				return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
			}
		}

		appointment.setDate(LocalDate.now().toString());
//		appointment.setStatus(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
		appointment.setStatus(AppointmentStatus.TREATMENT_PENDING.value());

		Appointment addedAppointment = appointmentService.addAppointment(appointment);

		User patient = userService.getUserById(addedAppointment.getPatientId());
		String EmailID = patient.getEmailId();
		String PatientName = patient.getFirstName();
		Double price = addedAppointment.getPrice();

		emailService.AppointmentBooked(EmailID,PatientName,addedAppointment,price);

		if (addedAppointment != null) {
			// Create response with appointment ID
			Map<String, Object> responseData = new HashMap<>();
			responseData.put("responseCode", ResponseCode.SUCCESS.value());
			responseData.put("responseMessage", "Appointment Added");
			responseData.put("appointmentId", addedAppointment.getId());
			responseData.put("appointment", addedAppointment);
			
			return new ResponseEntity(responseData, HttpStatus.OK);
		}

		else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to add Appointment");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

//	@PutMapping("/{id}/update")
//	public ResponseEntity<ApiResponse> updateAppointmentDetails(
//			@PathVariable int id,
//			@RequestBody AppointmentUpdateDto dto
//	) {
//		appointmentService.updatePrescriptionAndPrice(id, dto);
//		return ResponseEntity.ok(new ApiResponse("Appointment updated successfully", true));
//	}
@PutMapping("{id}/update")
public ResponseEntity<ApiResponse> updateAppointmentDetails(
		@PathVariable int id,
		@RequestBody AppointmentUpdateDto dto
) {
	System.out.println("Incoming prescription: " + dto.getPrescription());



	appointmentService.updatePrescriptionAndPrice(id, dto);
	return ResponseEntity.ok(new ApiResponse("Appointment updated successfully", true));
}


	@GetMapping("all")
	public ResponseEntity<?> getAllAppointments() {
		LOG.info("recieved request for getting ALL Appointments !!!");

		List<Appointment> appointments = this.appointmentService.getAllAppointment();

		List<AppointmentResponseDto> response = new ArrayList();

		for (Appointment appointment : appointments) {
			AppointmentResponseDto a = new AppointmentResponseDto();

			try {
				User patient = this.userService.getUserById(appointment.getPatientId());
				a.setPatientContact(patient.getContact());
				a.setPatientId(patient.getId());
				a.setPatientName(patient.getFirstName() + " " + patient.getLastName());
			} catch (ResourceNotFoundException ex) {
				LOG.warn("Skipping appointment {}: Patient not found", appointment.getId());
				continue;
			}

			if (appointment.getDoctorId() != 0) {
				try {
					User doctor = this.userService.getUserById(appointment.getDoctorId());
					a.setDoctorContact(doctor.getContact());
					a.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
					a.setDoctorId(doctor.getId());
					a.setPrescription(appointment.getPrescription());

					if (appointment.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
						a.setPrice(String.valueOf(appointment.getPrice()));
					} else {
						a.setPrice(AppointmentStatus.TREATMENT_PENDING.value());
					}
				} catch (ResourceNotFoundException ex) {
					LOG.warn("Doctor not found for appointment {}: Setting placeholders", appointment.getId());
					a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
					a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
					a.setDoctorId(0);
					a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
					a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				}
			} else {
				a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorId(0);
				a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
			}

			a.setStatus(appointment.getStatus());
			a.setProblem(appointment.getProblem());
			a.setDate(appointment.getDate());
			a.setAppointmentDate(appointment.getAppointmentDate());
			a.setId(appointment.getId());

			response.add(a);
		}


		LOG.info("response sent!!!");
		return ResponseEntity.ok(response);
	}

	@GetMapping("id")
	public ResponseEntity<?> getAllAppointments(@RequestParam("appointmentId") int appointmentId) {
		LOG.info("recieved request for getting  Appointment by id !!!");

		Appointment appointment = this.appointmentService.getAppointmentById(appointmentId);

		if (appointment == null) {
			throw new AppointmentNotFoundException();
		}

		AppointmentResponseDto a = new AppointmentResponseDto();

		User patient = this.userService.getUserById(appointment.getPatientId());

		a.setPatientContact(patient.getContact());
		a.setPatientId(patient.getId());
		a.setPatientName(patient.getFirstName() + " " + patient.getLastName());

		if (appointment.getDoctorId() != 0) {
			User doctor = this.userService.getUserById(appointment.getDoctorId());
			a.setDoctorContact(doctor.getContact());
			a.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
			a.setDoctorId(doctor.getId());
			a.setPrescription(appointment.getPrescription());

			if (appointment.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
				a.setPrice(String.valueOf(appointment.getPrice()));
			}

			else {
				a.setPrice(AppointmentStatus.TREATMENT_PENDING.value());
			}

		}

		else {
			a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
			a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
			a.setDoctorId(0);
			a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
			a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());

		}

		a.setStatus(appointment.getStatus());
		a.setProblem(appointment.getProblem());
		a.setDate(appointment.getDate());
		a.setAppointmentDate(appointment.getAppointmentDate());
		a.setBloodGroup(patient.getBloodGroup());
		a.setId(appointment.getId());

		LOG.info("response sent!!!");
		return ResponseEntity.ok(a);
	}

	@GetMapping("patient/id")
	public ResponseEntity<?> getAllAppointmentsByPatientId(@RequestParam("patientId") int patientId) {
		LOG.info("recieved request for getting ALL Appointments by patient Id !!!");

		List<Appointment> appointments = this.appointmentService.getAppointmentByPatientId(patientId);

		List<AppointmentResponseDto> response = new ArrayList();

		for (Appointment appointment : appointments) {

			AppointmentResponseDto a = new AppointmentResponseDto();

			User patient = this.userService.getUserById(appointment.getPatientId());

			a.setPatientContact(patient.getContact());
			a.setPatientId(patient.getId());
			a.setPatientName(patient.getFirstName() + " " + patient.getLastName());

			if (appointment.getDoctorId() != 0) {
				User doctor = this.userService.getUserById(appointment.getDoctorId());
				a.setDoctorContact(doctor.getContact());
				a.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
				a.setDoctorId(doctor.getId());
				a.setPrescription(appointment.getPrescription());

				if (appointment.getStatus().equals(AppointmentStatus.PAID.value())) {
					a.setPrice(String.valueOf(appointment.getPrice()));
				}

				else {
					a.setPrice(AppointmentStatus.PAID.value());
				}

			}

			else {
				a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorId(0);
				a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());

			}

			a.setStatus(appointment.getStatus());
			a.setProblem(appointment.getProblem());
			a.setDate(appointment.getDate());
			a.setAppointmentDate(appointment.getAppointmentDate());
			a.setBloodGroup(patient.getBloodGroup());
			a.setId(appointment.getId());

			response.add(a);

		}

		LOG.info("response sent!!!");
		return ResponseEntity.ok(response);
	}

	@PutMapping("{id}/schedule")
	public ResponseEntity<ApiResponse> scheduleAppointmentTime(
			@PathVariable int id,
			@RequestBody Map<String, String> requestBody
	) {
		String scheduledTime = requestBody.get("scheduledTime");
		if (scheduledTime == null || scheduledTime.isEmpty()) {
			return ResponseEntity.badRequest()
					.body(new ApiResponse("Scheduled time is required", false));
		}

		appointmentService.scheduleAppointment(id, scheduledTime);
		return ResponseEntity.ok(new ApiResponse("Appointment scheduled successfully", true));
	}


	@GetMapping("doctor/id")
	public ResponseEntity<?> getAllAppointmentsByDoctorId(@RequestParam("doctorId") int doctorId) {
		LOG.info("recieved request for getting ALL Appointments by doctor Id !!!");

		List<Appointment> appointments = this.appointmentService.getAppointmentByDoctorId(doctorId);

		List<AppointmentResponseDto> response = new ArrayList();

		for (Appointment appointment : appointments) {

			AppointmentResponseDto a = new AppointmentResponseDto();

			User patient = this.userService.getUserById(appointment.getPatientId());

			a.setPatientContact(patient.getContact());
			a.setPatientId(patient.getId());
			a.setPatientName(patient.getFirstName() + " " + patient.getLastName());

			if (appointment.getDoctorId() != 0) {
				User doctor = this.userService.getUserById(appointment.getDoctorId());
				a.setDoctorContact(doctor.getContact());
				a.setDoctorName(doctor.getFirstName() + " " + doctor.getLastName());
				a.setDoctorId(doctor.getId());
				a.setPrescription(appointment.getPrescription());

				if (appointment.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
					a.setPrice(String.valueOf(appointment.getPrice()));
				}

				else {
					a.setPrice(AppointmentStatus.TREATMENT_PENDING.value());
				}
				a.setPrescription(appointment.getPrescription());

			}

			else {
				a.setDoctorContact(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorName(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorId(0);
				a.setPrice(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setPrescription(AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
			}

			a.setStatus(appointment.getStatus());
			a.setProblem(appointment.getProblem());
			a.setDate(appointment.getDate());
			a.setAppointmentDate(appointment.getAppointmentDate());
			a.setBloodGroup(patient.getBloodGroup());
			a.setId(appointment.getId());

			response.add(a);

		}

		LOG.info("response sent!!!");
		return ResponseEntity.ok(response);
	}

	@PostMapping("admin/assign/doctor")
	@ApiOperation(value = "API to assign appointment to doctor")
	public ResponseEntity<?> updateAppointmentStatus(@RequestBody UpdateAppointmentRequest request) {
		LOG.info("Received request to assign appointment to doctor");

		CommanApiResponse response = new CommanApiResponse();

		// Basic validation
		if (request == null || request.getDoctorId() == 0 || request.getAppointmentId() == 0) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Invalid request data");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// Fetch doctor
		User doctor = this.userService.getUserById(request.getDoctorId());
		if (doctor == null) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Doctor not found");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// Fetch appointment
		Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());
		if (appointment == null) {
			throw new AppointmentNotFoundException();
		}

		if (appointment.getStatus().equals(AppointmentStatus.CANCEL.value())) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Appointment is cancelled by patient");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		// Update appointment fields
		appointment.setDoctorId(request.getDoctorId());
		appointment.setStatus(AppointmentStatus.ASSIGNED_TO_DOCTOR.value());

		if (request.getPrice() > 0) {
			appointment.setPrice(request.getPrice());
		}
		if (request.getPrescription() != null && !request.getPrescription().isEmpty()) {
			appointment.setPrescription(request.getPrescription());
		}

		// Save updated appointment
		Appointment updatedAppointment = this.appointmentService.addAppointment(appointment);

		String meetingLink = generateJitsiLink(updatedAppointment.getId());

		if (updatedAppointment != null) {
			try {
				// ✅ Fetch patient from appointment
				User patient = this.userService.getUserById(appointment.getPatientId());

				if (patient != null && patient.getEmailId() != null) {
					String Patientsubject = "Doctor Assigned to Your Appointment";
					String DoctorSubject = "Patient has Booked an Appointment with you";

					String patientMessage = "Dear " + patient.getFirstName() + ",\n\n"
							+ "Your appointment has been successfully booked with Dr. " + doctor.getFirstName() + " " + doctor.getLastName() + ".\n"
							+ "Appointment Date: " + appointment.getAppointmentDate() + "\n"
							+ "Appointment Time: 3:00 PM\n\n"
							+ "Please join the consultation at the scheduled time using the following link:\n"
							+ meetingLink + "\n\n"
							+"Note : "+"\"Please make sure the doctor joins the meeting first. Patients will be admitted after the doctor is inside the room.\""+"\n\n"
							+ "Regards,\n"
							+ "MedBridge Team";


					String DoctorMessage = "Dear Dr. " + doctor.getFirstName() + ",\n\n"
							+ "You have a new appointment booked by patient " + patient.getFirstName() + " " + patient.getLastName() + ".\n"
							+ "Appointment Date: " + appointment.getAppointmentDate() + "\n"
							+ "Appointment Time: 3:00 PM\n\n"
							+ "Please join the consultation at the scheduled time using the following link:\n"
							+ meetingLink + "\n\n"
							+ "Regards,\n"
							+ "MedBridge Team";


					if (appointment.getPrescription() != null) {
						patientMessage += "Prescription: " + appointment.getPrescription() + "\n";
					}

					if (appointment.getPrice() > 0) {
						patientMessage += "Fee: ₹" + appointment.getPrice() + "\n";
					}

					patientMessage += "\nThank you for using MedBridge.\n\nRegards,\nMedBridge Team";

					emailService.sendAppointmentConfirmation(patient.getEmailId(), Patientsubject,  patientMessage);
					emailService.sendAppointmentConfirmation(doctor.getEmailId(),DoctorSubject,DoctorMessage);
				}
			} catch (Exception e) {
				LOG.error("Failed to send email to patient: " + e.getMessage());
			}

			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("Successfully assigned appointment and email sent to patient");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to assign appointment");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	public String generateJitsiLink(int appointmentId) {
		String randomString = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
		String roomName = "MedBridge_" + appointmentId + "_" + randomString;
		return "https://meet.jit.si/" + roomName;
	}


//	@PostMapping("admin/assign/doctor")
//	@ApiOperation(value = "Api to assign appointment to doctor")
//	public ResponseEntity<?> updateAppointmentStatus(UpdateAppointmentRequest request) {
//		LOG.info("Recieved request to assign appointment to doctor");
//
//		CommanApiResponse response = new CommanApiResponse();
//
//		if (request == null) {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Failed to assign appointment");
//			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
//		}
//
//		if (request.getDoctorId() == 0) {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Doctor not found");
//			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
//		}
//
//		User doctor = this.userService.getUserById(request.getDoctorId());
//
//		if (doctor == null) {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Doctor not found");
//			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
//		}
//
//		if (request.getAppointmentId() == 0) {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Appointment not found");
//			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
//		}
//
//		Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());
//
//		if (appointment == null) {
//			throw new AppointmentNotFoundException();
//		}
//
//		if (appointment.getStatus().equals(AppointmentStatus.CANCEL.value())) {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Appointment is cancel by patient");
//			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
//		}
//
//		appointment.setDoctorId(request.getDoctorId());
//		appointment.setStatus(AppointmentStatus.ASSIGNED_TO_DOCTOR.value());
//
//		Appointment updatedAppointment = this.appointmentService.addAppointment(appointment);
//
//		if (updatedAppointment != null) {
//			response.setResponseCode(ResponseCode.SUCCESS.value());
//			response.setResponseMessage("Successfully Assigned Appointment to doctor");
//			return new ResponseEntity(response, HttpStatus.OK);
//		}
//
//		else {
//			response.setResponseCode(ResponseCode.FAILED.value());
//			response.setResponseMessage("Failed to assign");
//			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//
//	}

	@PostMapping("doctor/update")
	@ApiOperation(value = "Api to assign appointment to doctor")
	public ResponseEntity<?> assignAppointmentToDoctor(UpdateAppointmentRequest request) {
		LOG.info("Recieved request to update appointment");

		CommanApiResponse response = new CommanApiResponse();

		if (request == null) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to assign appointment");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getAppointmentId() == 0) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Appointment not found");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());

		if (appointment == null) {
			throw new AppointmentNotFoundException();
		}

		appointment.setPrescription(request.getPrescription());
		appointment.setStatus(request.getStatus());

		if (request.getStatus().equals(AppointmentStatus.TREATMENT_DONE.value())) {
			appointment.setPrice(request.getPrice());
		}

		Appointment updatedAppointment = this.appointmentService.addAppointment(appointment);

		if (updatedAppointment != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("Updated Treatment Status");
			return new ResponseEntity(response, HttpStatus.OK);
		}

		else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to update");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

	@PostMapping("patient/update")
	@ApiOperation(value = "Api to update appointment patient")
	public ResponseEntity<?> udpateAppointmentStatus(@RequestBody UpdateAppointmentRequest request) {
		LOG.info("Recieved request to update appointment");

		CommanApiResponse response = new CommanApiResponse();

		if (request == null) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to assign appointment");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		if (request.getAppointmentId() == 0) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Appointment not found");
			return new ResponseEntity(response, HttpStatus.BAD_REQUEST);
		}

		Appointment appointment = appointmentService.getAppointmentById(request.getAppointmentId());

		if (appointment == null) {
			throw new AppointmentNotFoundException();
		}

		appointment.setStatus(request.getStatus());
		Appointment updatedAppointment = this.appointmentService.addAppointment(appointment);

		if (updatedAppointment != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());

			response.setResponseMessage("Updated Treatment Status");
			return new ResponseEntity(response, HttpStatus.OK);
		}

		else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to update");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

}
