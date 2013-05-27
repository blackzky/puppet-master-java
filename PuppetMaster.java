import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import io.socket.IOAcknowledge;
import io.socket.IOCallback;
import io.socket.SocketIO;
import io.socket.SocketIOException;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import javax.net.ssl.SSLContext;

public class PuppetMaster implements IOCallback{
  private SocketIO socket;
  public static String app_id = "app";
  public static JFrame frame;
  public static JTextField tf;

  public static void main(String[] args) {
    String url = "http://puppet-master.ap01.aws.af.cm:3000/";

    frame = new JFrame();
    JPanel panel = new JPanel();
    frame.getContentPane().add(panel);

    panel.setLayout(null);

    JLabel l = new JLabel("App ID: ");
    tf = new JTextField(20);
    tf.setEditable(false);
    l.setBounds(60, 10, 80, 30);
    tf.setBounds(150, 10, 200, 30);
    tf.setHorizontalAlignment(JLabel.CENTER);
    panel.add(l);
    panel.add(tf);

    frame.setTitle("Puppet Master");
    frame.setSize(400, 90);
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    try {
      new PuppetMaster(url);
    } catch (Exception e) {
      e.printStackTrace();
    }

    frame.setVisible(true);
  }

  public PuppetMaster(String url) throws Exception {
    SocketIO.setDefaultSSLSocketFactory(SSLContext.getDefault());
    socket = new SocketIO();
    socket.connect(url, this);
    socket.emit("add-client-app", app_id);
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
    if(event.equals("set-app-id")){
      PuppetMaster.app_id = args[0].toString();
      System.out.println(" >>>>>>>> Your app id is: " + PuppetMaster.app_id + " <<<<<<<<<<<");
      frame.setTitle("Puppet Master | AppID: " + PuppetMaster.app_id);
      tf.setText(PuppetMaster.app_id);
    }else if(event.equals("server-to-client-app-" + PuppetMaster.app_id)){
      System.out.println("Browser has sent: ");
      try{
        JSONObject json = (JSONObject)args[0];
        JSONObject response = (JSONObject)runCommand(json.get("command").toString());

        System.out.println(args[0].toString());
        response.put("bid", json.get("bid"));
        socket.emit("app-to-server", response);
      }catch(Exception e){
        System.out.println("An exception has occured - on the event reciever: ");
        e.printStackTrace();
      }
    }

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
        if(ec == 0){ System.out.println("An error has occured:\n"); }
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

