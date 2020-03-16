import java.sql.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InnReservations
{


    private static void FRone()
    {
        try
        {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded");

        } catch (ClassNotFoundException ex)
        {
            System.err.println("Unable to load JDBC Driver");
            System.exit(-1);
        }

        try
        {
            Connection conn = DriverManager.getConnection("jdbc:mysql://db.labthreesixfive.com/rjmiddle?autoReconnect=true&useSSL=false",
                    "rjmiddle",
                    "WinterTwenty20_365_013793295");
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                    "select f.Room, f.pop, s.Checkin, t.CheckOut " +
                            "from " +
                            "( " +
                            "Select Room, round(Sum(DATEDIFF(CheckOut, Greatest(DATE_SUB(CURDATE(), INTERVAL 180 DAY), Checkin))) / 180, 2) as pop " +
                            "from lab7_reservations " +
                            "where DATEDIFF(CURDATE(), CheckOut) <= 180 " +
                            "and DATEDIFF(CURDATE(), CheckOut) > 0 " +
                            "group by Room " +
                            ") f " +
                            "inner join " +
                            "( " +
                            "select Room, Checkin " +
                            "from " +
                            "( " +
                            "Select distinct Room, Checkin, DATEDIFF(Checkin, CURDATE()) as diff, " +
                            "min(DATEDIFF(Checkin, CURDATE())) over (partition by Room) as mindiff " +
                            "from lab7_reservations " +
                            "where DATEDIFF(Checkin, CURDATE()) > 0 " +
                            ") si " +
                            "where diff = mindiff " +
                            ") s " +
                            "on f.Room = s.Room " +
                            "inner join " +
                            "( " +
                            "select Room, CheckOut " +
                            "from " +
                            "( " +
                            "Select distinct Room, CheckOut, DATEDIFF(CURDATE(), CheckOut) as diff, " +
                            "min(DATEDIFF(CURDATE(), CheckOut)) over (partition by Room) as mindiff " +
                            "from lab7_reservations " +
                            "where DATEDIFF(CURDATE(), CheckOut) > 0 " +
                            ") ti " +
                            "where diff = mindiff " +
                            ") t " +
                            "on f.Room = t.Room " +
                            "order by f.pop;");

            while (rs.next())
            {
                String RoomCode = rs.getString("Room");
                String count = rs.getString("pop");
                String checkin = rs.getString("Checkin");
                String checkout = rs.getString("CheckOut");
                System.out.println(RoomCode + "  " + count + "  " + checkin + "  " + checkout);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }


    }

    public static void main(String[] args)
    {
        FRone();
    }
}
