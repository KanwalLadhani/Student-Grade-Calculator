# 🎓 Student Grade Calculator

A desktop Student Grade Calculator built with Java and Java Swing.

## Features
- Add students and record multiple grades
- Auto-calculates average, highest, lowest score
- Letter grade (A/B/C/D/F) and Pass/Fail status
- Bar chart visualization built with Java2D (no external library)
- Dashboard showing class average, top student, and failing count
- Data saved automatically to local file

## Tech Stack
- Java 17+
- Java Swing (GUI)
- Java2D (custom bar chart)
- File I/O for persistence

## How to Run
1. Clone the repository
2. Open in IntelliJ IDEA
3. Run `GradeApp.java`

## Project Structure
- `Student.java` — data model with grade calculations
- `GradeManager.java` — business logic and statistics
- `GradeApp.java` — GUI with Swing and custom Java2D chart
