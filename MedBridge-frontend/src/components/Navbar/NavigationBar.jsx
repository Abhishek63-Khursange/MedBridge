import React, { useEffect, useState } from "react";
import "./NavigationBar.css";
import { Navbar, Container, Nav, Dropdown } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { getToken, removeToken } from "../../services/tokenService";

const NavigationBar = () => {
  const navigate = useNavigate();
  const [tokenPresent, setTokenPresent] = useState(false);
  const [role, setRole] = useState(null);
  const [userName, setUserName] = useState(null);

  useEffect(() => {
    const updateAuthState = () => {
      const token = sessionStorage.getItem("token");
      const storedRole = sessionStorage.getItem("role");
      const storedName = sessionStorage.getItem("firstName");

      if (token) {
        setTokenPresent(true);
        setRole(storedRole);
        setUserName(storedName);
      } else {
        setTokenPresent(false);
        setRole(null);
        setUserName(null);
      }
    };

    updateAuthState();
    
    // Listen for storage changes
    const handleStorageChange = () => {
      updateAuthState();
    };
    
    window.addEventListener('storage', handleStorageChange);
    
    return () => {
      window.removeEventListener('storage', handleStorageChange);
    };
  }, []);

  const handleLogOut = () => {
    removeToken();
    sessionStorage.clear(); // Also remove 'role' and any other user info
    setTokenPresent(false);
    setRole(null);
    setUserName(null);
    navigate("/");
    window.location.reload(); // Ensures Navbar re-renders properly
  };

  const handleLogin = (role) => {
    navigate("/admin-login", { state: { role } });
  };

  const handleDashboardRedirect = () => {
    if (role === "admin") {
      navigate("/admin/dashboard");
    } else if (role === "doctor") {
      navigate("/doctor/viewAllAppointment");
    } else if (role === "patient") {
      navigate("/patient-dashboard");
    }
  };

  return (
    <Navbar
      collapseOnSelect
      expand="lg"
      bg="light"
      variant="light"
      sticky="top"
      style={{
        background:
          "linear-gradient(90deg, rgb(203 172 223) 0%,rgb(92, 161, 230) 50%, rgb(223 212 195) 100%)",
        color: "red",
      }}
    >
      <Container>
        <Navbar.Brand
          href="/"
          className="d-flex align-items-center"
          style={{
            fontWeight: 800,
            fontFamily: "'Baskervville SC', serif",
          }}
        >
          <img
            src="../../../public/assets/LoginPageImages/HMSLogo.png"
            alt="MedBridge Logo"
            style={{
              height: "80px",
              objectFit: "contain",
              marginRight: "10px",
            }}
          />
        </Navbar.Brand>
        <Navbar.Toggle aria-controls="responsive-navbar-nav" />
        <Navbar.Collapse id="responsive-navbar-nav">
          <Nav className="me-auto">
            <Nav.Link
              href="/"
              className="ms-4 nav-link-custom"
              style={{ color: "whitesmoke", fontWeight: "500" }}
            >
              Home
            </Nav.Link>
            <Nav.Link
              href="/about"
              className="ms-4 nav-link-custom"
              style={{ color: "whitesmoke", fontWeight: "500" }}
            >
              About-Us
            </Nav.Link>
            <Nav.Link
              href="/contact"
              className="ms-4 nav-link-custom"
              style={{ color: "whitesmoke", fontWeight: "500" }}
            >
              Contact-Us
            </Nav.Link>
            {tokenPresent && role === "patient" && (
              <Nav.Link
                href="/patient/book-ambulance"
                className="ms-4 nav-link-custom"
                style={{ color: "whitesmoke", fontWeight: "500" }}
              >
                Book Ambulance
              </Nav.Link>
            )}
          </Nav>

          <Nav>
            {tokenPresent && userName && (
              <span className="navbar-text me-3" style={{ color: "whitesmoke", fontWeight: "500" }}>
                Hello {role?.charAt(0).toUpperCase() + role?.slice(1)} {userName}!
              </span>
            )}
            <Dropdown className="ms-4">
              <Dropdown.Toggle variant="secondary" id="user-dropdown">
                {tokenPresent ? "Logout" : "Login"}
              </Dropdown.Toggle>
              <Dropdown.Menu>
                {tokenPresent ? (
                  <>
                    <Dropdown.Item onClick={handleDashboardRedirect}>
                      🏠 Dashboard
                    </Dropdown.Item>
                    <Dropdown.Divider />
                    <Dropdown.Item onClick={handleLogOut}>Logout</Dropdown.Item>
                  </>
                ) : (
                  <>
                    <Dropdown.Item onClick={() => handleLogin("admin")}>
                      Admin Login
                    </Dropdown.Item>
                    <Dropdown.Item onClick={() => handleLogin("doctor")}>
                      Doctor Login
                    </Dropdown.Item>
                    <Dropdown.Item onClick={() => handleLogin("patient")}>
                      Patient Login
                    </Dropdown.Item>
                  </>
                )}
              </Dropdown.Menu>
            </Dropdown>
          </Nav>
        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
};

export default NavigationBar;

// import React, { useEffect, useState } from "react";
// import "./NavigationBar.css";
// import { Navbar, Container, Nav, Dropdown } from "react-bootstrap";
// import { useNavigate } from "react-router-dom";
// import { getToken, removeToken } from "../../services/tokenService";

// const NavigationBar = () => {
//   const navigate = useNavigate();
//   const [tokenPresent, setTokenPresent] = useState(false);

//   useEffect(() => {
//     const token = getToken();
//     setTokenPresent(!!token);
//   }, []);

//   const handleLogOut = () => {
//     removeToken();
//     setTokenPresent(false);
//     navigate("/");
//   };

//   const handleLogin = (role) => {
//     navigate("/admin-login", { state: { role } });
//     // if (role === "doctor") {
//     //   navigate("/admin-login");
//     // } else if (role === "patient") {
//     //   navigate("/admin-login");
//     // } else if (role === "admin") {
//     //   navigate("/admin-login");
//     // }
//   };

//   // const handleHome = () => {
//   //   navigate("/");
//   // };

//   return (
//     <Navbar
//       collapseOnSelect
//       expand="lg"
//       bg="light"
//       variant="light"
//       sticky="top"
//       style={{
//         background:
//           "linear-gradient(90deg, rgb(203 172 223) 0%,rgb(92, 161, 230) 50%, rgb(223 212 195) 100%)",
//         color: "red",
//       }}
//     >
//       <Container>
//         <Navbar.Brand
//           href="/"
//           className="d-flex align-items-center"
//           style={{
//             fontWeight: 800,
//             fontFamily: "'Baskervville SC', serif",
//           }}
//         >
//           <img
//             src="../../../public/assets/LoginPageImages/HMSLogo.png"
//             alt="MedBridge Logo"
//             style={{
//               height: "80px", // Adjust as needed
//               objectFit: "contain",
//               marginRight: "10px",
//             }}
//           />
//         </Navbar.Brand>
//         <Navbar.Toggle aria-controls="responsive-navbar-nav" />
//         <Navbar.Collapse id="responsive-navbar-nav">
//           <Nav className="me-auto">
//             <Nav.Link
//               href="/"
//               className="ms-4 nav-link-custom"
//               style={{ color: "whitesmoke", fontWeight: "500" }}
//             >
//               Home
//             </Nav.Link>
//             <Nav.Link
//               href="/about"
//               className="ms-4 nav-link-custom"
//               style={{ color: "whitesmoke", fontWeight: "500" }}
//             >
//               About-Us
//             </Nav.Link>
//             <Nav.Link
//               href="/contact"
//               className="ms-4 nav-link-custom"
//               style={{ color: "whitesmoke", fontWeight: "500" }}
//             >
//               Contact-Us
//             </Nav.Link>
//           </Nav>
//           <Nav>
//             <Dropdown className="ms-4">
//               {tokenPresent ? (
//                 <Dropdown.Toggle variant="secondary" id="logout-dropdown">
//                   Logout
//                 </Dropdown.Toggle>
//               ) : (
//                 <Dropdown.Toggle variant="secondary" id="login-dropdown">
//                   Login
//                 </Dropdown.Toggle>
//               )}
//               <Dropdown.Menu>
//                 {tokenPresent ? (
//                   <Dropdown.Item onClick={handleLogOut}>Logout</Dropdown.Item>
//                 ) : (
//                   <>
//                     <Dropdown.Item onClick={() => handleLogin("admin")}>
//                       Admin Login
//                     </Dropdown.Item>
//                     <Dropdown.Item onClick={() => handleLogin("doctor")}>
//                       Doctor Login
//                     </Dropdown.Item>
//                     <Dropdown.Item onClick={() => handleLogin("patient")}>
//                       Patient Login
//                     </Dropdown.Item>
//                   </>
//                 )}
//               </Dropdown.Menu>
//             </Dropdown>
//           </Nav>
//         </Navbar.Collapse>
//       </Container>
//     </Navbar>
//   );
// };

// export default NavigationBar;
