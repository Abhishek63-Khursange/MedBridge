package com.MedBridge.service;

import java.util.List;

import com.MedBridge.dto.PatientRegisterDto;
import com.MedBridge.exception.ResourceNotFoundException;
import com.MedBridge.utility.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.MedBridge.dao.UserDao;
import com.MedBridge.entity.User;
import com.MedBridge.utility.Constants.UserStatus;

import static com.MedBridge.dto.PatientRegisterDto.toEntity;


@Service
public class UserService {
	
	@Autowired
	private UserDao userDao;
	
	public User registerAdmin(User user) {
		User registeredAdmin = null;
		if(user != null) {
			registeredAdmin = this.userDao.save(user);
		}
		
		return registeredAdmin;
	}

	public User registerPatient(PatientRegisterDto patientRegisterDto) {
		User registeredPatient = null;
		if(patientRegisterDto != null){
			registeredPatient = this.userDao.save(toEntity(patientRegisterDto));
		}
		return registeredPatient;

	}
	public User registerDoctor(User user) {
		User registeredDoctor = null;
		if(user != null ){
			registeredDoctor = this.userDao.save(user);
		}
		return registeredDoctor;
	}
	public User getUserByEmailIdAndPassword(String emailId, String password) {
		return this.userDao.findByEmailIdAndPassword(emailId, password);
	}
	
	public User getUserByEmailIdAndPasswordAndRole(String emailId, String password, String role) {
		return this.userDao.findByEmailIdAndPasswordAndRole(emailId, password, role);
	}
	
	public User getUserByEmailIdAndRole(String emailId, String role) {
		return this.userDao.findByEmailIdAndRole(emailId, role);
	}
	
	public User getUserByEmailId(String emailId) {
		return this.userDao.findByEmailId(emailId);
	}

	public User getUserById(int id) {
		return userDao.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
	}


//	public User getUserById(int userId) {
//		return this.userDao.findById(userId).get();
//	}
	
	public User updateUser(User user) {
		return this.userDao.save(user);
	}
	
	public List<User> getAllUserByRole(String role) {
		return this.userDao.findByRoleAndStatus(role, UserStatus.ACTIVE.value());
	}

	public List<User> getAllUserByRoleAndStatus(String role, int status) {
		return this.userDao.findByRoleAndStatus(role, status);
	}

	public void deletUser(User user) {
		this.userDao.delete(user);
	}





}
