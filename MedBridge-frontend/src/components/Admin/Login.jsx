import { useEffect, useState } from "react";
import {
  Container,
  Form,
  Button,
  Card,
  Row,
  Col,
  FloatingLabel,
} from "react-bootstrap";
import { ToastContainer, toast } from "react-toastify";
import { useNavigate, useLocation } from "react-router-dom";
import "react-toastify/dist/ReactToastify.css";

import axios from "axios";

const Login = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const [loginRequest, setLoginRequest] = useState({
    emailId: "",
    password: "",
    role: location.state?.role || "admin", // Default to admin
  });

  // Optional: scroll to top when component loads
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  useEffect(() => {
    if (location.state?.role) {
      setLoginRequest((prev) => ({
        ...prev,
        role: location.state.role,
      }));
    }
  }, [location.state]);
  const handleUserInput = (e) => {
    setLoginRequest({ ...loginRequest, [e.target.name]: e.target.value });
  };

  const loginAction = async (e) => {
    e.preventDefault();
    console.log("Login payload:", loginRequest);
    try {
      const response = await axios.post(
        `http://localhost:8080/api/${loginRequest.role}/login`,
        loginRequest,
        {
          headers: {
            Accept: "application/json",
            "Content-Type": "application/json",
          },
        }
      );

      const res = response.data;
      console.log(res);

      // Store token and role
      if (res.jwtToken) {
        sessionStorage.setItem("token", res.jwtToken);
        sessionStorage.setItem("role", loginRequest.role);
        sessionStorage.setItem("firstName", res.firstName);
      }

      // Store user info and navigate based on role
      if (res.role === "admin") {
        sessionStorage.setItem("active-admin", JSON.stringify(res));
        navigate("/admin/dashboard");
      } else if (res.role === "patient") {
        sessionStorage.setItem("active-patient", JSON.stringify(res));
        navigate("/patient-dashboard");
      } else if (res.role === "doctor") {
        sessionStorage.setItem("active-doctor", JSON.stringify(res));
        navigate("/doctor/viewAllAppointment");
      }

      // Success toast
      toast.success("Logged in successfully!", {
        position: "top-center",
        autoClose: 1000,
      });

      // Optional page reload
      setTimeout(() => {
        window.location.reload();
      }, 1200);
    } catch (error) {
      console.error("Login failed:", error);

      // Handle invalid credentials (401/403)
      if (
        error.response &&
        (error.response.status === 401 || error.response.status === 403)
      ) {
        toast.error("Invalid username or password!", {
          position: "top-center",
          autoClose: 1000,
        });
      } else {
        // Other errors
        toast.error("Login failed. Please try again later.", {
          position: "top-center",
          autoClose: 1000,
        });
      }
    }
  };

  return (
    <>
      <Container className="mt-5">
        <Row className="justify-content-center">
          <Col md={6} lg={5}>
            <Card className="shadow-lg border-0">
              <Card.Header className="bg-primary text-white text-center">
                <h4 className="my-2">User Login</h4>
              </Card.Header>
              <Card.Body>
                <Form onSubmit={loginAction}>
                  <Form.Group controlId="role" className="mb-3">
                    <Form.Label>User Role</Form.Label>
                    <Form.Select
                      name="role"
                      value={loginRequest.role}
                      onChange={handleUserInput}
                    >
                      <option value="admin">Admin</option>
                      <option value="patient">Patient</option>
                      <option value="doctor">Doctor</option>
                    </Form.Select>
                  </Form.Group>

                  <FloatingLabel
                    controlId="emailId"
                    label="Email address"
                    className="mb-3"
                  >
                    <Form.Control
                      type="email"
                      placeholder="name@example.com"
                      name="emailId"
                      value={loginRequest.emailId}
                      onChange={handleUserInput}
                      required
                    />
                  </FloatingLabel>

                  <FloatingLabel
                    controlId="password"
                    label="Password"
                    className="mb-3"
                  >
                    <Form.Control
                      type="password"
                      placeholder="Password"
                      name="password"
                      value={loginRequest.password}
                      onChange={handleUserInput}
                      autoComplete="on"
                      required
                    />
                  </FloatingLabel>

                  <div className="d-grid">
                    <Button
                      type="submit"
                      variant="primary"
                      size="lg"
                      className="custom-bg-text"
                    >
                      Login
                    </Button>
                  </div>
                </Form>
              </Card.Body>
            </Card>
            <ToastContainer />
          </Col>
        </Row>
      </Container>
    </>
  );
};

export default Login;
