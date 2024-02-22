package jdbc;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SimpleJDBCRepository {

    private Connection connection = null;
    private PreparedStatement ps = null;
    private Statement st = null;

    private static final String createUserSQL = "insert into myusers (id, firstname, lastname, age) values (?,?,?,?)";
    private static final String updateUserSQL = "update myusers set id=?, firstname=?, lastname=?, age=?";
    private static final String deleteUser = "delete from myusers where id=?";
    private static final String findUserByIdSQL = "select * from myusers where id=?";
    private static final String findUserByNameSQL = "select * from myusers where firstname=?";
    private static final String findAllUserSQL = "select * from myusers";


    public Long createUser() {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        User user = new User();
        try {
            ps = connection.prepareStatement(createUserSQL, Statement.RETURN_GENERATED_KEYS);
            int i = 0;
            ps.setLong(++i, user.getId());
            ps.setString(++i, user.getFirstName());
            ps.setString(++i, user.getLastName());
            ps.setInt(++i, user.getAge());
            ResultSet rs = ps.getGeneratedKeys();
            while (rs.next()){
                user.setId(rs.getLong(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeResources();
        }
        return user.getId();

    }

    public User findUserById(Long userId) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ps = connection.prepareStatement(findUserByIdSQL);
            ps.setLong(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeResources();
        }
        throw new IllegalArgumentException("User not found");
    }

    public User findUserByName(String userName) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ps = connection.prepareStatement(findUserByNameSQL);
            ps.setString(1, userName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()){
                return mapResultSetToUser(rs);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeResources();
        }
        throw new IllegalArgumentException("User not found");
    }

    public List<User> findAllUser() {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        List<User> users = new ArrayList<>();
        try {
            ps = connection.prepareStatement(findAllUserSQL);
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                User user = mapResultSetToUser(rs);
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        finally {
            closeResources();
        }
        return users;
    }

    public User updateUser(User user) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ps = connection.prepareStatement(updateUserSQL);
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setInt(3, user.getAge());
            ps.setLong(4, user.getId());
            ps.executeUpdate();
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("Can't update user");
        } finally {
            closeResources();
        }

    }

    private void deleteUser(Long userId) {
        try {
            connection = CustomDataSource.getInstance().getConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        try {
            ps = connection.prepareStatement(deleteUser);
            ps.setLong(1, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new IllegalArgumentException("User not found");
        } finally {
            closeResources();
        }
    }
    private User mapResultSetToUser(ResultSet resultSet) throws SQLException {
        return new User(
                resultSet.getLong("id"),
                resultSet.getString("firstname"),
                resultSet.getString("lastname"),
                resultSet.getInt("age")
        );
    }

    private void closeResources() {
        try {
            if (ps != null) {
                ps.close();
            }
            if (st != null) {
                st.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
