package edu.pdx.cs410J.ankgupta;

import edu.pdx.cs410J.AirportNames;
import edu.pdx.cs410J.web.HttpRequestHelper;

import java.util.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.lang.String;
/**
 * The main class that parses the command line and communicates with the
 * Airline server using REST.
 */
public class Project4 {

    public static final String MISSING_ARGS = "Missing command line arguments";

    public static void main(String[] args) {
        String hostName = null;
        String portString = null;
        String key = null;
        String value = null;
        boolean printflag = false;
        boolean hostexist = false;
        boolean portexist = false;
        boolean searchexist = false;
        int j = 0;

        if (args.length < 8) {
            System.err.println("Missing command line arguments");
            System.exit(1);
        }


        for(int i = 0 ; i < args.length ; ++i) {
            if(args[i].equals("-search")){
                searchexist = true;
            }
        }

        for(int i = 0 ; i < args.length ; ++i){
            if(args[i].equals("-print")){
                printflag = true;
                j += 1;
            }
            else if(args[i].equals("-README")){
                System.out.println("Ankur Gupta. Project 4. Building a simple flight and airline web applet. Enter in options:");
                System.out.println(" -print , -README, -host hostname & -port port, -search");
                System.out.println("Followed by the following in this exact order:");
                System.out.println("  name , flight #, source, departure date, departure time, departure AM/PM, destination, arrival date, arrival time, arrival AM/PM");
                System.exit(0);
            }
            else if(args[i].equals("-host")){
                hostName = args[i+1];
                hostexist = true;
                j += 2;
            }
            else if(args[i].equals("-port")){
                portString = args[i+1];
                portexist = true;
                j += 2;
            }
            else if(args[i].equals("-search")){
                j+= 1;
            }
        }

        if(hostexist == false && portexist == true){
            usage("Missing host");
        }
        else if(hostexist == true && portexist == false){
            usage("Missing port");
        }

        String src, dest;

        if(searchexist == true)
        {
            if (args.length < 10) {
                String name = validateName(args[j]);
                src = validateAirport(args[j + 1]);
                dest = validateAirport(args[j + 2]);
                key = name;
            }
            else
            {
                String name = validateName(args[j]);
                int flightNumber = validateNumber(args[j+1]);
                src = validateAirport(args[j+2]);
                dest = validateAirport(args[j+6]);
                key = name;
            }
        }
        else
        {
                String name = validateName(args[j]);
                int flightNumber = validateNumber(args[j+1]);
                src = validateAirport(args[j+2]);
                String departDate = args[j+3];
                String departTime = args[j+4];
                String departLocale = args[j+5];
                Date departureDate = validateDate(departDate.concat(" " + departTime + " " + departLocale));
                dest = validateAirport(args[j+6]);
                String arriveDate = args[j+7];
                String arriveTime = args[j+8];
                String arriveLocale = args[j+9];
                Date arrivalDate = validateDate(arriveDate.concat(" " + arriveTime + " " + arriveLocale));
                String num = args[j+1];
                value = num.concat(" " + src + " " + departDate + " " + departTime + " " + departLocale + " "
                                    + dest + " " + arriveDate + " " + arriveTime + " " + arriveLocale);
                key = name;
        }


        if(args.length > j + 10){
            System.err.println("Extraneous arguments on the command line");
            System.exit(1);
        }

        if (hostName == null) {
            usage( MISSING_ARGS );

        } else if ( portString == null) {
            usage( "Missing port" );
        }

        int port;
        try {
            port = Integer.parseInt( portString );
            
        } catch (NumberFormatException ex) {
            usage("Port \"" + portString + "\" must be an integer");
            return;
        }

        AirlineRestClient client = new AirlineRestClient(hostName, port);
        HttpRequestHelper.Response response;
        try {
            if (key == null) {
                // Print all key/value pairs
                response = client.getAllKeysAndValues();

            } else if (value == null) {
                // Print all values of key
                response = client.getSearch(key, src, dest);

            } else {
                // Post the key/value pair
                response = client.addKeyValuePair(key, value);
            }

            checkResponseCode( HttpURLConnection.HTTP_OK, response);

        } catch ( IOException ex ) {
            error("While contacting server: " + ex);
            return;
        }

        if(printflag == true){
           System.out.println(key + " " + value);
        }

        System.out.println(response.getContent());
        System.exit(0);
    }

