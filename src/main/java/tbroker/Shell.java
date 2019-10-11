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

import com.sun.net.httpserver.*;
import java.io.*;
import java.lang.reflect.*;
import java.net.*;
import java.util.*;
import org.json.*;

public class Shell extends Util implements Runnable, DealListener, QuoteListener, BrokerMapper {
    InputStream in;
    PrintStream out;
    Quote quote;
    Broker broker;
    QuoteFetch fetch;
    History history;
    Hashtable<String, Tick> curQuotes;
    List<Order> odrs;
    Timer timer;
    boolean jni;
    StrategyFactoryH factory;
    LinkedList<Trader4Sym> traders;
    PrintWriter dealLog;
    Hashtable<String, RPCMethod> rpc;

    Shell() {
        this(System.in, System.out);
    }

    Shell(InputStream _in, PrintStream _out) {
        in = _in;
        out = _out;
        curQuotes = new Hashtable<String, Tick>();
        odrs = new LinkedList<Order>();
        timer = new Timer();
        jni = false;
        factory = new StrategyFactoryH();
        traders = null;
        dealLog = null;
        rpc = new Hashtable<String, RPCMethod>();
        rpc.put("order", new RPCOrder(this));
        rpc.put("cancel", new RPCCancel());
        rpc.put("right", new RPCRight());
        rpc.put("position", new RPCPosition());
    }

    void quote(String[] as) throws Exception {
        quote = (Quote) Class.forName(as[1]).newInstance();
        quote.login(as[2]);
    }

    void qfetch(String[] as) throws Exception {
        fetch = (QuoteFetch) Class.forName(as[1]).newInstance();
        fetch.login(as[2]);
        if (as.length > 3) {
            String sym = as[3];
            log(I, "qfetch %s", sym);
            Tick t = fetch.fetch(sym);
            log(I, "qfetch back %s = %.2f", sym, t.pri);
        }
    }

    void history(String[] as) throws Exception {
        history = (History) Class.forName(as[1]).newInstance();
        history.login(as[2]);
    }

    class ShellQuote implements QuoteListener {
        String sym;

        ShellQuote(String _sym) {
            sym = _sym;
        }

        public void dayOpen(Date date) {}

        public void tick(Tick tick) {
            curQuotes.put(sym, tick);
        }

        public void dayClose() {}
    }

    void bind(String[] as) {
        if (quote == null) log("no quote instance");
        quote.bind(as[1], new ShellQuote(as[1].toUpperCase()));
    }

    public Broker getBroker(String token) {
        return broker;
    }

    public boolean isSymValid(String sym) {
        return true;
    }

    void broker(String[] as) throws Exception {
        broker = (Broker) Class.forName(as[1]).newInstance();
        broker.login(as[2]);
        for (String name : rpc.keySet()) {
            rpc.get(name).setBroker(this);
        }
    }

