import java.sql.*;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

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




    }

    private static void FRtwo(Connection conn)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Creating new Reservation");
        System.out.println("------------------------");
        System.out.print("Please enter first name of reservation: ");
        String firstName = scanner.nextLine();
        System.out.print("Please enter Last name of reservation: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter desired room code or \"Any\" for no preference: ");
        String roomCode = scanner.nextLine();
        System.out.print("Enter desired bed type or \"Any\" for no preference: ");
        String bedType = scanner.nextLine();
        System.out.print("Begin date of stay: ");
        String beginDate = scanner.nextLine();
        System.out.print("End date of stay: ");
        String endDate = scanner.nextLine();
        System.out.print("Number of children: ");
        String children = scanner.nextLine();
        System.out.print("Number of adults: "); 
        String adults = scanner.nextLine();
        System.out.println("------------------------");

        try{
            Statement stmt = conn.createStatement();
            ResultSet maxOcc = stmt.executeQuery("select max(maxOcc) as max from lab7_rooms");
            int maxOccupancyRoom = 0;
            while(maxOcc.next()){    
                maxOccupancyRoom = maxOcc.getInt("max");
            }
            if(maxOccupancyRoom < Integer.valueOf(adults) + Integer.valueOf(children))
            {
                System.out.println("No suitable rooms are available");
                System.exit(-1);
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        /*System.out.printf("%s--%s--%s--%s--%s--%s--%s--%s", firstName, lastName, roomCode,
            bedType, beginDate, endDate, children, adults);*/
        Boolean roomChosen = false;
        Boolean bedTypeChosen = false;
        String gimmeStuff = "SELECT DISTINCT RoomCode, RoomName, bedType, basePrice from lab7_rooms join lab7_reservations on lab7_reservations.Room = lab7_rooms.RoomCode " +
                            "Where MaxOcc >= ? and RoomCode not in (" +
                            "select distinct RoomCode from lab7_reservations join lab7_rooms " +
                                "WHERE lab7_reservations.Room = lab7_rooms.RoomCode and " +
                                    "((? >= CheckIn and ? < CheckOut) or " +
                                    "(? < CheckIn and ? > CheckIn)))";
        if(!roomCode.equals("Any"))
        {
            roomChosen = true;
            gimmeStuff += " and Room = ?";
        }
        if(!bedType.equals("Any"))
        {
            System.out.println("here");
            bedTypeChosen = true;
            gimmeStuff+= " and bedType = ?";
        }

        List<List<String>> results = new ArrayList<List<String>>();
        int nextIndex = 6;
        try (PreparedStatement pstmt = conn.prepareStatement(gimmeStuff)) {
            pstmt.setInt(1, Integer.valueOf(children) + Integer.valueOf(adults));
            pstmt.setDate(2, java.sql.Date.valueOf(beginDate));
            pstmt.setDate(3, java.sql.Date.valueOf(beginDate));
            pstmt.setDate(4, java.sql.Date.valueOf(beginDate));
            pstmt.setDate(5, java.sql.Date.valueOf(endDate));
            if(roomChosen)
            {
                pstmt.setString(nextIndex++,roomCode);
            }
            if(bedTypeChosen)
            {
                pstmt.setString(nextIndex,bedType);
            }
            ResultSet rs = pstmt.executeQuery();
            int matchCount = 0;
            while(rs.next()){
                if(matchCount==0)
                {
                    System.out.println("Here are the rooms that matched your criteria: ");
                }
                List<String> result = new ArrayList<String>();
                result.add(rs.getString("RoomCode"));
                result.add(rs.getString("RoomName"));                    
                result.add(rs.getString("bedType"));
                result.add(rs.getString("basePrice"));
                results.add(result);
                System.out.format("%d) %s--%s\n", ++matchCount, results.get(matchCount-1).get(0), results.get(matchCount-1).get(1));
            }
            //If no matches were found...show them other options
            if(matchCount==0)
            {
                System.out.println("No exact matches were found :(.");
                System.out.println("Here are some similar options based on the dates that you entered");
                GiveSimilarOptions(conn, beginDate, endDate, firstName, lastName, adults, children);
            }
            //If matches were found allow them to select one of the rooms
            else
            {
                System.out.println("If you do not wish to book any of these rooms enter *cancel*");
                System.out.print("Please select a room by entering the option number: ");
                String roomPicked = scanner.nextLine();
                if(roomPicked.equals("cancel"))
                {
                    System.exit(-1);
                }
                else
                {
                    Integer roomPickedInt = Integer.valueOf(roomPicked)-1; //convert user input to array index
                    System.out.println("-----BOOKING INFORMATION-----");
                    System.out.printf("First Name: %s, Last Name: %s\n", firstName, lastName);
                    System.out.printf("Room Code: %s, Room Name: %s, Bed Type: %s\n", results.get(roomPickedInt).get(0), results.get(roomPickedInt).get(1), results.get(roomPickedInt).get(2));
                    System.out.printf("Adults: %s\n", adults);
                    System.out.printf("Kids: %s\n", children);
                    System.out.printf("Total Cost: %.2f\n", calculateTotalCost(beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3))));
                    BookTheDamnRoom(conn, results.get(roomPickedInt).get(0), beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3)), firstName, lastName, adults, children);
                }
            }
        }
        catch(SQLException e){
            e.printStackTrace();    
        }
    }
    private static void GiveSimilarOptions(Connection conn, String beginDate, String endDate, String firstName, String lastName, String adults, String children)
    {
        Scanner scanner = new Scanner(System.in);
        String similarListings = "with suggestedDates as (" +
            "with recursive C(TheDate) as " + 
            "(" +
              "select '2020-01-01' " + 
              "union all " + 
              "select date_add(TheDate, Interval 1 Day) " + 
              "from C " + 
              "where TheDate < '2020-12-31' " + 
            ")" +
            "select C.TheDate as startDate, C1.TheDate as endDate " +
            "from C " +
            "inner join C as C1 on " + 
            "C1.TheDate between date_add(C.TheDate, INTERVAL 1 DAY) and date_add(C.TheDate, INTERVAL ? DAY) and " +
            "C.TheDate between date_sub( ?, INTERVAL 14 DAY) and date_add( ?, INTERVAL 14 DAY) " + 
            "order by C.TheDate, C1.TheDate" + 
        ") " + 
        "select RoomCode, RoomName, bedType, basePrice, startDate, endDate from suggestedDates as sd1 " + 
        "join lab7_rooms as r1 " + 
        "where not exists(" +
            "select RoomCode, startDate, endDate from suggestedDates " + 
            "inner join lab7_reservations on " + 
            "((startDate >= CheckIn and startDate < CheckOut) or " + 
            "(startDate < CheckIn and endDate > CheckIn)) " + 
            "inner join lab7_rooms as r2 on " + 
                "lab7_reservations.Room = r2.RoomCode " +
            "where r1.RoomCode = r2.RoomCode and startDate = sd1.startDate and endDate = sd1.endDate " + 
            "group by r2.RoomCode, startDate, DATEDIFF(startDate, endDate), endDate" +
        ") " +
        "order by ABS(?-DATEDIFF(endDate,startDate)), ABS(DATEDIFF(?, startDate)), startDate";

        List<List<String>> results = new ArrayList<List<String>>();
        LocalDate startD = LocalDate.parse(beginDate);
        LocalDate endD = LocalDate.parse(endDate);
        long dateRange = ChronoUnit.DAYS.between(startD, endD);
        try (PreparedStatement pstmt = conn.prepareStatement(similarListings)) {
            pstmt.setInt(1, (int)dateRange);
            pstmt.setDate(2, java.sql.Date.valueOf(beginDate));
            pstmt.setDate(3, java.sql.Date.valueOf(beginDate));
            pstmt.setInt(4, (int)dateRange);
            pstmt.setDate(5, java.sql.Date.valueOf(beginDate));

            ResultSet rs = pstmt.executeQuery();
            int matchCount = 0;
            while(rs.next() && matchCount++ < 5){
                List<String> result = new ArrayList<String>();
                result.add(rs.getString("RoomCode"));
                result.add(rs.getString("RoomName"));                    
                result.add(rs.getString("bedType"));
                result.add(rs.getString("basePrice"));
                results.add(result);
            }
        }
        catch(SQLException e)
        {
            e.printStackTrace();
        }
        for (int i=0; i< results.size(); i++) 
        { 
            List<String> result = results.get(i);
            System.out.format("%d) %s--%s\n", i + 1, result.get(0), result.get(1));
        }

        System.out.println("If you do not wish to book any of these rooms enter *cancel*");
        System.out.print("Please select a room by entering the option number: ");
        String roomPicked = scanner.nextLine();
        if(roomPicked.equals("cancel"))
        {
            System.exit(-1);
        }
        else
        {
            Integer roomPickedInt = Integer.valueOf(roomPicked)-1; //convert user input to array index
            System.out.println("-----BOOKING INFORMATION-----");
            System.out.printf("First Name: %s, Last Name: %s\n", firstName, lastName);
            System.out.printf("Room Code: %s, Room Name: %s, Bed Type: %s\n", results.get(roomPickedInt).get(0), results.get(roomPickedInt).get(1), results.get(roomPickedInt).get(2));
            System.out.printf("Adults: %s\n", adults);
            System.out.printf("Kids: %s\n", children);
            System.out.printf("Total Cost: %.2f\n", calculateTotalCost(beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3))));
            BookTheDamnRoom(conn, results.get(roomPickedInt).get(0), beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3)), firstName, lastName, adults, children);
        }
    } 
    private static float calculateTotalCost(String startDate, String endDate, float basePrice)
    {
        float totalCost = 0;
        LocalDate startD = LocalDate.parse(startDate); 
        LocalDate endD = LocalDate.parse(endDate);
        for (LocalDate date = startD; date.isBefore(endD); date = date.plusDays(1))
        {
            if(date.getDayOfWeek().name().equals("SUNDAY") || date.getDayOfWeek().name().equals("SATURDAY"))
            {
                totalCost += basePrice * 1.1;
            }
            else
            {
                totalCost +=basePrice;
            }
        }
        return (totalCost * 1.18f);
    }

    private static void BookTheDamnRoom(Connection conn, String roomCode, String beginDate, String endDate, float basePrice, String firstName, String lastName, String adults, String children)
    {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Would you like to book the room?");
        System.out.print("Enter *yes* to confirm and *cancel* to return to the main menu: ");
        String decision = scanner.nextLine();
        if(decision.equals("cancel"))
        {
            System.exit(-1);
        }
        else
        {
            int newResCode = 0;
            try(Statement findMaxResCode = conn.createStatement())
            {
                ResultSet resCode = findMaxResCode.executeQuery("select max(CODE)+1 as maxResCode from lab7_reservations");
                //Insert new entry to our table
                while(resCode.next()){    
                    newResCode = resCode.getInt("maxResCode");
                    System.out.printf("%d\n", newResCode);                                    
                }
            }
            catch(SQLException e){
                e.printStackTrace();
            }

            String insertNewRes = "Insert into lab7_reservations (CODE, Room, CheckIn, CheckOut, Rate, LastName, FirstName, Adults, Kids)" +
                                " values( ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try(PreparedStatement pstmt = conn.prepareStatement(insertNewRes)){
                pstmt.setInt(1, newResCode);
                pstmt.setString(2, roomCode);
                pstmt.setDate(3, java.sql.Date.valueOf(beginDate));
                pstmt.setDate(4, java.sql.Date.valueOf(endDate));
                pstmt.setDouble(5, basePrice);
                pstmt.setString(6, lastName);
                pstmt.setString(7, firstName);
                pstmt.setInt(8, Integer.valueOf(adults));
                pstmt.setInt(9, Integer.valueOf(children));
                int row = pstmt.executeUpdate();
                System.out.println("Your booking has been confirmed!");
            }
            catch(SQLException e){
                e.printStackTrace();
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
                "ettucker",
                "WinterTwenty20_365_014575934");

            //FRone(conn);

            FRtwo(conn);
            
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }   

       
        
    }
}
