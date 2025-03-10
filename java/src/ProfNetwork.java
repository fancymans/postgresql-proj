/*
 * Team 14
 * Darren Trang 861007779
 * Darrin Lin 860988334
* Template JAVA User Interface
* =============================
*
* Database Management Systems
* Department of Computer Science &amp; Engineering
* University of California - Riverside
*
* Target DBMS: 'Postgres'
*
*/


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.Date;
import java.sql.Timestamp;

/**
* This class defines a simple embedded SQL utility class that is designed to
* work with PostgreSQL JDBC drivers.
*
*/
public class ProfNetwork {

    // reference to physical database connection.
     private Connection _connection = null;
     static String current_user = null;

    // handling the keyboard inputs through a BufferedReader
    // This variable can be global for convenience.
        static BufferedReader in = new BufferedReader(
            new InputStreamReader(System.in));

    /**
    * Creates a new instance of Messenger
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
    public ProfNetwork (String dbname, String dbport, String user, String passwd) throws SQLException {

        System.out.print("Connecting to database...");
        try{
    // constructs the connection URL
            String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
            System.out.println ("Connection URL: " + url + "\n");

    // obtain a physical connection
            this._connection = DriverManager.getConnection(url, user, passwd);
            System.out.println("Done");
        }catch (Exception e) {
            System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
            System.out.println("Make sure you started postgres on this machine");
            System.exit(-1);
    }//end catch
    }//end ProfNetwork

    /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
    public void executeUpdate (String sql) throws SQLException {
    // creates a statement object
        Statement stmt = this._connection.createStatement ();

    // issues the update instruction
        stmt.executeUpdate (sql);

    // close the instruction
        stmt.close ();
    }//end executeUpdate

    /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
    public int executeQueryAndPrintResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        /*
        ** obtains the metadata object for the returned result set.  The metadata
        ** contains row and column info.
        */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and output them to standard out.
        boolean outputHeader = true;
        while (rs.next()) {
            if(outputHeader) {
                for(int i = 1; i <= numCol; i++) {
                    System.out.print(rsmd.getColumnName(i) + "\t");
                }
                System.out.println();
                outputHeader = false;
            }
            for (int i=1; i<=numCol; ++i)
                System.out.print (rs.getString (i) + "\t");
            System.out.println ();
            ++rowCount;
        }//end while