    public void deal(Deal deal) {
        out.println(deal.toString());
        try {
            if (dealLog != null) {
                // synchronized (dealLog) {
                dealLog.println(deal.toString());
                // }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void order(String[] as) throws Exception {
        if (as.length < 5) {
            out.println("order sym vol pri tag");
            return;
        }
        String sym = as[1];
        int vol = Integer.parseInt(as[2]);
        double pri = new Double(as[3]).doubleValue();
        String tag = as[4];
        int type = 0;
        if (as.length >= 6) {
            type = Integer.parseInt(as[5]);
        }
        Order odr = broker.order(sym, vol, pri, new Date(), null, type, tag);
        if (odr == null) out.println("fail");
        else out.println("roid = " + new String(odr.oid));
        odrs.add(odr);
    }

    void cancel(String[] as) {
        if (as.length < 2) {
            out.println("cancel oid");
            return;
        }
        out.println(broker.cancel(as[1].getBytes()));
    }

    void right(String[] as) throws Exception {
        out.println(broker.getRight());
    }

    void oi(String[] as) throws Exception {
        if (as.length < 2) out.println("oi sym");
        out.println(broker.getOI(as[1]));
    }

    void lsq(String[] as) {
        for (Order o : odrs) out.println(o.toString());
        for (String sym : curQuotes.keySet()) {
            Tick tick = curQuotes.get(sym);
            out.print(sym + " ");
            out.println(tick.toString());
        }
    }

    void positions(String[] as) {
        for (String sym : broker.getPositions()) {
            out.println(sym + ":" + broker.getOI(sym));
        }
    }

    void jni(String[] args) throws Exception {
        for (final File fileEntry : new File(".").listFiles()) {
            String f = fileEntry.getName();
            if (f.startsWith("tbroker-driver") && f.endsWith("dll")) {
                f = f.replace(".dll", "");
                log("load %s", f);
                System.loadLibrary(f);
            }
        }
        jni = true;
    }

    void sconsole(String[] args) throws Exception {
        int sport = Integer.parseInt(args[1]);
        log("Server SConsole @ %d", sport);
        new SConsole(sport, this).start();
    }

    class StrategyFactoryH extends StrategyFactory {
        private boolean dump;

        private Hashtable<String, LinkedList<Strategy>> prepo =
                new Hashtable<String, LinkedList<Strategy>>();

        LinkedList<Strategy> getStrategyCache(String sym) {
            return prepo.get(sym);
        }
        // msk=StratgyN:1,StrategyNU:1
        LinkedList<Strategy> getStrategy(String sym, String msk) throws Exception {
            boolean loadIdx = Cfg.loadIdx;
            LinkedList<Strategy> pl = null;
            if (sym != null) pl = getStrategyCache(sym);
            if (pl != null) return pl;
            pl = new LinkedList<Strategy>();
            String[] stats = msk.split(",");
            for (String stat : stats) {
                String[] sts = stat.split(":");
                String cname = sts[0];
                int unit = Integer.parseInt(sts[1]);
                Class c = Class.forName("tbroker." + cname);
                Strategy p = (Strategy) (c.newInstance());
                p.initScale(unit);
                pl.add(p);
                if (p instanceof PData) {
                    PData pd = (PData) p;
                    if (loadIdx) pd.load(new File(Cfg.loadPath));
                }
                if (!dump) println(Y, cname);
            }
            dump = true;
            prepo.put(sym, pl);
            return pl;
        }
    }

    public void dayOpen(Date date) {
        Thread t =
                new Thread() {
                    public void run() {
                        try {
                            Thread.sleep(10000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        double fright = broker.getRight();
                        log(E, "%s FRIGHT-sta:%.2f", format(new Date()), fright);
                    }
                };
        t.start();
    }

    public void tick(Tick tick) {}

    public void dayClose() {
        double fright = broker.getRight();
        log(E, "%s FRIGHT-end:%.2f", format(new Date()), fright);
    }

    class Trader4Sym {
        String sym;
        LinkedList<Strategy> strats;

        Trader4Sym(LinkedList<Strategy> _strats, String _sym) throws Exception {
            strats = _strats;
            sym = _sym;
            log(E, "init %s", sym);
            for (int i = 0; i < strats.size(); i++)
                strats.get(i).init(sym, Shell.this, broker, quote);

            int total = 0;
            for (Strategy p : strats) {
                OI oi = p.getOI();
                total += oi.vol;
            }
            int real = broker.getOI(sym);
            log(E, "exp/real: " + total + "/" + real);
            int fix = total - real;
            if (fix != 0) {
                log("NOT sync");
                if (Cfg.daemonAutoFix) broker.order(sym, fix, 0, new Date(), Shell.this, 0, null);
            } else {
                log(E, "synced");
            }
        }

        LinkedList<Strategy> getStrategy() {
            return strats;
        }
    }

    // as[1]: tx,StratgyN:1,StrategyNU:1
    void trader(String[] as) throws Exception {
        if (quote == null) throw new Exception("no quote cmd before trader cmd");
        if (broker == null) throw new Exception("no broker cmd before trader cmd");
        if (traders != null) throw new Exception("trader has been started");
        int nmonth = Integer.parseInt(as[1]);
        String des = as[2];
        log("%s %s %S\n", as[0], as[1], as[2]);
        traders = new LinkedList<Trader4Sym>();
        dealLog = new PrintWriter(new FileOutputStream(Cfg.dealLog));

        Date now = new Date();
        StringTokenizer sk = new StringTokenizer(des, Cfg.sep);
        String future = sk.nextToken();
        String msk = "";
        while (sk.hasMoreTokens()) msk += sk.nextToken() + ",";
        for (int i = 0; i <= nmonth; i++) {
            Date d = addMonth(now, i);
            d.setDate(1);
            String sym = future + format(d, "yyyyMM");
            log("Trader4Sym %s", sym);
            LinkedList<Strategy> strats = factory.getStrategy(sym, msk);
            if (i == 0) quote.bind(sym, this);
            traders.add(new Trader4Sym(strats, sym));
        }
    }

    void auto(String[] as) {
        timer.schedule(
                new TimerTask() {
                    public void run() {
                        try {
                            exe("ls");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                2 * 60 * 1000,
                30 * 60 * 1000);
    }

    void ls(String[] as) {
        out.println(
                String.format(
                        "%3s %6s %8s %6s %6s %6s %6s %6s",
                        "oi", "ptag", "tar", "open", "last", "stp", "upp", "net"));
        out.flush();
        for (Trader4Sym dae : traders) {
            int total = 0;
            for (Strategy p : dae.getStrategy()) {
                out.println(p.toString());
                out.flush();
                total += p.getOI().vol;
            }
            out.println("EXP-eng: " + total + "/" + broker.getOI(dae.sym));
        }
    }

    void lso(String[] as) {
        for (Trader4Sym dae : traders) {
            for (Strategy p : dae.getStrategy()) {
                out.println("");
                out.println("strategy: " + p.ptag);
                for (OrderExe exe : p.getOdrExes()) out.println(exe.odr.toString());
            }
        }
        out.flush();
    }

    void help(String[] as) {
        out.println(String.format("%-8s %s", "quote", "<class> <acc_pass>"));
        out.println(String.format("%-8s %s", "bind", "<sym> <acc_pass>"));
        out.println(String.format("%-8s %s", "broker", "<class> <acc_pass>"));
        out.println(String.format("%-8s %s", "qfetch", "<class> <acc_pass>"));
        out.println(String.format("%-8s %s", "history", "<class> <acc_pass>"));
        out.println(String.format("%-8s %s", "order", "<sym> <vol> <pri> <tag> <isday>"));
        out.println(String.format("%-8s %s", "cancel", "<oid>"));
        out.println(String.format("%-8s %s", "right", ""));
        out.println(String.format("%-8s %s", "oi", "<sym>"));
        out.println(String.format("%-8s %s", "positions", ""));
        out.println(String.format("%-8s %s", "h", ""));
        out.println(String.format("%-8s %s", "jni", ""));
        out.println(String.format("%-8s %s", "sconsole", "<port>"));
        out.println(String.format("%-8s %s", "trader", "<nmonth> <des>"));
        out.println(String.format("%-8s %s", "auto", ""));
        out.println(String.format("%-8s %s", "ls", ""));
        out.println(String.format("%-8s %s", "ls-odr", ""));
        out.println(String.format("%-8s %s", "ls-quote", ""));
        out.println(String.format("%-8s %s", "rpc-http", "<port>"));
        out.println(String.format("%-8s %s", "rpc-exe", "<json contains cmds>"));
    }

    void rpcHttp(String[] as) throws Exception {
        int port = Integer.parseInt(as[1]);
        int max = 0;
        if (as.length > 2) {
            max = Integer.parseInt(as[2]);
        }
        // HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        HttpServer server = HttpServer.create(new InetSocketAddress(port), max);
        for (String k : rpc.keySet()) {
            server.createContext("/" + k, rpc.get(k));
        }
        server.setExecutor(null); // creates a default executor
        server.start();
        log("http @ %d, max = %d", port, max);
    }

    void rpcExe(String[] as) throws Exception {
        File f = new File(as[1]);
        if (!f.exists()) return;
        BufferedReader fin = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
        String l;
        while ((l = fin.readLine()) != null) {
            if (l.startsWith("//")) continue;
            JSONObject jsn = new JSONObject(l);
            JSONObject payload = jsn.getJSONObject("payload");
            String s = payload.getString("method");
            if (s == null) {
                log("no such attribute: method");
                return;
            }
            RPCMethod m = rpc.get(s);
            if (m == null) {
                log("no such method:" + s);
                return;
            }
            m.call(null, payload.toString(), payload);
        }
    }

    boolean exe(String line) throws Exception {
        return exe(line, out);
    }

    boolean exe(String line, PrintStream _out) throws Exception {
        PrintStream ori = out;
        out = _out;
        boolean ret;
        ret = _exe(line);
        out = ori;
        return ret;
    }

    boolean _exe(String line) throws Exception {
        String[] args = line.split(" ");
        String cmd = args[0];
        if (cmd.equals("echo")) {
            out.println(line);
        } else if (cmd.equals("test")) {
            out.println("test");
            String methodName = "main";
            Method[] methods = Class.forName(args[1]).getMethods();
            String[] targs = new String[args.length - 2];
            for (int i = 0; i < targs.length; i++) targs[i] = args[i + 2];
            for (Method m : methods) {
                if (methodName.equals(m.getName())) {
                    m.invoke(null, new Object[] {targs});
                    return true;
                }
            }
            return false;
        } else if (cmd.equals("quote")) {
            quote(args);
        } else if (cmd.equals("bind")) {
            bind(args);
        } else if (cmd.equals("broker")) {
            broker(args);
        } else if (cmd.equals("qfetch")) {
            qfetch(args);
        } else if (cmd.equals("history")) {
            history(args);
        } else if (cmd.equals("order")) {
            order(args);
        } else if (cmd.equals("cancel")) {
            cancel(args);
        } else if (cmd.equals("ls-quote") || cmd.equals("lsq")) {
            lsq(args);
        } else if (cmd.equals("right")) {
            right(args);
        } else if (cmd.equals("oi")) {
            oi(args);
        } else if (cmd.equals("positions") || cmd.equals("pos")) {
            positions(args);
        } else if (cmd.equals("h")) {
            help(args);
        } else if (cmd.equals("jni")) {
            jni(args);
        } else if (cmd.equals("sconsole")) {
            sconsole(args);
        } else if (cmd.equals("trader")) {
            trader(args);
        } else if (cmd.equals("auto")) {
            auto(args);
        } else if (cmd.equals("ls")) {
            ls(args);
        } else if (cmd.equals("ls-odr") || cmd.equals("lso")) {
            lso(args);
        } else if (cmd.equals("rpc-http")) {
            rpcHttp(args);
        } else if (cmd.equals("rpc-exe")) {
            rpcExe(args);
        } else {
            return false;
        }
        return true;
    }

    public void run() {
        BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        String line;
        try {
            out.print(">");
            while ((line = bin.readLine()) != null) {
                if (line.startsWith("\n") || line.startsWith("\r") || line.length() == 0) continue;
                try {
                    if (!exe(line)) log("no such [" + line + "]");
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                out.print(">");
            }
            System.exit(0);
        } catch (Exception e) {
            log(e);
        }
    }

    public static void main(String[] args) {
        try {
            Cfg.init();
            InputStream in = System.in;
            PrintStream out = System.out;
            Shell shell = new Shell(in, out);
            for (String s : args) {
                try {
                    println(E, "exe: " + s);
                    shell.exe(s);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            shell.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
