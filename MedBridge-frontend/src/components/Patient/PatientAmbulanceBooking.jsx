import React, { useState } from "react";
import axios from "axios";
import { Container, Card, Form, Button, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const PatientAmbulanceBooking = () => {
  const [form, setForm] = useState({
    patientName: "",
    contactNumber: "",
    pickupLocation: "",
    dropLocation: "",
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setMessage("");

    try {
      const token = sessionStorage.getItem("token");
      const res = await axios.post(
        "http://localhost:8080/api/ambulance/book",
        form,
        {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
          }
        }
      );
      
      setMessage("✅ Ambulance booked successfully! Driver will contact you soon.");
      
      // Redirect to patient dashboard after 2 seconds
      setTimeout(() => {
        navigate("/patient-dashboard");
      }, 2000);
      
    } catch (err) {
      console.error(err);
      setMessage("❌ Booking failed: " + (err?.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-4">
      <Card className="shadow-lg border-0">
        <Card.Header className="bg-danger text-white text-center">
          <h4>🚑 Book Ambulance</h4>
        </Card.Header>
        <Card.Body>
          {message && (
            <Alert 
              variant={message.includes("✅") ? "success" : "danger"}
              className="text-center"
            >
              {message}
            </Alert>
          )}
          
          <Form onSubmit={submit}>
            <Form.Group className="mb-3">
              <Form.Label>Patient Name</Form.Label>
              <Form.Control
                name="patientName"
                type="text"
                placeholder="Enter patient name"
                onChange={handle}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Contact Number</Form.Label>
              <Form.Control
                name="contactNumber"
                type="tel"
                placeholder="Enter contact number"
                onChange={handle}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Pickup Location</Form.Label>
              <Form.Control
                name="pickupLocation"
                type="text"
                placeholder="Enter pickup address"
                onChange={handle}
                required
              />
            </Form.Group>

            <Form.Group className="mb-3">
              <Form.Label>Drop Location</Form.Label>
              <Form.Control
                name="dropLocation"
                type="text"
                placeholder="Enter destination address"
                onChange={handle}
                required
              />
            </Form.Group>

            <div className="d-grid">
              <Button 
                variant="danger" 
                type="submit" 
                disabled={loading}
                size="lg"
              >
                {loading ? "Booking..." : "🚑 Book Ambulance Now"}
              </Button>
            </div>
          </Form>

          <div className="text-center mt-3">
            <Button 
              variant="secondary" 
              onClick={() => navigate("/patient-dashboard")}
            >
              ← Back to Dashboard
            </Button>
          </div>
        </Card.Body>
      </Card>
    </Container>
  );
};

export default PatientAmbulanceBooking;
