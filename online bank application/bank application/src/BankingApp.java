import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BankingApp {

    @SuppressWarnings("Convert2Lambda")
    public static void main(String[] args) {
        JFrame loginFrame = new JFrame("Banking Login");
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(400, 300);
        loginFrame.setLayout(new GridLayout(3, 2));

        JLabel lblEmail = new JLabel("Email:");
        JLabel lblPassword = new JLabel("Password:");
        JTextField txtEmail = new JTextField();
        JPasswordField txtPassword = new JPasswordField();
        JButton btnLogin = new JButton("Login");

        loginFrame.add(lblEmail);
        loginFrame.add(txtEmail);
        loginFrame.add(lblPassword);
        loginFrame.add(txtPassword);
        loginFrame.add(new JLabel());
        loginFrame.add(btnLogin);

        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());

                if (UserAuth.authenticate(email, password)) {
                    loginFrame.setVisible(false);  // Hide login window
                    new BankingDashboard(email).setVisible(true);  // Show dashboard
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "Invalid login credentials.");
                }
            }
        });

        loginFrame.setVisible(true);
    }
}
