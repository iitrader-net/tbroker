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
import java.util.*;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.*;

public abstract class RPCMethod extends Util implements HttpHandler {
    protected BrokerMapper brokerMapper;

    String ret(String msg, String res, String[] attrs) {
        JSONObject jsn = new JSONObject();
        jsn.put("ret", res);
        for (int i = 0; attrs != null && i < attrs.length; i += 2) {
            jsn.put(attrs[i], attrs[i + 1]);
        }
        log("RPC,    " + msg);
        log("RPC, -->" + jsn.toString());
        return jsn.toString();
    }

    String ret(String msg, String res) {
        return ret(msg, res, null);
    }

    String ok(String msg) {
        return ret(msg, "OK");
    }

    void setBroker(BrokerMapper brokerMapper) {
        this.brokerMapper = brokerMapper;
    }

    Broker getBroker(String token) {
        return brokerMapper.getBroker(token);
    }

    boolean valid(String token) {
        return getBroker(token) != null;
    }

    abstract String call(HttpExchange hh, String msg, JSONObject jsn);

    String getToken(HttpExchange hh) {
        Headers headers = hh.getRequestHeaders();
        List<String> atts = headers.get("Authorization");
        if (atts == null) return null;
        return atts.get(0);
    }

    String call(HttpExchange hh, String msg) {
        String token = getToken(hh);
        if (token == null) return ret(msg, "no Authorization atts");
        if (!valid(token)) {
            log(token);
            return ret(msg, "wrong token");
        }
        JSONObject jsn = msg.startsWith("{") ? new JSONObject(msg) : null;
        String ret = "";
        try {
            ret = call(hh, msg, jsn);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }

    public void handleOption(HttpExchange hh) throws IOException {
        InputStream input = hh.getRequestBody();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Util.copy(input, bout);
        log(E, new String(bout.toByteArray()));
        String msg = new String(bout.toByteArray());
        String response = "";
        hh.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        hh.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        hh.getResponseHeaders().add("Content-Type", "text/plain charset=UTF-8");
        hh.getResponseHeaders().add("Access-Control-Allow-Headers", "Authorization, Content-Type");
        hh.sendResponseHeaders(204, response.length());
        OutputStream os = hh.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    @Override
    public void handle(HttpExchange hh) throws IOException {
        if (hh.getRequestMethod().toUpperCase().startsWith("OPTION")) {
            handleOption(hh);
            return;
        }
        InputStream input = hh.getRequestBody();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        Util.copy(input, bout);
        log(new String(bout.toByteArray()));
        String msg = new String(bout.toByteArray());
        byte[] response = call(hh, msg).getBytes();
        hh.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        hh.getResponseHeaders().add("Content-Type", "application/json");
        hh.sendResponseHeaders(200, response.length);
        OutputStream os = hh.getResponseBody();
        os.write(response);
        os.close();
    }

    Map<String, String> getArgs(HttpExchange hh) {
        String query = hh.getRequestURI().getQuery();
        Map<String, String> result = new HashMap<String, String>();
        if (query == null) return result;
        for (String param : query.split("&")) {
            String pair[] = param.split("=");
            if (pair.length > 1) {
                result.put(pair[0], pair[1]);
            } else {
                result.put(pair[0], "");
            }
        }
        return result;
    }
}

// Get
class RPCRight extends RPCMethod {
    String call(HttpExchange hh, String msg, JSONObject jsn) {
        Broker broker = getBroker(getToken(hh));
        if (broker == null) return ret(msg, "broker does not login");
        double right = broker.getRight();
        String[] attrs = {"right", "" + right};
        return ret(msg, "OK", attrs);
    }
}

// Get
class RPCPosition extends RPCMethod {
    String call(HttpExchange hh, String msg, JSONObject jsn) {
        Broker broker = getBroker(getToken(hh));
        if (broker == null) return ret(msg, "broker does not login");
        List<String> positions = broker.getPositions();
        JSONObject ret = new JSONObject();
        JSONArray sym = new JSONArray();
        JSONArray vol = new JSONArray();
        JSONArray pri = new JSONArray();
        for (String s : positions) {
            sym.put(s);
            vol.put(broker.getOI(s));
        }
        ret.put("ret", "OK");
        ret.put("sym", sym);
        ret.put("vol", vol);
        ret.put("pri", pri);
        log(ret.toString());
        return ret.toString();
    }
}

// Delete
class RPCCancel extends RPCMethod {
    String call(HttpExchange hh, String msg, JSONObject jsn) {
        Broker broker = getBroker(getToken(hh));
        if (broker == null) return ret(msg, "broker does not login");
        Map<String, String> args = getArgs(hh);
        String roid = args.get("roid");
        boolean ok = broker.cancel(roid.getBytes());
        return ret(msg, ok ? "OK" : "fail");
    }
}

// Post
class RPCOrder extends RPCMethod implements DealListener {
    Shell shell;
    Hashtable<Order, String> callbacks;

    RPCOrder(Shell shell) {
        this.shell = shell;
        this.callbacks = new Hashtable<Order, String>();
    }

    public void deal(Deal deal) {
        shell.deal(deal);
        try {

            String callBack = null;
            synchronized (this) {
                callBack = callbacks.get(deal.odr);
            }
            if (callBack == null) {
                log("bug: no callback for:" + deal.toString());
                return;
            }
            JSONObject msg = new JSONObject();
            String token = (String) deal.odr.priv;
            if (token == null) {
                log("token == null, do not call back");
                return;
            }
            msg.put("token", token);
            msg.put("payload", deal.toJsn());

            HttpClient http = new HttpClient();
            log(callBack);
            log(msg.toString());
            PostMethod request = new PostMethod(callBack);
            request.setRequestBody(msg.toString());

            int e;
            if ((e = http.executeMethod(request)) != HttpStatus.SC_OK) {
                throw new Exception("fail to post:" + e);
            }
        } catch (Exception e) {
            log(e);
        }
    }

    String call(HttpExchange hh, String msg, JSONObject jsn) {
        return call(null, msg);
    }

    String call(HttpExchange hh, String msg) {

        String token = getToken(hh);
        if (token == null) return ret(msg, "no Authorization atts");
        if (!valid(token)) return ret(msg, "wrong token");
        Broker broker = getBroker(token);
        if (broker == null) return ret(msg, "broker does not login");
        JSONObject jsn = new JSONObject(msg);

        String sym = jsn.getString("sym").toUpperCase();
        int vol = jsn.getInt("vol");
        if (sym.endsWith("TW") && (vol % 1000) != 0) {
            return ret(msg, "*.TW symbol must have a volume in multiples of 1000");
        }
        String tag = jsn.getString("tag").trim();
        if (tag.contains("#")) {
            return ret(msg, "tag can not contain #");
        }
        if (!brokerMapper.isSymValid(sym)) {
            return ret(msg, "invalid symbol");
        }
        double pri = jsn.getDouble("pri");
        tag = tag.trim();
        String callback = jsn.getString("callback");
        int type = 0;
        String typeS = jsn.getString("type");
        if (typeS != null) {
            try {
                type = Integer.parseInt(typeS);
            } catch (Exception e) {
                return ret(msg, "type must be integer");
            }
        }
        log("order %s %d %f %d %s", sym, vol, pri, type, tag);
        Order odr = null;
        try {
            synchronized (this) {
                odr = broker.order(sym, vol, pri, new Date(), this, type, tag);
                if (odr != null && !callback.isEmpty()) {
                    odr.priv = token;
                    callbacks.put(odr, callback);
                }
            }
        } catch (Exception e) {
            return ret(msg, e.toString());
        }
        if (odr == null) {
            log("fail to order");
            return ret(msg, "fail");
        } else {
            log("roid = " + new String(odr.oid));
            shell.odrs.add(odr);
            String[] attrs = {"roid", new String(odr.oid)};
            return ret(msg, "OK", attrs);
        }
    }
}
