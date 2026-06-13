import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;

public class GradeApp extends JFrame {

    // ── Colors & Fonts ────────────────────────────────────────────────
    static final Color BG        = new Color(18,  18,  27);
    static final Color PANEL_BG  = new Color(28,  28,  40);
    static final Color CARD_BG   = new Color(38,  38,  55);
    static final Color ACCENT    = new Color(99,  102, 241);
    static final Color SUCCESS   = new Color(34,  197, 94);
    static final Color DANGER    = new Color(239, 68,  68);
    static final Color WARNING   = new Color(251, 191, 36);
    static final Color TEXT      = new Color(226, 226, 240);
    static final Color TEXT_DIM  = new Color(140, 140, 170);
    static final Color BORDER    = new Color(55,  55,  78);

    static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  14);
    static final Font FONT_BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 12);

    // ── State ─────────────────────────────────────────────────────────
    private GradeManager     manager = new GradeManager();
    private DefaultTableModel tableModel;
    private CardLayout        cardLayout;
    private JPanel            contentPanel;
    private BarChartPanel     chartPanel;

    // ── Stat labels ───────────────────────────────────────────────────
    private JLabel statStudents, statAverage, statPass, statFail;

    public static void main(String[] args) {
        try { UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); }
        catch (Exception ignored) {}
        SwingUtilities.invokeLater(() -> new GradeApp().setVisible(true));
    }

    public GradeApp() {
        manager.loadData();
        buildFrame();
    }

    // ── Frame ─────────────────────────────────────────────────────────
    private void buildFrame() {
        setTitle("🎓 Student Grade Calculator");
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setSize(1050, 680);
        setMinimumSize(new Dimension(850, 580));
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { manager.saveData(); System.exit(0); }
        });
        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);
    }

    // ── Sidebar ───────────────────────────────────────────────────────
    private JPanel buildSidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setPreferredSize(new Dimension(210, 0));
        side.setBackground(PANEL_BG);
        side.setBorder(new MatteBorder(0, 0, 0, 1, BORDER));

        JPanel logo = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        logo.setBackground(PANEL_BG);
        JLabel title = new JLabel("Grade Calculator");
        title.setFont(FONT_HEADING); title.setForeground(TEXT);
        logo.add(title);

        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setBackground(PANEL_BG);
        nav.setBorder(new EmptyBorder(10, 0, 0, 0));

        String[] pages = {"📊  Dashboard", "🎓  Students", "📈  Chart"};
        for (String page : pages) {
            JButton btn = sidebarButton(page);
            String pageName = page.substring(3).trim();
            btn.addActionListener(e -> switchPage(pageName));
            nav.add(btn);
            nav.add(Box.createVerticalStrut(4));
        }

        side.add(logo, BorderLayout.NORTH);
        side.add(nav,  BorderLayout.CENTER);
        return side;
    }

    private JButton sidebarButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BODY); btn.setForeground(TEXT_DIM);
        btn.setBackground(PANEL_BG); btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(CARD_BG); btn.setForeground(TEXT); }
            public void mouseExited(MouseEvent e)  { btn.setBackground(PANEL_BG); btn.setForeground(TEXT_DIM); }
        });
        return btn;
    }

    // ── Content ───────────────────────────────────────────────────────
    private JPanel buildContent() {
        cardLayout   = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(BG);
        contentPanel.add(buildDashboard(), "Dashboard");
        contentPanel.add(buildStudents(),  "Students");
        contentPanel.add(buildChart(),     "Chart");
        return contentPanel;
    }

    private void switchPage(String name) {
        if (name.equals("Students"))  refreshTable();
        if (name.equals("Dashboard")) refreshStats();
        if (name.equals("Chart"))     { chartPanel.setStudents(manager.getAllStudents()); chartPanel.repaint(); }
        cardLayout.show(contentPanel, name);
    }

    // ── Dashboard ─────────────────────────────────────────────────────
    private JPanel buildDashboard() {
        JPanel p = page();
        p.add(pageTitle("Dashboard"), BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 16, 0));
        cards.setBackground(BG);
        cards.setBorder(new EmptyBorder(20, 30, 20, 30));

        statStudents = new JLabel("0");
        statAverage  = new JLabel("0.0");
        statPass     = new JLabel("0");
        statFail     = new JLabel("0");

        cards.add(statCard("🎓  Students",     statStudents, ACCENT));
        cards.add(statCard("📊  Class Average", statAverage,  WARNING));
        cards.add(statCard("✅  Passing",        statPass,     SUCCESS));
        cards.add(statCard("❌  Failing",         statFail,     DANGER));

        // Top / Lowest student panel
        JPanel info = new JPanel(new GridLayout(1, 2, 16, 0));
        info.setBackground(BG);
        info.setBorder(new EmptyBorder(0, 30, 20, 30));
        info.add(infoCard("🏆  Top Student"));
        info.add(infoCard("📉  Needs Attention"));

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.add(cards, BorderLayout.NORTH);
        center.add(info,  BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        refreshStats();
        return p;
    }

    // Keep references to update later
    private JLabel topNameLabel, topAvgLabel, lowNameLabel, lowAvgLabel;

    private JPanel infoCard(String title) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel t = new JLabel(title);
        t.setFont(FONT_HEADING); t.setForeground(TEXT);
        card.add(t);
        card.add(Box.createVerticalStrut(12));

        JLabel nameLabel = new JLabel("—");
        JLabel avgLabel  = new JLabel("—");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        nameLabel.setForeground(title.contains("Top") ? SUCCESS : DANGER);
        avgLabel.setFont(FONT_SMALL); avgLabel.setForeground(TEXT_DIM);

        if (title.contains("Top")) { topNameLabel = nameLabel; topAvgLabel = avgLabel; }
        else                       { lowNameLabel = nameLabel; lowAvgLabel = avgLabel; }

        card.add(nameLabel);
        card.add(Box.createVerticalStrut(4));
        card.add(avgLabel);
        return card;
    }

    private JPanel statCard(String title, JLabel valueLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_BG);
        card.setBorder(new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(20, 20, 20, 20)
        ));
        JLabel t = new JLabel(title); t.setFont(FONT_SMALL); t.setForeground(TEXT_DIM);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        valueLabel.setForeground(accent);
        card.add(t, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void refreshStats() {
        statStudents.setText(String.valueOf(manager.getAllStudents().size()));
        statAverage.setText(String.format("%.1f", manager.getClassAverage()));
        statPass.setText(String.valueOf(manager.getPassCount()));
        statFail.setText(String.valueOf(manager.getFailCount()));

        Student top = manager.getTopStudent();
        Student low = manager.getLowestStudent();
        if (topNameLabel != null) {
            topNameLabel.setText(top != null ? top.getName() : "—");
            topAvgLabel.setText(top != null ? String.format("Average: %.1f%%  Grade: %s", top.getAverage(), top.getLetterGrade()) : "");
            lowNameLabel.setText(low != null ? low.getName() : "—");
            lowAvgLabel.setText(low != null ? String.format("Average: %.1f%%  Grade: %s", low.getAverage(), low.getLetterGrade()) : "");
        }
    }

    // ── Students page ─────────────────────────────────────────────────
    private JPanel buildStudents() {
        JPanel p = page();
        p.add(pageTitle("Students"), BorderLayout.NORTH);

        String[] cols = {"ID", "Name", "Grades", "Average", "Highest", "Lowest", "Grade", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = styledTable(tableModel);

        // Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        toolbar.setBackground(BG);

        JTextField idField   = styledField("Student ID", 8);
        JTextField nameField = styledField("Full Name",  14);
        JButton    addBtn    = accentButton("+ Add Student");
        JButton    gradeBtn  = accentButton("Add Grade");
        JButton    removeBtn = dangerButton("Remove");

        toolbar.add(idField); toolbar.add(nameField); toolbar.add(addBtn);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(gradeBtn); toolbar.add(removeBtn);

        addBtn.addActionListener(e -> {
            String id   = idField.getText().trim();
            String name = nameField.getText().trim();
            if (id.isEmpty() || id.equals("Student ID") || name.isEmpty() || name.equals("Full Name")) {
                toast("Enter both ID and Name."); return;
            }
            if (manager.addStudent(id, name)) {
                idField.setText("Student ID"); idField.setForeground(TEXT_DIM);
                nameField.setText("Full Name"); nameField.setForeground(TEXT_DIM);
                refreshTable(); refreshStats();
            } else toast("Student ID already exists.");
        });

        gradeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { toast("Select a student first."); return; }
            String sid = (String) tableModel.getValueAt(row, 0);
            String input = JOptionPane.showInputDialog(this, "Enter grade (0–100):", "Add Grade", JOptionPane.PLAIN_MESSAGE);
            if (input == null) return;
            try {
                double g = Double.parseDouble(input.trim());
                if (g < 0 || g > 100) { toast("Grade must be between 0 and 100."); return; }
                manager.findById(sid).addGrade(g);
                refreshTable(); refreshStats();
                if (chartPanel != null) { chartPanel.setStudents(manager.getAllStudents()); chartPanel.repaint(); }
            } catch (NumberFormatException ex) { toast("Please enter a valid number."); }
        });

        removeBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { toast("Select a student first."); return; }
            String sid = (String) tableModel.getValueAt(row, 0);
            manager.removeStudent(sid);
            refreshTable(); refreshStats();
        });

        JPanel center = new JPanel(new BorderLayout());
        center.setBackground(BG);
        center.add(toolbar, BorderLayout.NORTH);
        center.add(styledScroll(table), BorderLayout.CENTER);
        p.add(center, BorderLayout.CENTER);
        refreshTable();
        return p;
    }

    private void refreshTable() {
        if (tableModel == null) return;
        tableModel.setRowCount(0);
        for (Student s : manager.getAllStudents()) {
            tableModel.addRow(new Object[]{
                    s.getId(),
                    s.getName(),
                    s.getGrades().size() + " grades",
                    String.format("%.1f%%", s.getAverage()),
                    String.format("%.1f%%", s.getHighest()),
                    String.format("%.1f%%", s.getLowest()),
                    s.getLetterGrade(),
                    s.getStatus()
            });
        }
        refreshStats();
    }

    // ── Chart page ────────────────────────────────────────────────────
    private JPanel buildChart() {
        JPanel p = page();
        p.add(pageTitle("Grade Chart"), BorderLayout.NORTH);
        chartPanel = new BarChartPanel(manager.getAllStudents());
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(BG);
        wrapper.setBorder(new EmptyBorder(20, 30, 20, 30));
        wrapper.add(chartPanel, BorderLayout.CENTER);
        p.add(wrapper, BorderLayout.CENTER);
        return p;
    }

    // ── Bar Chart (pure Java2D — no external library) ─────────────────
    static class BarChartPanel extends JPanel {
        private ArrayList<Student> students;

        public BarChartPanel(ArrayList<Student> students) {
            this.students = students;
            setBackground(new Color(28, 28, 40));
            setBorder(new LineBorder(BORDER, 1, true));
        }

        public void setStudents(ArrayList<Student> students) { this.students = students; }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth(), h = getHeight();
            int padL = 60, padR = 20, padT = 30, padB = 60;
            int chartW = w - padL - padR;
            int chartH = h - padT - padB;

            // Title
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(TEXT);
            g2.drawString("Student Averages", padL, padT - 10);

            // Y-axis lines
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i <= 10; i++) {
                int y = padT + chartH - (i * chartH / 10);
                g2.setColor(BORDER);
                g2.drawLine(padL, y, padL + chartW, y);
                g2.setColor(TEXT_DIM);
                g2.drawString(i * 10 + "%", padL - 40, y + 4);
            }

            if (students == null || students.isEmpty()) {
                g2.setColor(TEXT_DIM);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
                g2.drawString("No students yet — add some from the Students page.", padL + 20, padT + chartH / 2);
                return;
            }

            int barW = Math.min(60, (chartW / students.size()) - 10);
            int spacing = chartW / students.size();

            for (int i = 0; i < students.size(); i++) {
                Student s  = students.get(i);
                double avg = s.getAverage();
                int barH   = (int) (avg / 100.0 * chartH);
                int x      = padL + i * spacing + (spacing - barW) / 2;
                int y      = padT + chartH - barH;

                // Bar color based on letter grade
                Color barColor;
                String letter = s.getLetterGrade();
                if      (letter.equals("A"))                              barColor = SUCCESS;
                else if (letter.equals("B") || letter.equals("C"))       barColor = WARNING;
                else                                                      barColor = DANGER;

                // Draw bar with rounded top
                g2.setColor(new Color(barColor.getRed(), barColor.getGreen(), barColor.getBlue(), 180));
                g2.fill(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));
                g2.setColor(barColor);
                g2.draw(new RoundRectangle2D.Float(x, y, barW, barH, 6, 6));

                // Average label above bar
                g2.setColor(TEXT);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                String avgStr = String.format("%.0f%%", avg);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(avgStr, x + (barW - fm.stringWidth(avgStr)) / 2, y - 5);

                // Student name below bar
                g2.setColor(TEXT_DIM);
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
                String name = s.getName().split(" ")[0]; // first name only
                g2.drawString(name, x + (barW - fm.stringWidth(name)) / 2, padT + chartH + 20);

                // Letter grade
                g2.setColor(barColor);
                g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                g2.drawString(s.getLetterGrade(), x + (barW - fm.stringWidth(s.getLetterGrade())) / 2, padT + chartH + 38);
            }
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(BG); return p;
    }

    private JPanel pageTitle(String text) {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 20));
        bar.setBackground(BG);
        bar.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        JLabel lbl = new JLabel(text);
        lbl.setFont(FONT_TITLE); lbl.setForeground(TEXT);
        bar.add(lbl); return bar;
    }

    private JTextField styledField(String placeholder, int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_BODY); f.setForeground(TEXT_DIM);
        f.setBackground(new Color(50, 50, 70)); f.setCaretColor(TEXT);
        f.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(6, 10, 6, 10)));
        f.setText(placeholder);
        f.addFocusListener(new FocusAdapter() {
            public void focusGained(FocusEvent e) { if (f.getText().equals(placeholder)) { f.setText(""); f.setForeground(TEXT); } }
            public void focusLost(FocusEvent e)   { if (f.getText().isEmpty()) { f.setText(placeholder); f.setForeground(TEXT_DIM); } }
        });
        return f;
    }

    private JButton accentButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BODY); btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JButton dangerButton(String label) {
        JButton btn = new JButton(label);
        btn.setFont(FONT_BODY); btn.setForeground(Color.WHITE);
        btn.setBackground(DANGER); btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 18, 8, 18));
        return btn;
    }

    private JTable styledTable(DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFont(FONT_BODY); table.setForeground(TEXT);
        table.setBackground(CARD_BG); table.setRowHeight(36);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(ACCENT); table.setSelectionForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_SMALL); header.setForeground(TEXT_DIM);
        header.setBackground(PANEL_BG); header.setBorder(new MatteBorder(0, 0, 1, 0, BORDER));
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object v, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, v, sel, foc, r, c);
                setBackground(sel ? ACCENT : (r % 2 == 0 ? CARD_BG : new Color(33, 33, 48)));
                setBorder(new EmptyBorder(0, 12, 0, 12));
                // Color the Status and Grade columns
                if (c == 7) setForeground(v != null && v.toString().equals("Pass") ? SUCCESS : DANGER);
                else if (c == 6) {
                    String g = v == null ? "" : v.toString();
                    setForeground(switch (g) { case "A" -> SUCCESS; case "B","C" -> WARNING; default -> DANGER; });
                } else setForeground(sel ? Color.WHITE : TEXT);
                return this;
            }
        };
        for (int i = 0; i < model.getColumnCount(); i++)
            table.getColumnModel().getColumn(i).setCellRenderer(renderer);
        return table;
    }

    private JScrollPane styledScroll(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(new EmptyBorder(10, 30, 20, 30));
        scroll.getViewport().setBackground(CARD_BG);
        return scroll;
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}