import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.net.URL;
import java.net.HttpURLConnection;


public class ToDoList {
    private JFrame frame;
    private DefaultListModel<String> listModel;
    private JList<String> taskList;
    private JTextField taskInput;
    private JButton addButton, removeButton, doneButton, clearButton, reminderButton;
    private JComboBox<String> sortByBox, filterBox;
    private final String FILE_NAME = "tasks.txt";

    public ToDoList() {
        // Frame setup
        frame = new JFrame("To-Do List");
        frame.setSize(550, 550);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Task input panel
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 10, 10)); // Adds space from the top

        taskInput = new JTextField();
        taskInput.setFont(new Font("Arial", Font.PLAIN, 20)); // Increase font size
        taskInput.setPreferredSize(new Dimension(0, 40)); // Increase height
        taskInput.addActionListener(e -> addTask());

        topPanel.add(taskInput, BorderLayout.CENTER);


        // Sort and Filter Panel
        JPanel sortFilterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        sortByBox = new JComboBox<>(new String[]{
                "Sort: Default",
                "Sort: A-Z",
                "Sort: Done First",
                "Sort: Pending First",
                "Sort: High to Low Priority",
                "Sort: Low to High Priority"
        });
        filterBox = new JComboBox<>(new String[]{"Filter: All", "Filter: Completed", "Filter: Pending"});
        sortByBox.addActionListener(e -> sortTasks());
        filterBox.addActionListener(e -> filterTasks());

        sortFilterPanel.add(sortByBox);
        sortFilterPanel.add(filterBox);
        topPanel.add(sortFilterPanel, BorderLayout.SOUTH);

        frame.add(topPanel, BorderLayout.NORTH);

        // Task list
        listModel = new DefaultListModel<>();
        loadTasks();
        taskList = new JList<>(listModel);
        taskList.setFont(new Font("Arial", Font.PLAIN, 16));
        taskList.setCellRenderer(new TaskRenderer());
        frame.add(new JScrollPane(taskList), BorderLayout.CENTER);

        // Buttons Panel
        JPanel panel = new JPanel(new GridLayout(1, 5, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addButton = createButton("Add", new Color(76, 175, 80), "src/add.png");
        removeButton = createButton("Remove", new Color(255, 87, 51), "src/remove.png");
        doneButton = createButton("Done", new Color(33, 150, 243), "src/done.png");
        clearButton = createButton("Clear", new Color(128, 128, 128), "src/clear.png");
        reminderButton = createButton("Reminder", new Color(255, 165, 0), "src/reminder.png");

        panel.add(addButton);
        panel.add(removeButton);
        panel.add(doneButton);
        panel.add(clearButton);
        panel.add(reminderButton);
        frame.add(panel, BorderLayout.SOUTH);

        // Button Listeners
        addButton.addActionListener(e -> addTask());
        removeButton.addActionListener(e -> removeTask());
        doneButton.addActionListener(e -> markAsDone());
        clearButton.addActionListener(e -> clearAllTasks());
        reminderButton.addActionListener(e -> setReminder());

        // Show Frame
        frame.setVisible(true);
    }

    private JButton createButton(String text, Color bgColor, String iconPath) {
        JButton button = new JButton(text);
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);

        // Load and resize icon
        ImageIcon icon = new ImageIcon(iconPath);
        Image img = icon.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH); // Adjust size as needed
        button.setIcon(new ImageIcon(img));

        return button;
    }

    private void addTask() {
        String task = taskInput.getText().trim();
        if (!task.isEmpty()) {
            String priority = predictPriority(task);
            String taskWithPriority = "[" + priority + "] " + task;
            listModel.addElement(taskWithPriority);
            taskInput.setText("");
            saveTasks();
            sortTasks();
            filterTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Task cannot be empty!");
        }
    }
    private String predictPriority(String taskText) {
        try {
            URL url = new URL("http://127.0.0.1:5000/predict");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);

            String jsonInput = "{\"task\": \"" + taskText.replace("\"", "\\\"") + "\"}";
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonInput.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
            StringBuilder response = new StringBuilder();
            String responseLine;

            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }

            return response.toString().replace("\"", ""); // remove quotes from response
        } catch (Exception e) {
            e.printStackTrace();
            return "Medium"; // fallback
        }
    }



    private void removeTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            listModel.remove(selectedIndex);
            saveTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Select a task to remove!");
        }
    }

    private void markAsDone() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = listModel.get(selectedIndex);
            if (task.startsWith("[Done] ")) {
                task = task.replace("[Done] ", ""); // Toggle back to pending
            } else {
                task = "[Done] " + task;
            }
            listModel.set(selectedIndex, task);
            saveTasks();
        } else {
            JOptionPane.showMessageDialog(frame, "Select a task to toggle status!");
        }
    }


    private void setReminder() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            String task = listModel.get(selectedIndex);
            String dateTime = JOptionPane.showInputDialog(frame, "Enter reminder date & time (dd/MM/yyyy HH:mm):");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                Date reminderTime = sdf.parse(dateTime);
                long delay = reminderTime.getTime() - System.currentTimeMillis();

                if (delay > 0) {
                    new Timer().schedule(new TimerTask() {
                        public void run() {
                            JOptionPane.showMessageDialog(frame, "Reminder: " + task);
                        }
                    }, delay);
                    JOptionPane.showMessageDialog(frame, "Reminder set for: " + dateTime);
                } else {
                    JOptionPane.showMessageDialog(frame, "Enter a future date & time.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid format! Use dd/MM/yyyy HH:mm.");
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Select a task to set a reminder!");
        }
    }

    private void clearAllTasks() {
        int response = JOptionPane.showConfirmDialog(frame, "Clear all tasks?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (response == JOptionPane.YES_OPTION) {
            listModel.clear();
            saveTasks();
        }
    }

    private void sortTasks() {
        List<String> tasks = Collections.list(listModel.elements());
        String selectedSort = (String) sortByBox.getSelectedItem();

        switch (selectedSort) {
            case "Sort: A-Z":
                Collections.sort(tasks);
                break;
            case "Sort: Done First":
                tasks.sort(Comparator.comparing(t -> !t.startsWith("[Done] ")));
                break;
            case "Sort: Pending First":
                tasks.sort(Comparator.comparing(t -> t.startsWith("[Done] ")));
                break;
            case "Sort: High to Low Priority":
                tasks.sort((a, b) -> getPriorityWeight(a) - getPriorityWeight(b));
                break;
            case "Sort: Low to High Priority":
                tasks.sort((a, b) -> getPriorityWeight(b) - getPriorityWeight(a));
                break;
            default:
                return;
        }

        listModel.clear();
        tasks.forEach(listModel::addElement);
        saveTasks();
    }
    private int getPriorityWeight(String task) {
        if (task.contains("[High]")) return 1;
        if (task.contains("[Medium]")) return 2;
        if (task.contains("[Low]")) return 3;
        return 4; // unknown or no label
    }


    private void filterTasks() {
        String selectedFilter = (String) filterBox.getSelectedItem();
        listModel.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (selectedFilter.equals("Filter: All") ||
                        (selectedFilter.equals("Filter: Completed") && line.startsWith("[Done] ")) ||
                        (selectedFilter.equals("Filter: Pending") && !line.startsWith("[Done] "))) {
                    listModel.addElement(line);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class TaskRenderer extends DefaultListCellRenderer {
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String task = value.toString();
            // Default values
            Color foregroundColor = Color.BLACK;
            Font font = c.getFont().deriveFont(Font.PLAIN);

// Priority-based colors
//            if (task.contains("[High]")) {
//                foregroundColor = new Color(64, 64, 64); // Dark Gray
//            } else if (task.contains("[Medium]")) {
//                foregroundColor = new Color(100, 100, 100); // Medium-Dark Gray
//            } else if (task.contains("[Low]")) {
//                foregroundColor = new Color(140, 140, 140); // Still Light Gray but darker than before
//            }



// Done overrides the color and style
            if (task.startsWith("[Done] ")) {
                foregroundColor = new Color(34, 139, 34); // Green
                font = c.getFont().deriveFont(Font.ITALIC | Font.BOLD);
            }

            c.setForeground(foregroundColor);
            c.setFont(font);

            return c;
        }
    }

    private void saveTasks() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (int i = 0; i < listModel.size(); i++) {
                writer.println(listModel.getElementAt(i));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadTasks() {
        File file = new File(FILE_NAME);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    listModel.addElement(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ToDoList::new);
    }
}