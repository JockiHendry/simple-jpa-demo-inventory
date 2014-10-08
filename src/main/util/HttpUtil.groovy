/*
 * Copyright 2014 Jocki Hendry.
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package util

import groovy.json.StringEscapeUtils
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import javax.net.ssl.HttpsURLConnection

public class HttpUtil {

    private static final Log LOG = LogFactory.getLog(HttpUtil.class)
    public static HttpUtil instance = new HttpUtil()

    private String server

    private HttpUtil() {
        Properties prop = new Properties()
        try {
            prop.load(getClass().getResourceAsStream("/server.properties"))
            this.server = prop.getProperty("slack.incoming")
        } catch (IOException e) {
            LOG.error("Can't read server.properties", e)
        }
    }

    private String prepareText(String text) {
        StringBuilder result = new StringBuilder(text)
        result.append("\nEnvironments:\n")
        for (String key: System.getenv().keySet()) {
            result.append("$key : ${System.getenv(key)}\n")
        }
        return StringEscapeUtils.escapeJavaScript(result.toString())
    }

    public void sendNotification(String username, String text) {
        try {
            HttpsURLConnection http = (HttpsURLConnection) new URL(server).openConnection()
            http.setDoOutput(true)
            http.setRequestMethod("POST")
            http.setUseCaches(false)
            if (username == null) username = "No Login"
            if (text == null) text = "[no message]"
            http.setRequestProperty("Content-Type", "application/json")
            http.setRequestProperty("Accept", "application/json")
            String result = "{\"icon_emoji\":\":girl:\", \"username\": \"" + username + "\", \"text\": \"" + prepareText(text) + "\"}"
            OutputStreamWriter writer = new OutputStreamWriter(http.getOutputStream())
            writer.write(result, 0, result.length())
            writer.close()
            LOG.info("Notification server response: [" + http.getResponseMessage() + "]")
        } catch (Exception ex) {
            LOG.error("Can't send to notification server: [" + ex.getMessage() + "]")
        }
    }
}
