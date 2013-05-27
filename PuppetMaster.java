import java.io.*;
import java.net.URL;
import java.util.List;
import com.google.gson.*;

public class PuppetMaster{

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

  private static void runCommand(String command) throws Exception {
    String s = null;

    try {
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));

      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      // read the output from the command
      System.out.println("Here is the standard output of the command:\n");
      while ((s = stdInput.readLine()) != null) {
        System.out.println(s);
      }

      // read any errors from the attempted command
      System.out.println("Here is the standard error of the command (if any):\n");
      while ((s = stdError.readLine()) != null) {
        System.out.println(s);
      }

      System.exit(0);
    }catch (IOException e) {
      System.out.println("exception happened - here's what I know: ");
      e.printStackTrace();
      System.exit(-1);
    }

  }

  public static void main(String args[]) {
    System.out.println("running..");
    try{
      //PuppetMaster.runCommand(args[0]);
      String json = readUrl("http://www.javascriptkit.com/dhtmltutors/javascriptkit.json");

      Gson gson = new Gson();
      Page page = gson.fromJson(json, Page.class);

      System.out.println(page.title);
      for (Item item : page.items)
        System.out.println("    " + item.title);
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
