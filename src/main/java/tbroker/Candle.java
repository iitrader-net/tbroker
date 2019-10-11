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

import java.io.*;
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.json.*;

public class Candle extends Util {
    long ts = 0;
    double o, h, l, c;

    Candle(double _o, double _h, double _l, double _c, long _ts) {
        o = _o;
        h = _h;
        l = _l;
        c = _c;
        ts = _ts;
    }

    Candle(double _o, double _h, double _l, double _c, Date d) {
        this(_o, _h, _l, _c, d.getTime() / 1000);
    }

    public Date getTime() {
        return new Date(ts * 1000);
    }

    public String toString() {
        return String.format("%s %.2f %.2f %.2f %.2f", formatL(new Date(ts * 1000)), o, h, l, c);
    }

    public String toJson() {
        return String.format(
                "{o:\"%.2f\", h:\"%.2f\", l:\"%.2f\", c:\"%.2f\", ts:\"%d\"}", o, h, l, c, ts);
    }
}
