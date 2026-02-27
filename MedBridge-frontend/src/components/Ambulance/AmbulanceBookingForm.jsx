import React, { useState } from "react";
import axios from "axios";

export default function AmbulanceBookingForm({ onBooked }) {
  const [form, setForm] = useState({
    patientName: "",
    contactNumber: "",
    pickupLocation: "",
    dropLocation: "",
  });
  const [loading, setLoading] = useState(false);
  const handle = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setLoading(true);
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
      onBooked(res.data);
    } catch (err) {
      console.error(err);
      alert("Booking failed: " + (err?.response?.data?.message || err.message));
    } finally {
      setLoading(false);
    }
  };

  return (
    <form onSubmit={submit} className="card p-3">
      <h4 className="mb-3">Book Ambulance</h4>
      <input
        name="patientName"
        className="form-control mb-2"
        placeholder="Patient Name"
        onChange={handle}
        required
      />
      <input
        name="contactNumber"
        className="form-control mb-2"
        placeholder="Contact Number"
        onChange={handle}
        required
      />
      <input
        name="pickupLocation"
        className="form-control mb-2"
        placeholder="Pickup Location"
        onChange={handle}
        required
      />
      <input
        name="dropLocation"
        className="form-control mb-3"
        placeholder="Drop Location"
        onChange={handle}
        required
      />
      <button className="btn btn-primary" type="submit" disabled={loading}>
        {loading ? "Booking..." : "Book Now"}
      </button>
    </form>
  );
}
