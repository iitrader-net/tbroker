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
import org.apache.commons.httpclient.params.*;
import org.apache.commons.httpclient.methods.*;
import org.json.*;

class RPCClient extends Util {
    String hostURL;
    String token;
    HttpClient http = new HttpClient();

    RPCClient() {
        http.getParams().setParameter("http.socket.timeout", new Integer(60000));
        HttpConnectionParams params = http.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(60000);
        params.setSoTimeout(60000);
    }

    JSONObject get(String path) throws Exception {
        String url = hostURL + path;
        GetMethod get = new GetMethod(url);
        get.addRequestHeader("Authorization", token);
        http.executeMethod(get);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(get.getResponseBodyAsStream(), bout);
        return new JSONObject(new String(bout.toByteArray()));
    }

    JSONObject delete(String path) throws Exception {
        String url = hostURL + path;
        DeleteMethod del = new DeleteMethod(url);
        del.addRequestHeader("Authorization", token);
        http.executeMethod(del);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(del.getResponseBodyAsStream(), bout);
        return new JSONObject(new String(bout.toByteArray()));
    }

    JSONObject post(String path, JSONObject jsn) throws Exception {
        String url = hostURL + path;
        PostMethod post = new PostMethod(url);
        System.out.println(url);
        System.out.println(jsn.toString());
        post.addRequestHeader("Authorization", token);
        post.setRequestBody(jsn.toString());
        http.executeMethod(post);
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        copy(post.getResponseBodyAsStream(), bout);
        return new JSONObject(new String(bout.toByteArray()));
    }
}
