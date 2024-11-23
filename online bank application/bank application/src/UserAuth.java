import java.sql.*;

public class UserAuth {

    @SuppressWarnings("CallToPrintStackTrace")
    public static boolean authenticate(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return true;  // User exists and credentials are valid
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;  // Authentication failed
    }
}
