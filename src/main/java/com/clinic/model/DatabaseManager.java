package com.clinic.model;

import com.clinic.model.Patient;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:C:/ClinicData/clinic.db";
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }


    public static void initializeDatabase() {
        System.out.println("DEBUG: Connection URL is: " + URL);

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Patients Table
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "fayda TEXT PRIMARY KEY, name TEXT, dob TEXT, gender TEXT, " +
                    "contact TEXT, status TEXT, doctor_name TEXT, diagnosis TEXT, " +
                    "treatment TEXT, prescription TEXT, appt_date TEXT, payment TEXT, " +
                    "reg_date TEXT, last_visit TEXT, balance_owed REAL DEFAULT 0.0);");

            // Users Table
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, " +
                    "password TEXT, role TEXT);");

            // Lab Results Table
            stmt.execute("CREATE TABLE IF NOT EXISTS lab_results (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id TEXT NOT NULL, " +
                    "test_name TEXT, result_value TEXT, test_date TEXT, " +
                    "FOREIGN KEY (patient_id) REFERENCES patients(fayda));");

            // Doctors, Appointments, and Inventory
            stmt.execute("CREATE TABLE IF NOT EXISTS doctors (" +
                    "doctor_id INTEGER PRIMARY KEY AUTOINCREMENT, first_name TEXT, last_name TEXT, " +
                    "specialization TEXT, schedule TEXT);");

            stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                    "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, patient_id TEXT, " +
                    "doctor_id INTEGER, appointment_date TEXT, status TEXT, " +
                    "FOREIGN KEY(patient_id) REFERENCES patients(fayda), " +
                    "FOREIGN KEY(doctor_id) REFERENCES doctors(doctor_id));");

            // fayda is the unique key we use for everything
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (fayda TEXT PRIMARY KEY, name TEXT);");

            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                    "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT, " +
                    "quantity INTEGER, expiration_date TEXT);");

            //RUN PATCHES
            String[] patches = {
                    "ALTER TABLE patients ADD COLUMN payment_status TEXT;",
                    "ALTER TABLE patients ADD COLUMN payment_amount TEXT;",
                    "ALTER TABLE patients ADD COLUMN last_visit TEXT;",
                    "ALTER TABLE patients ADD COLUMN reg_date TEXT;"
            };

            for (String patch : patches) {
                try {
                    stmt.execute(patch);
                } catch (SQLException e) {}
            }

            // (The Master Keys)
            // We used INSERT OR IGNORE so it won't crash if they already exist
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('doc123', 'admin', 'DOCTOR');");
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('hr123', 'admin', 'HR');");

            System.out.println("Database initialization complete. All tables and users verified!");

        } catch (SQLException e) {
            System.err.println("Initialization failed: " + e.getMessage());
        }
    }

    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient p = new Patient();

                //Loading basic info
                p.setName(rs.getString("name"));
                p.setFayda(rs.getString("fayda"));
                p.setAssignedDoctor(rs.getString("doctor_name"));
                p.setPaymentStatus(rs.getString("status"));
                p.setDob(rs.getString("dob"));
                p.setGender(rs.getString("gender"));
                p.setContact(rs.getString("contact"));
                p.setDiagnosis(rs.getString("diagnosis"));
                p.setTreatment(rs.getString("treatment"));
                p.setPrescription(rs.getString("prescription"));
                p.setAppointmentDate(rs.getString("appt_date"));
                p.setRegisteredDate(rs.getString("reg_date"));
                p.setLastVisit(rs.getString("last_visit"));
                p.setPaymentAmount(rs.getString("payment"));
                p.setBalanceOwed(rs.getDouble("balance_owed"));

                list.add(p);
            }
        } catch (SQLException e) {
            System.err.println("CRITICAL: Database Load Failed!");
            e.printStackTrace();
        }
        return list;
    }

    public static void savePatient(Patient p) {
        String sql = "INSERT INTO patients (fayda, name, dob, gender, contact, status, " +
                "doctor_name, diagnosis, treatment, prescription, appt_date, " +
                "payment, reg_date, balance_owed, last_visit) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getFayda());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getDob());
            pstmt.setString(4, p.getGender());
            pstmt.setString(5, p.getContact());
            pstmt.setString(6, p.getPaymentStatus());
            pstmt.setString(7, p.getAssignedDoctor());
            pstmt.setString(8, p.getDiagnosis());
            pstmt.setString(9, p.getTreatment());
            pstmt.setString(10, p.getPrescription());
            pstmt.setString(11, p.getAppointmentDate());
            pstmt.setString(12, p.getPaymentAmount());
            pstmt.setString(13, p.getRegisteredDate());
            pstmt.setDouble(14, p.getBalanceOwed());
            pstmt.setString(15, p.getLastVisit());

            pstmt.executeUpdate();
            System.out.println("SAVE SUCCESS: Patient " + p.getName() + " added to database.");
        } catch (SQLException e) {
            System.err.println("SAVE ERROR: Could not save patient. Ensure DB schema matches!");
            e.printStackTrace();
        }
    }
    public static void updatePatient(Patient p) {
        String sql = "UPDATE patients SET name=?, dob=?, gender=?, contact=?, status=?, " +
                "doctor_name=?, diagnosis=?, treatment=?, prescription=?, appt_date=?, " +
                "payment=?, balance_owed=?, reg_date=?, last_visit=? WHERE fayda=?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDob());
            pstmt.setString(3, p.getGender());
            pstmt.setString(4, p.getContact());
            pstmt.setString(5, p.getPaymentStatus());
            pstmt.setString(6, p.getAssignedDoctor());
            pstmt.setString(7, p.getDiagnosis());
            pstmt.setString(8, p.getTreatment());
            pstmt.setString(9, p.getPrescription());
            pstmt.setString(10, p.getAppointmentDate());
            pstmt.setString(11, p.getPaymentAmount());
            pstmt.setDouble(12, p.getBalanceOwed());
            pstmt.setString(13, p.getRegisteredDate());
            pstmt.setString(14, p.getLastVisit());
            pstmt.setString(15, p.getFayda());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Update Status: " + (rowsAffected > 0 ? "Success" : "No patient found with Fayda: " + p.getFayda()));

        } catch (SQLException e) {
            System.err.println("SQL ERROR in updatePatient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void updateBillingInfo(String faydaID, String amount, String status) {
        String sql = "UPDATE patients SET payment_amount = ?, payment_status = ? WHERE fayda = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, amount);
                pstmt.setString(2, status);
                pstmt.setString(3, faydaID);

                int affected = pstmt.executeUpdate();
                conn.commit();

                System.out.println("BILLING DEBUG: Updated [" + faydaID + "] - Status: " + status + " - Rows: " + affected);
            } catch (SQLException e) {
                conn.rollback(); // Undo if something breaks
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addLabResult(String patientId, String test, String result, String date) {
        String sql = "INSERT INTO lab_results(patient_id, test_name, result_value, test_date) VALUES(?,?,?,?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            pstmt.setString(2, test);
            pstmt.setString(3, result);
            pstmt.setString(4, date);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Lab Save Error: " + e.getMessage());
        }
    }

    public static void deletePatient(String fayda) {
        String sql = "DELETE FROM patients WHERE fayda = ?"; // Use fayda, not id
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Delete failed: " + e.getMessage());
        }
    }
    public static List<LabResult> getLabResults(String patientId) {
        List<LabResult> results = new ArrayList<>();
        String sql = "SELECT test_name, result_value, test_date FROM lab_results WHERE patient_id = ?";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                results.add(new LabResult(rs.getString("test_name"), rs.getString("result_value"), rs.getString("test_date")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return results;
    }
    public static void deleteLabResult(String patientId, String testName) {
        String sql = "DELETE FROM lab_results WHERE patient_id = ? AND test_name = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            pstmt.setString(2, testName);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public static String generateUniquePatientID() {
        String newID = "PT-" + (int)(Math.random() * 90000 + 10000);
        return newID;

    }
    public static boolean verifyLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static Patient getFullMedicalHistory(String faydaId) {
        Patient patient = null;
        String patientSql = "SELECT * FROM patients WHERE fayda = ?";
        String labsSql = "SELECT * FROM lab_results WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmtPatient = conn.prepareStatement(patientSql)) {

            //Fetch Patient Info
            pstmtPatient.setString(1, faydaId);
            ResultSet rs = pstmtPatient.executeQuery();

            if (rs.next()) {
                patient = new Patient();
                patient.setName(rs.getString("name"));
                patient.setFayda(rs.getString("fayda"));
                patient.setDiagnosis(rs.getString("diagnosis"));
                patient.setTreatment(rs.getString("treatment"));
                patient.setPrescription(rs.getString("prescription"));

                //Fetch Lab Results for this patient
                try (PreparedStatement pstmtLabs = conn.prepareStatement(labsSql)) {
                    pstmtLabs.setString(1, faydaId);
                    ResultSet rsLabs = pstmtLabs.executeQuery();

                    while (rsLabs.next()) {
                        LabResult lab = new LabResult(
                                rsLabs.getString("test_name"),
                                rsLabs.getString("result_value"),
                                rsLabs.getString("test_date")
                        );
                        patient.getLabResults().add(lab);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medical history: " + e.getMessage());
        }
        return patient;
    }
    public static ObservableList<LabResult> getPatientLabs(String faydaId) {
        ObservableList<LabResult> labs = FXCollections.observableArrayList();
        String sql = "SELECT * FROM lab_results WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, faydaId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                labs.add(new LabResult(
                        rs.getString("test_name"),
                        rs.getString("result_value"),
                        rs.getString("test_date")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return labs;
    }
    public static void updatePaymentStatus(String faydaId, String newStatus) {
        String sql = "UPDATE patients SET payment_status = ? WHERE fayda = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start a manual transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, faydaId);

                int affected = pstmt.executeUpdate();
                conn.commit(); // FORCE the data onto the hard driv

                System.out.println("CRITICAL DEBUG: Tried updating [" + faydaId + "]");
                System.out.println("CRITICAL DEBUG: Rows actually changed: " + affected);

            } catch (SQLException e) {
                conn.rollback(); // If it fails, undo
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Registration error: " + e.getMessage());
            return false;
        }
    }
    public static String verifyUserRole(String username, String password) {
        String sql = "SELECT role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("role");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}