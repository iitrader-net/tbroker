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

public class Cfg extends Util {
    static void dump() {
        println(Y, "TBROKER_loadIdx       = %s", "" + loadIdx);
        println(Y, "TBROKER_loadPath      = %s", loadPath);
        println(Y, "TBROKER_saveIdx       = %s", "" + saveIdx);
        println(Y, "TBROKER_daemonAutoFix = %s", "" + daemonAutoFix);
        println(Y, "TBROKER_dealLog       = %s", dealLog);
    }

    static void init() {
        String s;
        if ((s = System.getenv("TBROKER_loadIdx")) != null) {
            loadIdx = s.equals("true");
        }
        if ((s = System.getenv("TBROKER_loadPath")) != null) {
            loadPath = s;
        }
        if ((s = System.getenv("TBROKER_saveIdx")) != null) {
            saveIdx = s.equals("true");
        }
        if ((s = System.getenv("TBROKER_daemonAutoFix")) != null) {
            daemonAutoFix = s.equals("true") ? true : false;
        }
        if ((s = System.getenv("TBROKER_dealLog")) != null) {
            dealLog = s;
        }
        dump();
    }

    static String loadPath = ".";

    static boolean loadIdx = false;

    static boolean saveIdx = false;

    static boolean daemonAutoFix = false;

    static String dealLog = "deals.log";

    static String sep = ",";

    /** the estimated round-trip between broker & client */
    static int latencyMS = 999;

    /** the estimated wear price */
    static int wear = 1;
}
