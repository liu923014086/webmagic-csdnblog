package csdnblog;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CsdnBlogDao {

	private Connection conn = null;
	private Statement stmt = null;

	public CsdnBlogDao() {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://localhost:3306/webmagic?user=root&password=1234&useUnicode=true&characterEncoding=utf-8";
			conn = DriverManager.getConnection(url);
			stmt = conn.createStatement();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		CsdnBlog csdnBlog = new CsdnBlog();
		csdnBlog.setCategory("category");
		csdnBlog.setTitle("中文测试");
		new CsdnBlogDao().add(csdnBlog);
	}

	public int add(CsdnBlog csdnBlog) {
		try {
			System.out.println("开始保存 = [" + csdnBlog + "]");
			String sql = "INSERT INTO `webmagic`.`csdnblog` (`id`, `title`, `date`, `tags`, `category`, `view`, `comments`, `copyright`) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
			PreparedStatement ps = conn.prepareStatement(sql);
			ps.setInt(1, csdnBlog.getId());
			ps.setString(2, csdnBlog.getTitle());
			ps.setString(3, csdnBlog.getDate());
			ps.setString(4, csdnBlog.getTags());
			ps.setString(5, csdnBlog.getCategory());
			ps.setInt(6, csdnBlog.getView());
			ps.setInt(7, csdnBlog.getComments());
			ps.setInt(8, csdnBlog.getCopyright());
			return ps.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
	}

}
