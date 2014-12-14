package edu.pdx.cs410J.ankgupta;

import edu.pdx.cs410J.AirportNames;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AirlineServlet extends HttpServlet
{
    public Map<String, Airline> map = new HashMap<String, Airline>();

    public Map returnMap(){
        return map;
    }

    @Override
    protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        String key = getParameter( "name", request );
        String src = getParameter( "src" , request );
        String dest = getParameter( "dest", request);
        boolean search = false;
        if(dest != null && src != null){
            search = true;
        }

      //  System.out.println(src + "->" + dest);

        if (key != null && search == false) {
            writeValue(key, response);
        } else if(key != null && search == true){
            writeSearch(key, src, dest, response);
        }
        else
        {
            writeAllMappings(response);
        }
    }

    private void writeSearch( String key, String src, String dest, HttpServletResponse response ) throws IOException
    {
        PrintWriter pw = response.getWriter();
        try {
            String d = dest.substring(0,3);
            Collection<Flight> flightList;
            Airline airline = map.get(key);
            flightList = airline.getFlights();
            boolean matchfound = false;
            pw.println("Flights for airline" + key);
            pw.println("From: " + src + " To: " + d);
            pw.println();
            for(Flight f : flightList){
                if(f.getSource().equals(src) ){
                    if(f.getDestination().equals(d)) {
                        pw.println(f.writePrettyFile());
                        matchfound = true;
                    }
                }
            }
            if(matchfound == false){
                pw.println("No matching flights were found");
            }
        } catch (Exception e){
            pw.println(key + ": airline does not exist");
        }
        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }

    @Override
    protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
    {
        response.setContentType( "text/plain" );

        String key = getParameter( "key", request );
        if (key == null) {
            missingRequiredParameter( response, key );
            return;
        }

        String value = getParameter( "value", request );
        if ( value == null) {
            missingRequiredParameter( response, value );
            return;
        }

        String[] array = value.split(" ");
        int flightNumber = validateNumber(array[0]);
        String src = validateAirport(array[1]);
        String departDate = array[2];
        String departTime = array[3];
        String departLocale = array[4];
        Date departureDate = validateDate(departDate.concat(" " + departTime + " " + departLocale));
        String dest = validateAirport(array[5]);
        String arriveDate = array[6];
        String arriveTime = array[7];
        String arriveLocale = array[8];
        Date arrivalDate = validateDate(arriveDate.concat(" " + arriveTime + " " + arriveLocale));

        try{
            Airline airline = map.get(key);
            if(airline == null) {
                airline = new Airline(key);
                airline.addFlight(new Flight(flightNumber, src, departureDate, dest, arrivalDate));
                map.put(key, airline);
            }
            else
            {
                airline.addFlight(new Flight(flightNumber, src, departureDate, dest, arrivalDate));
            }
        } catch(Exception e){
            System.err.println("Error adding flight");
            System.exit(1);
        }

        response.setStatus( HttpServletResponse.SC_OK);
    }

    private void missingRequiredParameter( HttpServletResponse response, String key )
        throws IOException
    {
        PrintWriter pw = response.getWriter();
        pw.println( Messages.missingRequiredParameter(key));
        pw.flush();
        
        response.setStatus( HttpServletResponse.SC_PRECONDITION_FAILED );
    }

    private void writeValue( String key, HttpServletResponse response ) throws IOException
    {

        PrintWriter pw = response.getWriter();
        Airline airline = map.get(key);
        try {
            pw.println(airline.writePrettyFile());
        } catch (Exception e){
            pw.println(key + ": airline does not exist");
        }
        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }

    private void writeAllMappings( HttpServletResponse response ) throws IOException
    {
        PrintWriter pw = response.getWriter();
        for(Map.Entry<String, Airline> entry : this.map.entrySet())
        {
            String name = entry.getKey();
            Airline airline = map.get(name);
            pw.println(airline.writePrettyFile());
        }
        pw.flush();

        response.setStatus( HttpServletResponse.SC_OK );
    }

    private String getParameter(String name, HttpServletRequest request) {
      String value = request.getParameter(name);
      if (value == null || "".equals(value)) {
        return null;

      } else {
        return value;
      }
    }

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

        SimpleDateFormat dateformat = new SimpleDateFormat("mm/dd/yyyy hh:mm a");
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
