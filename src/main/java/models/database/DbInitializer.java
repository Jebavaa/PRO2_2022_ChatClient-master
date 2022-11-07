package models.database;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbInitializer
{

    private final String driver;
    private final String url;

    public DbInitializer(String driver, String url)
    {
        this.driver = driver;
        this.url = url;
    }

    public void init()
    {
        try
        {
            Class.forName(driver); // nacte jdb:derby driver
            Connection conn = DriverManager.getConnection(url); // otevře spojeni

            String sql = "CRETE TABLE ChatMessages "
                    + "(id INT NOT NULL GENERATED ALWAYS AS IDENTITY"
                        + "CONSTRAINT ChatMessages_PK PRIMARY KEY, "
                    + "author varchar(50), "
                    + "text varchar(1000), "
                    + "create timestamp)";
            Statement statement = conn.createStatement();
            statement.execute(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