    /**
     * Makes sure that the give response has the expected HTTP status code
     * @param code The expected status code
     * @param response The response from the server
     */
    private static void checkResponseCode( int code, HttpRequestHelper.Response response )
    {
        if (response.getCode() != code) {
            error(String.format("Expected HTTP code %d, got code %d.\n\n%s", code,
                                response.getCode(), response.getContent()));
        }
    }

    private static void error( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);

        System.exit(1);
    }

    /**
     * Prints usage information for this program and exits
     * @param message An error message to print
     */
    private static void usage( String message )
    {
        PrintStream err = System.err;
        err.println("** " + message);
        err.println();
        err.println("usage: java Project4 host port [key] [value]");
        err.println("  host    Host of web server");
        err.println("  port    Port of web server");
        err.println("  key     Key to query");
        err.println("  value   Value to add to server");
        err.println();
        err.println("This simple program posts key/value pairs to the server");
        err.println("If no value is specified, then all values are printed");
        err.println("If no key is specified, all key/value pairs are printed");
        err.println();

        System.exit(1);
    }

    /**
     *
     * @param name name of the flight
     * @return after validating the name, returns the name, if multi-worded removes ""
     */
    public static String validateName(String name){
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        return sb.toString();
    }

    /**
     *
     * @param flightNumber validates the flight number is an integer
     * @return the flight number after validation
     * checks the flight number is a proper integer
     */
    public static int validateNumber(String flightNumber){
        int num = 0;

        try {
            num = Integer.parseInt(flightNumber);
        }
        catch (NumberFormatException ex) {
            System.err.print("Invalid flight number");
            System.exit(1);
        }

        if(num < 0){
            System.err.println("Please enter integer");
            System.exit(1);
        }

        return num;
    }

    /**
     *
     * @param src the source of the flight
     * @return the source after validating the correct 3-letter airport code
     */
    public static String validateAirport(String src){
        for(char c : src.toCharArray()){
            if(!Character.isLetter(c)){
                System.out.println("Please use characters for airport code");
                System.exit(1);
            }
        }
        if(src.length() != 3){
            System.out.println("Only use 3 letters for airport code");
            System.exit(1);
        }
        String name = null;
        name = AirportNames.getName(src.toUpperCase());
        if(name == null){
            System.err.println("Please enter a valid airport code");
            System.exit(1);
        }
        src = src.toUpperCase();
        return src;
    }

    /**
     *
     * @param timestamp check the value of the date is valid
     * @return the date after validation
     */
    public static Date validateDate(String timestamp) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
        Date departure = null;

        SimpleDateFormat dateformat = new SimpleDateFormat("mm/dd/yyyy hh:mm aaa");
        try {
            departure = dateformat.parse(timestamp);
        } catch (ParseException pe) {
            System.err.println("Invalid date/time");
        }
        return departure;
    }

    /**
     *
     * @param time validate the time of the flight
     * @return the time after validation
     */
    public static String validateTime(String time){
        String[] times = time.split(":");
        int minute = Integer.parseInt(times[0]);
        int hour = Integer.parseInt(times[1]);

        try {
            hour = Integer.parseInt(times[0]);
            minute = Integer.parseInt(times[1]);
        }
        catch(NumberFormatException nf)
        {
            System.err.println("Please use integers for time");
        }

        if(hour > 23 && minute != 00) {
            System.out.println("Please use 24 hour time");
            System.exit(1);
        }

        if(minute > 59 || minute < 00 ) {
            System.out.println("Please use 24 hour time");
            System.exit(1);
        }


        return times[0] + ":" + times[1];
    }
}