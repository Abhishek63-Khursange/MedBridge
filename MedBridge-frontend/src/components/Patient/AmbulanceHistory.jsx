import React, { useState, useEffect } from "react";
import axios from "axios";
import { Container, Card, Table, Button, Spinner, Alert } from "react-bootstrap";
import { useNavigate } from "react-router-dom";

const AmbulanceHistory = () => {
  const [bookings, setBookings] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    fetchBookings();
  }, []);

  const fetchBookings = async () => {
    try {
      const token = sessionStorage.getItem("token");
      const response = await axios.get("http://localhost:8080/api/ambulance/patient/bookings", {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      });
      setBookings(response.data);
    } catch (err) {
      setError("Failed to fetch ambulance bookings");
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container className="mt-4">
      <Card className="shadow-lg border-0">
        <Card.Header className="bg-info text-white text-center">
          <h4>📋 My Ambulance Bookings</h4>
        </Card.Header>
        <Card.Body>
          {error && (
            <Alert variant="danger" className="text-center">
              {error}
            </Alert>
          )}

          {loading ? (
            <div className="text-center mt-4">
              <Spinner animation="border" variant="info" />
              <p className="mt-2">Loading your ambulance bookings...</p>
            </div>
          ) : bookings.length === 0 ? (
            <div className="text-center mt-4">
              <h5 className="text-muted">No ambulance bookings found.</h5>
              <Button 
                variant="danger" 
                onClick={() => navigate("/patient/book-ambulance")}
                className="mt-3"
              >
                Book Your First Ambulance
              </Button>
            </div>
          ) : (
            <Table responsive hover bordered className="text-center">
              <thead className="table-info">
                <tr>
                  <th>Booking ID</th>
                  <th>Patient Name</th>
                  <th>Contact</th>
                  <th>Pickup Location</th>
                  <th>Drop Location</th>
                  <th>Booking Time</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {bookings.map((booking) => (
                  <tr key={booking.id}>
                    <td>{booking.id}</td>
                    <td>{booking.patientName}</td>
                    <td>{booking.contactNumber}</td>
                    <td>{booking.pickupLocation}</td>
                    <td>{booking.dropLocation}</td>
                    <td>{booking.bookingTime}</td>
                    <td>
                      <span className={`badge bg-${
                        booking.status === 'COMPLETED' ? 'success' : 
                        booking.status === 'ASSIGNED' ? 'warning' : 'primary'
                      }`}>
                        {booking.status}
                      </span>
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          )}

          <div className="text-center mt-4">
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

export default AmbulanceHistory;
