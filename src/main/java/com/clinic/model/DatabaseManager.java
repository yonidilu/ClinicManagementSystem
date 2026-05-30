package com.clinic.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    // SQLite production absolute system path location string references
    private static final String URL = "jdbc:sqlite:clinic.db";

    // Global Operational Context State Caches
    private static String currentUserSessionToken = "";
    private static String currentUserRoleBoundary = "";

    // Shared global transfer context used to safely pass patient references across controllers
    public static Patient selectedPatientSessionContext = null;

    public static void setCurrentSession(String username, String role) {
        currentUserSessionToken = username;
        currentUserRoleBoundary = role;
        System.out.println("Session State Updated: Active User Context locked to -> [" + username + "] Role Boundary -> [" + role + "]");
    }

    public static String getCurrentUserSession() {
        return currentUserSessionToken;
    }

    public static String getCurrentUserRole() {
        return currentUserRoleBoundary;
    }

    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
            conn.setAutoCommit(true);
        } catch (SQLException e) {
            System.err.println("Database connection failed execution trace: " + e.getMessage());
        }
        return conn;
    }

    public static Connection getConnection() throws SQLException {
        return connect();
    }

    public static void initializeDatabase() {
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            System.out.println("Initializing Database Engine: Compiling baseline schema structures...");

            // Build Tables safely from Schema specification file definitions
            stmt.execute(DatabaseSchema.CREATE_PATIENTS_TABLE);
            stmt.execute(DatabaseSchema.CREATE_USERS_TABLE);
            stmt.execute(DatabaseSchema.CREATE_LAB_RESULTS_TABLE);
            stmt.execute(DatabaseSchema.CREATE_DOCTORS_TABLE);
            stmt.execute(DatabaseSchema.CREATE_APPOINTMENTS_TABLE);
            stmt.execute(DatabaseSchema.CREATE_INVENTORY_TABLE);
            stmt.execute(DatabaseSchema.CREATE_ENCOUNTERS_TABLE);

            // NEW: Automatically provision the new financial tracking subsystem table
            try {
                stmt.execute(DatabaseSchema.CREATE_BILLING_TABLE);
                System.out.println("Database Engine: Billing ledger tables validated smoothly.");
            } catch (SQLException e) {
                System.err.println("Notice: Billing table initialization alert: " + e.getMessage());
            }

            // Execute schema migration maintenance patches cleanly
            if (DatabaseSchema.STRUCTURAL_PATCHES != null) {
                for (String patch : DatabaseSchema.STRUCTURAL_PATCHES) {
                    try {
                        stmt.execute(patch);
                    } catch (SQLException e) {
                        // Catch silently if columns are already registered in schema maps
                    }
                }
            }

            // Insert default foundational logins safely
            try { stmt.execute(DatabaseSchema.INSERT_DEFAULT_DOCTOR); } catch (Exception e) {}
            try { stmt.execute(DatabaseSchema.INSERT_DEFAULT_HR); } catch (Exception e) {}

            // GUARANTEED TEST LOGIN PROFILES: Seed exactly 3 accounts matching interface boundaries
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('doctor', 'doc123', 'DOCTOR');");
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('receptionist', 'rec123', 'RECEPTION');");
            stmt.execute("INSERT OR IGNORE INTO users (username, password, role) VALUES ('labtech', 'lab123', 'LAB_UNIT');");

            System.out.println("Database Engine Bootstrapping Complete: Core authentication keys loaded successfully.");

        } catch (SQLException e) {
            System.err.println("Fatal system initialization SQL crash occurred: " + e.getMessage());
        }
    }
    public static boolean savePatient(Patient patient) {
        String sql = "INSERT OR REPLACE INTO patients (fayda, name, dob, gender, contact, status, doctor_name, reg_date, diagnosis, treatment, prescription, appt_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getFayda());
            pstmt.setString(2, patient.getName());
            pstmt.setString(3, patient.getDob());
            pstmt.setString(4, patient.getGender());
            pstmt.setString(5, patient.getContact());
            pstmt.setString(6, patient.getPaymentStatus());
            pstmt.setString(7, patient.getAssignedDoctor());
            pstmt.setString(8, patient.getRegisteredDate());
            pstmt.setString(9, patient.getDiagnosis());
            pstmt.setString(10, patient.getTreatment());
            pstmt.setString(11, patient.getPrescription());
            pstmt.setString(12, patient.getAppointmentDate());

            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Database patient profile serialization failure: " + e.getMessage());
            return false;
        }
    }

    public static List<Patient> getAllPatients() {
        List<Patient> directoryList = new ArrayList<>();

        // UPDATED: By removing the WHERE clause, the system now pulls
        // the complete registry for any user with valid access permissions.
        String sql = "SELECT * FROM patients ORDER BY reg_date DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Patient p = new Patient();
                p.setFayda(rs.getString("fayda"));
                p.setName(rs.getString("name"));
                p.setDob(rs.getString("dob"));
                p.setGender(rs.getString("gender"));
                p.setContact(rs.getString("contact"));
                p.setPaymentStatus(rs.getString("status"));
                p.setAssignedDoctor(rs.getString("doctor_name"));
                p.setRegisteredDate(rs.getString("reg_date"));

                directoryList.add(p);
            }
        } catch (SQLException e) {
            System.err.println("Directory registry fetch failed: " + e.getMessage());
        }
        return directoryList;
    }

    public static List<LabResult> getAllLabResults() {
        List<LabResult> allOrders = new ArrayList<>();
        String sql = "SELECT * FROM lab_results ORDER BY test_date DESC";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                allOrders.add(new LabResult(
                        rs.getString("patient_id"),
                        rs.getString("test_name"),
                        rs.getString("result_value"),
                        rs.getString("test_date"),
                        rs.getString("ordering_doctor")
                ));
            }
        } catch (SQLException e) {
            // Logging the specific error helps you identify if it's a table name
            // or column name mismatch in your clinic.db
            System.err.println("Database fetch failure: " + e.getMessage());
        }
        return allOrders;
    }

    public static void updatePatient(Patient patient) {
        String sql = "UPDATE patients SET name=?, dob=?, gender=?, contact=?, status=?, doctor_name=?, diagnosis=?, treatment=?, prescription=?, appt_date=? WHERE fayda=?";

        System.out.println("DEBUG: Attempting to update ID: " + patient.getFayda()); // Check this ID

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getName());
            pstmt.setString(2, patient.getDob());
            pstmt.setString(3, patient.getGender());
            pstmt.setString(4, patient.getContact());
            pstmt.setString(5, patient.getPaymentStatus());
            pstmt.setString(6, patient.getAssignedDoctor());
            pstmt.setString(7, patient.getDiagnosis());
            pstmt.setString(8, patient.getTreatment());
            pstmt.setString(9, patient.getPrescription());
            pstmt.setString(10, patient.getAppointmentDate());
            pstmt.setString(11, patient.getFayda());

            int rows = pstmt.executeUpdate();
            System.out.println("DEBUG: Rows updated: " + rows); // This is the key!

        } catch (SQLException e) {
            System.err.println("DEBUG: SQL Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static boolean orderLabTest(String patientFayda, String testName, String doctorId) {
        String sql = "INSERT INTO lab_results (patient_id, test_name, result_value, test_date, ordering_doctor) VALUES (?, ?, 'PENDING', ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientFayda);
            pstmt.setString(2, testName);
            pstmt.setString(3, java.time.LocalDate.now().toString());
            // Implicitly tags the active authenticated doctor context signature
            pstmt.setString(4, doctorId);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Laboratory diagnostic placement subsystem allocation failed: " + e.getMessage());
            return false;
        }
    }

    public static ObservableList<LabResultQueueItem> getAllPendingLabOrders() {
        ObservableList<LabResultQueueItem> orderQueue = FXCollections.observableArrayList();
        // Inner join captures name contexts securely while resolving reference rows
        String sql = "SELECT l.patient_id, p.name AS patient_name, l.test_name, l.result_value, l.test_date, l.ordering_doctor " +
                "FROM lab_results l " +
                "JOIN patients p ON l.patient_id = p.fayda " +
                "ORDER BY l.test_date DESC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                orderQueue.add(new LabResultQueueItem(
                        rs.getString("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("test_name"),
                        rs.getString("result_value"),
                        rs.getString("test_date"),
                        rs.getString("ordering_doctor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Failed to parse full global lab orders matrix stream: " + e.getMessage());
        }
        return orderQueue;
    }

    public static boolean updateLabResultValue(String patientFayda, String testName, String resultVal, String completionDate) {
        String sql = "UPDATE lab_results SET result_value = ?, test_date = ? WHERE patient_id = ? AND test_name = ? AND result_value = 'PENDING'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, resultVal);
            pstmt.setString(2, completionDate);
            pstmt.setString(3, patientFayda);
            pstmt.setString(4, testName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to write clinical laboratory measurements map profile: " + e.getMessage());
        }
        return false;
    }
    // This is the bridge method required by BillingController
    public static ObservableList<LabResult> getPatientLabs(String patientFayda) {
        ObservableList<LabResult> results = FXCollections.observableArrayList();
        String sql = "SELECT * FROM lab_results WHERE patient_id = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patientFayda);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new LabResult(
                        rs.getString("patient_id"),
                        rs.getString("test_name"),
                        rs.getString("result_value"),
                        rs.getString("test_date"),
                        rs.getString("ordering_doctor")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Database fetch error in getPatientLabs: " + e.getMessage());
        }
        return results;
    }

    public static boolean registerUser(String username, String password, String role) {
        String sql = "INSERT OR IGNORE INTO users (username, password, role) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.setString(3, role);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Identity enrollment structural entry error code: " + e.getMessage());
            return false;
        }
    }

    public static String verifyUserRole(String username, String password) {
        // Force inputs to trim out any trailing or leading invisible spaces
        if (username == null || password == null) return null;
        String cleanUser = username.trim();
        String cleanPass = password.trim();

        // Using LOWER() in SQL ensures case-insensitive verification
        String sql = "SELECT role FROM users WHERE LOWER(username) = LOWER(?) AND password = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cleanUser);
            pstmt.setString(2, cleanPass);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String standardRole = rs.getString("role");
                    System.out.println("Auth Success: Found user matching role [" + standardRole + "]");
                    return standardRole;
                }
            }
        } catch (SQLException e) {
            System.err.println("Role security validation query vector failure trace: " + e.getMessage());
        }

        System.out.println("Auth Failure: No matching credentials found for username: [" + cleanUser + "]");
        return null;
    }
    public static Patient getPatientByFayda(String fayda) {
        String sql = "SELECT * FROM patients WHERE fayda = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Patient(
                        rs.getString("name"), rs.getString("dob"), rs.getString("gender"),
                        rs.getString("contact"), rs.getString("status"), rs.getString("doctor_name"),
                        rs.getString("diagnosis"), rs.getString("treatment"), rs.getString("prescription"),
                        rs.getString("appt_date"), rs.getString("fayda")
                );
            }
        } catch (SQLException e) {
            System.err.println("Database fetch error: " + e.getMessage());
        }
        return null;
    }
    public static void addEncounter(String fayda, String diag, String treat, String presc) {
        String sql = "INSERT INTO encounters (fayda, diagnosis, treatment, prescription, date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            pstmt.setString(2, diag);
            pstmt.setString(3, treat);
            pstmt.setString(4, presc);
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    public static ObservableList<Encounter> getPatientEncounters(String fayda) {
        ObservableList<Encounter> list = javafx.collections.FXCollections.observableArrayList();
        String sql = "SELECT date, diagnosis, treatment, prescription FROM encounters WHERE fayda = ?";

        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                list.add(new Encounter(
                        rs.getString("date"),
                        rs.getString("diagnosis"),
                        rs.getString("treatment"),
                        rs.getString("prescription")
                ));
            }
        } catch (SQLException e) { System.err.println("Error saving to encounter history: " + e.getMessage());}
        return list;
    }
}