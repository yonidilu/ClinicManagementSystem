package com.clinic.model;

import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.stage.Stage;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:clinic.db";

    // Standard connection method used by all DB tasks
    private static Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
        return conn;
    }


    public static void initializeDatabase() {
        // 1. Core Patient Table structure
        String createTableSQL = "CREATE TABLE IF NOT EXISTS patients ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "dob TEXT, "
                + "gender TEXT, "
                + "contact TEXT, "
                + "status TEXT, "
                + "fayda TEXT NOT NULL, "
                + "doctor_name TEXT, "
                + "diagnosis TEXT, "
                + "treatment TEXT, "
                + "prescription TEXT, "
                + "appt_date TEXT, "
                + "payment TEXT, "
                + "reg_date TEXT"
                + ");";

        // 2. Lab Results Table structure
        String labTableSql = "CREATE TABLE IF NOT EXISTS lab_results (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "patient_id TEXT NOT NULL," +
                "test_name TEXT," +
                "result_value TEXT," +
                "test_date TEXT," +
                "FOREIGN KEY (patient_id) REFERENCES patients(fayda)" +
                ");";

        // 3. Users Table structure
        String userTableSql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT UNIQUE," +
                "password TEXT" +
                ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            // Initialize all core tables
            stmt.execute(createTableSQL);
            stmt.execute(labTableSql);
            stmt.execute(userTableSql);

            // --- NEW CREDENTIALS LOGIC ---
            // First, we clear old test users to prevent 'admin' from working
            stmt.execute("DELETE FROM users");

            // Insert your specific Doctor credentials
            stmt.execute("INSERT INTO users (username, password) VALUES ('Admin778', 'doc123')");
            System.out.println("Authorized User set: Admin778");

            // 4. Billing Column Patches
            try {
                stmt.execute("ALTER TABLE patients ADD COLUMN payment_status TEXT;");
            } catch (SQLException e) { /* Column already exists */ }

            try {
                stmt.execute("ALTER TABLE patients ADD COLUMN payment_amount TEXT;");
            } catch (SQLException e) { /* Column already exists */ }

            System.out.println("Database, Lab, and Login tables ready!");
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
                // Matches your new Patient constructor order
                list.add(new Patient(
                        rs.getString("name"), rs.getString("dob"), rs.getString("gender"),
                        rs.getString("contact"), rs.getString("status"), rs.getString("fayda"),
                        rs.getString("doctor_name"), rs.getString("diagnosis"),
                        rs.getString("treatment"), rs.getString("prescription"),
                        rs.getString("appt_date"), rs.getString("payment"), rs.getString("reg_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void savePatient(Patient p) {
        String sql = "INSERT INTO patients(name, dob, gender, contact, status, fayda, " +
                "doctor_name, diagnosis, treatment, prescription, appt_date, payment, reg_date) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDob());
            pstmt.setString(3, p.getGender());
            pstmt.setString(4, p.getContact());
            pstmt.setString(5, p.getStatus());
            pstmt.setString(6, p.getFayda());
            pstmt.setString(7, p.getDoctorName());
            pstmt.setString(8, p.getDiagnosis());
            pstmt.setString(9, p.getTreatment());
            pstmt.setString(10, p.getPrescription());
            pstmt.setString(11, p.getApptDate());
            pstmt.setString(12, p.getPayment());
            pstmt.setString(13, p.getRegDate());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void updatePatient(Patient p) {
        String sql = "UPDATE patients SET name = ?, dob = ?, gender = ?, contact = ?, " +
                "status = ?, doctor_name = ?, diagnosis = ?, treatment = ?, " +
                "prescription = ?, appt_date = ?, payment = ? WHERE fayda = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getDob());
            pstmt.setString(3, p.getGender());
            pstmt.setString(4, p.getContact());
            pstmt.setString(5, p.getStatus());
            pstmt.setString(6, p.getDoctorName());
            pstmt.setString(7, p.getDiagnosis());
            pstmt.setString(8, p.getTreatment());
            pstmt.setString(9, p.getPrescription());
            pstmt.setString(10, p.getApptDate());
            pstmt.setString(11, p.getPayment());
            pstmt.setString(12, p.getFayda());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // This goes in DatabaseManager.java
    public static void updateBillingInfo(String faydaID, String amount, String status) {
        // Match 'fayda' instead of 'fayda_id'
        String sql = "UPDATE patients SET payment_amount = ?, payment_status = ? WHERE fayda = ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, amount);
            pstmt.setString(2, status);
            pstmt.setString(3, faydaID);

            pstmt.executeUpdate();
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


}