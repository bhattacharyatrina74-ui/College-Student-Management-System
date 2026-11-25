import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class Student {
    private String id;
    private String name;
    private String course;
    private double cgpa;

    public Student(String id, String name, String course, double cgpa) {
        this.id = id;
        this.name = name;
        this.course = course;
        this.cgpa = cgpa;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCourse() {
        return course;
    }

    public double getCgpa() {
        return cgpa;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + course + " | " + cgpa;
    }

    public String toFileString() {
        return id + "|" + name + "|" + course + "|" + cgpa;
    }
}

public class project {
    private static final String FILE_NAME = "students.txt";
    private ArrayList<Student> students = new ArrayList<>();
    private HashMap<String, Student> studentMap = new HashMap<>();
    private int nextId = 101;

    private void rebuildMap() {
        studentMap.clear();
        for (Student s : students) {
            studentMap.put(s.getId().toUpperCase(), s);
        }
    }

    private void updateNextIdFromExisting() {
        int maxNum = 100;
        for (Student s : students) {
            String id = s.getId();
            if (id != null && id.toUpperCase().startsWith("S")) {
                try {
                    int num = Integer.parseInt(id.substring(1).trim());
                    if (num > maxNum) {
                        maxNum = num;
                    }
                } catch (NumberFormatException ignored) {
                    // Skip non-numeric ID suffix
                }
            }
        }
        nextId = Math.max(101, maxNum + 1);
    }

    private String generateNextId() {
        String candidate;
        do {
            candidate = "S" + nextId;
            nextId++;
        } while (studentMap.containsKey(candidate.toUpperCase()));
        return candidate;
    }

    private void loadStudents() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return;
        }

        students.clear();
        studentMap.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] parts = line.split("\\|");
                if (parts.length != 4) {
                    continue;
                }

                String id = parts[0].trim();
                String name = parts[1].trim();
                String course = parts[2].trim();
                String cgpaStr = parts[3].trim();

                if (id.isEmpty() || name.isEmpty() || course.isEmpty()) {
                    continue;
                }

                if (studentMap.containsKey(id.toUpperCase())) {
                    continue;
                }

                try {
                    double cgpa = Double.parseDouble(cgpaStr);
                    if (cgpa < 0.0 || cgpa > 10.0 || Double.isNaN(cgpa) || Double.isInfinite(cgpa)) {
                        continue;
                    }
                    Student student = new Student(id, name, course, cgpa);
                    students.add(student);
                    studentMap.put(id.toUpperCase(), student);
                } catch (NumberFormatException ignored) {
                    // Skip invalid CGPA row
                }
            }
        } catch (IOException e) {
            System.out.println("Error loading records from " + FILE_NAME + ": " + e.getMessage());
        }

        updateNextIdFromExisting();
    }

    private void saveStudents() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                writer.write(s.toFileString());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error saving records to " + FILE_NAME + ": " + e.getMessage());
        }
    }

    private void addStudent(Scanner sc) {
        String name;
        while (true) {
            System.out.print("Name: ");
            name = sc.nextLine().trim();
            if (!name.isEmpty()) {
                break;
            }
            System.out.println("Name cannot be blank. Please try again.");
        }

        String course;
        while (true) {
            System.out.print("Course: ");
            course = sc.nextLine().trim();
            if (!course.isEmpty()) {
                break;
            }
            System.out.println("Course cannot be blank. Please try again.");
        }

        double cgpa;
        while (true) {
            System.out.print("CGPA: ");
            String input = sc.nextLine().trim();
            try {
                cgpa = Double.parseDouble(input);
                if (cgpa >= 0.0 && cgpa <= 10.0 && !Double.isNaN(cgpa) && !Double.isInfinite(cgpa)) {
                    break;
                }
                System.out.println("CGPA must be between 0 and 10. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid CGPA. Please enter a numeric value between 0 and 10.");
            }
        }

        String id = generateNextId();
        Student student = new Student(id, name, course, cgpa);
        students.add(student);
        studentMap.put(id.toUpperCase(), student);
        saveStudents();

        System.out.println("Student added. " + student);
    }

    private void displayStudents() {
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }

        System.out.println("All Student Records:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i));
        }
    }

    private void searchById(Scanner sc) {
        System.out.print("Student ID: ");
        String id = sc.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Student ID cannot be blank.");
            return;
        }

        Student found = studentMap.get(id.toUpperCase());
        if (found != null) {
            System.out.println(found);
        } else {
            System.out.println("Student ID " + id + " not found.");
        }
    }

    private void searchByName(Scanner sc) {
        System.out.print("Name keyword: ");
        String keyword = sc.nextLine().trim();
        if (keyword.isEmpty()) {
            System.out.println("Search keyword cannot be blank.");
            return;
        }

        String lowerKeyword = keyword.toLowerCase();
        List<Student> matches = new ArrayList<>();
        for (Student s : students) {
            if (s.getName().toLowerCase().contains(lowerKeyword)) {
                matches.add(s);
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No student found");
        } else {
            System.out.println("Matching students:");
            for (int i = 0; i < matches.size(); i++) {
                System.out.println((i + 1) + ". " + matches.get(i));
            }
        }
    }

    private void sortByCgpa() {
        students.sort(
            Comparator.comparingDouble(Student::getCgpa).reversed()
                      .thenComparing(Student::getId)
        );
        rebuildMap();
        saveStudents();

        System.out.println("Sorted list (CGPA high to low):");
        if (students.isEmpty()) {
            System.out.println("No student records found.");
            return;
        }
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i));
        }
    }

    private void deleteStudent(Scanner sc) {
        System.out.print("Student ID to delete: ");
        String id = sc.nextLine().trim();
        if (id.isEmpty()) {
            System.out.println("Student ID cannot be blank.");
            return;
        }

        Student removed = studentMap.remove(id.toUpperCase());
        if (removed != null) {
            students.removeIf(s -> s.getId().equalsIgnoreCase(id));
            saveStudents();
            System.out.println("Student " + removed.getId() + " deleted.");
        } else {
            System.out.println("Student ID " + id + " not found.");
        }
    }

    private void printMenu() {
        System.out.println("\n--- College Student Management System ---");
        System.out.println("1. Add Student");
        System.out.println("2. Display All Students");
        System.out.println("3. Search by ID");
        System.out.println("4. Sort by CGPA");
        System.out.println("5. Delete Student");
        System.out.println("6. Search by Name");
        System.out.println("7. Exit");
        System.out.print("Enter your choice (1-7): ");
    }

    public static void main(String[] args) {
        project app = new project();
        app.loadStudents();

        try (Scanner sc = new Scanner(System.in)) {
            boolean running = true;
            while (running) {
                app.printMenu();
                String choiceInput = sc.nextLine().trim();

                switch (choiceInput) {
                    case "1":
                        app.addStudent(sc);
                        break;
                    case "2":
                        app.displayStudents();
                        break;
                    case "3":
                        app.searchById(sc);
                        break;
                    case "4":
                        app.sortByCgpa();
                        break;
                    case "5":
                        app.deleteStudent(sc);
                        break;
                    case "6":
                        app.searchByName(sc);
                        break;
                    case "7":
                        app.saveStudents();
                        running = false;
                        System.out.println("Records saved. Exiting...");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number between 1 and 7.");
                        break;
                }
            }
        }
    }
}
