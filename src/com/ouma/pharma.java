package com.ouma;
// Ouma Emmanuel Douglas
// ICS
// 168053
// 19/11/2023

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class pharma {

    private static final String DB_NAME = "db_ouma_emmanuel_168053";
    private static final String TABLE_NAME = "drugs";

    private JFrame frame;
    private JPanel mainPanel;
    private PharmacistPanel pharmacistPanel;
    private DoctorPanel doctorPanel;
    private JPanel cardPanel; // Panel to hold cards with different views
    private ArrayList<Drug> drugStock;
    private Map<String, Integer> prescription;
    private Connection connection;

    public pharma() {
        drugStock = new ArrayList<>();
        prescription = new HashMap<>();

        // Initialize database connection
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            String jdbcUrl = "jdbc:mysql://localhost:3306/" + DB_NAME;
            String user = "root";
            String password = "Emm@2005anuel";
            connection = DriverManager.getConnection(jdbcUrl, user, password);
            createTable();
            retrieveDrugs(); // Load drugs from the database on startup
        } catch (Exception e) {
            e.printStackTrace();
        }

        frame = new JFrame("Pharma");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
        frame.setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(Color.LIGHT_GRAY);

        // Heading Label
        JLabel headingLabel = new JLabel("Course Correct Medics", SwingConstants.CENTER);
        headingLabel.setFont(new Font("Arial", Font.BOLD, 15));
        mainPanel.add(headingLabel, BorderLayout.NORTH);

        // Buttons Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        JButton pharmacistButton = new JButton("Pharmacist");
        pharmacistButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showPharmacistPanel();
            }
        });
        pharmacistButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pharmacistButton.setPreferredSize(new Dimension(200, 40));

        JButton doctorButton = new JButton("Doctor");
        doctorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showDoctorPanel();
            }
        });
        doctorButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        doctorButton.setPreferredSize(new Dimension(200, 40));

        buttonPanel.add(Box.createVerticalStrut(20)); // Add spacing
        buttonPanel.add(pharmacistButton);
        buttonPanel.add(Box.createVerticalStrut(10)); // Add spacing
        buttonPanel.add(doctorButton);

        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        cardPanel = new JPanel(new CardLayout());
        mainPanel.add(cardPanel, BorderLayout.SOUTH);

        frame.add(mainPanel);
        frame.setVisible(true);
    }

    private void showPharmacistPanel() {
        if (pharmacistPanel == null) {
            pharmacistPanel = new PharmacistPanel(this, connection, drugStock, prescription);
            cardPanel.add(pharmacistPanel.getPanel(), "Pharmacist");
        }
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, "Pharmacist");

        addCopyrightLabel(pharmacistPanel.getPanel());
    }

    private void showDoctorPanel() {
        if (doctorPanel == null) {
            doctorPanel = new DoctorPanel(this, drugStock, prescription);
            cardPanel.add(doctorPanel.getPanel(), "Doctor");
        }
        CardLayout cardLayout = (CardLayout) cardPanel.getLayout();
        cardLayout.show(cardPanel, "Doctor");

        addCopyrightLabel(doctorPanel.getPanel());
    }

    private void addCopyrightLabel(JPanel panel) {
        JLabel copyrightLabel = new JLabel("\u00a9 2023 DOUGLAS", SwingConstants.CENTER);
        copyrightLabel.setFont(new Font("Arial", Font.PLAIN, 10));
        panel.add(copyrightLabel, BorderLayout.SOUTH);
    }

    private void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + TABLE_NAME +
                    " (id INT PRIMARY KEY AUTO_INCREMENT, drug_name VARCHAR(255), patient_name VARCHAR(255))");
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void retrieveDrugs() {
        drugStock.clear();
        try {
            String query = "SELECT * FROM " + TABLE_NAME;
            try (Statement statement = connection.createStatement();
                 ResultSet resultSet = statement.executeQuery(query)) {

                while (resultSet.next()) {
                    String drugName = resultSet.getString("name");
                    drugStock.add(new Drug(drugName));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error retrieving drugs from the database.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new pharma();
            }
        });
    }

    public class Drug {
        private String name;

        public Drug(String name) {
            this.name = name != null ? name : " "; // Ensure name is not null
        }

        public String getName() {
            return name;
        }
    }

    class PharmacistPanel {

        private JPanel panel;
        private pharma parent;
        private Connection connection;
        private ArrayList<Drug> drugStock;
        private Map<String, Integer> prescription;

        public PharmacistPanel(pharma parent, Connection connection, ArrayList<Drug> drugStock, Map<String, Integer> prescription) {
            this.parent = parent;
            this.connection = connection;
            this.drugStock = drugStock;
            this.prescription = prescription;
            initPanel();
        }

        private void initPanel() {
            panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(Color.GRAY);

            JButton addButton = new JButton("Add Drug.");
            addButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    addDrug();
                }
            });

            JButton removeButton = new JButton("Remove Expired Drugs.");
            removeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    removeDrug();
                }
            });

            JButton buyButton = new JButton("Buy Prescribed Drugs For Patient");
            buyButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    buyPrescribedDrugs();
                }
            });

            panel.add(addButton);
            panel.add(removeButton);
            panel.add(buyButton);
        }

        public JPanel getPanel() {
            return panel;
        }

        private void addDrug() {
            String drugName = JOptionPane.showInputDialog(panel, "Enter name of drug and expiry date:");
            if (drugName != null && !drugName.isEmpty()) {
                insertDrug(drugName);
                drugStock.add(new Drug(drugName));

                JOptionPane.showMessageDialog(panel, "Drug added successfully.");
            }
        }

        private void removeDrug() {
            if (drugStock.isEmpty()) {
                JOptionPane.showMessageDialog(panel, "No drugs in stock.");
            } else {
                String[] drugs = drugStock.stream().map(Drug::getName).toArray(String[]::new);
                String selectedDrug = (String) JOptionPane.showInputDialog(panel, "Select a drug to remove:", "Remove Drug",
                        JOptionPane.QUESTION_MESSAGE, null, drugs, drugs[0]);

                if (selectedDrug != null) {
                    deleteDrug(selectedDrug);
                    drugStock.removeIf(drug -> drug.getName().equals(selectedDrug));
                    JOptionPane.showMessageDialog(panel, "Drug removed successfully.");
                }
            }
        }

        private void buyPrescribedDrugs() {
            // Prompt the patient to enter their name
            String patientName = JOptionPane.showInputDialog(panel, "Enter patient's name:");

            if (patientName != null && !patientName.isEmpty()) {
                if (prescription.isEmpty()) {
                    JOptionPane.showMessageDialog(panel, "No prescribed drugs in the prescription.");
                } else {
                    StringBuilder prescriptionList = new StringBuilder("purchase confirmed for " + patientName + ":\n");
                    for (Map.Entry<String, Integer> entry : prescription.entrySet()) {
                        if (entry.getKey().startsWith(patientName + "-")) {
                            String drugName = entry.getKey().substring(patientName.length() + 1);
                            prescriptionList.append(drugName).append(" - Quantity: ").append(entry.getValue()).append("\n");
                        }
                    }
                    JOptionPane.showMessageDialog(panel, prescriptionList.toString());
                }
            } else {
                JOptionPane.showMessageDialog(panel, "Please enter patient's name.");
            }
        }

        private void insertDrug(String drugName) {
            try {
                String query = "INSERT INTO " + TABLE_NAME + " (name) VALUES (?)";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, drugName);
                    preparedStatement.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error adding drug to the database.");
            }
        }

        private void deleteDrug(String drugName) {
            try {
                String query = "DELETE FROM " + TABLE_NAME + " WHERE name = ?";
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, drugName);
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    class DoctorPanel {

        private JPanel panel;
        private pharma parent;
        private ArrayList<Drug> drugStock;
        private Map<String, Integer> prescription;

        public DoctorPanel(pharma parent, ArrayList<Drug> drugStock, Map<String, Integer> prescription) {
            this.parent = parent;
            this.drugStock = drugStock;
            this.prescription = prescription;
            initPanel();
        }

        private void initPanel() {
            panel = new JPanel();
            panel.setLayout(new FlowLayout());
            panel.setBackground(Color.GRAY);

            JButton prescribeButton = new JButton("Prescribe Drug.");
            prescribeButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    prescribeDrug();
                }
            });

            JButton searchButton = new JButton("Search for Drugs.");
            searchButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    searchForDrugs();
                }
            });

            panel.add(prescribeButton);
            panel.add(searchButton);
        }

        public JPanel getPanel() {
            return panel;
        }

        private void prescribeDrug() {
            String drugName = JOptionPane.showInputDialog(panel, "Enter the drug name to prescribe:");
            if (drugName != null && !drugName.isEmpty()) {
                String patientName = JOptionPane.showInputDialog(panel, "Enter the patient's name:");

                if (patientName != null && !patientName.isEmpty()) {
                    boolean found = false;
                    for (Drug drug : drugStock) {
                        if (drug.getName().equalsIgnoreCase(drugName)) {
                            found = true;
                            // Update the prescription map with patient's name and drug quantity
                            String prescriptionKey = patientName + "-" + drugName;
                            prescription.put(prescriptionKey, prescription.getOrDefault(prescriptionKey, 0) + 1);

                            // Update the database with the patient's name
                            updatePatientNameInDatabase(drugName, patientName);

                            JOptionPane.showMessageDialog(panel, "Drug prescribed successfully.");
                            break;
                        }
                    }
                    if (!found) {
                        JOptionPane.showMessageDialog(panel, "Drug not found in stock.");
                    }
                } else {
                    JOptionPane.showMessageDialog(panel, "Please enter the patient's name.");
                }
            }
        }

        private void updatePatientNameInDatabase(String drugName, String patientName) {
            try {
                String query = "UPDATE " + TABLE_NAME + " SET patient_name = ? WHERE name = ?";
                try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                    preparedStatement.setString(1, patientName);
                    preparedStatement.setString(2, drugName);
                    int rowsAffected = preparedStatement.executeUpdate();

                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(panel, "Patient name updated successfully.");
                    } else {
                        JOptionPane.showMessageDialog(panel, "No records updated. Drug not found in the database.");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(panel, "Error updating patient name in the database: " + e.getMessage());
            }
        }

        private void searchForDrugs() {
            String keyword = JOptionPane.showInputDialog(panel, "Enter the Keyword to search for drugs:");
            if (keyword != null && !keyword.isEmpty()) {
                StringBuilder searchResult = new StringBuilder("Available drugs in the pharmacy:\n");
                for (Drug drug : drugStock) {
                    if (drug.getName().toLowerCase().contains(keyword.toLowerCase())) {
                        searchResult.append(drug.getName()).append("\n");
                    }
                }
                JOptionPane.showMessageDialog(panel, searchResult.toString());
            }
        }
    }
}

