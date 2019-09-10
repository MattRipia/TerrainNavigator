package terrainnavigator;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Database 
{
    public Connection conn = null;
    public Statement stmt = null;
    public final String DRIVER = "com.mysql.jdbc.Driver";
    
    public Database() throws SQLException
    {
        try 
        {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection("jdbc:mysql://raptor2.aut.ac.nz:3306/terrains", "student", "fpn871");
            stmt = conn.createStatement();
            System.out.println("Connected");
        } 
        catch (ClassNotFoundException | SQLException ex) 
        {
            Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ResultSet queryDB(String query)
    {
        if(stmt != null)
        {
            try 
            {
                return stmt.executeQuery(query);
            } 
            catch (SQLException ex) 
            {
                Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return null;
    }
}

