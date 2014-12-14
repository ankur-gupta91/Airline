package edu.pdx.cs410J.ankgupta;

import edu.pdx.cs410J.AbstractFlight;
import edu.pdx.cs410J.AirportNames;

import java.util.Collections;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by Ankur on 7/5/2014.
 */

/**
 * Class defining the flight object and its associated variables
 */
public class Flight extends AbstractFlight implements Comparable<Flight>{
    int flightNumber;
    String src;
    String dest;
    Date departDate;
    Date arriveDate;
    DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, Locale.US);
    long flightDuration;

    public Flight(int flightNumber, String src, Date departDate, String dest, Date arriveDate){
        this.flightNumber = flightNumber;
        this.src = src;
        this.departDate = departDate;
        this.dest = dest;
        this.arriveDate = arriveDate;
        this.flightDuration = getFlightDuration(departDate, arriveDate);
    }


    /**
     *
     * @param f
     * @return the comparison between two flight's sources, or departure time if source the same
     */
    @Override
    public int compareTo(Flight f){
        if(this.getSource().compareTo(f.getSource()) != 0){
            return this.getSource().compareTo(f.getSource());
        }
        else {
            return this.departDate.toString().compareTo(f.departDate.toString());
        }
    }

    /**
     * Return the flights duration in hours
     * @param departDate date/time of departure
     * @param arriveDate date/time of arrival
     * @return the difference between the two times
     */
    public long getFlightDuration(Date departDate, Date arriveDate){
        return((arriveDate.getTime() - departDate.getTime())/3600000);
    }

    /**
     * return the flight's duration
     * @return flightDuration
     */
    public long getLength(){
        return flightDuration;
    }

    /**
     *
     * @return the flight's number
     */
    public int getNumber(){
        return flightNumber;
    }

    /**
     *
     * @return the flights departure source
     */
    public java.lang.String getSource(){
        return src;
    }

    /**
     *
     * @return null, since this is an optional functiont to implement
     */
    public java.util.Date getDeparture() { /* compiled code */
        return departDate;
    }

    /**
     *
     * @return the flights departure time
     */
    public String getDepartureString(){
        return formatter.format(departDate);
    }

    /**
     *
     * @return the flight's destination
     */
    public java.lang.String getDestination(){
        return dest;
    }

    /**
     *
     * @return null since its an optional function
     */
    public java.util.Date getArrival() { /* compiled code */
        return arriveDate;
    }

    /**
     *
     * @return the flight's arrival time.
     */
    public String getArrivalString(){
        return formatter.format(arriveDate);
    }

    /**
     *
     * @return the flight's full information (except the name)
     */
    public String toString() { /* compiled code */
        return this.flightNumber + " " + this.src + " " + this.departDate + " " + this.dest + " " + this.arriveDate;
    }

    public String writePrettyFile(){
            String file = "";
            Date arrive = null;
            Date depart = null;
            String arrives = getArrivalString();
            String departs = getDepartureString();
            SimpleDateFormat dateformat = new SimpleDateFormat("mm/dd/yy hh:mm aaa");
            try {
                depart = dateformat.parse(departs);
                arrive = dateformat.parse(arrives);
            } catch(ParseException p) {
                System.err.println("Invalid date/time");
            }

            String source = getSource();
            String src = AirportNames.getName(source.toUpperCase());
            String destination = getDestination();
            String dest = AirportNames.getName(destination.toUpperCase());

            file += "Flight number:     " + getNumber() + "\n" +
                    "From:              " + src + "\n" +
                    "Departure time:    " + depart + "\n" +
                    "To:                " + dest + "\n" +
                    "Arrival time:      " + arrive + "\n" +
                    "Flight time (hrs): " + getLength() + "\n" + "\n";

            return file;
    }
}
