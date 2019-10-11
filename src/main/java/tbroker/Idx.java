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

public class Idx {

    private static Hashtable<String, Idx> instances;

    static {
        instances = new Hashtable<String, Idx>();
    }

    public static Idx getInstance(String sym) {
        Idx idx = instances.get(sym);
        if (idx == null) {
            idx = new Idx(sym);
            instances.put(sym, idx);
        }
        return idx;
    }

    private Hashtable<String, IdxImpl> idxh;
    private String sym;

    private Idx(String _sym) {
        sym = _sym;
        idxh = new Hashtable<String, IdxImpl>();
    }

    public void bind(IdxImpl impl, String name) {
        if (idxh.containsKey(name)) throw new RuntimeException("");
        idxh.put(name, impl);
    }

    public double getIdx(String i) {
        IdxImpl ii = idxh.get(i);
        if (ii == null) throw new RuntimeException("no such:" + i);
        return ii.get(i);
    }
}
