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

public class PositionCache extends Util {
    private static final int FETCH_MS = 6000;

    private Hashtable<String, Integer> positions;

    private long fetchTime;

    /** @param <sym>,<vol>,<sym>,<vol>.... */
    PositionCache(String s) {
        positions = new Hashtable<String, Integer>();
        StringTokenizer sk = new StringTokenizer(s, Cfg.sep);
        while (sk.hasMoreTokens()) {
            String sym = sk.nextToken();
            int vol = Integer.parseInt(sk.nextToken());
            positions.put(sym, new Integer(vol));
        }
        fetchTime = new Date().getTime();
    }

    boolean isExpired() {
        return new Date().getTime() - fetchTime > FETCH_MS;
    }

    List<String> getList() {
        LinkedList<String> r = new LinkedList<String>();
        for (String sym : positions.keySet()) r.add(sym);
        return r;
    }

    int getOI(String sym) {
        Integer i = positions.get(sym);
        return i == null ? 0 : i.intValue();
    }
}
