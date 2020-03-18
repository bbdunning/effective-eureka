import java.sql.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

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
        //System.out.println("Creating new Reservation");
        //System.out.print("Please enter first name of reservation: ");


        //System.out.print("Please enter Last name of reservation: ");

        //System.out.println("Enter desired room code or \"Any\" for no preference: ");
    }

    private static void FRthree(Connection conn) throws SQLException
    {
        Scanner scanner = new Scanner(System.in);
        String code = "";
        String firstName = "";
        String lastName = "";
        String beginDate = "";
        String endDate = "";
        String numChild = "";
        String numAdult = "";
      
        //Construct SQL statement
        System.out.print("Enter a reservation code: ");
        code = scanner.nextLine();
        System.out.print("Enter a new first name (or Enter for no change): ");
        firstName = scanner.nextLine();
        System.out.print("Enter a new last name (or Enter for no change): ");
        lastName = scanner.nextLine();
        System.out.print("Enter a new begin date (or Enter for no change): ");
        beginDate = scanner.nextLine();
        System.out.print("Enter a new end date (or Enter for no change): ");
        endDate = scanner.nextLine();
        System.out.print("Enter a new number of children (or Enter for no change): ");
        numChild = scanner.nextLine();
        System.out.print("Enter a new number of adults (or Enter for no change): ");
        numAdult = scanner.nextLine();

        //update firstname if changed
        if (!firstName.equals("")) {
            String updateSql = "UPDATE lab7_reservations SET FirstName = ? WHERE CODE = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, firstName); 
                pstmt.setInt(2, Integer.valueOf(code));
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                System.out.format("Updated %d reservation%n", rowCount);

                // Step 6: Commit or rollback transaction
                conn.commit();
            } 
            catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        }

        //update lastname if changed
        if (!lastName.equals("")) {
            String updateSql = "UPDATE lab7_reservations SET LastName = ? WHERE CODE = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setString(1, lastName); 
                pstmt.setInt(2, Integer.valueOf(code));
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                System.out.format("Updated %d reservation%n", rowCount);

                // Step 6: Commit or rollback transaction
                conn.commit();
            } 
            catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        }

        //update begin date if changed
        if (!beginDate.equals("") || !endDate.equals("")) {
            conn.setAutoCommit(false);

           //get start & end dates
           if (beginDate.equals("")) {
              String stmt = 
                 "select checkin " +
                 "from lab7_reservations " +
                 "where code = ?";
              try (PreparedStatement pstmt = conn.prepareStatement(stmt)) {
                  pstmt.setString(1, code);
                  ResultSet res = pstmt.executeQuery();
                  if(res.next()){
                     beginDate = res.getString(1);
                     System.out.printf("beginDate: %s\n", beginDate);
                  }
              }
           }
           if (endDate.equals("")) {
              String stmt = 
                 "select checkout " +
                 "from lab7_reservations " +
                 "where code = ?";
              try (PreparedStatement pstmt = conn.prepareStatement(stmt)) {
                  pstmt.setString(1, code);
                  ResultSet res = pstmt.executeQuery();
                  if(res.next()){
                     endDate = res.getString(1);
                     System.out.printf("endDate: %s\n", endDate);
                  }
              }
           }

           //check for conflicts
           String getRoomStmt = 
              "select room " +
              "from lab7_reservations " +
              "where code = ?";
          
           String stmt = 
              "select count(*) " +
              "from lab7_reservations " +
              "where room = ? and code != ? " +
                 "and ((? >= CheckIn and ? <= Checkout) " +
                 "or (? > CheckIn and ? <= Checkout) " +
                 "or (? < CheckIn and ? > Checkout))";

           try (PreparedStatement pstmt1 = conn.prepareStatement(getRoomStmt)) {
               pstmt1.setString(1, code);
               ResultSet res1 = pstmt1.executeQuery();
               String roomName = "";
               if(res1.next()){
                  roomName = res1.getString(1);
               }

               try (PreparedStatement pstmt2 = conn.prepareStatement(stmt)) {
                  pstmt2.setString(1, roomName);
                  pstmt2.setInt(2, Integer.valueOf(code));
                  pstmt2.setDate(3, java.sql.Date.valueOf(beginDate));
                  pstmt2.setDate(4, java.sql.Date.valueOf(beginDate));
                  pstmt2.setDate(5, java.sql.Date.valueOf(endDate));
                  pstmt2.setDate(6, java.sql.Date.valueOf(endDate));
                  pstmt2.setDate(7, java.sql.Date.valueOf(beginDate));
                  pstmt2.setDate(8, java.sql.Date.valueOf(endDate));

                  ResultSet res = pstmt2.executeQuery();
                  res.next();
                  int resCount = res.getInt("count(*)");
                  if (resCount == 0) {
                     String updateSql = "UPDATE lab7_reservations SET checkIn = ?, checkout = ? WHERE CODE = ?";

                     try (PreparedStatement pstmt3 = conn.prepareStatement(updateSql)) {

                         pstmt3.setDate(1, java.sql.Date.valueOf(beginDate)); 
                         pstmt3.setDate(2, java.sql.Date.valueOf(endDate)); 
                         pstmt3.setInt(3, Integer.valueOf(code)); 
                         int rowCount = pstmt3.executeUpdate();

                         System.out.format("Updated %d reservation%n", rowCount);

                         conn.commit();
                     } 
                     catch (SQLException e) {
                         e.printStackTrace();
                         conn.rollback();
                     }
                  }
                  else {
                     System.out.println("conflict in dates");
                  }
               }
            }
        }

        if (!numChild.equals("")) {
            String updateSql = "UPDATE lab7_reservations SET LastName = ? WHERE CODE = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setInt(1, Integer.valueOf(numChild)); 
                pstmt.setInt(2, Integer.valueOf(code));
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                System.out.format("Updated %d reservation%n", rowCount);

                // Step 6: Commit or rollback transaction
                conn.commit();
            } 
            catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        }
        if (!numAdult.equals("")) {
            String updateSql = "UPDATE lab7_reservations SET LastName = ? WHERE CODE = ?";
            conn.setAutoCommit(false);

            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

                // Step 4: Send SQL statement to DBMS
                pstmt.setInt(1, Integer.valueOf(numAdult)); 
                pstmt.setInt(2, Integer.valueOf(code));
                int rowCount = pstmt.executeUpdate();

                // Step 5: Handle results
                System.out.format("Updated %d reservation%n", rowCount);

                // Step 6: Commit or rollback transaction
                conn.commit();
            } 
            catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
            }
        }
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
            FRthree(conn);
            
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }   

       
        
    }
}