        stmt.close ();
        return rowCount;
    }//end executeQuery

    /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
    public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
        // creates a statement object
        Statement stmt = this._connection.createStatement ();

        // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        /*
        ** obtains the metadata object for the returned result set.  The metadata
        ** contains row and column info.
        */
        ResultSetMetaData rsmd = rs.getMetaData ();
        int numCol = rsmd.getColumnCount ();
        int rowCount = 0;

        // iterates through the result set and saves the data returned by the query.
        boolean outputHeader = false;
        List<List<String>> result  = new ArrayList<List<String>>();

        while (rs.next()) {
            List<String> record = new ArrayList<String>();
            for (int i=1; i<=numCol; ++i)
            {
                record.add(rs.getString (i));
                // System.out.println(rs.getString(i));
            }
            result.add(record);
        }//end while

        stmt.close ();
        return result;
    }//end executeQueryAndReturnResult

    /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
    public int executeQuery (String query) throws SQLException {
    // creates a statement object
        Statement stmt = this._connection.createStatement ();

    // issues the query instruction
        ResultSet rs = stmt.executeQuery (query);

        int rowCount = 0;

    // iterates through the result set and count nuber of results.
        if(rs.next()) {
            rowCount++;
        }//end while
        stmt.close ();
        return rowCount;
    }

    /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
    public int getCurrSeqVal(String sequence) throws SQLException {
        Statement stmt = this._connection.createStatement ();

        ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
        if (rs.next())
            return rs.getInt(1);
        return -1;
    }

    /**
    * Method to close the physical connection if it is open.
    */
    public void cleanup() {
        try{
            if (this._connection != null) {
                this._connection.close ();
            }//end if
        }catch (SQLException e) {
            // ignored.
        }//end try
    }//end cleanup

    /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
    public static void main (String[] args) {
        if (args.length != 3) {
            System.err.println (
                "Usage: " +
                "java [-classpath <classpath>] " +
                ProfNetwork.class.getName () +
                " <dbname> <port> <user>");
            return;
        }//end if

        Greeting();
        ProfNetwork esql = null;
        try{
        // use postgres JDBC driver.
            Class.forName ("org.postgresql.Driver").newInstance ();
        // instantiate the ProfNetwork object and creates a physical
        // connection.
            String dbname = args[0];
            String dbport = args[1];
            String user = args[2];
            esql = new ProfNetwork (dbname, dbport, user, "");

            boolean keepon = true;
            while(keepon) {
        // These are sample SQL statements
                System.out.println("\n\nMAIN MENU");
                System.out.println("---------");
                System.out.println("1. Create user");
                System.out.println("2. Log in");
                System.out.println("0. < EXIT");
                String authorisedUser = null;
                switch (readChoice()) {
                    case 1: CreateUser(esql); break;
                    case 2: authorisedUser = LogIn(esql); break;
                    case 0: keepon = false; break;
                    default : System.out.println("Unrecognized choice!"); break;
                }//end switch
                if (authorisedUser != null) {
                    boolean usermenu = true;
                    while(usermenu) {
                        System.out.println("\n\nMAIN MENU");
                        System.out.println("---------");
                        System.out.println("1. Goto Friend List");
                        System.out.println("2. Update Password");
                        System.out.println("3. Write a new message");
                        System.out.println("4. Send Friend Request");
                        System.out.println("5. Check Friend Requests");
						System.out.println("6. Search for someone");
                        System.out.println("7. View Messages");
                        System.out.println("8. Check status of sent messages");
                        System.out.println("9. Delete message");
                        System.out.println("-------------------------");
                        System.out.println("0. Log out");
                        System.out.println("-------------------------");
                        switch (readChoice()) {
                            case 1: FriendList(esql, authorisedUser); break;
                            case 2: UpdatePassword(esql); break;
                            case 3: NewMessage(esql); break;
                            case 4: SendRequest(esql, authorisedUser); break;
                            case 5: CheckRequests(esql, authorisedUser); break;
                            case 6: Search(esql); break;
                            case 7: ViewMessages(esql);break;
                            case 8: StatusOfSentMessages(esql); break;
                            case 9: DeleteMessage(esql); break;
							case 0: usermenu = false; break;
                            default : System.out.println("Unrecognized choice!"); break;
                        }
                    }
                }
            }//end while
        } catch(Exception e) {
            System.out.println("skipped over the whole try block in main");
            System.err.println (e.getMessage ());
        } finally {
        // make sure to cleanup the created table and close the connection.
            try {
                if(esql != null) {
                    System.out.print("Disconnecting from database...");
                    esql.cleanup ();
                    System.out.println("Done\n\nBye !");
                }//end if
            }catch (Exception e) {
                // ignored.
            }//end try
        }//end try
    }//end main

    public static void Greeting() {
        System.out.println(
            "\n\n*******************************************************\n" +
                "                 Fancy User Interface                  \n" +
                "*******************************************************\n");
    }//end Greeting

    /*
    * Reads the users choice given from the keyboard
    * @int
    **/
    public static int readChoice() {
        int input;
        // returns only if a correct value is given.
        do {
            System.out.print("Please make your choice: ");
            try { // read the integer, parse it and break.
                input = Integer.parseInt(in.readLine());
                break;
            } catch (Exception e) {
                System.out.println("Your input is invalid!");
                continue;
            }//end try
        } while (true);
        return input;
    }//end readChoice

    /*
    * Creates a new user with privided login, passowrd and phoneNum
    * An empty block and contact list would be generated and associated with a user
    **/
    public static void CreateUser(ProfNetwork esql) {
        try{
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();
            System.out.print("\tEnter user email: ");
            String email = in.readLine();
            System.out.print("\tEnter user name: ");
            String name = in.readLine();
            System.out.print("\tEnter user date of birth (MM/DD/YYYY): ");
            String dob = in.readLine();

            //Creating empty contact\block lists for a user
            String query = String.format("INSERT INTO USR (userId, password, email, name, dateOfBirth) VALUES ('%s','%s','%s','%s','%s')", login, password, email, name, dob);
            esql.executeUpdate(query);
            System.out.println ("User " + login + " successfully created!\n");
        } catch(Exception e) {
            System.err.println (e.getMessage ());
        }
    }//end

    /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
    public static String LogIn(ProfNetwork esql) {
        try {
            System.out.print("\tEnter user login: ");
            String login = in.readLine();
            System.out.print("\tEnter user password: ");
            String password = in.readLine();

            String query = String.format("SELECT * FROM USR WHERE userId = '%s' AND password = '%s'", login, password);
            int userNum = esql.executeQuery(query);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            // This will get the real name of the person logging in
            String realName = null;
            Iterator<List<String>> it = rs.iterator();
            while (it.hasNext()) {
                List<String> ls = it.next();
                for (int i = 0; i < ls.size(); ++i) {
                    if (i == 3) {
                        realName = ls.get(i);
                    }
                }
            }

            if (userNum > 0) {
                System.out.println("Welcome: "+login+" | "+realName+"\n\n");
                current_user = login;
                return login;
            }
            else
            {
                System.out.println("Username or Password is wrong.\n");
            }
            return null;

        } catch(Exception e) {
            System.err.println (e.getMessage ());
            return null;
        }
    }//end

    public static void NewMessage(ProfNetwork esql, String to) {
        try{
            if(userExists(esql,to))
            {
                String content = null;
                System.out.print("\t\tWhat would you like to say: ");
                content = in.readLine();
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                String status = "sent";
                int ds = 0;
                //String output1 = String.format("senderid =  " + current_user + "\nreceiverid =  " + to + "\nconetent =  " + content + "\nsendtime =  "+ time + "\ndeletestatus =  "+ ds +"\nstatus =  " + status);
                //System.out.println(output1);
                String query = String.format("INSERT INTO message(senderid,receiverid,contents,sendtime,deletestatus,status) VALUES ('%s','%s','%s','%s',%s,'%s')", current_user, to, content, ts, ds, status);
                esql.executeUpdate(query);
                String output = String.format("\n\nSUCCESSFULLY SENT MESSAGE TO %s \n\n",to);
                System.out.println(output);
            }
        }catch(Exception e)
        {
            // !!!!!! insert command so do executeUpdate NOT executeQuery
            System.err.println (e.getMessage ());
        }
    }

    public static String GetName(ProfNetwork esql, String userid) {
        try {
            String query = String.format(
                "SELECT U.name " +
                "FROM USR U " +
                "WHERE U.userId = '%s'",
                userid);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            for (List<String> ls : rs) {
                for (String s : ls) {
                    return s;
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }

    public static String GetEmail(ProfNetwork esql, String userid) {
        try {
            String query = String.format(
                "SELECT U.email " +
                "FROM USR U " +
                "WHERE U.userId = '%s'",
                userid);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            for (List<String> ls : rs) {
                for (String s : ls) {
                    return s;
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
    }


    public static void GetAndPrintEduDet(ProfNetwork esql, String userid) {
        try {
            String query = String.format(
                "SELECT E.instituitionName, E.major, E.degree, E.startdate, E.enddate " +
                "FROM EDUCATIONAL_DETAILS E " +
                "WHERE E.userId = '%s'",
                userid);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            System.out.println("\t-------------------------------------------");
            System.out.println("\tEducation Details:");

            if (rs.size() == 0) {
                System.out.println("\t" + userid + " has no education details.\n");
                return;
            }

            System.out.println();
            for (List<String> ls : rs) {
                for (String s : ls) {
                    System.out.print("\t");
                    System.out.println(s);
                }
                System.out.println();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public static void GetAndPrintWorkExp(ProfNetwork esql, String userid) {
        try {
            String query = String.format(
                "SELECT W.company, W.role, W.location, W.startdate, W.enddate " +
                "FROM WORK_EXPR W " +
                "WHERE W.userId = '%s'",
                userid);

            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            System.out.println("\t-------------------------------------------");
            System.out.println("\tWork Details:");

            if (rs.size() == 0) {
                System.out.println("\t" + userid + " has no work experience.\n");
                return;
            }

            System.out.println();
            for (List<String> ls : rs) {
                for (String s : ls) {
                    System.out.print("\t");
                    System.out.println(s);
                }
                System.out.println();
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    // ---------------------------------------------------------------------
    // list out user's friends
    // ---------------------------------------------------------------------
    public static void Go2Profile(ProfNetwork esql, List<List<String>> rs) {
        try {

            System.out.println();
            System.out.print("\tWhose profile would you like to view: ");
            String userid = in.readLine();
            System.out.println();

            boolean isValid = false;
            for (List<String> ls : rs) {
                for (String s : ls) {
                    if (userid.equals(s)) {
                        isValid = true;
                    }
                }
            }

            if (isValid) {
                // show them profile menu

                String n = GetName(esql, userid);
                String e = GetEmail(esql, userid);
                System.out.println("\t-------------------------------------------");
                System.out.println("\t" + userid + "'s Profile");
                System.out.println("\t-------------------------------------------");
                System.out.println("\tName:\t" + n);
                System.out.println("\tEmail:\t" + e);
                System.out.println();
                GetAndPrintEduDet(esql, userid);
                GetAndPrintWorkExp(esql, userid);
                System.out.println("\tWhat would you like to do: ");
                System.out.println("\t1. View " + userid + "'s friends");
                System.out.println("\t2. Send a message to " + userid);
                System.out.print("\t");

                switch (readChoice()) {
                    case 1:
                        FriendList(esql, userid);
                        break;

                    case 2:
                        NewMessage(esql, userid);
                        break;

                    default:
                        break;
                }
            }
            else {
                System.out.println(
                    "\tNone of the friends match the name \"" + userid +
                    "\". Please try again.\n");
                return;
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    // ---------------------------------------------------------------------
    // list out user's friends
    // ---------------------------------------------------------------------
    public static void FriendList(ProfNetwork esql, String currentUser) {
        try {
            System.out.println();
            String query = String.format(
                "SELECT CU.connectionId " +
                "FROM USR U, CONNECTION_USR CU " +
                "WHERE U.userId = '%s' AND U.userId = CU.userId AND " +
                "CU.status = 'Accept'", currentUser);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);
            int q = esql.executeQueryAndPrintResult(query);

            if (rs.size() == 0) {
                System.out.println(currentUser + " has no friends. QQ. :'(");
                System.out.println("Returning to main menu\n");
                return;
            }

            System.out.println();
            System.out.println("\tWould you like to view a friend's profile?");
            System.out.println("\t1. Yes");
            System.out.println("\t2. No, return to main menu");
            System.out.print("\t");

            switch (readChoice()) {
                case 1:
                    Go2Profile(esql, rs);
                    break;

                case 2:
                    return;

                default :
                    System.out.println("Unrecognized choice!");
                    break;
            }


        } catch(Exception e) {
            System.err.println(e.getMessage() + "\n");
        }
    }


    // ---------------------------------------------------------------------
    // update user's password
    // ---------------------------------------------------------------------
    public static void UpdatePassword(ProfNetwork esql){
        try {
            System.out.print("\tEnter your new password: ");
            String newpw = in.readLine();
            System.out.print("\tEnter new password again: ");
            String newpw2 = in.readLine();
            if(newpw.equals(newpw2)){
                String query = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", newpw, current_user);
                esql.executeQuery(query);
            }
            else
                System.err.println("New password did not match\n");
        }
        catch(Exception e)
        {
            System.err.println("UPDATED PASSWORD SUCCESSFUL\n");
        }
    }


    // ---------------------------------------------------------------------
    // compose and send new message
    // ---------------------------------------------------------------------
    public static void NewMessage(ProfNetwork esql) {
        try{
            System.out.print("\t\tWho would you like to send a message to (enter userId): ");
            String to = null;
            to = in.readLine();
            if(userExists(esql,to))
            {
                String content = null;
                System.out.print("\t\tWhat would you like to say: ");
                content = in.readLine();
                Timestamp ts = new Timestamp(System.currentTimeMillis());
                String status = "sent";
                int ds = 0;
                //String output1 = String.format("senderid =  " + current_user + "\nreceiverid =  " + to + "\nconetent =  " + content + "\nsendtime =  "+ time + "\ndeletestatus =  "+ ds +"\nstatus =  " + status);
                //System.out.println(output1);
                String query = String.format("INSERT INTO message(senderid,receiverid,contents,sendtime,deletestatus,status) VALUES ('%s','%s','%s','%s',%s,'%s')", current_user, to, content, ts, ds, status);
                esql.executeUpdate(query);
                String output = String.format("\n\nSUCCESSFULLY SENT MESSAGE TO %s \n\n",to);
                System.out.println(output);
            }
        }catch(Exception e)
        {
            // !!!!!! insert command so do executeUpdate NOT executeQuery
            System.err.println (e.getMessage ());
        }
    }


    // ---------------------------------------------------------------------
    // read messages
    // ---------------------------------------------------------------------
    public static void ViewMessages(ProfNetwork esql){
        try{
            String query = String.format("SELECT R.msgId, R.senderId, R.contents, R.sendTime FROM MESSAGE R WHERE R.receiverId = '%s' AND R.deleteStatus=0 ", current_user);
            int i = esql.executeQueryAndPrintResult(query);
            System.out.println("");
            if(i < 0)
            {
                System.out.println("ERROR IN ViewMessages()");
            }
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            if(result.isEmpty())
            {
                System.out.println("\nYou have 0 Messages");
            }
            String query2 = String.format("UPDATE message SET status='read' WHERE message.receiverid='%s'",current_user);
            esql.executeUpdate(query2);
            //System.out.println("UPDATED MESSAGES TO 'READ'");
        }catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // check if messages sent have been "read"
    // ---------------------------------------------------------------------
    public static void StatusOfSentMessages(ProfNetwork esql) {
        try{
            String query = String.format("SELECT R.receiverId, R.contents, R.sendTime, R.status FROM message R WHERE R.senderId='%s'",current_user);
            int i = esql.executeQueryAndPrintResult(query);
            if(i<0)
                System.out.println("ERROR IN StatusOfSentMessages()");
            List<List<String>> result = esql.executeQueryAndReturnResult(query);
            if(result.isEmpty())
            {
                System.out.println("\nYou sent 0 Messages");
            }

        } catch(Exception e){
            System.err.println(e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // delete message   REMEMBER DATABSE UPDATED SO THAT ALL MESSAGES HAVE DS=0
    // -------------------------------------------------------------------------
    public static void DeleteMessage(ProfNetwork esql){
        try{
            ViewMessages(esql);
            System.out.print("\t\tEnter the msgId of the message you want to delete: ");
            int mid = Integer.parseInt(in.readLine());
            String query_check_valid_mid = String.format("SELECT * FROM message WHERE message.receiverId='%s' AND message.msgId=%s",current_user,mid);
            List<List<String>> result = esql.executeQueryAndReturnResult(query_check_valid_mid);
            if(!result.isEmpty())
            {
                String query = String.format("UPDATE message SET deletestatus=5 where receiverId='%s' and msgId='%s' ",current_user,mid);
                esql.executeUpdate(query);
                //System.out.println("SUCCESSFULLY UPDATED DELETE STATUS TO 5");
                System.out.printf("\tMessage %s successfully deleted from your inbox.\n",mid);
                String query2 = String.format("UPDATE message SET status='deleted' WHERE message.receiverid='%s' AND message.msgId=%s",current_user,mid);
                esql.executeUpdate(query2);
            }
            else
            {
                System.out.printf("\t\tError: msgId: %s does not exist in your inbox\n",mid);
            }
        } catch(Exception e){
            System.out.println("ERROR: please enter an integer. No letters");
            //System.err.println(e.getMessage());
        }
    }

    // ---------------------------------------------------------------------
    // search by input
    // ---------------------------------------------------------------------
	public static void Search(ProfNetwork esql) {
		try{
            boolean flag = true;
            String input = null;
            while(flag)
            {
                System.out.print("\n\nWhat do you want to search by: \n");
        		System.out.println("1. userId");
        		System.out.println("2. email");
        		System.out.println("3. name");
        		System.out.println("4. dateOfBirth");
                System.out.println("0. return to main menu");
                System.out.print("Please make your choice: ");
        		input = in.readLine();
                if(input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("0"))
                    flag = false;
                else
                    System.out.println("ERROR: Please enter 1, 2, 3, 4, or 9. Try again\n");
            }
            if(!input.equals("0"))
            {
                System.out.print("\tEnter search criteria: ");
                String criteria = in.readLine();
        		String search_by = null;
        		if(input.equals("1"))
                    search_by = "userId";
                else if(input.equals("2"))
                    search_by = "email";
                else if(input.equals("3"))
                    search_by = "name";
                else if(input.equals("4"))
                    search_by = "dateOfBirth";

                String query = String.format("SELECT R.userId, R.email, R.name, R.dateOfBirth FROM USR R WHERE R.%s = '%s' ", search_by, criteria);
                int i = esql.executeQueryAndPrintResult(query);
                System.out.println("");
                if(i < 0)
                {
                    System.out.println("ERROR IN SEARCH");
                }
                List<List<String>> result = esql.executeQueryAndReturnResult(query);
                if(result.isEmpty())
                {
                    System.out.println("\nNO RESULTS FOR THAT CRITERIA FOUND");
                }
            }
        }
        catch(Exception e){
            //System.err.println (e.getMessage ());
            System.out.println("\tInvalid syntax entered.\n\tFor userId,email,or name, please enter a string\n\tFor dateOfBirth please enter in format MM/DD/YYYY\n");
        }
	}


    // ---------------------------------------------------------------------
    // check if user exists and print
    // ---------------------------------------------------------------------
    public static boolean userExistsAndPrint(ProfNetwork esql, String userid)
    {
        try{
            String query = String.format("SELECT * FROM USR WHERE USR.userid='%s'", userid);
            List<List<String>> i = esql.executeQueryAndReturnResult(query);

            if(i.isEmpty())
            {
                System.out.println("ERROR NO USER FOUND");
                return false;
            }
            for(int j = 0; j < i.size()-1; j++)
            {
                Iterator<List<String>> iterator = i.iterator();
                while(iterator.hasNext())
                {
                    System.out.printf("Current element in list is %s %n", iterator.next());
                }
            }
            return true;
        }
        catch (Exception e) {
            System.out.println("userExistsAndPrint() caught exception");
        }
        return true;
    }


    // ---------------------------------------------------------------------
    // check if a user exists
    // ---------------------------------------------------------------------
    public static boolean userExists(ProfNetwork esql, String userid)
    {
        try{
            String query = String.format("SELECT * FROM USR WHERE USR.userId='%s'", userid);
            List<List<String>> i = esql.executeQueryAndReturnResult(query);

            if(i.isEmpty())
            {
                System.out.println("ERROR NO USER FOUND");
                return false;
            }
            return true;
        }
        catch (Exception e) {
            System.out.println("userExists() caught exception");
        }
        return true;
    }

    // ---------------------------------------------------------------------
    // checks if a user is new or not
    // new user: not having any connections -- aka a loner D:
    // ---------------------------------------------------------------------
    public static boolean isNewUser(ProfNetwork esql, String currentUser) {
        try {
            System.out.println();
            String query = String.format(
                "SELECT CU.connectionId " +
                "FROM USR U, CONNECTION_USR CU " +
                "WHERE U.userId = '%s' AND U.userId = CU.userId AND " +
                "CU.status = 'Accept'", current_user);
            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            // friend count
            int fc = rs.size();

            System.out.println("You currently have " + fc + " connection(s).");

            if (fc > 0) {
                System.out.println(
                    "Since you are not a new user, you are only able to " +
                    "request connections with those who are within 3 mutual " +
                    "connections to you.\n");
                return false;
            } else {
                System.out.println(
                    "As such, you are entitled to request up to 5 new " +
                    "connections.\n");
                return true;
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
            return false;
        }
    }


    // ---------------------------------------------------------------------
    // gets the number of connection requests sent by a user
    // ---------------------------------------------------------------------
    public static int numRequests(ProfNetwork esql, String currentUser) {
        try {
            String query = String.format(
                "SELECT * FROM CONNECTION_USR C " +
                "WHERE C.userId = '%s' AND C.status = 'Request'",
                currentUser);

            List<List<String>> rs = esql.executeQueryAndReturnResult(query);

            // request count
            int rc = rs.size();
            return rc;
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
            return -1;
        }
    }


    // ---------------------------------------------------------------------
    // send friend request
    // ---------------------------------------------------------------------
    public static void SendRequest(ProfNetwork esql, String currentUser) {
        try {
            // check if user is a "new user"
            boolean n = isNewUser(esql, currentUser);

            // Ask who to send a request to
            System.out.print("Who would you like to send a request to: ");
            String userid = in.readLine();
            System.out.println();

            // check if that user exists
            // if user doesn't exist, exit function with error message
            String query = null;
            if (userExists(esql, userid)) {
                query = String.format(
                    "INSERT INTO CONNECTION_USR " +
                    "VALUES('%s','%s','Request')",
                    currentUser, userid);
            }
            else {
                System.out.println(
                "Your request could not be sent.\n" +
                "The user: \"" + userid + "\" does not exist.\n");
                return;
            }

            // if new user allow only max of 5 new connections
            // else allow only requests to people who are within 3 levels of connextion.
            if (n) {
                // maximum request count
                int maxrc = 5;

                // Get request count
                int rc = numRequests(esql, currentUser);

                if (rc < maxrc) {
                    esql.executeUpdate(query);
                    rc++;

                    System.out.println("Request to \"" + userid + "\" sent.");
                    System.out.println(
                        "You have currently sent " + rc + " out of " +
                        maxrc + " connection requests.\n");
                }
                else {
                    System.err.println(
                        "You have met or exceeded your available amount of " +
                        "connection requests.\n" +
                        "You may not request any more. " +
                        "Please try again at a later time.\n");
                }
            }
            else {
                // else allow only requests to people who are within 3 levels of connextion
                // TODO: check if user is within the scope of 3 mutual connections

                // for each active connection curr_user has
                //      check each one
                //      if request is in this list, stop and exQuery
                //      get a list of active connections
                //
                //      for each active connection those users have
                //          check each one
                //          if request is in this list, stop and exQuery
                //          get a list of active connections2
                //
                //          for each active connections those users have
                //              check each one
                //              if request is in this list, stop and exQuery
                //              get a list of active connections2
                //

                boolean isWithinScope = false;

                String q1 = String.format(
                    "SELECT C.connectionId " +
                    "FROM CONNECTION_USR C " +
                    "WHERE C.userId = '%s' AND C.status = 'Accept'",
                    currentUser);
                List<List<String>> rs1 = esql.executeQueryAndReturnResult(q1);

                for (List<String> ls1 : rs1) {
                    for (String s1 : ls1) {
                        if (userid.equals(s1)) {
                            isWithinScope = true;
                            break;
                        }
                        else {
                            String q2 = String.format(
                                "SELECT C.connectionId " +
                                "FROM CONNECTION_USR C " +
                                "WHERE C.userId = '%s' AND C.status = 'Accept'",
                                s1);
                            List<List<String>> rs2 =
                                esql.executeQueryAndReturnResult(q2);

                            for (List<String> ls2 : rs2) {
                                for(String s2 : ls2) {
                                    if (userid.equals(s2)) {
                                        isWithinScope = true;
                                        break;
                                    }
                                    else {
                                        String q3 = String.format(
                                            "SELECT C.connectionId " +
                                            "FROM CONNECTION_USR C " +
                                            "WHERE C.userId = '%s' " +
                                            "AND C.status = 'Accept'",
                                            s2);
                                        List<List<String>> rs3 =
                                        esql.executeQueryAndReturnResult(q3);

                                        for (List<String> ls3 : rs3) {
                                            for (String s3 : ls3) {
                                                if (userid.equals(s3)) {
                                                    isWithinScope = true;
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (isWithinScope) {
                    query = String.format(
                        "INSERT INTO CONNECTION_USR " +
                        "VALUES('%s','%s','Request')",
                        currentUser, userid);

                    esql.executeUpdate(query);

                    System.out.println("Request to \"" + userid + "\" sent.\n");
                }
                else {
                    System.out.println(
                        "The user \"" + userid + " is not within 3 mutual " +
                        "connections. " +
                        "As such, your request has not been sent.");
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
        }
    }

    public static void UpdateConnection(ProfNetwork esql, String query) {
        try {
            esql.executeUpdate(query);
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
        }
    }

    public static void Respond2Connection(ProfNetwork esql, String currentUser) {
        try {
            while (true) {
                System.out.print("\n\t\tWhich user would you like to respond to: ");
                String userid = in.readLine();
                System.out.println();

                String query = String.format(
                    "SELECT EXISTS( " +
                        "SELECT * FROM CONNECTION_USR C WHERE C.userId = '%s' " +
                        "AND C.connectionId = '%s' AND C.status = 'Request')",
                    userid, currentUser);

                List<List<String>> rs = esql.executeQueryAndReturnResult(query);

                for (List<String> ls : rs) {
                    for (String s : ls) {
                        if (s.equals("t")) {
                            System.out.println("\t\tYou have a friend request from \"" + userid + "\"");
                            System.out.println("\t\tWould you like to accept or reject this request?");
                            System.out.println("\t\t1. Accept");
                            System.out.println("\t\t2. Reject");
                            System.out.println("\t\t3. Choose different request");
                            System.out.println("\t\t0. Go to previous menu");

                            switch (readChoice()) {
                                case 1:
                                    query = String.format(
                                        "UPDATE CONNECTION_USR " +
                                        "SET status = 'Accept'" +
                                        "WHERE userId = '%s' " +
                                        "AND connectionId = '%s'",
                                        userid, currentUser);
                                    UpdateConnection(esql, query);
                                    return;

                                case 2:
                                    query = String.format(
                                        "UPDATE CONNECTION_USR " +
                                        "SET status = 'Reject'" +
                                        "WHERE userId = '%s' " +
                                        "AND connectionId = '%s'",
                                        userid, currentUser);
                                    UpdateConnection(esql, query);
                                    return;

                                case 3:
                                    break;

                                case 0:
                                    return;

                                default :
                                    System.out.println("Unrecognized choice!");
                                    break;
                            }
                        }
                        else {
                            System.out.println("There is no request from \"" + userid + "\"");
                            return;
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
        }
    }

    public static void CheckRequests(ProfNetwork esql, String currentUser) {
        try {
            while (true) {
                String query = String.format(
                    "SELECT C.userId " +
                    "FROM CONNECTION_USR C " +
                    "WHERE C.connectionId = '%s' " +
                    "AND C.status = 'Request'", currentUser);

                System.out.println();
                List<List<String>> rs = esql.executeQueryAndReturnResult(query);
                if (rs.size() == 0) {
                    System.out.println(
                        "You have no pending requests.\n" +
                        "Returning to main menu.\n");
                    return;
                }

                System.out.println("Pending requests: ");
                int q = esql.executeQueryAndPrintResult(query);
                System.out.println();

                System.out.println("\tDo you want to respond to a request?");
                System.out.println("\t1. Yes");
                System.out.println("\t2. No, return to main menu");
                System.out.print("\t");

                switch (readChoice()) {
                    case 1:
                        Respond2Connection(esql, currentUser);
                        break;

                    case 2:
                        return;

                    default :
                        System.out.println("Unrecognized choice!");
                        break;
                }
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage() + "\n");
        }
    }
}//end ProfNetwork
