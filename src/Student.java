import java.util.ArrayList;
import java.util.List;

public class Student {

    private String id;
    private String name;
    private List<Double> grades;   // ArrayList of exam scores

    public Student(String id, String name) {
        this.id     = id;
        this.name   = name;
        this.grades = new ArrayList<>();
    }

    // ── Grade operations ─────────────────────────────────────────────
    public void addGrade(double grade) { grades.add(grade); }

    public double getAverage() {
        if (grades.isEmpty()) return 0.0;
        double sum = 0;
        for (double g : grades) sum += g;
        return sum / grades.size();
    }

    public double getHighest() {
        return grades.stream().mapToDouble(Double::doubleValue).max().orElse(0);
    }

    public double getLowest() {
        return grades.stream().mapToDouble(Double::doubleValue).min().orElse(0);
    }

    // Letter grade based on average
    public String getLetterGrade() {
        double avg = getAverage();
        if (avg >= 90) return "A";
        if (avg >= 80) return "B";
        if (avg >= 70) return "C";
        if (avg >= 60) return "D";
        return "F";
    }

    public String getStatus() {
        return getAverage() >= 50 ? "Pass" : "Fail";
    }

    // ── Getters ──────────────────────────────────────────────────────
    public String         getId()     { return id; }
    public String         getName()   { return name; }
    public List<Double>   getGrades() { return grades; }

    // ── File format: id|name|grade1,grade2,grade3 ─────────────────────
    public String toFileString() {
        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|").append(name).append("|");
        for (int i = 0; i < grades.size(); i++) {
            sb.append(grades.get(i));
            if (i < grades.size() - 1) sb.append(",");
        }
        return sb.toString();
    }

    public static Student fromFileString(String line) {
        String[] parts = line.split("\\|");
        Student s = new Student(parts[0], parts[1]);
        if (parts.length > 2 && !parts[2].isEmpty()) {
            for (String g : parts[2].split(","))
                s.addGrade(Double.parseDouble(g));
        }
        return s;
    }
}