package alysson.br.graficosarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;


public class MainActivity extends ActionBarActivity {


    // Bluetooth
    protected BluetoothAdapter btfAdapter;
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private InputStream inputStream;
    // Bluetooth



    // Widgets
    TextView tvNomeDevice;
    Button btSearchD,btReconnect;
    WebView wvGraph;
    // Widgets

    private static final String TAG ="DatASyMCER";

    private boolean running;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializando widgets
        tvNomeDevice = (TextView) findViewById(R.id.tvNomeDevice);
        wvGraph = (WebView) findViewById(R.id.wvGraph);
        btSearchD = (Button) findViewById(R.id.btSearchD);
        btReconnect = (Button) findViewById(R.id.btReconnect);


        btfAdapter = BluetoothAdapter.getDefaultAdapter();
        // Testando presença de bluetooth
        if(btfAdapter==null){
            makeToast(this, getString(R.string.ttBlueU), Toast.LENGTH_SHORT);
            finish();
        }


        // Listener do Botão SearchD
        btSearchD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                running = false;
                Intent buscaPareados = new Intent(MainActivity.this, BluetoothCheckActivity.class);
                startActivityForResult(buscaPareados, 1);
            }
        });

        // Listener do Botão Reconnect
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

        // Ao reiniciar a aplicacao, checa se um device ja foi selecionado e ativa ou não a reconexao
        if(device!=null && socket!=null){
            btReconnect.setEnabled(true);
        }else{
            btReconnect.setEnabled(false);
        }


        // Checa se o bluetooth esta ligado
        if(btfAdapter.isEnabled()){

            // Habilita a busca se verdadeiro
            btSearchD.setEnabled(true);

        }else{

            // Senão, requisita que o bluetoothe seja ligado
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,0);

        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        running = false;

        // Fecha o InputStream se não for nulo
        try{
            if(inputStream!=null){
                inputStream.close();
            }

        }catch (IOException e){}

        // Fecha o Socket se não for nulo
        try{
            if(socket!=null){
                socket.close();
            }

        }catch (IOException e){}
    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Caso o usuário se recusar a ativar o bluetooth
        if(requestCode==0 && resultCode != RESULT_OK){

            // Mostra uma mensagem e disabilita o botão btSearchD
            makeToast(MainActivity.this,getString(R.string.ttBlueR),Toast.LENGTH_SHORT);
            btSearchD.setEnabled(false);

        }

        // Obtendo resultado de BlutoothCheckActivity
        if(requestCode==1 && resultCode == RESULT_OK){

            // Armazena o device resultado
            device = data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            setDeviceName(device.getName(),device.getAddress());
            btReconnect.setEnabled(true);

            // Inicia a Thread de Conexão
            new ThreadClient().start();
            running=true;
        }

    }


    /**
     *
     *      Inicio
     * Funções de Interface
     *
     **/

    // Função para mostrar mensagens
    private void makeToast(final Context context,final String content, final int LENGTH){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, content,LENGTH).show();
            }
        });

    }

    // Função para por o nome do device conectado na TextView
    private void setDeviceName(final String name, final String address){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvNomeDevice.setText(name + " - " + address);
            }
        });

    }

    // Função para habilitar a reconexão
    private void enableReconnection(){

        runOnUiThread(new Runnable() {
        @Override
        public void run() {
            tvNomeDevice.setText(getString(R.string.tvNomeDevice));
            btReconnect.setEnabled(true);
        }
    });

    }

    // Função que configura e mostra o grafico
    private void loadChart(final String[] vals){

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String content = null;

                try {

                    // Abre o html e chama a função para ler o arquivo
                    AssetManager assetManager = getAssets();
                    InputStream lcin = assetManager.open("grafico.html");
                    byte[] bytes = readHTML(lcin);
                    content = new String(bytes, "UTF-8");

                    // Formata a string, adicionando os valores passados para função
                    final String formattedContent = String.format(content,
                            Float.parseFloat(vals[0]),
                            Float.parseFloat(vals[1]),
                            Float.parseFloat(vals[2]),
                            Float.parseFloat(vals[3]));

                    // Carrega na WebView o conteudo formatado
                    wvGraph.loadDataWithBaseURL("file:///android_asset/", formattedContent, "text/html", "utf-8", null);

                } catch (IOException e) {
                    Log.e("Exceção captada: ", e.getMessage());
                }

            }
        });

    }

    /**
     *
     *       Fim
     * Funções de Interface
     *
     **/

    // Função chamada para ler o HTML
    private static byte[] readHTML(InputStream in) throws IOException {
        ByteArrayOutputStream baout = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        for (int count; (count = in.read(buffer)) != -1; ) {
            baout.write(buffer, 0, count);
        }
        return baout.toByteArray();
    }



    class  ThreadClient extends Thread {

        @Override
        public void run() {

            try{

                makeToast(MainActivity.this, getString(R.string.ttBlueC) +device.getName(),Toast.LENGTH_SHORT);
                socket = device.createRfcommSocketToServiceRecord(uuid);

                // Após essa linha esta conectado senão foi pra excessão
                socket.connect();
                if(socket.isConnected()){

                    // Avisa ao usuário que a conexão foi estabelecida
                    makeToast(MainActivity.this, getString(R.string.ttBlueE),Toast.LENGTH_SHORT);

                    //Objetos e variavel necessários para ler a mensagem
                    inputStream = socket.getInputStream();
                    byte[] bytes = new byte[1024];
                    int length;


                    // Entra em loop esperando para ler a mensagem do microcontrolador
                    while(running){

                        // Inicia a leitura da mensagem
                        length = inputStream.read(bytes);

                        // As linhas abaixo só são executadas após a leitura da mensagem

                        // O conteudo da mensagem é guardado nessa variavel
                        String values = new String(bytes, 0, length);
                        // E separado nessa
                        String[] sValues = values.split("/");

                        // A função que mostra o grafico é chamada com os valores
                        loadChart(sValues);

                    }


                }

            // Caso haja algum problema ao se conectar ou a conexão seja perdida
            }catch(IOException e){

                // Mostra uma mensagem avisando o usuário e reabilita a reconexão
                makeToast(MainActivity.this, getString(R.string.ttBlueP) + device.getName(), Toast.LENGTH_SHORT);
                enableReconnection();
                running=false;

                Log.e(TAG,"erro ao conectar: "+e.getMessage());
            }
            running=false;

        }
    }


}
