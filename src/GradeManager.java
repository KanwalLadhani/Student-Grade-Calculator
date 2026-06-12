import java.util.*;
import java.io.*;

public class GradeManager {

    private ArrayList<Student> students = new ArrayList<>();
    private static final String FILE = "students.txt";

    // ── CRUD ─────────────────────────────────────────────────────────
    public boolean addStudent(String id, String name) {
        if (findById(id) != null) return false;
        students.add(new Student(id, name));
        return true;
    }

    public boolean removeStudent(String id) {
        Student s = findById(id);
        if (s == null) return false;
        students.remove(s);
        return true;
    }

    public Student findById(String id) {
        for (Student s : students)
            if (s.getId().equals(id)) return s;
        return null;
    }

    public ArrayList<Student> getAllStudents() { return students; }

    // ── Class statistics ─────────────────────────────────────────────
    public double getClassAverage() {
        if (students.isEmpty()) return 0;
        double sum = 0;
        for (Student s : students) sum += s.getAverage();
        return sum / students.size();
    }

    public Student getTopStudent() {
        return students.stream()
                .max(Comparator.comparingDouble(Student::getAverage))
                .orElse(null);
    }

    public Student getLowestStudent() {
        return students.stream()
                .min(Comparator.comparingDouble(Student::getAverage))
                .orElse(null);
    }

    public int getPassCount() {
        return (int) students.stream().filter(s -> s.getAverage() >= 50).count();
    }

    public int getFailCount() {
        return students.size() - getPassCount();
    }

    // ── Save & Load ──────────────────────────────────────────────────
    public void saveData() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (Student s : students) pw.println(s.toFileString());
        } catch (IOException e) {
            System.out.println("Could not save: " + e.getMessage());
        }
    }

    public void loadData() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null)
                students.add(Student.fromFileString(line));
        } catch (IOException e) {
            System.out.println("Could not load: " + e.getMessage());
        }
    }
}