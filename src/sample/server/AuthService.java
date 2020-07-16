package sample.server;

import java.sql.*;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC"); // Инициализация драйвера
            connection = DriverManager.getConnection("jdbc:sqlite:main.db");
            stmt = connection.createStatement(); // Создали стейтмент, с помощью него выполняем запросы
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass (String login, String pass) {
        String sql = String.format("SELECT nickname " +
                                     "FROM main " +
                                    "WHERE login = '%s' " +
                                      "AND password = '%s'", login, pass);

        try {
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getId (String nickname) { // Получаем id пользователя из базы
        String sql = String.format("SELECT id " +
                                    "FROM main " +
                                    "WHERE nickname = '%s'", nickname);
        try {
            ResultSet rs = stmt.executeQuery(sql);

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static void addToBlacklist (ClientHandler from, String blockUserNick) {
        int blockUserId = getId(blockUserNick);
        String sql = String.format("INSERT INTO blacklist (user_id, block_user_id)" +
                                    "VALUES (%d, %d)", from.getId(), blockUserId);

        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeFromBlacklist (ClientHandler from, String nickBlockUser) {
        int idBlockUser = getId(nickBlockUser);
        String sql = String.format("DELETE FROM blacklist " +
                                    "WHERE user_id = %d AND block_user_id = %d", from.getId(), idBlockUser);

        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean checkBlacklist(int user_id, int checked_user_id) {
        String sql = String.format("SELECT bl_id " +
                                    "FROM blacklist " +
                                    "WHERE user_id = %d " +
                                      "AND block_user_id = %d", user_id, checked_user_id);

        ResultSet rs = null;
        try {
            rs = stmt.executeQuery(sql);
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
