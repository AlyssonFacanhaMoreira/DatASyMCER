package alysson.br.graficosarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {

    protected BluetoothAdapter btfAdapter;


    private static final String TAG ="yopa";
    //UUID para conexão
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothDevice device;
    private boolean running;
    private OutputStream out;
    private InputStream in;
    private BluetoothSocket socket;
    Button btConnect,btReconnect;

    TextView tvNomeDevice;

    WebView wvGraph;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvNomeDevice = (TextView) findViewById(R.id.tvNomeDevice);
        btConnect = (Button) findViewById(R.id.btConnect);
        btReconnect = (Button) findViewById(R.id.btReconnect);


        //Testando presença de bluetooth
        btfAdapter = BluetoothAdapter.getDefaultAdapter();
        if(btfAdapter==null){
            Toast.makeText(this,"Bluetooth não presente",Toast.LENGTH_SHORT).show();
            finish();
        }

        btConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if(device==null) {
                    Intent buscaPareados = new Intent(MainActivity.this, BluetoothCheckActivity.class);
                    startActivityForResult(buscaPareados, 1);

                //}
            }
        });

        btReconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ThreadClient().start();
                running = true;
            }
        });



    }

    @Override
    protected void onResume() {
        super.onResume();

        if(device!=null && socket!=null){
            btReconnect.setEnabled(true);
        }else{
            btReconnect.setEnabled(false);
        }

        if(btfAdapter.isEnabled()){
            //Toast.makeText(this,"Bluetooth ligado",Toast.LENGTH_SHORT).show();
            btConnect.setClickable(true);
            //checkDeviceStatus();
        }else{
            //Toast.makeText(this,"Bluetooth desligado",Toast.LENGTH_SHORT).show();
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,0);
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==0 && resultCode != RESULT_OK){
            Toast.makeText(this,"Bluetooth Não foi ativado",Toast.LENGTH_SHORT).show();
            btConnect.setClickable(true);
        }

        if(requestCode==1 && resultCode == RESULT_OK){
            device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            setDeviceName(device.getName(),device.getAddress());
            btReconnect.setEnabled(true);
            new ThreadClient().start();
            running=true;
        }

    }


    private void makeToast(final String content, final int LENGTH){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, content,LENGTH).show();
            }
        });

    }

    private void setDeviceName(final String name, final String address){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvNomeDevice.setText(name + " - " + address);
            }
        });

    }

    private void enableReconnection(){

        runOnUiThread(new Runnable() {
        @Override
        public void run() {
            tvNomeDevice.setText(getString(R.string.tvNomeDevice));
            btReconnect.setClickable(true);
            btReconnect.setEnabled(true);
        }
    });

    }


    private void loadChart(final String[] vals){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content = null;

                try {

                    AssetManager assetManager = getAssets();
                    InputStream lcin = assetManager.open("grafico.html");
                    byte[] bytes = readHTML(lcin);
                    content = new String(bytes, "UTF-8");

                    final String formattedContent = String.format(content,
                            Float.parseFloat(vals[0]),
                            Float.parseFloat(vals[1]),
                            Float.parseFloat(vals[2]),
                            Float.parseFloat(vals[3]));
                    wvGraph.loadDataWithBaseURL("file:///android_asset/", formattedContent, "text/html", "utf-8", null);

                } catch (IOException e) {
                    Log.e("Exceção captada: ", e.getMessage());
                }

            }
        });

    }

    private static byte[] readHTML(InputStream in) throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            baout.write(buffer, 0, count);
        }
        return baout.toByteArray();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        running = false;

        try{
            if(in!=null){
                in.close();
            }

        }catch (IOException e){}

        try{
            if(socket!=null){
                socket.close();
            }

        }catch (IOException e){}
    }


    class  ThreadClient extends Thread {

        @Override
        public void run() {
            //super.run();
            try{
                makeToast("Estabelecendo Conexão Com: "+device.getName(),Toast.LENGTH_SHORT);
                socket = device.createRfcommSocketToServiceRecord(uuid);
                //Após essa linha esta conectado senão foi pra excessão
                socket.connect();
                if(socket.isConnected()){
                    makeToast("Conexão Estabelecida",Toast.LENGTH_SHORT);
                    //out = socket.getOutputStream();

                    in = socket.getInputStream();


                    byte[] bytes = new byte[1024];
                    int length;


                    //entra em loop esperando para ler a mensagem do microcontrolador

                    while(running){



                            length = in.read(bytes);
                            String values = new String(bytes, 0, length);
                            String[] sValues = values.split("/");
                            loadChart(sValues);
                            values= null;
                            sValues = null;



                    }


                }


            }catch(IOException e){
                makeToast("Problemas com a conexão: " + device.getName(), Toast.LENGTH_SHORT);
                enableReconnection();
                running=false;
                Log.e(TAG,"erro ao conectar: "+e.getMessage());
            }
            running=false;

        }
    }


}
