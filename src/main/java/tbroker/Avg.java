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

public class Avg {
    private LinkedList<Double> l;
    private LinkedList<Double> w;
    private LinkedList<Double> v;
    private int M;
    double TOT, WEI, DIV;

    public Avg(int M) {
        this.M = M;
        l = new LinkedList<Double>();
        w = new LinkedList<Double>();
        v = new LinkedList<Double>();
    }

    public void push(double d, double wa) {
        TOT += wa * d;
        WEI += wa;
        DIV += (d - avg()) * (d - avg());
        l.addLast(wa * d);
        w.addLast(wa);
        v.addLast((d - avg()) * (d - avg()));
        if (l.size() > M) {
            TOT -= l.remove();
            WEI -= w.remove();
            DIV -= v.remove();
        }
    }

    public void push(double d) {
        push(d, 1);
    }

    public double avg() {
        return TOT / WEI;
    }

    public int sz() {
        return l.size();
    }

    double stddiv() {
        return Math.sqrt(DIV / sz());
    }

    void div(double div) {
        TOT -= WEI * div;
        LinkedList<Double> ln = new LinkedList<Double>();
        for (int i = 0; i < l.size(); i++) {
            double d = l.get(i) - w.get(i) * div;
            ln.addLast(d);
        }
        l = ln;
    }

    public Object clone() {
        Avg o = new Avg(M);
        o.TOT = TOT;
        o.WEI = WEI;
        o.l = l;
        o.w = w;
        return o;
    }
}
