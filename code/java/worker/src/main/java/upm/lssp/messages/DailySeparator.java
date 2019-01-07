package upm.lssp.messages;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DailySeparator implements MessageWrapper {
    private Date time;

    public DailySeparator(Date time) {
        this.time = getDateOfTheDay(time);
    }

    public DailySeparator() {
        this(new Date());
    }

    public Date getTime() {
        return time;
    }

    private Date getDateOfTheDay(Date date) {
        SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        String dateString = fmt.format(date) + " 00:00:00";
        Date dateNew = null;
        try {
            dateNew = sdf.parse(dateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateNew;
    }
}
