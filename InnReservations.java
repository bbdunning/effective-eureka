import java.sql.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InnReservations
{
    private static void FRone(Connection conn)
    {
        try
        {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                "select rooms.*, f.pop, s.Checkin, t.CheckOut " +
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
                "inner join lab7_rooms as rooms on RoomCode = f.Room " +
                "order by f.pop;");

            while (rs.next())
            {
                String roomCode = rs.getString("RoomCode");
                String roomName = rs.getString("RoomName");
                int numBeds = rs.getInt("Beds");
                String bedType = rs.getString("bedType");
                int maxOcc = rs.getInt("maxOcc");
                double basePrice = rs.getDouble("basePrice");
                String decor = rs.getString("decor");

                String pop = rs.getString("pop");
                String checkin = rs.getString("Checkin");
                String checkout = rs.getString("CheckOut");
                // System.out.println(roomCode + "  " + roomName + "  " 
                //     + numBeds + "  " + bedType + " "
                //     + maxOcc + " " + basePrice + " "
                //     + decor + " " + pop + " "
                //     + checkin + " " + checkout);
                System.out.printf("%5s  %-25s %2d %-8s %d %-6.2f %-15s %-4s %10s %10s\n", roomCode, roomName, numBeds, 
                    bedType, maxOcc, basePrice, decor, pop, checkin, checkout);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }   
            

    }

    private static void learningOne(Connection conn)
    {
       

        try
        {
            
            Statement stmt = conn.createStatement();

            //Manually has the 180 day back date and current date
            ResultSet rs = stmt.executeQuery(
                "Select Room, round(Sum(DATEDIFF(CheckOut, Greatest(DATE_SUB(CURDATE(), INTERVAL 180 DAY), Checkin))) / 180, 2) as pop " +
                "from lab7_reservations " +
                "where DATEDIFF(CURDATE(), CheckOut) <= 180 " +
                "and DATEDIFF(CURDATE(), CheckOut) > 0 " + 
                "group by Room " +
                "order by pop desc;");

            while (rs.next())
            {
                String RoomCode = rs.getString("Room");
                String count = rs.getString("pop");
                System.out.println(RoomCode + "  " + count);
            }
            System.out.println("\n\n");

            Statement stmt2 = conn.createStatement();
            ResultSet rs2 = stmt2.executeQuery("select Room, Checkin " +
            "from " +
            "( " +
                "Select distinct Room, Checkin, DATEDIFF(Checkin, CURDATE()) as diff, " +
                    "min(DATEDIFF(Checkin, CURDATE())) over (partition by Room) as mindiff " +
                "from lab7_reservations " +
                "where DATEDIFF(Checkin, CURDATE()) > 0 " +
            ") a " +
            "where diff = mindiff;"); 

            while (rs2.next())
            {
                String RoomCode = rs2.getString("Room");
                String checkin = rs2.getString("Checkin");
                System.out.println(RoomCode + "  " + checkin);
            }
            System.out.println("\n\n");
            Statement stmt3 = conn.createStatement();
            ResultSet rs3 = stmt3.executeQuery("select Room, CheckOut " +
            "from " +
            "( " +
                "Select distinct Room, CheckOut, DATEDIFF(CURDATE(), CheckOut) as diff, " +
                    "min(DATEDIFF(CURDATE(), CheckOut)) over (partition by Room) as mindiff " +
                "from lab7_reservations " +
                "where DATEDIFF(CURDATE(), CheckOut) > 0 " +
            ") a " +
            "where diff = mindiff;"); 

            while (rs3.next())
            {
                String RoomCode = rs3.getString("Room");
                String checkout = rs3.getString("CheckOut");
                System.out.println(RoomCode + "  " + checkout);
            }

        } catch (SQLException e)
        {
            e.printStackTrace();
        }



//        try
//        {
//            Connection conn = DriverManager.getConnection("jdbc:mysql://csc365.toshikuboi.net:3306/group08", "group08", "group08@CSC365");
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery("Select * from lab7_reservations");
//
////            DateFormat df = new SimpleDateFormat("MM/dd/yy");
////            Date dateobj = new Date();
////            System.out.println(df.format(dateobj));
//
////            Calendar cal = Calendar.getInstance();
////
////            System.out.println(cal.getTime());
////            System.out.println(cal.get(Calendar.MONTH) + 1);
////            System.out.println(cal.get(Calendar.DATE));
////            System.out.println(cal.get(Calendar.YEAR));
//
//            while (rs.next())
//            {
//                String RoomCode = rs.getString("CODE");
//                String checkIn = rs.getString("CheckIn");
//                String Checkout = rs.getString("Checkout");
//
//                System.out.println(RoomCode + "  " + checkIn + "  " + Checkout);
//
//            }
//        } catch (SQLException e)
//        {
//            e.printStackTrace();
//        }
    }

    private static void FRtwo()
    {
        System.out.println("Creating new Reservation");
        System.out.print("Please enter first name of reservation: ");


        System.out.print("Please enter Last name of reservation: ");

        System.out.println("Enter desired room code or \"Any\" for no preference: ");
    }

    public static void main(String[] args)
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
            Connection conn = DriverManager.getConnection(
                "jdbc:mysql://db.labthreesixfive.com/rjmiddle?autoReconnect=true&useSSL=false",
                "rjmiddle",
                "WinterTwenty20_365_013793295");

            FRone(conn);

            FRtwo();
            
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }   

       
        
    }
}
