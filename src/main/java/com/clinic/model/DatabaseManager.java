package com.clinic.model;


import com.clinic.model.Patient;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.stage.Stage;

import java.awt.event.ActionEvent;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static java.sql.DriverManager.getConnection;


public class DatabaseManager {
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
    // Ensure you have 13 column names and 13 question marks
    String sql = "INSERT INTO patients(name, ailment, gender, contact, status, fayda, " +
            "doctor_name, diagnosis, treatment, prescription, appt_date, payment, reg_date) " +
            "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";
    private static final String URL = "jdbc:sqlite:clinic.db";

    public static void initializeDatabase() {
        // The "Chief Surgeon" of SQL strings—everything in one place!
        String createTableSQL = "CREATE TABLE IF NOT EXISTS patients ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
                + "name TEXT NOT NULL, "
                + "ailment TEXT, "
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

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createTableSQL);
            System.out.println("Database initialized with full medical record support!");

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
        }
    }

    @FXML
    private void onCloseClick(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }

    public static void addPatient(String name, String ailment, int fayda ) {
        String sql = "INSERT INTO patients(name, ailment, fayda) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ailment);
            pstmt.setString(3, String.valueOf(fayda));
            pstmt.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public static List<Patient> getAllPatients() {
        List<Patient> list = new ArrayList<>();
        String sql = "SELECT * FROM patients";
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new Patient(
                        rs.getString("name"), rs.getString("ailment"), rs.getString("gender"),
                        rs.getString("contact"), rs.getString("status"), rs.getString("fayda"),
                        rs.getString("doctor_name"), rs.getString("diagnosis"), // Pull these from DB!
                        rs.getString("treatment"), rs.getString("prescription"),
                        rs.getString("appt_date"), rs.getString("payment"), rs.getString("reg_date")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void deletePatient(String fayda) {
        String sql = "DELETE FROM patients WHERE fayda = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, fayda);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public static void savePatient(Patient p) {
        // You must have 13 column names and 13 question marks!
        String sql = "INSERT INTO patients(name, ailment, gender, contact, status, fayda, " +
                "doctor_name, diagnosis, treatment, prescription, appt_date, payment, reg_date) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, p.getName());
            pstmt.setString(2, p.getAilment());
            pstmt.setString(3, p.getGender());
            pstmt.setString(4, p.getContact());
            pstmt.setString(5, p.getStatus());
            pstmt.setString(6, p.getFayda());
            pstmt.setString(7, p.getDoctorName()); // Now this won't crash!
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
}