/*-****************************************************************************/
/*-                                                                           */
/*-            Copyright (c) of tbroker                                       */
/*-                                                                           */
/*-  This software is copyrighted by and is the sole property of socware.net. */
/*-  All rights, title, ownership, or other interests in the software remain  */
/*-  the property of socware.net. The source code is FREE for short-term      */
/*-  evaluation, educational or non-commercial research only. Any commercial  */
/*-  application may only be used in accordance with the corresponding license*/
/*-  agreement. Any unauthorized use, duplication, transmission, distribution,*/
/*-  or disclosure of this software is expressly forbidden.                   */
/*-                                                                           */
/*-  Knowledge of the source code may NOT be used to develop a similar product*/
/*-                                                                           */
/*-  This Copyright notice may not be removed or modified without prior       */
/*-  written consent of socware.net.                                          */
/*-                                                                           */
/*-  socware.net reserves the right to modify this software                   */
/*-  without notice.                                                          */
/*-                                                                           */
/*-  To contact socware.net:                                                  */
/*-                                                                           */
/*-             socware.help@gmail.com                                        */
/*-                                                                           */
/*-****************************************************************************/
package tbroker;

import java.util.*;

public class Tick extends Util {
    private long bs, ts;

    private Date date;

    /** h: hours of the day m: minutes of the hour s: seconds of the minutes */
    public long h, m, s, ms;

    public int vol;

    public double pri;

    Date fetchTime = new Date();

    public Tick(String des) {
        StringTokenizer sk = new StringTokenizer(des, " ");
        date = parse(sk.nextToken() + " " + sk.nextToken(), "yyyyMMdd HH:mm:ss");
        pri = Double.parseDouble(sk.nextToken());
        vol = Integer.parseInt(sk.nextToken());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        h = calendar.get(Calendar.HOUR_OF_DAY);
        m = calendar.get(Calendar.MINUTE);
        s = calendar.get(Calendar.SECOND);
        ts = (h << 16) | (m << 8) | s;
    }

    public Tick(Date _date, int _vol, double _pri) {
        date = _date;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        h = calendar.get(Calendar.HOUR_OF_DAY);
        m = calendar.get(Calendar.MINUTE);
        s = calendar.get(Calendar.SECOND);
        ts = (h << 16) | (m << 8) | s;
        vol = _vol;
        pri = _pri;
    }

    public Tick(long _bs, long _ts, int _vol, double _pri) {
        bs = _bs;
        ts = _ts;
        h = (ts >> 16) & 0xff;
        m = (ts >> 8) & 0xff;
        s = (ts >> 0) & 0xff;
        vol = _vol;
        pri = _pri;
    }

    public Date getDate() {
        if (date == null) date = new Date(bs + (((h * 60 + m) * 60) + s) * 1000);
        return date;
    }

    public String toString() {
        return formatL(getDate()) + String.format(" %.2f %d", pri, vol);
    }
}
