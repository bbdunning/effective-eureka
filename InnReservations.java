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
import java.text.DateFormatSymbols;


public class InnReservations
{
    private static void FRone(Connection conn)
    {
        try
        {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                "select rooms.*, f.pop, s.Checkin, t.lastStayLength, t.CheckOut " +
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
                    "select Room, CheckOut, DATEDIFF(CheckOut, Checkin) as lastStayLength " +
                    "from " +
                    "( " +
                        "Select distinct Room, CheckIn, CheckOut, DATEDIFF(CURDATE(), CheckOut) as diff, " +
                            "min(DATEDIFF(CURDATE(), CheckOut)) over (partition by Room) as mindiff " +
                        "from lab7_reservations " +
                        "where DATEDIFF(CURDATE(), CheckOut) > 0 " +
                    ") ti " +
                    "where diff = mindiff " +
                ") t " +
                "on f.Room = t.Room " +
                "inner join lab7_rooms as rooms on RoomCode = f.Room " +
                "order by f.pop desc;");

            System.out.printf("%8s %-25s %4s %-8s %6s %9s %-15s %10s %15s %16s %10s\n", 
                "RoomCode", "RoomName", "Beds", "BedType", "MaxOcc", "BasePrice", "Decor", 
                "Popularity", "NextAvilCheckIn", "LengthOfLastStay", "LastCheckoutDate");
                
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
                int lastStayLength = rs.getInt("lastStayLength");
                String checkout = rs.getString("CheckOut");
            
                
                System.out.printf("%-8s %-25s %-4d %-8s %-6d %-9.2f %-15s %-10s %-15s %-16d %10s\n", roomCode, roomName, numBeds, 
                    bedType, maxOcc, basePrice, decor, pop, checkin, lastStayLength, checkout);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }   
    }


    private static void FRtwo(Connection conn) throws SQLException
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

        conn.setAutoCommit(false);
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
                return;
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
        String gimmeStuff = "SELECT DISTINCT RoomCode, RoomName, bedType, basePrice, maxOcc from lab7_rooms join lab7_reservations on lab7_reservations.Room = lab7_rooms.RoomCode " +
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
                result.add(rs.getString("MaxOcc"));
                results.add(result);
                System.out.format("%d) %s\nRoom Name: %s\nMax Occupancy: %s\nBed Type: %s\nBase Price: $%s\n", 
                                    ++matchCount, results.get(matchCount-1).get(0),results.get(matchCount-1).get(1), results.get(matchCount-1).get(4),results.get(matchCount-1).get(2), results.get(matchCount-1).get(3));
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
                if(!roomPicked.equals("cancel"))
                {
                    Integer roomPickedInt = Integer.valueOf(roomPicked)-1; //convert user input to array index
                    System.out.println("-----BOOKING INFORMATION-----");
                    System.out.printf("First Name: %s\nLast Name: %s\n", firstName, lastName);
                    System.out.printf("Room Code: %s\nRoom Name: %s\nBed Type: %s\n", results.get(roomPickedInt).get(0), results.get(roomPickedInt).get(1), results.get(roomPickedInt).get(2));
                    System.out.printf("Adults: %s\n", adults);
                    System.out.printf("Kids: %s\n", children);
                    System.out.printf("Total Cost: $%.2f\n", calculateTotalCost(beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3))));
                    BookTheDamnRoom(conn, results.get(roomPickedInt).get(0), beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3)), firstName, lastName, adults, children);
                }
            }
            conn.commit();
        }
        catch(SQLException e){
            conn.rollback();
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
        "select RoomCode, RoomName, bedType, basePrice, maxOcc, startDate, endDate from suggestedDates as sd1 " + 
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
                result.add(rs.getString("MaxOcc"));
                result.add(rs.getString("startDate"));
                result.add(rs.getString("endDate"));
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
            System.out.format("%d) %s\nRoom Name: %s\nMax Occupancy: %s\nBed Type: %s\nBase Price: $%s\n", 
                                i + 1, result.get(0), result.get(1),result.get(4),result.get(2),result.get(3));
            System.out.format("Start Date: %s\nEnd Date: %s\n", result.get(5), result.get(6));
        }    

        System.out.println("If you do not wish to book any of these rooms enter *cancel*");
        System.out.print("Please select a room by entering the option number: ");
        String roomPicked = scanner.nextLine();
        if(!roomPicked.equals("cancel")){
            Integer roomPickedInt = Integer.valueOf(roomPicked)-1; //convert user input to array index
            System.out.println("-----BOOKING INFORMATION-----");
            System.out.printf("First Name: %s\nLast Name: %s\n", firstName, lastName);
            System.out.printf("Room Code: %s\nRoom Name: %s\nBed Type: %s\n", results.get(roomPickedInt).get(0), results.get(roomPickedInt).get(1), results.get(roomPickedInt).get(2));
            System.out.printf("Adults: %s\n", adults);
            System.out.printf("Kids: %s\n", children);
            System.out.printf("Total Cost: $%.2f\n", calculateTotalCost(beginDate, endDate, Float.parseFloat(results.get(roomPickedInt).get(3))));
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
            return;
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

    private static void FRfour(Connection conn) throws SQLException
    {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter the reservation code of the reservation you would like to cancel: ");
        String code = scanner.nextLine();
        String deleteRes = "delete from lab7_reservations where CODE = ?";
        conn.setAutoCommit(false);
        try (PreparedStatement pstmt = conn.prepareStatement(deleteRes)) {
            pstmt.setString(1, code);
            int row = pstmt.executeUpdate();
            if(row>0){
                System.out.printf("Reservation %s has been successfully canceled\n", code);
            }
            else{
                System.out.printf("There is no reservations with the code %s\n", code);
            }
            conn.commit();
        }
        catch(SQLException e){
            conn.rollback();
            e.printStackTrace();
        }
    }

    private static void FRfive(Connection conn) throws SQLException
    {
        Scanner scanner = new Scanner(System.in);
        String roomCode = "";
        String reservationCode = "";
        String firstName = "";
        String lastName = "";
        String beginDate = "";
        String endDate = "";

        //get input
        System.out.print("Enter a room code (or Enter for Any): ");
        roomCode = scanner.nextLine();
        System.out.print("Enter a reservation code (or Enter for Any): ");
        reservationCode = scanner.nextLine();
        System.out.print("Enter a first name (or Enter for Any): ");
        firstName = scanner.nextLine();
        System.out.print("Enter a last name (or Enter for Any): ");
        lastName = scanner.nextLine();
        System.out.print("Enter a begin date (or Enter for Any): ");
        beginDate = scanner.nextLine();
        System.out.print("Enter a end date (or Enter for Any): ");
        endDate = scanner.nextLine();
        
        conn.setAutoCommit(false);

        String stmt = "select * from lab7_reservations join lab7_rooms on Room=RoomCode where room LIKE '%'";

        if (!roomCode.equals("")) 
           stmt += "and room LIKE ? ";
        if (!reservationCode.equals(""))
           stmt += "and code LIKE ? ";
        if (!firstName.equals(""))
           stmt += "and firstName LIKE ? ";
        if (!lastName.equals("")) 
           stmt += "and lastName LIKE ? ";
        if (!beginDate.equals("") && !endDate.equals(""))
           stmt += "and ((? >= CheckIn and ? <= Checkout) " +
                 "or (? > CheckIn and ? <= Checkout) " +
                 "or (? < CheckIn and ? > Checkout))";

        try (PreparedStatement pstmt = conn.prepareStatement(stmt)) {
            int i = 1;
            if (!roomCode.equals(""))
               pstmt.setString(i++, roomCode);
            if (!reservationCode.equals(""))
               pstmt.setString(i++, reservationCode);
            if (!firstName.equals(""))
               pstmt.setString(i++, firstName);
            if (!lastName.equals(""))
               pstmt.setString(i++, lastName);
            if (!beginDate.equals("") && !endDate.equals("")) {
               pstmt.setDate(i++, java.sql.Date.valueOf(beginDate));
               pstmt.setDate(i++, java.sql.Date.valueOf(beginDate));
               pstmt.setDate(i++, java.sql.Date.valueOf(endDate));
               pstmt.setDate(i++, java.sql.Date.valueOf(endDate));
               pstmt.setDate(i++, java.sql.Date.valueOf(beginDate));
               pstmt.setDate(i++, java.sql.Date.valueOf(endDate));
            }

            ResultSet res = pstmt.executeQuery();
            String roomName = "";
            if(res.next())
               roomName = res.getString(1);
            System.out.printf("Code, Room, CheckIn, Checkout, Rate, LastName, Firstname, Adults, Kids, FullRoomName\n");
            while(res.next())
               System.out.printf("%d, %s, %s, %s, %d, %s, %s, %d, %d, %s\n",
                     res.getInt("CODE"), res.getString("Room"), res.getString("CheckIn"), res.getString("Checkout"),
                     res.getInt("Rate"), res.getString("Lastname"), res.getString("FirstName"), res.getInt("Adults"), res.getInt("Kids"),
                     res.getString("RoomName"));
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            conn.rollback();
        }   

    }

    private static int printMonthRevs(String curRoom, ArrayList<Integer> roomMonthRevs)
    {
        int numMonthsNoRev = 12 - roomMonthRevs.size();
        for (int i = 0; i < numMonthsNoRev; i++) //Padding Months with 0 revenue
        {
            roomMonthRevs.add(0);
        }

        System.out.printf("%-30s", curRoom);

        for (Integer rev : roomMonthRevs)
        {
            System.out.printf("%9d ", rev);
        }
        int yearRev = roomMonthRevs.stream().mapToInt(a -> a).sum();
        System.out.printf("%15d" , yearRev);
        System.out.println();

        return yearRev;
    }

    private static void printColTotals(Statement stmt, int totalYearRevenue)
    {
        try
        {
            //Gets monthly totals across all rooms
            ResultSet rs = stmt.executeQuery(
                "with recursive C(TheDate) as " +
                "(" +
                "select MAKEDATE(year(now()),1) " +
                "union all " +
                "select date_add(TheDate, Interval 1 Day) " +
                "from C " +
                "where TheDate < last_day(MAKEDATE(year(now()),360))" +
                "), " +
                "myq as ( " +
                "select RoomName, Month(TheDate) as MonthNum, round(Sum(Rate)) as MonthSum " +
                "from C " +
                "inner join lab7_reservations on TheDate >= CheckIn " +
                "and TheDate < CheckOut " +
                "inner join lab7_rooms on RoomCode = Room " +
                "group by Room, Month(TheDate) " +
                "order by Room" +
                ") " +
                "select MonthNum, sum(MonthSum) as AllRoomsMonthSum " +
                "from myq " +
                "group by MonthNum;");

            ArrayList<Integer> colTotals = new ArrayList<Integer>();
            while (rs.next())
            {
                colTotals.add(rs.getInt("AllRoomsMonthSum"));
            }

            int numMonthsNoRev = 12 - colTotals.size();
            for (int i = 0; i < numMonthsNoRev; i++) //Padding Months with 0 revenue
            {
                colTotals.add(0);
            }

            System.out.printf("%-30s", "Totals");
            for (Integer colTotal : colTotals)
            {
                System.out.printf("%9d ", colTotal);
            }
            System.out.printf("%15d\n", totalYearRevenue);      
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        } 
    }

    private static void FRsix(Connection conn)
    {
        try
        {
            Statement stmt = conn.createStatement();

            ResultSet rs = stmt.executeQuery(
                "with recursive C(TheDate) as " +
                "(" +
                  "select MAKEDATE(year(now()),1) " +
                  "union all " +
                  "select date_add(TheDate, Interval 1 Day) " +
                  "from C " +
                  "where TheDate < last_day(MAKEDATE(year(now()),360))" +
                ") " +
                "select RoomName, Month(TheDate) as MonthNum, round(Sum(Rate)) as MonthSum " +
                "from C " +
                "inner join lab7_reservations on TheDate >= CheckIn " +
                "and TheDate < CheckOut " +
                "inner join lab7_rooms on RoomCode = Room " +
                "group by Room, Month(TheDate) " +
                "order by Room;");
 
            String curRoom = "";
            ArrayList<Integer> roomRevList = new ArrayList<Integer>();

            System.out.printf("%-30s", "Room");
            String[] months = new DateFormatSymbols().getMonths(); //Gets month names
            for (int i = 0; i < 12; i++) //Prints months, 9 chars cause length of longest month name
            {
                System.out.printf("%9s ", months[i]);
            }
            System.out.printf("%15s", "Yearly Revenue");

            System.out.println();

            int totalYearRevenue = 0;
            while (rs.next())
            {
                if (curRoom.equals(""))
                {
                    curRoom = rs.getString("RoomName");
                    roomRevList.add(rs.getInt("MonthSum"));
                }
                else if (rs.getString("RoomName").equals(curRoom))
                {
                    roomRevList.add(rs.getInt("MonthSum"));
                }
                else
                {
                    totalYearRevenue += printMonthRevs(curRoom, roomRevList); //Gives back room year revenue;
                    
                    curRoom = "";
                    roomRevList.clear();
                    rs.previous();
                }
            }
            //Handles last room in list
            totalYearRevenue += printMonthRevs(curRoom, roomRevList); //Gives back room year revenue;
            
            printColTotals(stmt, totalYearRevenue); 

        }
        catch (SQLException e)
        {
            e.printStackTrace();
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

        String jdbcUrl = System.getenv("APP_JDBC_URL");
        String dbUsername = System.getenv("APP_JDBC_USER");
        String dbPassword = System.getenv("APP_JDBC_PW");

        try
        {
            Connection conn = DriverManager.getConnection(jdbcUrl, dbUsername, dbPassword);
                
            Scanner scanner = new Scanner(System.in);
            System.out.println("----------------------\nInn Reservation Options");
            System.out.println("1) Room popularity");
            System.out.println("2) Create a new reservation");
            System.out.println("3) Modify a reservation");
            System.out.println("4) Delete a reservation");
            System.out.println("5) Detailed reservation information");
            System.out.println("6) Monthly room revenue");
            System.out.print("What would you like to do? (enter 'quit' to quit): ");
            String input = scanner.nextLine();

            while(!input.toLowerCase().equals("quit"))
            {
                if(input.equals("1")){
                    FRone(conn);
                }
                else if(input.equals("2")){
                    FRtwo(conn);
                }
                else if(input.equals("3")){
                    FRthree(conn);
                }
                else if(input.equals("4")){
                    FRfour(conn);
                }
                else if(input.equals("5")){
                    FRfive(conn);
                }
                else if(input.equals("6")){
                    FRsix(conn);
                }
                System.out.println("----------------------\nInn Reservation Options");
                System.out.println("1) Room popularity");
                System.out.println("2) Create a new reservation");
                System.out.println("3) Modify a reservation");
                System.out.println("4) Delete a reservation");
                System.out.println("5) Detailed reservation information");
                System.out.println("6) Monthly room revenue");
                System.out.print("What would you like to do? (enter 'quit' to quit): ");
                input = scanner.nextLine();
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }
}
