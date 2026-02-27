import React, { useState, useEffect } from "react";
import axios from "axios";
import { Container, Table, Button, Card, Spinner } from "react-bootstrap";
import { BsTrashFill } from "react-icons/bs";
import { useNavigate } from "react-router-dom";
import NavigationBar from "../Navbar/NavigationBar";

const ViewPatientToAdmin = () => {
  const [allPatient, setAllPatient] = useState([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const getAllPatient = async () => {
      const allPatient = await retrieveAllPatient();
      if (allPatient) {
        setAllPatient(allPatient);
        setLoading(false);
      }
    };

    getAllPatient();
  }, []);

  const retrieveAllPatient = async () => {
    const token = sessionStorage.getItem("token");
    const response = await axios.get("http://localhost:8080/api/patient/all", {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    console.log(response.data);
    return response.data;
  };

  const token = sessionStorage.getItem("token");

  const deletePatient = (patientId) => {
    const confirmDelete = window.confirm(
      "Are you sure you want to delete this patient?"
    );
    if (!confirmDelete) return;

    fetch(`http://localhost:8080/api/admin/delete/id?userId=${patientId}`, {
      method: "DELETE",
      headers: {
        Accept: "application/json",
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
    }).then((result) => {
      result.json().then((res) => {
        alert(res.responseMessage);
        // Refresh the patient list after deletion
        setAllPatient((prev) =>
          prev.filter((patient) => patient.id !== patientId)
        );
      });
    });
  };

  return (
    <>
      <Container className="mt-5">
        <Card className="shadow-lg border-0">
          <Card.Header className="bg-primary text-white text-center">
            <h4>All Registered Patients</h4>
          </Card.Header>
          <Card.Body style={{ overflowX: "auto", maxHeight: "500px" }}>
            {loading ? (
              <div className="text-center mt-4">
                <Spinner animation="border" variant="primary" />
              </div>
            ) : allPatient.length === 0 ? (
              <h5 className="text-center text-muted">No patients found.</h5>
            ) : (
              <Table
                bordered
                hover
                responsive
                className="text-center align-middle"
              >
                <thead className="table-primary">
                  <tr>
                    <th>First Name</th>
                    <th>Last Name</th>
                    <th>Email</th>
                    <th>Age</th>
                    <th>Gender</th>
                    <th>Blood Group</th>
                    <th>Phone No</th>
                    <th>Address</th>
                    <th>Action</th>
                  </tr>
                </thead>
                <tbody>
                  {allPatient.map((patient) => (
                    <tr key={patient.id}>
                      <td>{patient.firstName}</td>
                      <td>{patient.lastName}</td>
                      <td>{patient.emailId}</td>
                      <td>{patient.age}</td>
                      <td>{patient.sex}</td>
                      <td>{patient.bloodGroup}</td>
                      <td>{patient.contact}</td>
                      <td>
                        {patient.street}, {patient.city}, {patient.pincode}
                      </td>
                      <td>
                        <Button
                          variant="danger"
                          size="sm"
                          onClick={() => deletePatient(patient.id)}
                        >
                          <BsTrashFill /> Delete
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            )}
          </Card.Body>
          <Card.Footer className="text-center">
            <Button
              variant="secondary"
              onClick={() => navigate("/admin/dashboard")}
            >
              ⬅ Back to Dashboard
            </Button>
          </Card.Footer>
        </Card>
      </Container>
    </>
  );
};

export default ViewPatientToAdmin;
