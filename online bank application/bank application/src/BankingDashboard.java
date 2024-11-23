import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class BankingDashboard extends JFrame {

    private JPanel contentPanel;
    private JLabel lblBalance;
    private JTextArea transactionHistoryArea;
    private JButton btnTransfer;
    private String currentUserEmail;

    @SuppressWarnings("Convert2Lambda")
    public BankingDashboard(String email) {
        currentUserEmail = email;
        setTitle("Banking Dashboard");
        setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));

        lblBalance = new JLabel("Account Balance: $0.00");
        lblBalance.setFont(new Font("Arial", Font.BOLD, 16));
        contentPanel.add(lblBalance);

        transactionHistoryArea = new JTextArea(10, 30);
        transactionHistoryArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(transactionHistoryArea);
        contentPanel.add(scrollPane);

        btnTransfer = new JButton("Transfer Money");
        btnTransfer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTransferDialog();
            }
        });
        contentPanel.add(btnTransfer);

        add(contentPanel, BorderLayout.CENTER);

        // Load account balance and transaction history
        loadAccountDetails();
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void loadAccountDetails() {
        try (Connection con = DatabaseConnection.getConnection()) {
            // Fetch account balance
            String balanceQuery = "SELECT balance FROM users WHERE email = ?";
            try (PreparedStatement ps = con.prepareStatement(balanceQuery)) {
                ps.setString(1, currentUserEmail);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double balance = rs.getDouble("balance");
                    lblBalance.setText("Account Balance: $" + balance);
                }
            }

            // Fetch transaction history
            String transactionQuery = "SELECT * FROM transactions WHERE user_id = (SELECT id FROM users WHERE email = ?)";
            try (PreparedStatement ps = con.prepareStatement(transactionQuery)) {
                ps.setString(1, currentUserEmail);
                ResultSet rs = ps.executeQuery();
                StringBuilder history = new StringBuilder();
                while (rs.next()) {
                    String type = rs.getString("type");
                    double amount = rs.getDouble("amount");
                    String description = rs.getString("description");
                    history.append(type).append(" of $").append(amount).append(": ").append(description).append("\n");
                }
                transactionHistoryArea.setText(history.toString());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void showTransferDialog() {
        JTextField recipientField = new JTextField(20);
        JTextField amountField = new JTextField(20);
        JTextField descriptionField = new JTextField(20);

        JPanel transferPanel = new JPanel();
        transferPanel.setLayout(new GridLayout(3, 2));
        transferPanel.add(new JLabel("Recipient Email:"));
        transferPanel.add(recipientField);
        transferPanel.add(new JLabel("Amount:"));
        transferPanel.add(amountField);
        transferPanel.add(new JLabel("Description:"));
        transferPanel.add(descriptionField);

        int option = JOptionPane.showConfirmDialog(this, transferPanel, "Enter Transfer Details", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String recipientEmail = recipientField.getText();
            double amount = Double.parseDouble(amountField.getText());
            String description = descriptionField.getText();
            processTransfer(recipientEmail, amount, description);
        }
    }

    @SuppressWarnings("CallToPrintStackTrace")
    private void processTransfer(String recipientEmail, double amount, String description) {
        try (Connection con = DatabaseConnection.getConnection()) {
            // Fetch sender ID
            String senderQuery = "SELECT id, balance FROM users WHERE email = ?";
            PreparedStatement ps = con.prepareStatement(senderQuery);
            ps.setString(1, currentUserEmail);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int senderId = rs.getInt("id");
                double senderBalance = rs.getDouble("balance");

                if (senderBalance >= amount) {
                    // Deduct amount from sender's account
                    String updateSenderQuery = "UPDATE users SET balance = balance - ? WHERE id = ?";
                    ps = con.prepareStatement(updateSenderQuery);
                    ps.setDouble(1, amount);
                    ps.setInt(2, senderId);
                    ps.executeUpdate();

                    // Add transaction to the history table
                    String transactionQuery = "INSERT INTO transactions (user_id, amount, type, description) VALUES (?, ?, 'debit', ?)";
                    ps = con.prepareStatement(transactionQuery);
                    ps.setInt(1, senderId);
                    ps.setDouble(2, amount);
                    ps.setString(3, description);
                    ps.executeUpdate();

                    // Add amount to recipient's account
                    String recipientQuery = "SELECT id FROM users WHERE email = ?";
                    ps = con.prepareStatement(recipientQuery);
                    ps.setString(1, recipientEmail);
                    rs = ps.executeQuery();
                    if (rs.next()) {
                        int recipientId = rs.getInt("id");
                        String updateRecipientQuery = "UPDATE users SET balance = balance + ? WHERE id = ?";
                        ps = con.prepareStatement(updateRecipientQuery);
                        ps.setDouble(1, amount);
                        ps.setInt(2, recipientId);
                        ps.executeUpdate();

                        // Add recipient transaction history
                        String recipientTransactionQuery = "INSERT INTO transactions (user_id, amount, type, description) VALUES (?, ?, 'credit', ?)";
                        ps = con.prepareStatement(recipientTransactionQuery);
                        ps.setInt(1, recipientId);
                        ps.setDouble(2, amount);
                        ps.setString(3, description);
                        ps.executeUpdate();

                        JOptionPane.showMessageDialog(this, "Transfer successful!");
                        loadAccountDetails();  // Reload account balance and transaction history
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Insufficient balance.");
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error processing transfer.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String email = JOptionPane.showInputDialog("Enter your email:");
            new BankingDashboard(email).setVisible(true);
        });
    }

    public JPanel getContentPanel() {
        return contentPanel;
    }

    public void setContentPanel(JPanel contentPanel) {
        this.contentPanel = contentPanel;
    }

    public JLabel getLblBalance() {
        return lblBalance;
    }

    public void setLblBalance(JLabel lblBalance) {
        this.lblBalance = lblBalance;
    }

    public JTextArea getTransactionHistoryArea() {
        return transactionHistoryArea;
    }

    public void setTransactionHistoryArea(JTextArea transactionHistoryArea) {
        this.transactionHistoryArea = transactionHistoryArea;
    }

    public JButton getBtnTransfer() {
        return btnTransfer;
    }

    public void setBtnTransfer(JButton btnTransfer) {
        this.btnTransfer = btnTransfer;
    }

    public String getCurrentUserEmail() {
        return currentUserEmail;
    }

    public void setCurrentUserEmail(String currentUserEmail) {
        this.currentUserEmail = currentUserEmail;
    }
}
