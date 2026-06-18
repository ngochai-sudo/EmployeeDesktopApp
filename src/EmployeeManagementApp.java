import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import java.awt.*;

public class EmployeeManagementApp {
    private static Map<String, String> userCredentials = new HashMap<>();
    private static List<Employee> employees = new ArrayList<>();
    private static final String USER_FILE = "users.txt";
    private static final String EMPLOYEES_FILE = "employee.txt";
    private static String currentRole = "";
    private static Map<String, String> userRole = new HashMap<>();
    public static void main(String[] args) {
        loadCredentials(USER_FILE);
        loadEmployees(EMPLOYEES_FILE);

        SwingUtilities.invokeLater(() -> {
            createLoginUI();
        });
    }

    private static void createLoginUI() {
        JFrame frame = new JFrame("Login System");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(3,2,10,10));
        JLabel userLabel = new JLabel("Username:");
        JTextField userField = new JTextField();
        JLabel passLabel = new JLabel("Password:");
        JPasswordField passField = new JPasswordField();

        panel.add(userLabel);
        panel.add(userField);
        panel.add(passLabel);
        panel.add(passField);

        JPanel buttonPanel = new JPanel(new GridLayout(1,3,10,10));
        JButton logiButton = new JButton("Login");
        JButton registerButton = new JButton("Register");
        JButton exitButton = new JButton("Exit");
        buttonPanel.add(logiButton);
        buttonPanel.add(registerButton);
        buttonPanel.add(exitButton);
        JLabel statLabel = new JLabel("", SwingConstants.CENTER);

        logiButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());
            if (validateLogin(username, password)) {
                currentRole = userRole.get(username);
                frame.dispose();
                createEmployeeManagementUI();
            } else {
                statLabel.setText("Invalid username or password");
            }
        });

        registerButton.addActionListener(e -> {
            registerUser();
        });

        exitButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                frame,
                "Are you sure to exit?",
                "Exit", 
                JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                frame.dispose();
            }
        });

        userField.addActionListener(e -> {
            logiButton.doClick();
        });

        passField.addActionListener(e -> {
            logiButton.doClick();
        });

        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.add(statLabel, BorderLayout.NORTH);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void registerUser() {
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();

        String[] roles = {"user", "admin"};
        JComboBox<String> roleBox = new JComboBox<>(roles);

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Role:", roleBox
        };

        int option = JOptionPane.showConfirmDialog(
            null, 
            message,
            "Register",
            JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();
            String role = (String) roleBox.getSelectedItem();

            if (username.isBlank() || password.isBlank()) {
                JOptionPane.showMessageDialog(
                    null,
                    "Username and Password cannot be empty!",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            userCredentials.put(username, password);
            userRole.put(username, role);

            saveUser(username, password, role);

            JOptionPane.showMessageDialog(
                null, 
                "Register Successfully!",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void saveUser(String username, String password, String role) {
        try (BufferedWriter writer = 
            new BufferedWriter(new FileWriter(USER_FILE, true))) {
            writer.write(username + "," + password + "," + role);
            writer.newLine();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(
                null, 
                "Error saving user!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void createEmployeeManagementUI() {
        JFrame frame = new JFrame("Employee Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        JPanel panel = new JPanel(new GridLayout(5,1,10,10));
        JButton viewButton = new JButton("View Employees");
        JButton addButton = new JButton("Add Employee");
        JButton updateButton = new JButton("Update Employee");
        JButton deleteButton = new JButton("Delete Employee");
        JButton saveButton = new JButton("Save Employee");
        JButton logoutButton = new JButton("Log Out");

        if (currentRole.equalsIgnoreCase("user")) {
            panel.remove(addButton);
            panel.remove(updateButton);
            panel.remove(deleteButton);
            panel.remove(saveButton);

            panel.revalidate();
            panel.repaint();
        }

        if (currentRole.equalsIgnoreCase("admin")) {
            panel.add(viewButton);
            panel.add(addButton);
            panel.add(updateButton);
            panel.add(deleteButton);
            panel.add(saveButton);
            panel.add(logoutButton);
        } else {
            panel.add(viewButton);
            panel.add(logoutButton);
}

        frame.add(panel, BorderLayout.CENTER);

        viewButton.addActionListener(e -> viewEmployees());
        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        saveButton.addActionListener(e -> saveEmployee());
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(frame, 
                    "Are you sure to logout?", 
                    "Logout Confirmation",
                    JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                frame.dispose();
                createLoginUI();
            }
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void viewEmployees() {
        JFrame frame = new JFrame("View Employees");
        frame.setSize(300,300);
        frame.setLayout(new BorderLayout());

        JPanel optionsPanel = new JPanel(new GridLayout(4,1,10,10));
        JButton listAllButton = new JButton("List All Employees");
        JButton countByDeptButton = new JButton("Count Employees by Department");
        JButton searchByNameButton = new JButton("Search by Employee name");
        JButton listByDeptButton = new JButton("List Employees by Department");

        optionsPanel.add(listAllButton);
        optionsPanel.add(countByDeptButton);
        optionsPanel.add(searchByNameButton);
        optionsPanel.add(listByDeptButton);

        JButton backButton = new JButton("Back");
        JPanel backPanel = new JPanel();
        backPanel.add(backButton);

        frame.add(optionsPanel, BorderLayout.CENTER);
        frame.add(backPanel, BorderLayout.SOUTH);

        listAllButton.addActionListener(e -> {
            listAllEmployees();
        });

        countByDeptButton.addActionListener(e -> {
            countEmployeesByDepartment();
        });

        searchByNameButton.addActionListener(e -> {
            searchEmployeeByName();
        });

        listByDeptButton.addActionListener(e -> {
            listEmployeesByDepartment();
        });

        backButton.addActionListener(e -> {
            frame.dispose();
        });

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static void listAllEmployees() {
        if (employees.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No employee to display.", "List All Employees", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        StringBuilder message = new StringBuilder("Employee List:\n");
        for (Employee employee : employees) {
            message.append(employee).append("\n");
        }
        JOptionPane.showMessageDialog(null, message.toString(), "List All Employees", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void countEmployeesByDepartment() {
        List<String> departments = employees.stream()
                                            .map(Employee::getDepartment)
                                            .distinct()
                                            .toList();
        if (departments.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No departments available.", "Count Employees by Department", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String department = (String) JOptionPane.showInputDialog(
            null,
            "Select a Department:",
            "Count Employees by Department",
            JOptionPane.PLAIN_MESSAGE,
            null,
            departments.toArray(),
            departments.get(0)
        );
        if (department == null) return;
        long count = employees.stream()
                              .filter(employee -> employee.getDepartment().equalsIgnoreCase(department))
                              .count();
        StringBuilder message = new StringBuilder("Employees in " + department + ":\n");
        employees.stream()
                 .filter(employee -> employee.getDepartment().equalsIgnoreCase(department))
                 .forEach(employee -> message.append(employee).append("\n"));
        message.append("\nTotal count: ").append(count);
        JOptionPane.showMessageDialog(null, message.toString(), "Count Employees by Department", JOptionPane.INFORMATION_MESSAGE);
    }

    private static void searchEmployeeByName() {
        String name = JOptionPane.showInputDialog("Enter employee name to search: ");
        if (name == null || name.trim().isEmpty()) return;
        List<Employee> found = employees.stream()
                                        .filter(employee -> employee.getName().toLowerCase().contains(name.trim().toLowerCase()))
                                        .toList();
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No employee found with name: " + name, "Search by Employee name", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("Employees found:\n");
            for (Employee employee : found) {
                message.append(employee).append("\n");
            }
            JOptionPane.showMessageDialog(null, message.toString(), "Search by Employee name", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void listEmployeesByDepartment() {
        List<String> departments = employees.stream()
                                            .map(Employee::getDepartment)
                                            .distinct()
                                            .toList();
        if (departments.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No departments available.", 
                                      "List Employees by Department", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String department = (String) JOptionPane.showInputDialog(
            null,
            "Select Department:",
            "List Employees by Department",
            JOptionPane.PLAIN_MESSAGE,
            null,
            departments.toArray(),
            departments.get(0)
        );
        if (department == null) return;
        List<Employee> found = employees.stream()
                                        .filter(employee -> employee.getDepartment().equalsIgnoreCase(department))
                                        .toList();
        if (found.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No employees found in department: " + department, "List Employees by Department", JOptionPane.INFORMATION_MESSAGE);
        } else {
            StringBuilder message = new StringBuilder("Employees in " + department + ":\n");
            found.forEach(employee -> message.append(employee).append("\n"));
            JOptionPane.showMessageDialog(null, message.toString(), "List Employees by Department", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private static void addEmployee() {

        if (!currentRole.equalsIgnoreCase("admin")) {
        JOptionPane.showMessageDialog(
                null,
                "You do not have permission to add employees!",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        String[] departments = {"Human Resource", "IT Department", "Sales", "Account", "Finance"};
        JComboBox<String> departmentField = new JComboBox<>(departments);
        String[] positions = {"Manager", "Staff", "Analyst"};
        JComboBox<String> positionField = new JComboBox<>(positions);
        Object[] message = {
            "Employee ID:", idField,
            "Employee Name:", nameField,
            "Department:", departmentField,
            "Position:", positionField
        };

        int option = JOptionPane.showConfirmDialog(null, message, "Add Employee", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                Integer id = parseIntegerInput(idField.getText());
                if (id == null) return;
                String name = nameField.getText().trim();
                String department = (String) departmentField.getSelectedItem();
                String position = (String) positionField.getSelectedItem();
                if(name.isBlank()) {
                    JOptionPane.showMessageDialog(null, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                boolean exist = employees.stream().anyMatch(employee -> employee.getId() == id);
                if (exist) {
                    JOptionPane.showMessageDialog(null, "Employee ID already exist!", "Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    employees.add(new Employee(id, name, department, position));
                    JOptionPane.showMessageDialog(null, "Employee added successfully");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(null, "Invalid ID, Please enter number.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private static void updateEmployee() {

        if (!currentRole.equalsIgnoreCase("admin")) {
        JOptionPane.showMessageDialog(
                null,
                "You do not have permission to update employees!",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
        return;
    }
        String idStr = JOptionPane.showInputDialog("Enter Employee ID to update:");
        if (idStr == null) return;
        Integer id = parseIntegerInput(idStr);
        if (id == null) return;

        Employee employee = findEmployeeById(id);
        if (employee != null) {
            JTextField nameField = new JTextField(employee.getName());
            String [] departments = {"Human Resource", "IT Department", "Sales", "Account", "Finance"};
            JComboBox<String> departmentField = new JComboBox<>(departments);
            departmentField.setSelectedItem(employee.getDepartment());
            String[] positions = {"Manager", "Staff", "Analyst"};
            JComboBox<String> positionField = new JComboBox<>(positions);
            positionField.setSelectedItem(employee.getPosition());
            Object[] message = {
                "Employee Name:", nameField,
                "Department:", departmentField,
                "Position:", positionField
            };

            int option = JOptionPane.showConfirmDialog(null, message, "Update Employee", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String name = nameField.getText().trim();
                String department = (String) departmentField.getSelectedItem();
                String position = (String) positionField.getSelectedItem();
                if (name.isBlank()) {
                    JOptionPane.showMessageDialog(null, "All fields must be filled", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                employee.setName(name);
                employee.setDepartment(department);
                employee.setPosition(position);
                JOptionPane.showMessageDialog(null, "Employee updated successfully");
            }
        } else {
            JOptionPane.showMessageDialog(null, "Employee not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static void deleteEmployee() {

    if (!currentRole.equalsIgnoreCase("admin")) {
        JOptionPane.showMessageDialog(
                null,
                "You do not have permission to delete employees!",
                "Access Denied",
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    String idStr = JOptionPane.showInputDialog("Enter Employee ID to delete:");
    if (idStr == null) return;

    Integer id = parseIntegerInput(idStr);
    if (id == null) return;

    Employee employee = findEmployeeById(id);

    if (employee != null) {
        employees.remove(employee);
        JOptionPane.showMessageDialog(
                null,
                "Employee deleted successfully.",
                "Delete Employee",
                JOptionPane.INFORMATION_MESSAGE);
    } else {
        JOptionPane.showMessageDialog(
                null,
                "Employee not found!",
                "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}

    private static void saveEmployee() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(EMPLOYEES_FILE))) {
            for (Employee employee : employees) {
                writer.write(employee.getId() + "," + employee.getName() + "," + employee.getDepartment() + "," + employee.getPosition());
                writer.newLine();
            }
            JOptionPane.showMessageDialog(null, "Employees saved successfully!", "Save Employees", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error saving employees!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private static Employee findEmployeeById(int id) {
        for (Employee employee : employees) {
            if (employee.getId() == id) {
                return employee;
            }
        }
        return null;
    }

    private static void loadCredentials(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
        String line;

        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");

            if (parts.length == 3) {
                String username = parts[0].trim();
                String password = parts[1].trim();
                String role = parts[2].trim();

                userCredentials.put(username, password);
                userRole.put(username, role);
            }
        }
    } catch (IOException e) {
        System.out.println("Error loading credentials: " + e.getMessage());
    }
    }

    private static void loadEmployees(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(EMPLOYEES_FILE))){
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    int id = Integer.parseInt(parts[0].trim());
                    String name = parts[1].trim();
                    String department = parts[2].trim();
                    String position = parts[3].trim();
                    employees.add(new Employee(id, name, department, position));
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading employees: " + e.getMessage());
        }
    }

    private static boolean validateLogin(String username, String password) {
        if (username == null || password == null || username.isBlank() || password.isBlank()) {
            return false;
        }
        return userCredentials.getOrDefault(username, "").equals(password);
    }

    private static Integer parseIntegerInput(String input) {
        if (input == null || input.trim().isEmpty()) {
            System.out.println("Don't leave blank text");   
        }
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, "Invalid input. Please enter a numeric value.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
}