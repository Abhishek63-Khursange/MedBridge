package com.MedBridge.controller;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.MedBridge.dto.CommanApiResponse;
import com.MedBridge.dto.UserLoginRequest;
import com.MedBridge.dto.UserLoginResponse;
import com.MedBridge.dto.UserRoleResponse;
import com.MedBridge.dto.UsersResponseDto;
import com.MedBridge.entity.User;
import com.MedBridge.exception.UserNotFoundException;
import com.MedBridge.service.CustomUserDetailsService;
import com.MedBridge.service.UserService;
import com.MedBridge.utility.Constants.ResponseCode;
import com.MedBridge.utility.Constants.Sex;
import com.MedBridge.utility.Constants.UserRole;
import com.MedBridge.utility.Constants.UserStatus;
import com.MedBridge.utility.JwtUtil;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = {"http://localhost:5173" , "http://localhost:3000"})
public class UserController {

	Logger LOG = LoggerFactory.getLogger(UserController.class);

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private UserService userService;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService customUserDetailsService;

	@Autowired
	private JwtUtil jwtUtil;

	@GetMapping("roles")
	@ApiOperation(value = "Api to get all user roles")
	public ResponseEntity<?> getAllUsers() {
		
		UserRoleResponse response = new UserRoleResponse();
		List<String> roles = new ArrayList<>();
		
		for(UserRole role : UserRole.values() ) {
			roles.add(role.value());
		}
		
		if(roles.isEmpty()) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Fetch User Roles");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		else {
		    response.setRoles(roles);
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("User Roles Fetched success");
			return new ResponseEntity(response, HttpStatus.OK);
		}
		
	}
	
	@GetMapping("gender")
	@ApiOperation(value = "Api to get all user gender")
	public ResponseEntity<?> getAllUserGender() {
		
		UserRoleResponse response = new UserRoleResponse();
		List<String> genders = new ArrayList<>();
		
		for(Sex gender : Sex.values() ) {
			genders.add(gender.value());
		}
		
		if(genders.isEmpty()) {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Fetch User Genders");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		else {
			response.setGenders(genders);
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage("User Genders Fetched success");
			return new ResponseEntity(response, HttpStatus.OK);
		}
		
	}
	
	@PostMapping("/register")
	@ApiOperation(value = "Api to register any User")
	public ResponseEntity<?> register(@RequestBody User user) {
		LOG.info("Recieved request for User  register");

		CommanApiResponse response = new CommanApiResponse();
		String encodedPassword = passwordEncoder.encode(user.getPassword());

		user.setPassword(encodedPassword);
		user.setStatus(UserStatus.ACTIVE.value());

		User registerUser = userService.registerAdmin(user);

		if (registerUser != null) {
			response.setResponseCode(ResponseCode.SUCCESS.value());
			response.setResponseMessage(user.getRole() + " User Registered Successfully");
			return new ResponseEntity(response, HttpStatus.OK);
		}

		else {
			response.setResponseCode(ResponseCode.FAILED.value());
			response.setResponseMessage("Failed to Register " + user.getRole() + " User");
			return new ResponseEntity(response, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}
	
	@PostMapping("/login")
	@ApiOperation(value = "Api to login any User")
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
			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}

		UserDetails userDetails = customUserDetailsService.loadUserByUsername(userLoginRequest.getEmailId());

		user = userService.getUserByEmailId(userLoginRequest.getEmailId());
		
		if(user.getStatus() != UserStatus.ACTIVE.value()) {
			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("User is Inactive");
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}
		
		for (GrantedAuthority grantedAuthory : userDetails.getAuthorities()) {
			if (grantedAuthory.getAuthority().equals(userLoginRequest.getRole())) {
				jwtToken = jwtUtil.generateToken(userDetails.getUsername());
			}
		}

		// user is authenticated
		if (jwtToken != null) {
			useLoginResponse = User.toUserLoginResponse(user);
			
			useLoginResponse.setResponseCode(ResponseCode.SUCCESS.value());
			useLoginResponse.setResponseMessage(user.getFirstName() + " logged in Successful");
			useLoginResponse.setJwtToken(jwtToken);
			return new ResponseEntity(useLoginResponse, HttpStatus.OK);
		
		}

		else {

			useLoginResponse.setResponseCode(ResponseCode.FAILED.value());
			useLoginResponse.setResponseMessage("Failed to Login as " + userLoginRequest.getEmailId());
			return new ResponseEntity(useLoginResponse, HttpStatus.BAD_REQUEST);
		}
	}

	@GetMapping("id")
	@ApiOperation(value = "Api to fetch the User using user Id")
	public ResponseEntity<?> fetchUser(@RequestParam("userId") int userId) {
		
		UsersResponseDto response = new UsersResponseDto();
		
		User user = userService.getUserById(userId);
		
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		response.setUser(user);
		response.setResponseCode(ResponseCode.SUCCESS.value());
		response.setResponseMessage("User Fetched Successfully");
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
	@GetMapping("/pending/doctors")
	@ApiOperation(value = "Api to get all pending doctors for admin verification")
	public ResponseEntity<?> getPendingDoctors() {
		LOG.info("Received request for getting PENDING Doctors!!!");
		
		List<User> doctors = userService.getAllUserByRoleAndStatus(UserRole.DOCTOR.value(), UserStatus.PENDING.value());
		
		LOG.info("Response sent!!!");
		return ResponseEntity.ok(doctors);
	}
	
	@PostMapping("/verify/doctor/{doctorId}")
	@ApiOperation(value = "Api to verify a pending doctor")
	public ResponseEntity<?> verifyDoctor(@PathVariable int doctorId) {
		LOG.info("Received request to verify doctor with id: " + doctorId);
		
		CommanApiResponse response = new CommanApiResponse();
		
		User doctor = userService.getUserById(doctorId);
		
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
		User updatedDoctor = userService.updateUser(doctor);
		
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
	
	@DeleteMapping("/delete/id")
	@ApiOperation(value = "Api to delete user by using user id")
	public ResponseEntity<?> deleteUser(@RequestParam("userId") int userId) {
		
		System.out.println("request came for USER DELETE By ID");
		
		CommanApiResponse response = new CommanApiResponse();
		
		User user = null;
		user = userService.getUserById(userId);
		
		if(user == null) {
			throw new UserNotFoundException();
		}
		
		user.setStatus(UserStatus.DELETED.value());
		
		userService.registerAdmin(user);
		
		response.setResponseCode(ResponseCode.SUCCESS.value());
		response.setResponseMessage("User Deleted Successfully");
		
		return new ResponseEntity(response, HttpStatus.OK);
	}
	
}
