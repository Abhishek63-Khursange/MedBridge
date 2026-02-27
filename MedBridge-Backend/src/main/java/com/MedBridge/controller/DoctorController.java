package com.MedBridge.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.print.Doc;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import com.MedBridge.dto.*;
import com.MedBridge.entity.Appointment;
import com.MedBridge.entity.Doctor;
import com.MedBridge.service.AppointmentService;
import com.MedBridge.service.CustomUserDetailsService;
import com.MedBridge.service.DoctorService;
import com.MedBridge.utility.Constants;
import com.MedBridge.utility.JwtUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import com.MedBridge.entity.User;
import com.MedBridge.service.UserService;
import com.MedBridge.utility.Constants.DoctorSpecialist;
import com.MedBridge.utility.Constants.ResponseCode;
import com.MedBridge.utility.Constants.UserRole;
import com.MedBridge.utility.Constants.UserStatus;
import com.MedBridge.utility.StorageService;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/doctor")
//@CrossOrigin(origins = "http://localhost:5173")
@CrossOrigin(origins = {"http://localhost:5173" , "http://localhost:3000"})
public class DoctorController {

	Logger LOG = LoggerFactory.getLogger(DoctorController.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private AppointmentService appointmentService;

	@Autowired
	private DoctorService doctorService;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private StorageService storageService;
	@PostMapping("/register")
	@ApiOperation(value = "API to register doctor")
	public ResponseEntity<?> registerDoctor(
			@RequestPart("doctor") DoctorRegisterDto doctorRegisterDto,
			@RequestPart("image") MultipartFile image) {

		LOG.info("Received request for doctor register");

		CommanApiResponse response = new CommanApiResponse();

		// Convert DTO to Entity
		User user = DoctorRegisterDto.toEntity(doctorRegisterDto);

		// Store image
		String imageName = storageService.store(image);
		LOG.info("Image stored with name: " + imageName);
//		user.setDoctorImage(imageName);
		user.getDoctor().setDoctorImage(imageName);
		LOG.info("Doctor image set to: " + user.getDoctor().getDoctorImage());

		// Encode password
		String encodedPassword = passwordEncoder.encode(user.getPassword());
		user.setPassword(encodedPassword);
		user.setStatus(UserStatus.PENDING.value());

		// Save user
		User registeredUser = userService.registerDoctor(user);

		if (registeredUser != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage(user.getRole() + " Doctor Registered Successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Register Doctor");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}



	@PostMapping("/login")
	@ApiOperation(value = "Api to login any Doctor")
	public ResponseEntity<?> login(@RequestBody UserLoginRequest userLoginRequest) {
		LOG.info("Recieved request for User Login");

		String jwtToken = null;
		UserLoginResponse useLoginResponse = new UserLoginResponse();
		User user = null;
		try {
			authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(userLoginRequest.getEmailId(), userLoginRequest.getPassword()));
		} catch (Exception ex) {
			LOG.error("Autthentication Failed!!!");
			useLoginResponse.setResponseCode(Constants.ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.UNAUTHORIZED);
		}

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userLoginRequest.getEmailId());

		user = userService.getUserByEmailId(userLoginRequest.getEmailId());

		if (user.getStatus() != Constants.UserStatus.ACTIVE.value()) {
			useLoginResponse.setResponseCode(Constants.ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Doctor account is not verified. Please wait for admin approval.");
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}

		for (GrantedAuthority grantedAuthory : userDetails.getAuthorities()) {
			if (grantedAuthory.getAuthority().equals(userLoginRequest.getRole())) {

				List<String> roles = userDetails.getAuthorities()
						.stream()
						.map(GrantedAuthority::getAuthority)
						.collect(Collectors.toList());

				jwtToken = jwtUtil.generateToken(userDetails.getUsername());

			}
		}

		// user is authenticated
		if (jwtToken != null) {
			useLoginResponse = User.toUserLoginResponse(user);

			useLoginResponse.setResponseCode(Constants.ResponseCode.SUCCESS.value());
			useLoginResponse.setResponseMessage(user.getFirstName() + " logged in Successful");
			useLoginResponse.setJwtToken(jwtToken);
			return new ResponseEntity(useLoginResponse, HttpStatus.OK);

		} else {

			useLoginResponse.setResponseCode(Constants.ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}


	}

	@GetMapping("/all")
	public ResponseEntity<?> getAllDoctor() {
		LOG.info("recieved request for getting ALL Doctor!!!");

		List<User> doctors = this.userService.getAllUserByRole(UserRole.DOCTOR.value());

		LOG.info("response sent!!!");
		return ResponseEntity.ok(doctors);
	}

	@GetMapping("/pending")
	public ResponseEntity<?> getPendingDoctors() {
		LOG.info("recieved request for getting PENDING Doctors!!!");

		List<User> doctors = this.userService.getAllUserByRoleAndStatus(UserRole.DOCTOR.value(), UserStatus.PENDING.value());

		LOG.info("response sent!!!");
		return ResponseEntity.ok(doctors);
	}

	@PostMapping("/verify/{doctorId}")
	public ResponseEntity<?> verifyDoctor(@PathVariable int doctorId) {
		LOG.info("recieved request to verify doctor with id: " + doctorId);

		CommanApiResponse response = new CommanApiResponse();

		User doctor = this.userService.getUserById(doctorId);
		
		if (doctor == null) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Doctor not found");
			return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
		}

		if (!doctor.getRole().equals(UserRole.DOCTOR.value())) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("User is not a doctor");
			return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
		}

		doctor.setStatus(UserStatus.ACTIVE.value());
		User updatedDoctor = this.userService.updateUser(doctor);

		if (updatedDoctor != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("Doctor verified successfully");
			return new ResponseEntity<>(response, HttpStatus.OK);
		} else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to verify doctor");
			return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping(value = "/{doctorImageName}", produces = "image/*")
	@ApiOperation(value = "Api to fetch doctor image by using image name")
	public void fetchProductImage(@PathVariable("doctorImageName") String doctorImageName, HttpServletResponse resp) {
		LOG.info("request came for fetching doctor pic");
		LOG.info("Loading file: " + doctorImageName);
		LOG.info("Base path: " + System.getProperty("user.dir"));
		
		try {
			Resource resource = storageService.load(doctorImageName);
			if (resource != null && resource.exists()) {
				resp.setContentType("image/jpeg");
				try (InputStream in = resource.getInputStream()) {
					ServletOutputStream out = resp.getOutputStream();
					FileCopyUtils.copy(in, out);
				}
				LOG.info("Image served successfully: " + doctorImageName);
			} else {
				LOG.warn("Image not found: " + doctorImageName);
				LOG.warn("Resource is null or doesn't exist");
				resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
		} catch (Exception e) {
			LOG.error("Error serving image: " + doctorImageName, e);
			resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		}
	}

	@GetMapping("/image/{imageName}")
	@ApiOperation(value = "Api to get doctor image")
	public ResponseEntity<?> getDoctorImage(@PathVariable String imageName) {
		LOG.info("Received request for doctor image: " + imageName);
		
		try {
			// Create a default image if not found
			ClassPathResource resource = new ClassPathResource("static/images/default-doctor.jpg");
			
			if (!resource.exists()) {
				// Create a simple placeholder response
				return ResponseEntity.ok()
					.contentType(org.springframework.http.MediaType.IMAGE_JPEG)
					.body(new byte[0]);
			}
			
			byte[] imageBytes = FileCopyUtils.copyToByteArray(resource.getInputStream());
			
			return ResponseEntity.ok()
				.contentType(org.springframework.http.MediaType.IMAGE_JPEG)
				.body(imageBytes);
				
		} catch (Exception e) {
			LOG.error("Error serving image: " + e.getMessage());
			return ResponseEntity.notFound().build();
		}
	}

	@GetMapping("/specialist/all")
	public ResponseEntity<?> getAllSpecialist() {

		LOG.info("Received the request for getting as Specialist");

		List<String> specialists = new ArrayList<>();

		for (DoctorSpecialist s : DoctorSpecialist.values()) {
			specialists.add(s.value());
		}

		LOG.info("Response sent!!!");

		return new ResponseEntity(specialists, HttpStatus.OK);
	}

	@GetMapping("/id")
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

				if (appointment.getStatus().equals(Constants.AppointmentStatus.TREATMENT_DONE.value())) {
					a.setPrice(String.valueOf(appointment.getPrice()));
				}

				else {
					a.setPrice(Constants.AppointmentStatus.TREATMENT_PENDING.value());
				}
				a.setPrescription(appointment.getPrescription());

			}

			else {
				a.setDoctorContact(Constants.AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorName(Constants.AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setDoctorId(0);
				a.setPrice(Constants.AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());
				a.setPrescription(Constants.AppointmentStatus.NOT_ASSIGNED_TO_DOCTOR.value());


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


	@Data
	@AllArgsConstructor
	public class DoctorDetailsDto {
		private int id;
		private String firstName;
		private String lastName;
		private String specialization;
	}

	@GetMapping("/by-speciality/{speciality}")
	public ResponseEntity<List<DoctorDetailsDto>> getDoctorsBySpeciality(@PathVariable String speciality) {
		List<Doctor> doctors = doctorService.getDoctorsBySpeciality(speciality);
		List<DoctorDetailsDto> dtos = doctors.stream()
				.map(d -> new DoctorDetailsDto(
						d.getUser().getId(),
						d.getUser().getFirstName(),
						d.getUser().getLastName(),
						d.getSpecialist()
				))
				.collect(Collectors.toList());
		return ResponseEntity.ok(dtos);
	}



}
