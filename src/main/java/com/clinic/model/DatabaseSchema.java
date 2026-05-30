package com.clinic.model;

public class DatabaseSchema {

    // Core table creation SQL strings expected by DatabaseManager
    public static final String CREATE_PATIENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS patients (" +
                    "fayda TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "dob TEXT, " +
                    "gender TEXT, " +
                    "contact TEXT, " +
                    "status TEXT DEFAULT 'Pending', " +
                    "doctor_name TEXT, " +
                    "reg_date TEXT, " +
                    "diagnosis TEXT, " +
                    "treatment TEXT, " +
                    "prescription TEXT, " +
                    "appt_date TEXT" +
                    ");";

    public static final String CREATE_USERS_TABLE =
            "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL" +
                    ");";

    public static final String CREATE_LAB_RESULTS_TABLE =
            "CREATE TABLE IF NOT EXISTS lab_results (" +
                    "order_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id TEXT, " +
                    "test_name TEXT, " +
                    "result_value TEXT, " +
                    "test_date TEXT, " +
                    "ordering_doctor TEXT" +
                    ");";

    public static final String CREATE_DOCTORS_TABLE =
            "CREATE TABLE IF NOT EXISTS doctors (" +
                    "id TEXT PRIMARY KEY, " +
                    "name TEXT NOT NULL, " +
                    "specialization TEXT" +
                    ");";

    public static final String CREATE_APPOINTMENTS_TABLE =
            "CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id TEXT, " +
                    "appointment_date TEXT, " +
                    "status TEXT" +
                    ");";

    public static final String CREATE_ENCOUNTERS_TABLE =
            "CREATE TABLE IF NOT EXISTS encounters (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "fayda TEXT, " +
                    "diagnosis TEXT, " +
                    "treatment TEXT, " +
                    "prescription TEXT, " +
                    "date DATETIME)";

        public static final String CREATE_BILLING_TABLE =
                "CREATE TABLE IF NOT EXISTS billing (" +
                        "    id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "    patient_fayda TEXT NOT NULL, " +
                        "    item_description TEXT NOT NULL, " +
                        "    amount_paid REAL NOT NULL, " +
                        "    payment_date TEXT NOT NULL, " +
                        "    FOREIGN KEY (patient_fayda) REFERENCES patients(fayda) ON DELETE CASCADE" +
                        ");";

    public static final String CREATE_INVENTORY_TABLE =
            "CREATE TABLE IF NOT EXISTS inventory (" +
                    "item_id TEXT PRIMARY KEY, " +
                    "item_name TEXT NOT NULL, " +
                    "quantity INTEGER" +
                    ");";

    // Structural patches array for maintenance updates
    public static final String[] STRUCTURAL_PATCHES = {
            "ALTER TABLE patients ADD COLUMN diagnosis TEXT;",
            "ALTER TABLE patients ADD COLUMN treatment TEXT;",
            "ALTER TABLE patients ADD COLUMN prescription TEXT;",
            "ALTER TABLE patients ADD COLUMN appt_date TEXT;",
            // Safety structural check to append ordering tracking columns safely
            "ALTER TABLE lab_results ADD COLUMN ordering_doctor TEXT;"
    };

    // Default system seed strings
    public static final String INSERT_DEFAULT_DOCTOR =
            "INSERT OR IGNORE INTO users (username, password, role) VALUES ('doctor', 'doc123', 'DOCTOR');";

    public static final String INSERT_DEFAULT_HR =
            "INSERT OR IGNORE INTO users (username, password, role) VALUES ('hr_admin', 'hr123', 'HR');";

}