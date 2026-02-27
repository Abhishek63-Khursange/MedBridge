import React from "react";
import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import NavigationBar from "../Navbar/NavigationBar";
import "./PatientDashboard.css"; // 👈 import custom CSS

const PatientDashboard = () => {
  const navigate = useNavigate();

  const cardStyle = {
    cursor: "pointer",
    transition: "transform 0.2s ease-in-out",
    boxShadow: "0 4px 8px rgba(0,0,0,0.1)",
  };

  const handleHover = (e) => {
    e.currentTarget.style.transform = "scale(1.03)";
  };

  const handleLeave = (e) => {
    e.currentTarget.style.transform = "scale(1)";
  };

  return (
    <>
      <div className="patient-dashboard-container">
        <Container>
          <h2 className="text-center mb-4 patient-dashboard-heading text-white mt-5 ">
            Welcome to Patient Dashboard
          </h2>
          <Row className="justify-content-center">
            <Col md={5} className="mb-4">
              <Card
                style={cardStyle}
                onMouseEnter={handleHover}
                onMouseLeave={handleLeave}
                onClick={() => navigate("/patient/addAppointment")}
              >
                <Card.Body className="text-center">
                  <Card.Title className="mb-3">🩺 Take Appointment</Card.Title>
                  <Card.Text>
                    Book an appointment with your preferred doctor quickly and
                    easily.
                  </Card.Text>
                  <Button variant="primary">Book Now</Button>
                </Card.Body>
              </Card>
            </Col>

            <Col md={5} className="mb-4">
              <Card
                style={cardStyle}
                onMouseEnter={handleHover}
                onMouseLeave={handleLeave}
                onClick={() => navigate("/patient/viewAppointment")}
              >
                <Card.Body className="text-center">
                  <Card.Title className="mb-3">📅 My Appointments</Card.Title>
                  <Card.Text>
                    View your scheduled appointments and track your health
                    visits.
                  </Card.Text>
                  <Button variant="success">View Appointments</Button>
                </Card.Body>
              </Card>
            </Col>
          </Row>

          <Row className="justify-content-center">
            <Col md={5} className="mb-4">
              <Card
                style={cardStyle}
                onMouseEnter={handleHover}
                onMouseLeave={handleLeave}
                onClick={() => navigate("/patient/book-ambulance")}
              >
                <Card.Body className="text-center">
                  <Card.Title className="mb-3">🚑 Book Ambulance</Card.Title>
                  <Card.Text>
                    Need emergency medical transport? Book an ambulance instantly.
                  </Card.Text>
                  <Button variant="danger">Book Ambulance</Button>
                </Card.Body>
              </Card>
            </Col>

            <Col md={5} className="mb-4">
              <Card
                style={cardStyle}
                onMouseEnter={handleHover}
                onMouseLeave={handleLeave}
                onClick={() => navigate("/patient/ambulance-history")}
              >
                <Card.Body className="text-center">
                  <Card.Title className="mb-3">📋 Ambulance History</Card.Title>
                  <Card.Text>
                    View your ambulance booking history and status.
                  </Card.Text>
                  <Button variant="info">View History</Button>
                </Card.Body>
              </Card>
            </Col>
          </Row>
        </Container>
      </div>
    </>
  );
};

export default PatientDashboard;
