package robot.com.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import robot.com.myapplication.mqtt.Constants;
import robot.com.myapplication.mqtt.SubscriptClient;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private MqttClient client;//创建可用于与MQTT服务器通信的MqttClient。
    private String host = "tcp://47.105.183.54:61613"; //主机的ip(tcp连接)---要连接的服务器的地址，指定为URI
    private String userName = "admin";    // MQTT的server的用户名
    private String passWord = "password"; // MQTT的server的密码
    private MqttTopic topic;
    private MqttMessage message;

    private String myTopic = "LT/me";     //   发布消息主题
    private String myClientID = "187"; //  发布消息的ID , 可以是任意唯一字符串 （比如：邮箱，手机号，UUID等）


    private String fromWho = "LT";
    private String toUser = "me";

    private List<ListData> lists; //消息列表
    private ListView lv;    //列表控件
    private EditText et_sendText; //消息输入框
    private Button btn_send;  //发送button
    private String content_str; //
    private TextAdapter adapter;
    private double currentTime,oldTime = 0;//

    private IntentFilter intentFilter;
    private LocalReceiver localReceiver;
    private LocalBroadcastManager localBroadcastManager;

    private String TAG = "Test";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView(); //初始化界面
        connectMQTTServer(); // 连接MQTT服务
        new Thread(new Runnable() {
            @Override
            public void run() {

                Log.i(TAG,"==============The client begin to start ....");
                SubscriptClient client = new SubscriptClient(MainActivity.this);
                client.start();
                Log.i(TAG,"==============The client is running....");

            }
        }).start();

        intentFilter = new IntentFilter();
        intentFilter.addAction( Constants.MY_MQTT_BROADCAST_NAME );
        localReceiver = new LocalReceiver();
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        //注册本地接收器
        localBroadcastManager.registerReceiver(localReceiver,intentFilter);
    }

    /*
      *广播接收器
      */
    private class LocalReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String message = intent.getStringExtra("message");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG,"MainActivity message is:"+message);
                    ListData listData;
                    Gson gson = new Gson();
                    listData = gson.fromJson( message,ListData.class );
                    lists.add(listData);
                }
            });
        }
    }

    /*
     *连接MQTT服务
     */
    private void connectMQTTServer(){
        try {
            Log.i(TAG, "=================begin to connect MQTT server====================");
            client = new MqttClient(host, myClientID, new MemoryPersistence()); // myClientID 是客户端ID，可以是任何唯一的标识

            // 连接 MQTT服务器
            new Thread(new Runnable() {
                @Override
                public void run() {
                    connect();
                }
            }).start();

            Log.i(TAG, "=================connect MQTT server end=========================");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 连接MQTT服务器
     */
    private void connect() {

        MqttConnectOptions options = new MqttConnectOptions();//一组覆盖默认值的连接参数
        options.setCleanSession(false);
        options.setUserName(userName);
        options.setPassword(passWord.toCharArray());
        // 设置超时时间
        options.setConnectionTimeout(10);
        // 设置会话心跳时间
        options.setKeepAliveInterval(20);
        try {
            client.setCallback(new MqttCallback() {
                /**
                 * 消息连接丢失
                 */
                @Override
                public void connectionLost(Throwable cause) {
                    Log.i(TAG, "connectionLost-----------");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.i(TAG, "deliveryComplete---------" + token.isComplete());
                }
                /**
                 * 接收到消息的回调的方法
                 */
                @Override
                public void messageArrived(String topic, MqttMessage arg1)
                        throws Exception {
                    Log.i(TAG, "messageArrived----------");

                }
            });

            topic = client.getTopic(myTopic);//获取可用于发布消息的主题对象。
            client.connect(options);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /*
     *初始化界面
     */
    private void initView(){
        lists = new ArrayList<ListData>();
        lv = (ListView) findViewById(R.id.lv);
        et_sendText = (EditText) findViewById(R.id.et_sendText);
        btn_send = (Button) findViewById(R.id.btn_send);
        btn_send.setOnClickListener(this);
        adapter = new TextAdapter(lists, this);
        lv.setAdapter(adapter);
    }


    /*
     *设置时间
     */
    private String getTime(){
        currentTime = System.currentTimeMillis();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = format.format(curDate);
        if(currentTime - oldTime >= 5*60*1000){
            oldTime = currentTime;
            return str;
        }else{
            return "";
        }
    }

    /*
     *点击事件的处理
     */
    @Override
    public void onClick(View v) {
        content_str = et_sendText.getText().toString();
        et_sendText.setText("");

//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        String time = sdf.format(new Date());
        ListData listData;
        listData = new ListData(fromWho,toUser,content_str, ListData.SEND, getTime());
        lists.add(listData);

        Log.i(TAG, "----------content_str="+content_str);
        adapter.notifyDataSetChanged();

        Log.i(TAG, "----------content_str="+content_str);
        Gson gson = new Gson();
        final String jsonStr = gson.toJson(listData, ListData.class);
        Log.i( TAG, "myRepublish: jsonStr is "+jsonStr );

        if(lists.size() > 30){
            for (int i = 0; i < lists.size(); i++) {
                lists.remove(i);
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                myRepublish(jsonStr);
            }
        }).start();
    }

    /*
     *发布消息
     */
    private void myRepublish(String jsonStr) {
        try {
            message = new MqttMessage();
            message.setQos(0); // 可以有三种值（0,1,2），分别代表消息发送情况：至少发送一次，至少
            message.setRetained(false);
            Log.i(TAG, message.isRetained() + "------retained状态");

            //设置负载，即消息内容
            message.setPayload(jsonStr.getBytes());

            Log.i(TAG, "message is :" + new String(message.getPayload()));
            MqttDeliveryToken token = topic.publish(message);
            token.waitForCompletion();
            Log.i(TAG, token.isComplete() + "===============================================");
        } catch (Exception e) {
            e.printStackTrace();
            Log.i( TAG, "myRepublish: "+message.toString() );
        }
    }

}
