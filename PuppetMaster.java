import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

public class PuppetMaster implements IOCallback {
  private SocketIO socket;

  public static void main(String[] args) {
    String url = "http://localhost:5000/";
    try {
      new PuppetMaster(url);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public PuppetMaster(String url) throws Exception {
    socket = new SocketIO();
    socket.connect(url, this);

    socket.emit("from-client", new JSONObject().put("key2", "another value"));
  }

  @Override
  public void onMessage(JSONObject json, IOAcknowledge ack) { try { System.out.println("Server said:" + json.toString(2)); } catch (JSONException e) { e.printStackTrace(); } }
  @Override
  public void onMessage(String data, IOAcknowledge ack) { System.out.println("Server said: " + data); }
  @Override
  public void onError(SocketIOException socketIOException) { System.out.println("an Error occured"); socketIOException.printStackTrace(); }
  @Override
  public void onDisconnect() { System.out.println("Connection terminated."); }
  @Override
  public void onConnect() { System.out.println("Connection established"); }

  @Override
  public void on(String event, IOAcknowledge ack, Object... args) {
    System.out.println("Server triggered event '" + event + "'");
    System.out.println(args[0].toString());
  }

  private JSONObject runCommand(String command) throws Exception {
    String s = null;
    String response = "";
    String error = "";
    int ec = 0;
    int rc = 0;
    JSONObject json = new JSONObject();

    try {
      Process p = Runtime.getRuntime().exec(command);
      BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
      BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

      System.out.println("Response:\n");
      while ((s = stdInput.readLine()) != null) {
        System.out.println(s);
        response += (rc++ > 0 ? "\n" : "") + s;
      }
      json.put("response", response);

      // read any errors from the attempted command
      while ((s = stdError.readLine()) != null) {
        if(e == 0){ System.out.println("An error has occured:\n"); }
        System.out.println(s);
        error += (ec++ > 0 ? "\n" : "") + s;
      }
      json.put("error", error);

      return json;

    }catch (IOException e) {
      System.out.println("An exception has occured - here's what I know: ");
      e.printStackTrace();
      return json;
    }
  }

}

