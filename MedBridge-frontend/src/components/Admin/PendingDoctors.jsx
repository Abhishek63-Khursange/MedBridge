import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Container,
  Row,
  Col,
  Table,
  Button,
  Image,
  Card,
} from "react-bootstrap";
import { BsTrashFill, BsCheckCircle } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import NavigationBar from "../Navbar/NavigationBar";

const PendingDoctors = () => {
  const [pendingDoctors, setPendingDoctors] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const getPendingDoctors = async () => {
      const doctors = await retrievePendingDoctors();
      if (doctors) {
        setPendingDoctors(doctors);
      }
    };

    getPendingDoctors();
  }, []);

  const retrievePendingDoctors = async () => {
    const token = sessionStorage.getItem("token");
    const response = await axios.get("http://localhost:8080/api/admin/pending/doctors", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    return response.data;
  };

  const token = sessionStorage.getItem("token");

  const deleteDoctor = (doctorId) => {
    fetch(`http://localhost:8080/api/admin/delete/id?userId=${doctorId}`, {
      method: "DELETE",

      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    }).then((result) =>
      result.json().then((res) => {
        alert(res.responseMessage);
        window.location.reload(true);
      })
    );
  };

  const verifyDoctor = (doctorId) => {
    fetch(`http://localhost:8080/api/admin/verify/doctor/${doctorId}`, {
      method: "POST",

      headers: {
        Authorization: `Bearer ${token}`,
        Accept: "application/json",
        "Content-Type": "application/json",
      },
    }).then((result) =>
      result.json().then((res) => {
        alert(res.responseMessage);
        window.location.reload(true);
      })
    );
  };

  return (
    <>
      <Container className="mt-4">
        <Card className="shadow">
          <Card.Header className="text-center bg-warning text-dark">
            <h3>Pending Doctors Verification</h3>
          </Card.Header>
          <Card.Body style={{ overflowX: "auto" }}>
            <div className="mb-3 text-start">
              <Button
                variant="outline-secondary"
                onClick={() => navigate("/admin/dashboard")}
              >
                ← Back to Dashboard
              </Button>
            </div>

            {pendingDoctors.length === 0 ? (
              <div className="text-center py-5">
                <h4 className="text-muted">No pending doctors to verify</h4>
              </div>
            ) : (
              <Table
                responsive
                hover
                bordered
                className="text-center align-middle"
              >
                <thead className="table-warning">
                  <tr>
                    <th>Doctor</th>
                    <th>First Name</th>
                    <th>Last Name</th>
                    <th>Email</th>
                    <th>Specialist</th>
                    <th>Experience</th>
                    <th>Age</th>
                    <th>Phone</th>
                    <th>Address</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pendingDoctors.map((doctor, index) => (
                    <tr key={index}>
                      <td>
                        <Image
                          src={`http://localhost:8080/api/doctor/${doctor.doctor?.doctorImage}`}
                          roundedCircle
                          width={60}
                          height={60}
                          alt="doctor"
                          style={{
                            objectFit: "cover",
                            border: "2px solid #ffc107",
                            backgroundColor: "#f8f9fa"
                          }}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.src = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='60' height='60' viewBox='0 0 60 60'%3E%3Ccircle cx='30' cy='30' r='30' fill='%23ddd'/%3E%3Ctext x='50%25' y='50%25' text-anchor='middle' dy='.3em' font-family='Arial' font-size='12' fill='%23666'%3EDr%3C/text%3E%3C/svg%3E";
                          }}
                        />
                      </td>
                      <td>{doctor.firstName}</td>
                      <td>{doctor.lastName}</td>
                      <td>{doctor.emailId}</td>
                      <td>{doctor.doctor?.specialist}</td>
                      <td>{doctor.doctor?.experience} yrs</td>
                      <td>{doctor.age}</td>
                      <td>{doctor.contact}</td>
                      <td>{`${doctor.street}, ${doctor.city}, ${doctor.pincode}`}</td>
                      <td>
                        <div className="d-flex gap-2 justify-content-center">
                          <Button
                            variant="success"
                            size="sm"
                            onClick={() => verifyDoctor(doctor.id)}
                          >
                            <BsCheckCircle /> Verify
                          </Button>
                          <Button
                            variant="danger"
                            size="sm"
                            onClick={() => deleteDoctor(doctor.id)}
                          >
                            <BsTrashFill /> Delete
                          </Button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
        </Card>
      </Container>
    </>
  );
};

export default PendingDoctors;
