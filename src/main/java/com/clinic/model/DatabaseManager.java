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
    private static final String URL = "jdbc:sqlite:C:/ClinicData/clinic.db";// Standard connection method used by all DB tasks
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            // Add this line to ensure changes are written immediately
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

            // 1. Create ALL Tables first to avoid foreign key or "missing table" errors

            // Patients Table
            stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "fayda TEXT PRIMARY KEY, name TEXT, dob TEXT, gender TEXT, " +
                    "contact TEXT, status TEXT, doctor_name TEXT, diagnosis TEXT, " +
                    "treatment TEXT, prescription TEXT, appt_date TEXT, payment TEXT, " +
                    "reg_date TEXT, last_visit TEXT, balance_owed REAL DEFAULT 0.0);");

            // Users Table (CRITICAL for login)
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

            stmt.execute("CREATE TABLE IF NOT EXISTS inventory (" +
                    "inventory_id INTEGER PRIMARY KEY AUTOINCREMENT, item_name TEXT, " +
                    "quantity INTEGER, expiration_date TEXT);");

            // 2. RUN PATCHES (For existing databases missing columns)
            String[] patches = {
                    "ALTER TABLE patients ADD COLUMN payment_status TEXT;",
                    "ALTER TABLE patients ADD COLUMN payment_amount TEXT;",
                    "ALTER TABLE patients ADD COLUMN last_visit TEXT;",
                    "ALTER TABLE patients ADD COLUMN reg_date TEXT;"
            };

            for (String patch : patches) {
                try {
                    stmt.execute(patch);
                } catch (SQLException e) {
                    // Column likely exists already; ignore safely
                }
            }

            // 3. SEED USERS (The Master Keys)
            // We use INSERT OR IGNORE so it won't crash if they already exist
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

        try (Connection conn = connect(); // Using your existing connect() method
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                // 1. Create the patient using the primary constructor
                Patient p = new Patient(
                        rs.getString("name"),
                        rs.getString("dob"),
                        rs.getString("gender"),
                        rs.getString("contact"),
                        rs.getString("status"),
                        rs.getString("fayda"),
                        rs.getString("doctor_name"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("prescription"),
                        rs.getString("appt_date"),
                        rs.getString("payment"),
                        rs.getString("reg_date")
                );

                // 2. Map the "Problem" columns safely
                // Using a helper or checking if column exists prevents the 'no such column' crash
                try {
                    p.setLastVisit(rs.getString("last_visit"));
                    p.setPaymentAmount(rs.getString("payment_amount"));
                    p.setPaymentStatus(rs.getString("payment_status"));
                    p.setBalanceOwed(rs.getDouble("balance_owed"));
                } catch (SQLException e) {
                    // If these columns aren't found, we log it but don't stop the app!
                    System.out.println("Note: Some optional columns were not found in the result set.");
                }

                list.add(p);
            }
            System.out.println("LOAD DEBUG: Successfully loaded " + list.size() + " patients.");
        } catch (SQLException e) {
            System.err.println("CRITICAL: Database Load Failed!");
            e.printStackTrace();
        }
        return list;
    }

    public static void savePatient(Patient p) {
        // 1. Updated SQL to include 'balance_owed' and 'doctor_name'
        String sql = "INSERT INTO patients (fayda, name, dob, gender, contact, status, " +
                "doctor_name, diagnosis, treatment, prescription, appt_date, " +
                "payment, reg_date, balance_owed) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getFayda());
            pstmt.setString(2, p.getName());
            pstmt.setString(3, p.getDob());
            pstmt.setString(4, p.getGender());
            pstmt.setString(5, p.getContact());
            pstmt.setString(6, p.getPaymentStatus());
            pstmt.setString(7, p.getAssignedDoctor()); // From 'doctor_name' field
            pstmt.setString(8, p.getDiagnosis());
            pstmt.setString(9, p.getTreatment());
            pstmt.setString(10, p.getPrescription());
            pstmt.setString(11, p.getAppointmentDate());
            pstmt.setString(12, p.getPaymentAmount());
            pstmt.setString(13, p.getRegisteredDate()); // This is also your 'Last Visit'

            // 2. Save the total lab/service costs as the initial balance
            pstmt.setDouble(14, p.getBalanceOwed());

            pstmt.executeUpdate();
            System.out.println("SAVE SUCCESS: Patient " + p.getName() + " stored in " + URL);
        } catch (SQLException e) {
            System.err.println("SAVE ERROR: Could not save patient. Check column names!");
            e.printStackTrace();
        }
    }

    public static void updatePatient(Patient p) {
        String sql = "UPDATE patients SET name=?, dob=?, gender=?, contact=?, status=?, " +
                "doctor_name=?, diagnosis=?, treatment=?, prescription=?, appt_date=?, " +
                "payment=?, balance_owed=?, reg_date=?, last_visit=? WHERE fayda=?";

        System.out.println("DEBUG: Saving Patient: " + p.getName() + " | RegDate: " + p.getRegisteredDate());

        try (Connection conn = DriverManager.getConnection(URL);
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
            pstmt.setString(13, p.getRegisteredDate()); // Map these clearly!
            pstmt.setString(14, p.getLastVisit());
            pstmt.setString(15, p.getFayda());

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("DEBUG: Rows Updated: " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This goes in DatabaseManager.java
    public static void updateBillingInfo(String faydaID, String amount, String status) {
        String sql = "UPDATE patients SET payment_amount = ?, payment_status = ? WHERE fayda = ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Step 1: Stop auto-pilot

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, amount);
                pstmt.setString(2, status);
                pstmt.setString(3, faydaID);

                int affected = pstmt.executeUpdate();
                conn.commit(); // Step 2: Force the save to the physical disk

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
        String sql = "DELETE FROM patients WHERE fayda = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
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
        String newID = "PT-" + (int)(Math.random() * 90000 + 10000); // Simple random ID for now

        // In a real system, you'd query the DB here to ensure 'newID' doesn't exist yet
        return newID;

    }
    public static boolean verifyLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // Returns true only if Admin778/doc123 matches
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    // Add this to DatabaseManager.java
    public static Patient getFullMedicalHistory(String faydaId) {
        Patient patient = null;
        String patientSql = "SELECT * FROM patients WHERE fayda = ?";
        String labsSql = "SELECT * FROM lab_results WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmtPatient = conn.prepareStatement(patientSql)) {

            // 1. Fetch Patient Info
            pstmtPatient.setString(1, faydaId);
            ResultSet rs = pstmtPatient.executeQuery();

            if (rs.next()) {
                patient = new Patient();
                patient.setName(rs.getString("name"));
                patient.setFayda(rs.getString("fayda"));
                patient.setDiagnosis(rs.getString("diagnosis"));
                patient.setTreatment(rs.getString("treatment"));
                patient.setPrescription(rs.getString("prescription"));
                // Add any other fields you need here...

                // 2. Fetch Lab Results for this patient
                try (PreparedStatement pstmtLabs = conn.prepareStatement(labsSql)) {
                    pstmtLabs.setString(1, faydaId);
                    ResultSet rsLabs = pstmtLabs.executeQuery();

                    while (rsLabs.next()) {
                        LabResult lab = new LabResult(
                                rsLabs.getString("test_name"),
                                rsLabs.getString("result_value"),
                                rsLabs.getString("test_date")
                        );
                        patient.getLabResults().add(lab); // Assuming your Patient class has a List<LabResult>
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching medical history: " + e.getMessage());
        }

        return patient; // This satisfies the 'return value expected' error!
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

        // We manually manage the connection here to ensure a COMMIT happens
        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // Start a manual transaction

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, newStatus);
                pstmt.setString(2, faydaId);

                int affected = pstmt.executeUpdate();
                conn.commit(); // FORCE the data onto the hard drive now

                System.out.println("CRITICAL DEBUG: Tried updating [" + faydaId + "]");
                System.out.println("CRITICAL DEBUG: Rows actually changed: " + affected);

            } catch (SQLException e) {
                conn.rollback(); // If it fails, undo everything
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static boolean registerUser(String username, String password, String role) {
        String sql = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

        try (Connection conn = connect(); // Ensure you have a connect() method
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
                return rs.getString("role"); // Returns "HR" or "DOCTOR"
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // Login failed if no user found
    }

    public static Connection getConnection() throws SQLException {
        // This ensures MainController can find the method it's looking for!
        return DriverManager.getConnection(URL);
    }

}