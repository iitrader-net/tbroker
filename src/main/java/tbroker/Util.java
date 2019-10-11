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
import java.security.*;
import java.text.*;
import java.util.*;
import org.json.*;

public class Util {

    public static byte[] toCStr(String s) {
        int len = s.length();
        byte[] cs = new byte[len + 1];
        System.arraycopy(s.getBytes(), 0, cs, 0, len);
        cs[len] = 0;
        return cs;
    }

    static String byteToHex(final byte[] hash) {
        Formatter formatter = new Formatter();
        for (byte b : hash) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    public static String byte2Hex(byte b) {
        String[] h = {
            "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f"
        };
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return h[i / 16] + h[i % 16];
    }

    public static String sha1(String s) {
        byte[] convertme = s.getBytes();
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return byteToHex(md.digest(convertme));
    }

    public static String md5(String str) throws Exception {
        String md5 = null;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] barr = md.digest(str.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < barr.length; i++) sb.append(byte2Hex(barr[i]));
        String hex = sb.toString();
        md5 = hex.toLowerCase();
        return md5;
    }

    public static void copy(InputStream in, OutputStream out) throws IOException {
        copy(in, out, 256, 0);
    }

    public static void copy(InputStream in, OutputStream out, int bufSize) throws IOException {
        copy(in, out, bufSize, 0);
    }

    public static void copy(InputStream in, OutputStream out, int bufSize, long limit)
            throws IOException {
        synchronized (in) {
            synchronized (out) {
                byte[] buffer = new byte[bufSize];
                long totalWritten = 0;
                while (true) {
                    if (limit != 0 && totalWritten >= limit) {
                        break;
                    }
                    int bytesRead = in.read(buffer);
                    if (bytesRead == -1) {
                        break;
                    }
                    out.write(buffer, 0, bytesRead);
                    totalWritten += bytesRead;
                }
            }
        }
    }

    static boolean inRange(Date d, Date mh, Date mt) {
        if (d == null) return false;
        return (mh.getTime() <= d.getTime()) && (mt.getTime() >= d.getTime());
    }

    static boolean earlierThan(Date now, String hhmmss) {
        Date opd = combine(now, hhmmss);
        return now.getTime() < opd.getTime();
    }

    static boolean laterThan(Date now, String hhmmss) {
        Date opd = combine(now, hhmmss);
        return now.getTime() > opd.getTime();
    }

    public static long diff(Date a, Date b) {
        return a.getTime() - b.getTime();
    }

    public static Date combine(Date day, String hhmmss) {
        return parseL(format(day) + " " + hhmmss);
    }

    public static Date parse(String date) {
        return parse(date, "yyyyMMdd");
    }

    public static Date parseL(String date) {
        return parse(date, "yyyyMMdd HH:mm:ss");
    }

    public static Date parse(String date, String pattern) {
        try {
            return new SimpleDateFormat(pattern).parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static String format(Date date) {
        return format(date, "yyyyMMdd");
    }

    public static String formatL(Date date) {
        return format(date, "yyyyMMdd HH:mm:ss");
    }

    public static String format(Date date, String pattern) {
        return new SimpleDateFormat(pattern).format(date);
    }

    public static Date addDate(Date date, int field, int value) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(field, value);
        return c.getTime();
    }

    public static Date addYear(Date date, int year) {
        return addDate(date, Calendar.YEAR, year);
    }

    public static Date addMonth(Date date, int month) {
        return addDate(date, Calendar.MONTH, month);
    }

    public static Date addDay(Date date, int day) {
        return addDate(date, Calendar.DAY_OF_YEAR, day);
    }

    public static Date addHour(Date date, int hour) {
        return addDate(date, Calendar.HOUR_OF_DAY, hour);
    }

    static String[] opTag = {"I", "D", "", "M", "", "", "", "Y"};

    public static final int E = 0x10; // engine
    public static final int I = 0x01; // info
    public static final int D = 0x02; // day
    public static final int M = 0x04; // mm
    public static final int Y = 0x08; // year

    public static int tarOp = 0xff;
    private static boolean printTS = false;

    public static void setOP(String s) {
        setOP(s, false);
    }

    public static void setOP(String s, boolean _printTS) {
        tarOp = 0;
        for (int i = 0; i < s.length(); i++) {
            switch (s.charAt(i)) {
                case 'E':
                    tarOp |= E;
                    break;
                case 'I':
                    tarOp |= I;
                    break;
                case 'D':
                    tarOp |= D;
                    break;
                case 'M':
                    tarOp |= M;
                    break;
                case 'Y':
                    tarOp |= Y;
                    break;
            }
        }
        printTS = _printTS;
    }

    public static void println(int op, String fmt, Object... obj) {
        println(op, String.format(fmt, obj));
    }

    public static void println(int op, Object o) {
        println(op, o.toString());
    }

    public static void println(int op, String s) {
        print(op, s);
        if ((op & tarOp) != 0) System.out.println();
    }

    public static void print(int op, String s) {
        if ((op & tarOp) != 0) {
            if (op == E) {
                System.out.print("E" + Cfg.sep);
                if (printTS) System.out.print(format(new Date(), "MMdd HH:mm:ss") + Cfg.sep);
                System.out.print(s);
            } else if (op <= 8) {
                System.out.print(opTag[op - 1] + Cfg.sep);
                if (printTS) System.out.print(format(new Date(), "MMdd HH:mm:ss") + Cfg.sep);
                System.out.print(s);
            }
        }
    }

    public static boolean isSameDay(Date d1, Date d2) {
        String s1 = format(d1);
        String s2 = format(d2);
        return s1.equals(s2);
    }

    public static boolean isSameMon(Date d1, Date d2) {
        String s1 = format(d1, "yyyyMM");
        String s2 = format(d2, "yyyyMM");
        return s1.equals(s2);
    }

    public static boolean isSameYea(Date d1, Date d2) {
        String s1 = format(d1, "yyyy");
        String s2 = format(d2, "yyyy");
        return s1.equals(s2);
    }

    private String cname;
    static boolean dbg = false;

    public Util() {
        cname = this.getClass().getName();
        StringTokenizer st = new StringTokenizer(cname, ".");
        while (st.hasMoreTokens()) cname = st.nextToken();
    }

    public void log(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        log(sw.toString());
    }

    public static void logT(int op, String tag, String fmt, Object... obj) {
        println(op, tag + Cfg.sep + String.format(fmt, obj));
    }

    public void log(int op, String fmt, Object... obj) {
        println(op, cname + Cfg.sep + String.format(fmt, obj));
    }

    public void log(String fmt, Object... obj) {
        log(E, cname + Cfg.sep + String.format(fmt, obj));
    }

    public void dbg(String fmt, Object... obj) {
        if (dbg) log(cname + Cfg.sep + String.format(fmt, obj));
    }

    public JSONObject loadJson(File f) throws Exception {
        FileInputStream fi = new FileInputStream(f);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(fi, out);
        out.close();
        String jss = new String(out.toByteArray());
        JSONObject js = new JSONObject(jss);
        return js;
    }

    public void saveJson(JSONObject jobj, File f) throws Exception {
        FileOutputStream fo = new FileOutputStream(f);
        PrintWriter po = new PrintWriter(fo);
        po.println(jobj.toString());
        po.flush();
        po.close();
    }

    public static void main(String[] args) {
        new Util().log("%d", 100);
    }
}
