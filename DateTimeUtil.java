package gitlet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateTimeUtil {
    private static  final SimpleDateFormat logTimestampSDF = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");

    public static Date createInitTimestamp() {
        //timestamp for this initial commit will be 00:00:00 UTC, Thursday, 1 January 1970
//        January 1st, 1970, 00:00:00
        String dateStr = "1970-01-01 00:00:00";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date dateInit = sdf.parse(dateStr);
            return dateInit;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }

    }

    public static void main(String[] args) {
        Date initialTimestamp = createInitTimestamp();
        System.out.println(initialTimestamp);
        System.out.println(new Date());
        System.out.println();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");

//        OffsetDateTime.of(initialTimestamp);

        System.out.println(sdf.format(initialTimestamp));
        System.out.println(sdf.format(new Date()));
        System.out.println(new Date());
        System.out.println("Thu Nov 9 20:00:05 2017 -0800");
         sdf = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        System.out.println(sdf.format(initialTimestamp));


    }

    public static String formatForLog(Date timestamp) {
        return logTimestampSDF.format(timestamp);
    }
}
