import javax.swing.*;
import java.awt.*;
import java.io.File;

public class RepligenValidationApplication extends JFrame {

    JLabel l1, l2, l3;
    JTextField tf1;
    JButton btn2, btn1;

    /* Doc: Swing Application to Debug KFComm logs*/
    RepligenValidationApplication() {
        JFrame frame = new JFrame("KFComm Debugger");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        l1 = new JLabel("KFComm Debugger");
        l1.setForeground(Color.gray);
        l1.setFont(new Font("Sherif", Font.BOLD, 26));

        l2 = new JLabel("Folder path:");
        l2.setFont(new Font("Sherif", Font.BOLD, 18));

        l3 = new JLabel("Note: The application notes down exception in the given log files.");
        l3.setFont(new Font("Sheriff", Font.ITALIC, 12));

        tf1 = new JTextField();

        // defining the browse button
        btn1 = new JButton("Browse");
        btn1.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(frame);
            if (option == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                tf1.setText(file.getAbsolutePath());
                tf1.setFont(new Font("Sherif", Font.BOLD, 14));
            }
        });

        // defining the debug button
        btn2 = new JButton("Debug");
        btn2.addActionListener(e -> {
            String result = GetExceptionFromLog.getException(tf1.getText());
            // Unsuccessful debugging
            if(result.equals("FAIL")) {
                JOptionPane.showMessageDialog(this, "Please enter a valid folder path",
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
            else{
                JOptionPane.showMessageDialog(frame, "File generated at "+ result, "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        l1.setBounds(230, 50, 400, 30);
        l2.setBounds(80, 110, 280, 30);
        l3.setBounds(80, 130, 400, 30);
        tf1.setBounds(250, 110, 230, 30);
        btn1.setBounds(480,110,80,30);
        btn2.setBounds(260, 180, 100, 30);

        frame.add(l1);
        frame.add(l2);
        frame.add(l3);
        frame.add(tf1);
        frame.add(btn1);
        frame.add(btn2);

        frame.setSize(670, 400);
        frame.setLayout(null);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        new RepligenValidationApplication();
    }

}
