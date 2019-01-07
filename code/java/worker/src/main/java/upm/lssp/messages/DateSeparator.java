package upm.lssp.messages;

import java.util.Date;

public class DateSeparator implements MessageWrapper {
    private Date time;

    public DateSeparator(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }
}
