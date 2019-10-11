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

public class Kbar extends Util {
    private long ms;
    private Date sd;
    double o, e, h, l;
    double vol;

    public Kbar(long _ms) {
        ms = _ms;
        o = e = h = l = 0;
        sd = null;
    }

    public boolean isNext(Date d) {
        if (sd == null) return false;
        return (d.getTime() - sd.getTime()) > ms;
    }

    public void tick(double pri, Date d, int _vol) {
        if (sd == null || isNext(d)) {
            o = e = h = l = pri;
            sd = d;
            vol = _vol;
        }
        if (pri > h) h = pri;
        if (pri < l) l = pri;
        e = pri;
        vol += _vol;
    }

    public Object clone() {
        Kbar obj = new Kbar(M);
        obj.ms = ms;
        obj.sd = sd;
        obj.o = o;
        obj.e = e;
        obj.h = h;
        obj.l = l;
        obj.vol = vol;
        return obj;
    }
}
