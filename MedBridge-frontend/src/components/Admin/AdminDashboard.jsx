import React from "react";
import { Container, Row, Col, Card, Button } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import {
  FaUserInjured,
  FaUserMd,
  FaClipboardList,
  FaUserPlus,
  FaHourglassHalf,
} from "react-icons/fa";
import NavigationBar from "../Navbar/NavigationBar";
import "./AdminDashboard.css"; // Make sure this CSS file exists

const AdminDashboard = () => {
  const navigate = useNavigate();

  const cards = [
    {
      title: "View Patients",
      icon: <FaUserInjured size={40} color="#0d6efd" />,
      text: "See all registered patients and their details.",
      route: "/admin/dashboard/viewpatient",
    },
    {
      title: "View Doctors",
      icon: <FaUserMd size={40} color="#198754" />,
      text: "See all doctors and manage them.",
      route: "/admin/dashboard/viewdoctor",
    },
    {
      title: "Pending Doctors",
      icon: <FaHourglassHalf size={40} color="#ffc107" />,
      text: "Verify pending doctor registrations.",
      route: "/admin/dashboard/pendingdoctors",
    },
    {
      title: "Check Appointments",
      icon: <FaClipboardList size={40} color="#ffc107" />,
      text: "View and manage all appointments.",
      route: "/admin/dashboard/appointment",
    },
    {
      title: "Register New Doctor",
      icon: <FaUserPlus size={40} color="#dc3545" />,
      text: "Add a new doctor to the system.",
      route: "/doctor/register",
    },
  ];

  return (
    <>
      <div className="admin-dashboard-bg">
        <Container className="py-5 admin-container">
          <h2 className="text-center mb-4 fw-bold custom-red-bold">
            Admin Dashboard
          </h2>

          <Row className="g-4">
            {cards.map((card, idx) => (
              <Col key={idx} sm={12} md={6} lg={6} xl={3}>
                <Card className="shadow-sm h-100 text-center border-1">
                  <Card.Body>
                    <div className="mb-3">{card.icon}</div>
                    <Card.Title>{card.title}</Card.Title>
                    <Card.Text>{card.text}</Card.Text>
                    <Button
                      variant="primary"
                      onClick={() => navigate(card.route)}
                    >
                      Go
                    </Button>
                  </Card.Body>
                </Card>
              </Col>
            ))}
          </Row>
        </Container>
      </div>
    </>
  );
};

export default AdminDashboard;
