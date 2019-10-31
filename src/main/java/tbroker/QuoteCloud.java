package tbroker;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class QuoteCloud extends Cloud implements Quote {
    private Hashtable<String, List<QuoteListener>> id2listeners =
            new Hashtable<String, List<QuoteListener>>();
    private Hashtable<String, String> sym2id = new Hashtable<String, String>();

    HashSet<String> dbg = new HashSet<String>();

    void cloudIn(byte[] v) throws Exception {
        dbg("in:" + new String(v));
        StringTokenizer sk = new StringTokenizer(new String(v), Cfg.sep);
        String id = sk.nextToken();
        if (id.equals("_")) {
            String qid = sk.nextToken();
            List<QuoteListener> qll = null;
            synchronized (id2listeners) {
                qll = id2listeners.get(qid);
            }
            if (qll == null) {
                if (!dbg.contains("" + qid)) {
                    dbg.add("" + qid);
                    log(E, "QuoteListener %s is null", qid);
                }
                return;
            }
            String event = sk.nextToken();
            Date d = null;
            for (QuoteListener ql : qll) {
                if (event.equals("dO")) {
                    if (d == null) {
                        d = new Date(Long.parseLong(sk.nextToken()));
                    }
                    ql.dayOpen(d);
                } else if (event.equals("dC")) {
                    ql.dayClose();
                } else {
                    Tick tick = new Tick(event);
                    ql.tick(tick);
                }
            }
        } else {
            super.cloudIn(v);
        }
    }

    public void login(String _acc_pass) throws Exception {
        StringTokenizer st = new StringTokenizer(_acc_pass, Cfg.sep);
        super.login(st.nextToken(), 6688);
        String email = st.nextToken();
        String passwd = st.nextToken();
        String seed = handShake("hello", md5(email));
        String res = handShake("auth", sha1(seed + passwd));
        accPass = _acc_pass;
        if (!res.equals("t")) throw new RuntimeException("can not login");
        initSec(passwd);
        Hashtable<String, String> sm = sym2id;
        sym2id = new Hashtable<String, String>();
        for (String sym : sm.keySet()) {
            String id = sm.get(sym);
            List<QuoteListener> ll = id2listeners.get(id);
            id2listeners.remove(id);
            for (QuoteListener l : ll) {
                bind(sym, l);
            }
            Thread.sleep(2000);
        }
    }

    public boolean support(String sym) {
        String res = handShakeSec("sup", sym);
        return res.equals("t");
    }

    String getBindID(String sym) {
        return handShakeSec("bind", sym);
    }

    public void bind(String sym, QuoteListener l) {
        synchronized (id2listeners) {
            if (!sym2id.containsKey(sym)) {
                String id = getBindID(sym);
                sym2id.put(sym, id);
                LinkedList<QuoteListener> qll = new LinkedList<QuoteListener>();
                id2listeners.put(id, qll);
            }
            String id = sym2id.get(sym);
            id2listeners.get(id).add(l);
        }
    }

    class QuoteDump implements QuoteListener {
        public void dayOpen(Date date) {
            log(E, "dayOpen: %s", formatL(date));
        }

        public void tick(Tick tick) {
            log(E, "tick: %s", tick.toString());
        }

        public void dayClose() {
            log(E, "dayClose:");
        }
    }
}
