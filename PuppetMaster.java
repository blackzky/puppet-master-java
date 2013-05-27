import java.io.*;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import com.google.gson.*;
import java.io.UnsupportedEncodingException;

public class PuppetMaster{

  static String urlEncodeUTF8(String s) {
    try {
      return URLEncoder.encode(s, "UTF-8");
    } catch (UnsupportedEncodingException e) {
      throw new UnsupportedOperationException(e);
    }
  }
  static String urlEncodeUTF8(Map<?,?> map) {
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<?,?> entry : map.entrySet()) {
      if (sb.length() > 0) {
        sb.append("&");
      }
      sb.append(String.format("%s=%s",
            urlEncodeUTF8(entry.getKey().toString()),
            urlEncodeUTF8(entry.getValue().toString())
            ));
    }
    return sb.toString();
  }

  private static String readUrl(String urlString) throws Exception {
    BufferedReader reader = null;
    try {
      URL url = new URL(urlString);
      reader = new BufferedReader(new InputStreamReader(url.openStream()));
      StringBuffer buffer = new StringBuffer();
      int read;
      char[] chars = new char[1024];
      while ((read = reader.read(chars)) != -1)
        buffer.append(chars, 0, read);

      return buffer.toString();
    } finally {
      if (reader != null)
        reader.close();
    }
  }

  private static void sendRequest(String host, String urlParameters) throws Exception {
    // i.e.: request = "http://example.com/index.php?param1=a&param2=b&param3=c";
    try{
      URL url = new URL(host);
      HttpURLConnection connection = (HttpURLConnection) url.openConnection();
      connection.setDoOutput(true);
      connection.setDoInput(true);
      connection.setInstanceFollowRedirects(false);
      connection.setRequestMethod("POST");
      connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
      connection.setRequestProperty("charset", "utf-8");
      connection.setRequestProperty("Content-Length", "" + Integer.toString(urlParameters.getBytes().length));
      connection.setUseCaches (false);

      DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
      wr.writeBytes(urlParameters);
      wr.flush();
      wr.close();
      connection.disconnect();
    }catch(Exception e){
      e.printStackTrace();
    }
  }

  private static void runCommand(String host, String command) throws Exception {
    String s = null;

    try {
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      // read the output from the command
      String response = "";
      int rc = 0;
      System.out.println("Response:\n");
      while ((s = stdInput.readLine()) != null) {
        System.out.println(s);
        response += (rc > 0 ? "\n" : "") + s;
      }
      Map<String,Object> map = new HashMap<String,Object>();
      map.put("response", response);
      map.put("data", 1);
      response = "?" + urlEncodeUTF8(map);
      System.out.println("out: " + response);
      sendRequest(host, response);

      // read any errors from the attempted command
      int error_count = 0;
      while ((s = stdError.readLine()) != null) {
        if(error_count == 0){
          System.out.println("An error has occured:\n");
          error_count++;
        }
        System.out.println(s);
      }

    }catch (IOException e) {
      System.out.println("An exception has occured - here's what I know: ");
      e.printStackTrace();
    }

  }

  public static void main(String args[]) {
    System.out.println("running..");
    try{
      //for linux commands that require sudo use: $echo <password> | sudo -S <command>
      String url = "http://localhost:5000/";
      PuppetMaster.runCommand(url, args[0]);
      String json = readUrl(url);

      //Gson gson = new Gson();
      //Page page = gson.fromJson(json, Page.class);

      //System.out.println(page.title);
      //for (Item item : page.items)
      //  System.out.println("    " + item.title);

      System.exit(0);
    }catch(Exception e){
      e.printStackTrace();
      System.exit(-1);
    }
  }

}

class Item {
  String title;
  String link;
  String description;
}
class Page {
  String title;
  String link;
  String description;
  String language;
  List<Item> items;
}
