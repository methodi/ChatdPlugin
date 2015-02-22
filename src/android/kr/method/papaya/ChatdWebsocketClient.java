package kr.method.papaya;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.framing.FrameBuilder;
import org.java_websocket.framing.Framedata;
import org.java_websocket.framing.FramedataImpl1;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;

public class ChatdWebsocketClient extends WebSocketClient {
  private FrameBuilder frameBuilder;
  private ChatdService chatdService;

  public static final int ERROR = 0;
  public static final int OPEN = 1;
  public static final int CLOSE = 2;
  public static final int MESSAGE = 3;
  
  private static final Map<READYSTATE, Integer> stateMap = new HashMap<READYSTATE, Integer>();
  static {
    stateMap.put(READYSTATE.CONNECTING, 0);
    stateMap.put(READYSTATE.OPEN, 1);
    stateMap.put(READYSTATE.CLOSING, 2);
    stateMap.put(READYSTATE.CLOSED, 3);
    stateMap.put(READYSTATE.NOT_YET_CONNECTED, 3);
  }

  public ChatdWebsocketClient(URI serverURI, Draft draft, Map<String, String> headers, ChatdService chatdService) {
    super(serverURI, draft, headers, 0);
    this.frameBuilder = new FramedataImpl1();
    this.chatdService = chatdService;
    
    if (serverURI.getScheme().equals("wss")) {
      try {
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, null, null);
        SSLSocketFactory factory = sslContext.getSocketFactory();
        this.setSocket(factory.createSocket());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }  
  }

  @Override
  public void onOpen(ServerHandshake handshakedata) {
    //sendResult("", "open", PluginResult.Status.OK);
	  chatdService.onMessage(OPEN, "");
  }

  @Override
  public void onMessage(String message) {
    //sendResult(message, "message", PluginResult.Status.OK);
	  chatdService.onMessage(MESSAGE, message);
  }
  
  @Override
  public void onMessage(ByteBuffer bytes) {
	  JSONArray jsonArr = byteArrayToJSONArray(bytes.array());
	  chatdService.onMessage(MESSAGE, jsonArr.toString());
  }

  @Override
  public void onFragment(Framedata frame) {
    try {
      this.frameBuilder.append(frame);
      
      if (frame.isFin()) {
        ByteBuffer bytes = this.frameBuilder.getPayloadData();

        if (this.frameBuilder.getOpcode() == Framedata.Opcode.BINARY) {
          this.onMessage(bytes);
        } 
        else {
          this.onMessage(new String(bytes.array(), "UTF-8"));
        }

        this.frameBuilder.getPayloadData().clear();
      }
    } 
    catch (Exception e) {} 
  }

  @Override
  public void onClose(int code, String reason, boolean remote) {
    //sendResult("", "close", PluginResult.Status.OK);
	  chatdService.onMessage(CLOSE, "");
  }

  @Override
  public void onError(Exception ex) {
    //sendResult(ex.getMessage(), "error", PluginResult.Status.ERROR);
	  chatdService.onMessage(ERROR, ex.getMessage());
  }

  @Override
  public String getResourceDescriptor() {
    return "*";
  }

  public JSONArray byteArrayToJSONArray(byte data[]) {
    JSONArray result = new JSONArray();

    for (int i = 0; i < data.length; i++) {
      result.put(data[i]);
    }

    return result;
  }
}