package com.careerverse.dao;
import com.careerverse.config.DB; import java.sql.*;
public class UserDAO {
 public static Integer findIdByEmail(String email)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement s=c.prepareStatement("SELECT id FROM users WHERE email=?")){s.setString(1,email.toLowerCase());try(ResultSet r=s.executeQuery()){return r.next()?r.getInt(1):null;}}}
 public static Result find(String email)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement s=c.prepareStatement("SELECT id,name,email,password_hash FROM users WHERE email=?")){s.setString(1,email.toLowerCase());try(ResultSet r=s.executeQuery()){if(!r.next())return null;return new Result(r.getInt(1),r.getString(2),r.getString(3),r.getString(4));}}}
 public record Result(int id,String name,String email,String hash){}
 public static int create(String name,String email,String hash)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement s=c.prepareStatement("INSERT INTO users(name,email,password_hash) VALUES(?,?,?)",Statement.RETURN_GENERATED_KEYS)){s.setString(1,name);s.setString(2,email.toLowerCase());s.setString(3,hash);s.executeUpdate();try(ResultSet r=s.getGeneratedKeys()){r.next();int id=r.getInt(1);try(PreparedStatement p=c.prepareStatement("INSERT INTO profiles(user_id) VALUES(?)")){p.setInt(1,id);p.executeUpdate();}return id;}}}
 public static void changePassword(int id,String hash)throws SQLException{try(Connection c=DB.getConnection();PreparedStatement s=c.prepareStatement("UPDATE users SET password_hash=? WHERE id=?")){s.setString(1,hash);s.setInt(2,id);s.executeUpdate();}}
}
