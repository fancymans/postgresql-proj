/*
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
                System.out.println("9. < EXIT");
                String authorisedUser = null;
                switch (readChoice()) {
                    case 1: CreateUser(esql); break;
                    case 2: authorisedUser = LogIn(esql); break;
                    case 9: keepon = false; break;
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
						System.out.println("5. Search for someone");
                        System.out.println(".........................");
                        System.out.println("9. Log out");
                        System.out.println(".........................");
                        switch (readChoice()) {
                            case 1: FriendList(esql, authorisedUser); break;
                            case 2: UpdatePassword(esql); break;
                            case 3: NewMessage(esql); break;
                            case 4: SendRequest(esql); break;
                            case 5: Search(esql);break;
							case 9: usermenu = false; break;
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
            "              User Interface                         \n" +
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



    // ---------------------------------------------------------------------
    // self defined functions
    // ---------------------------------------------------------------------
    public static void FriendList(ProfNetwork esql, String login) {
        try {
            System.out.println();
            String query = String.format(
                "SELECT CU.connectionId " +
                "FROM USR U, CONNECTION_USR CU " +
                "WHERE U.userId = '%s' AND U.userId = CU.userId AND " +
                "CU.status = 'Accept'", login);
            List<List<String>> s = esql.executeQueryAndReturnResult(query);

            System.out.println();

        } catch(Exception e) {
            System.err.println (e.getMessage ());
        }
    }

    public static void UpdatePassword(ProfNetwork esql){
        try{
            System.out.print("\tEnter your new password: ");
            String newpw = in.readLine();
            System.out.print("\tEnter new password again: ");
            String newpw2 = in.readLine();
            if(newpw.equals(newpw2)){
                String query = String.format("UPDATE USR SET password = '%s' WHERE userId = '%s'", newpw, current_user);
                esql.executeQuery(query);
            }
            else
                System.out.println("New password did not match\n");
        }
        catch(Exception e)
        {
            System.out.println("UPDATED PASSWORD SUCCESSFUL\n");
        }
    }

    public static void NewMessage(ProfNetwork esql) {
        System.out.println("you didn't do this yet");
    }

    public static void SendRequest(ProfNetwork esql) {
        System.out.println("you didn't do this yet");
    }
	
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
                System.out.println("9. return to main menu");
                System.out.print("Please make your choice: ");
        		input = in.readLine();
                if(input.equals("1") || input.equals("2") || input.equals("3") || input.equals("4") || input.equals("9"))
                    flag = false;   
                else
                    System.out.println("ERROR: Please enter 1, 2, 3, 4, or 9. Try again\n"); 
            }
            if(!input.equals("9"))
            {
                System.out.print("Enter search criteria: ");
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
            }
        }
        catch(Exception e){
            System.out.println("caught exception in search");
        }
	}







    // Rest of the functions definition go in here

}//end ProfNetwork
