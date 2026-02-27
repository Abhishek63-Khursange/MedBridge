import { BrowserRouter, Route, Routes } from "react-router-dom";
import PrivateRoute from "./components/PrivateRoute";
import { ToastContainer } from "react-toastify";
import Home from "./components/HomePage/Home";
import PatientRegistration from "./components/Patient Signup/PatientRegistration";
import AboutUs from "./components/Aboutus/AboutUs";
import ContactPage from "./components/Contactus/ContactPage";
import Login from "./components/Admin/Login";
import PatientDashboard from "./components/Patient/PatientDashboard";
import AddAppointment from "./components/Patient/AddAppointment";
import ViewMyAppointment from "./components/Patient/ViewMyAppointment";
import PatientAmbulanceBooking from "./components/Patient/PatientAmbulanceBooking";
import AmbulanceHistory from "./components/Patient/AmbulanceHistory";
import DoctorRegister from "./components/Doctor/DoctorRegister";
import ViewDoctorAppointment from "./components/Doctor/ViewDoctorAppointment";
import AdminRegistration from "./components/Admin/AdminRegistration";
import AdminDashboard from "./components/Admin/AdminDashboard";
import ViewPatientToAdmin from "./components/Admin/ViewPatientToAdmin";
import ViewDoctorToAdmin from "./components/Admin/ViewDoctorToAdmin";
import ViewAppointmentToAdmin from "./components/Admin/ViewAppointmentToAdmin";
import AssignAppointment from "./components/Admin/AssignAppointment";
import PendingDoctors from "./components/Admin/PendingDoctors";
import NavigationBar from "./components/Navbar/NavigationBar";
import MedBridgeFooter from "./components/Footer/MedBridgeFooter";
import Ambulance from "./components/Ambulance/Ambulance";

function App() {
  return (
    <BrowserRouter>
      <NavigationBar />
      <Routes>
        {/* public routes */}
        <Route path="/" element={<Home />}></Route>
        <Route path="/about" element={<AboutUs></AboutUs>}></Route>
        <Route path="/contact" element={<ContactPage></ContactPage>}></Route>
        <Route path="/admin-login" element={<Login />}></Route>
        <Route path="/Ambulance" element={<Ambulance />}></Route>

        <Route
          path="/patientRegister"
          element={<PatientRegistration />}
        ></Route>
        <Route path="/doctor/register" element={<DoctorRegister />}></Route>
        <Route path="/admin/register" element={<AdminRegistration />}></Route>

        {/* patient routes */}
        <Route element={<PrivateRoute allowedRoles={["patient"]} />}>
          <Route
            path="/patient-dashboard"
            element={<PatientDashboard />}
          ></Route>
          <Route
            path="/patient/addAppointment"
            element={<AddAppointment />}
          ></Route>
          <Route
            path="/patient/viewAppointment"
            element={<ViewMyAppointment />}
          ></Route>
          <Route
            path="/patient/book-ambulance"
            element={<PatientAmbulanceBooking />}
          ></Route>
          <Route
            path="/patient/ambulance-history"
            element={<AmbulanceHistory />}
          ></Route>
        </Route>

        {/* Doctor Route*/}
        <Route element={<PrivateRoute allowedRoles={["doctor"]} />}>
          <Route
            path="/doctor/viewAllAppointment"
            element={<ViewDoctorAppointment />}
          ></Route>
        </Route>

        {/*  ADMIN routes */}
        <Route element={<PrivateRoute allowedRoles={["admin"]} />}>
          <Route path="/admin/dashboard" element={<AdminDashboard />}></Route>
          <Route
            path="/admin/dashboard/viewpatient"
            element={<ViewPatientToAdmin />}
          ></Route>
          <Route
            path="/admin/dashboard/viewdoctor"
            element={<ViewDoctorToAdmin />}
          ></Route>
          <Route
            path="/admin/dashboard/pendingdoctors"
            element={<PendingDoctors />}
          ></Route>
          <Route
            path="/admin/dashboard/appointment"
            element={<ViewAppointmentToAdmin />}
          ></Route>
          <Route
            element={<AssignAppointment />}
            path="/admin/appointment/:appointmentId/assign"
          ></Route>
        </Route>
      </Routes>

      <ToastContainer></ToastContainer>
      <MedBridgeFooter />
    </BrowserRouter>
  );
}

export default App;
