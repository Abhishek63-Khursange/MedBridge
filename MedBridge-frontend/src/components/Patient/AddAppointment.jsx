import { useEffect, useState } from "react";
import {
  Container,
  Row,
  Col,
  Card,
  Form,
  Button,
  FloatingLabel,
} from "react-bootstrap";
import { ToastContainer, toast } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import NavigationBar from "../Navbar/NavigationBar";
import { useNavigate } from "react-router-dom";
import { FaClipboardList } from "react-icons/fa";
import Footer from "../Footer/Footer";
import axios from "axios";

const AddAppointment = () => {
  const navigate = useNavigate();
  const patient = JSON.parse(sessionStorage.getItem("active-patient"));

  const amount = 200;
  const [isLoading, setIsLoading] = useState(false);

  // ✅ Existing appointment state + new fields for speciality & doctor
  const [appointment, setAppointment] = useState({
    patientId: patient?.id || "",
    speciality: "",
    doctorId: "",
    problem: "",
    appointmentDate: "",
    price: amount,
    paymentID: "",
  });

  // ✅ New state for dropdowns
  const [specialities, setSpecialities] = useState([]);
  const [doctors, setDoctors] = useState([]);

  // ✅ Fetch all specialities on mount
  useEffect(() => {
    axios
      .get("http://localhost:8080/api/doctor/specialist/all")
      .then((res) => {
        if (Array.isArray(res.data)) {
          setSpecialities(res.data);
        }
      })
      .catch((err) => console.error("Error loading specialities", err));
  }, []);

  // ✅ When speciality changes, fetch doctors
  useEffect(() => {
    if (appointment.speciality) {
      axios
        .get(
          `http://localhost:8080/api/doctor/by-speciality/${appointment.speciality}`
        )
        .then((res) => {
          if (Array.isArray(res.data)) {
            setDoctors(res.data);
          }
        })
        .catch((err) => console.error("Error loading doctors", err));
    } else {
      setDoctors([]);
    }
  }, [appointment.speciality]);

  // ✅ Handle input changes
  const handleUserInput = (e) => {
    setAppointment({ ...appointment, [e.target.name]: e.target.value });
  };

  const saveAppointment = async (event) => {
    event.preventDefault();

    if (
      !appointment.speciality ||
      !appointment.doctorId ||
      !appointment.problem ||
      !appointment.appointmentDate
    ) {
      toast.error("Please fill in all fields.");
      return;
    }

    const token = sessionStorage.getItem("token");

    try {
      setIsLoading(true);

      // Step 1: Save appointment first to get appointment ID
      const appointmentResponse = await fetch("http://localhost:8080/api/appointment/patient/add", {
        method: "POST",
        headers: {
          Accept: "application/json",
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(appointment),
      });

      if (!appointmentResponse.ok) {
        throw new Error("Failed to save appointment");
      }

      const savedAppointment = await appointmentResponse.json();
      const appointmentId = savedAppointment.id || savedAppointment.appointmentId;
      
      console.log("Appointment saved with ID:", appointmentId);

      // Step 2: Create Razorpay order
      const orderRes = await fetch(
        `http://localhost:8080/api/payment/createOrder?amount=${amount}`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const orderData = await orderRes.json();

      // Step 3: Razorpay options
      const options = {
        key: "rzp_test_TNpcedZQbcsMrm",
        amount: orderData.amount,
        currency: "INR",
        name: "MedBridge",
        description: "Appointment Payment",
        order_id: orderData.id,
        handler: async function (response) {
          // Step 4: Verify payment with appointment ID
          const verifyRes = await fetch(
            "http://localhost:8080/api/payment/verify",
            {
              method: "POST",
              headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${token}`,
              },
              body: JSON.stringify({
                order_id: orderData.id,
                razorpay_payment_id: response.razorpay_payment_id,
                razorpay_signature: response.razorpay_signature,
                appointment_id: appointmentId, // ✅ Added appointment ID
              }),
            }
          );

          if (verifyRes.ok) {
            toast.success("Appointment booked and payment successful!");
          } else {
            toast.error("Payment verification failed");
          }
          setIsLoading(false);
        },
        prefill: {
          name: patient?.firstName || "Patient",
          email: patient?.email || "",
        },
        theme: {
          color: "#4070b8ff",
        },
      };

      const razorpay = new window.Razorpay(options);
      razorpay.open();
    } catch (error) {
      console.error("Error:", error);
      toast.error("Something went wrong.");
      setIsLoading(false);
    }
  };

  return (
    <>
      <div
        style={{
          backgroundImage: `url('/images/hospital-bg.jpg')`,
          backgroundSize: "cover",
          backgroundRepeat: "no-repeat",
          minHeight: "100vh",
          paddingTop: "60px",
          paddingBottom: "60px",
        }}
      >
        <Container>
          <Row className="justify-content-center">
            <Col md={6} lg={5}>
              <Card className="shadow-lg border-0 rounded-4">
                <Card.Header className="bg-primary text-white rounded-top-4 d-flex justify-content-between align-items-center">
                  <h4 className="mb-0">🩺 Take Appointment</h4>
                  <Button
                    variant="light"
                    className="fw-semibold rounded-pill shadow-sm d-flex align-items-center"
                    onClick={() => navigate("/patient/viewAppointment")}
                  >
                    <FaClipboardList className="me-2" />
                    View Appointments
                  </Button>
                </Card.Header>

                <Card.Body className="p-4">
                  <Form onSubmit={saveAppointment}>
                    {/* Speciality Dropdown */}
                    <Form.Group className="mb-3">
                      <Form.Label>Select Speciality</Form.Label>
                      <Form.Select
                        name="speciality"
                        value={appointment.speciality}
                        onChange={handleUserInput}
                        required
                      >
                        <option value="">-- Select Speciality --</option>
                        {specialities.map((spec, idx) => (
                          <option key={idx} value={spec}>
                            {spec}
                          </option>
                        ))}
                      </Form.Select>
                    </Form.Group>

                    {/* Doctor Dropdown */}
                    <Form.Group className="mb-4">
                      <Form.Label>Select Doctor</Form.Label>
                      <Form.Select
                        name="doctorId"
                        value={appointment.doctorId}
                        onChange={handleUserInput}
                        required
                        disabled={!doctors.length}
                      >
                        <option value="">-- Select Doctor --</option>
                        {doctors.map((doc) => (
                          <option key={doc.id} value={doc.id}>
                            Dr. {doc.firstName} {doc.lastName}
                          </option>
                        ))}
                      </Form.Select>
                    </Form.Group>

                    {/* Problem */}
                    <FloatingLabel
                      controlId="problem"
                      label="Mention your problems"
                      className="mb-4"
                    >
                      <Form.Control
                        as="textarea"
                        name="problem"
                        style={{ height: "100px" }}
                        value={appointment.problem}
                        onChange={handleUserInput}
                        placeholder="Mention your problems"
                      />
                    </FloatingLabel>

                    {/* Date */}
                    <FloatingLabel
                      controlId="appointmentDate"
                      label="Appointment Date"
                      className="mb-4"
                    >
                      <Form.Control
                        type="date"
                        name="appointmentDate"
                        value={appointment.appointmentDate}
                        onChange={handleUserInput}
                        min={new Date().toISOString().split("T")[0]}
                      />
                    </FloatingLabel>

                    <div className="d-grid">
                      <Button variant="primary" size="lg" type="submit">
                        Book Appointment
                      </Button>
                    </div>
                  </Form>
                </Card.Body>
              </Card>
            </Col>
          </Row>
          <ToastContainer />
        </Container>

        {/* Loader */}
        {isLoading && (
          <div
            style={{
              position: "fixed",
              top: 0,
              left: 0,
              width: "100vw",
              height: "100vh",
              backgroundColor: "rgba(255, 255, 255, 0.7)",
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              zIndex: 9999,
            }}
          >
            <div className="text-center">
              <div
                className="spinner-border text-primary"
                role="status"
                style={{ width: "4rem", height: "4rem" }}
              >
                <span className="visually-hidden">Loading...</span>
              </div>
              <div className="mt-3 fw-bold text-primary">
                Processing, please wait...
              </div>
            </div>
          </div>
        )}
      </div>
    </>
  );
};

export default AddAppointment;

// import React, { useEffect, useState } from "react";
// import axios from "axios";
// import { Form, Button, Container, Row, Col, Card } from "react-bootstrap";

// export default function BookAppointment() {
//   const [specialities, setSpecialities] = useState([]);
//   const [doctors, setDoctors] = useState([]);
//   const [timeSlots, setTimeSlots] = useState([]);
//   const [formData, setFormData] = useState({
//     speciality: "",
//     doctorId: "",
//     appointmentDate: "",
//     timeSlotId: "",
//   });

//   // ✅ Fetch all specialities on page load
//   useEffect(() => {
//     axios
//       .get("http://localhost:8080/api/doctor/specialist/all")
//       .then((res) => {
//         if (Array.isArray(res.data)) {
//           setSpecialities(res.data);
//         }
//       })
//       .catch((err) => console.error("Error loading specialities", err));
//   }, []);

//   // ✅ When speciality changes, fetch doctors for that speciality
//   useEffect(() => {
//     if (formData.speciality) {
//       console.log(formData.speciality);
//       axios
//         .get(
//           `http://localhost:8080/api/doctor/by-speciality/${formData.speciality}`
//         )
//         .then((res) => {
//           if (Array.isArray(res.data)) {
//             setDoctors(res.data);
//           }
//         })
//         .catch((err) => console.error("Error loading doctors", err));
//     } else {
//       setDoctors([]);
//     }
//   }, [formData.speciality]);

//   // ✅ When doctor & date are selected, fetch available time slots
//   // useEffect(() => {
//   //   if (formData.doctorId && formData.appointmentDate) {
//   //     axios
//   //       .get(
//   //         `/api/timeslot/available?doctorId=${formData.doctorId}&date=${formData.appointmentDate}`
//   //       )
//   //       .then((res) => {
//   //         if (Array.isArray(res.data)) {
//   //           setTimeSlots(res.data);
//   //         }
//   //       })
//   //       .catch((err) => console.error("Error loading time slots", err));
//   //   } else {
//   //     setTimeSlots([]);
//   //   }
//   // }, [formData.doctorId, formData.appointmentDate]);

//   // ✅ Handle input changes
//   const handleChange = (e) => {
//     setFormData((prev) => ({
//       ...prev,
//       [e.target.name]: e.target.value,
//     }));
//   };

//   // ✅ Handle form submit
//   const handleSubmit = (e) => {
//     e.preventDefault();
//     axios
//       .post("/api/appointment/book", formData)
//       .then((res) => {
//         alert("Appointment booked successfully!");
//         setFormData({
//           speciality: "",
//           doctorId: "",
//           appointmentDate: "",
//           timeSlotId: "",
//         });
//         setDoctors([]);
//         setTimeSlots([]);
//       })
//       .catch((err) => {
//         console.error("Error booking appointment", err);
//         alert("Error booking appointment");
//       });
//   };

//   return (
//     <Container className="mt-4">
//       <Card className="p-4 shadow-lg border-0 rounded">
//         <h2 className="mb-4 text-center">Book an Appointment</h2>
//         <Form onSubmit={handleSubmit}>
//           {/* Speciality */}
//           <Form.Group className="mb-3">
//             <Form.Label>Select Speciality</Form.Label>
//             <Form.Select
//               name="speciality"
//               value={formData.speciality}
//               onChange={handleChange}
//               required
//             >
//               <option value="">-- Select Speciality --</option>
//               {specialities.map((spec, idx) => (
//                 <option key={idx} value={spec}>
//                   {spec}
//                 </option>
//               ))}
//             </Form.Select>
//           </Form.Group>

//           {/* Doctor */}

//           <Form.Group className="mb-3">
//             <Form.Label>Select Doctor</Form.Label>
//             <Form.Select
//               name="doctorId"
//               value={formData.doctorId}
//               onChange={handleChange}
//               required
//               disabled={!doctors.length}
//             >
//               <option value="">-- Select Doctor --</option>
//               {doctors.map((doc) => (
//                 <option key={doc.id} value={doc.id}>
//                   Dr. {doc.firstName} {doc.lastName}
//                 </option>
//               ))}
//             </Form.Select>
//           </Form.Group>

//           <Row>
//             {/* Date */}
//             <Col md={6}>
//               <Form.Group className="mb-3">
//                 <Form.Label>Select Date</Form.Label>
//                 <Form.Control
//                   type="date"
//                   name="appointmentDate"
//                   value={formData.appointmentDate}
//                   onChange={handleChange}
//                   required
//                   min={new Date().toISOString().split("T")[0]}
//                 />
//               </Form.Group>
//             </Col>
//           </Row>

//           <div className="text-center">
//             <Button type="submit" variant="primary">
//               Book Appointment
//             </Button>
//           </div>
//         </Form>
//       </Card>
//     </Container>
//   );
// }

// import { useState } from "react";
// import {
//   Container,
//   Row,
//   Col,
//   Card,
//   Form,
//   Button,
//   FloatingLabel,
// } from "react-bootstrap";
// import { ToastContainer, toast } from "react-toastify";
// import "react-toastify/dist/ReactToastify.css";
// import NavigationBar from "../Navbar/NavigationBar";
// import { useNavigate } from "react-router-dom";
// import { FaClipboardList } from "react-icons/fa";
// import Footer from "../Footer/Footer";

// const AddAppointment = () => {
//   const navigate = useNavigate();
//   const patient = JSON.parse(sessionStorage.getItem("active-patient"));

//   const amount = 200;
//   const [isLoading, setIsLoading] = useState(false);
//   const [appointment, setAppointment] = useState({
//     patientId: patient?.id || "",
//     problem: "",
//     appointmentDate: "",
//     price: amount,
//   });

//   const handleUserInput = (e) => {
//     setAppointment({ ...appointment, [e.target.name]: e.target.value });
//   };

//   const saveAppointment = async (event) => {
//     event.preventDefault();

//     if (!appointment.problem || !appointment.appointmentDate) {
//       toast.error("Please fill in all fields.");
//       return;
//     }

//     const token = sessionStorage.getItem("token");

//     try {
//       setIsLoading(true); // 🔵 Start loader

//       // Step 1: Create Razorpay order from backend
//       const orderRes = await fetch(
//         `http://localhost:8080/api/payment/createOrder?amount=${amount}`,
//         {
//           method: "POST",
//           headers: {
//             "Content-Type": "application/json",
//             Authorization: `Bearer ${token}`,
//           },
//         }
//       );

//       const orderData = await orderRes.json();

//       // Step 2: Razorpay options
//       const options = {
//         key: "rzp_test_TNpcedZQbcsMrm",
//         amount: orderData.amount,
//         currency: "INR",
//         name: "MedBridge",
//         description: "Appointment Payment",
//         order_id: orderData.id,
//         handler: async function (response) {
//           // Step 3: Verify payment
//           const verifyRes = await fetch(
//             "http://localhost:8080/api/payment/verify",
//             {
//               method: "POST",
//               headers: {
//                 "Content-Type": "application/json",
//                 Authorization: `Bearer ${token}`,
//               },
//               body: JSON.stringify({
//                 order_id: orderData.id,
//                 razorpay_payment_id: response.razorpay_payment_id,
//                 razorpay_signature: response.razorpay_signature,
//               }),
//             }
//           );

//           if (verifyRes.ok) {
//             // Step 4: Save appointment after successful payment
//             fetch("http://localhost:8080/api/appointment/patient/add", {
//               method: "POST",
//               headers: {
//                 Accept: "application/json",
//                 "Content-Type": "application/json",
//                 Authorization: `Bearer ${token}`,
//               },
//               body: JSON.stringify(appointment),
//             })
//               .then(async (res) => {
//                 if (!res.ok) throw new Error(await res.text());
//                 toast.success("Appointment booked and payment successful!");
//               })
//               .catch(() => {
//                 toast.error("Payment done but appointment failed to save.");
//               })
//               .finally(() => setIsLoading(false));
//           } else {
//             toast.error("Payment verification failed");
//             setIsLoading(false);
//           }
//         },
//         prefill: {
//           name: patient?.firstName || "Patient",
//           email: patient?.email || "",
//         },
//         theme: {
//           color: "#0d6efd",
//         },
//       };

//       const razorpay = new window.Razorpay(options);
//       razorpay.open();
//     } catch (error) {
//       console.error("Error:", error);
//       toast.error("Something went wrong.");
//       setIsLoading(false);
//     }
//   };

//   return (
//     <>
//       <div
//         style={{
//           backgroundImage: `url('/images/hospital-bg.jpg')`,
//           backgroundSize: "cover",
//           backgroundRepeat: "no-repeat",
//           minHeight: "100vh",
//           paddingTop: "60px",
//           paddingBottom: "60px",
//         }}
//       >
//         <Container>
//           <Row className="justify-content-center">
//             <Col md={6} lg={5}>
//               <Card className="shadow-lg border-0 rounded-4">
//                 <Card.Header className="bg-primary text-white rounded-top-4 d-flex justify-content-between align-items-center">
//                   <h4 className="mb-0">🩺 Take Appointment</h4>
//                   <Button
//                     variant="light"
//                     className="fw-semibold rounded-pill shadow-sm d-flex align-items-center"
//                     onClick={() => navigate("/patient/viewAppointment")}
//                   >
//                     <FaClipboardList className="me-2" />
//                     View Appointments
//                   </Button>
//                 </Card.Header>

//                 <Card.Body className="p-4">
//                   <Form onSubmit={saveAppointment}>
//                     <FloatingLabel
//                       controlId="problem"
//                       label="Mention your problems"
//                       className="mb-4"
//                     >
//                       <Form.Control
//                         as="textarea"
//                         name="problem"
//                         style={{ height: "100px" }}
//                         value={appointment.problem}
//                         onChange={handleUserInput}
//                         placeholder="Mention your problems"
//                       />
//                     </FloatingLabel>

//                     <FloatingLabel
//                       controlId="appointmentDate"
//                       label="Appointment Date"
//                       className="mb-4"
//                     >
//                       <Form.Control
//                         type="date"
//                         name="appointmentDate"
//                         value={appointment.appointmentDate}
//                         onChange={handleUserInput}
//                       />
//                     </FloatingLabel>

//                     <div className="d-grid">
//                       <Button variant="primary" size="lg" type="submit">
//                         Book Appointment
//                       </Button>
//                     </div>
//                   </Form>
//                 </Card.Body>
//               </Card>
//             </Col>
//           </Row>
//           <ToastContainer />
//         </Container>

//         {/* 🔄 Loader Overlay */}
//         {isLoading && (
//           <div
//             style={{
//               position: "fixed",
//               top: 0,
//               left: 0,
//               width: "100vw",
//               height: "100vh",
//               backgroundColor: "rgba(255, 255, 255, 0.7)",
//               display: "flex",
//               justifyContent: "center",
//               alignItems: "center",
//               zIndex: 9999,
//             }}
//           >
//             <div className="text-center">
//               <div
//                 className="spinner-border text-primary"
//                 role="status"
//                 style={{ width: "4rem", height: "4rem" }}
//               >
//                 <span className="visually-hidden">Loading...</span>
//               </div>
//               <div className="mt-3 fw-bold text-primary">
//                 Processing, please wait...
//               </div>
//             </div>
//           </div>
//         )}
//       </div>
//     </>
//   );
// };

// export default AddAppointment;
