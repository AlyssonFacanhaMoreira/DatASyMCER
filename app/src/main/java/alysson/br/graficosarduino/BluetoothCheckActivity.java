package alysson.br.graficosarduino;


import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.List;


public class BluetoothCheckActivity extends MainActivity implements OnItemClickListener {

    protected List<BluetoothDevice> lista;
    private ListView listView;
    private ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_check);

        listView = (ListView) findViewById(R.id.listview);

        // Registra o receiver para receber as mensagens de dispositivos pareados
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // Registra o receiver para reveber as mensagens de quando a busca terminar
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

    }

    @Override
    protected void onResume() {
        super.onResume();

        if(btfAdapter!=null){

            // Se o btfAdapter não for nulo, prenche o arraylist com os dispositivos pareados
            lista = new ArrayList<BluetoothDevice>(btfAdapter.getBondedDevices());
            updateList();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //Garante o canncelamento da busca ao sair
        if(btfAdapter!=null){
            btfAdapter.cancelDiscovery();
        }

        //Cancela o registro do receiver
        this.unregisterReceiver(mReceiver);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,int idx,long id){

        if(idx==0){

            // Se o primeiro item foi clicado chama a activity de busca de dispositivos
            // Inicia a Busca
            search();

            //Intent intent = new Intent(BluetoothCheckActivity.this,BluetoothSearchDevices.class);
            //startActivity(intent);

        }else{

            //Recupera o device selecionado
            BluetoothDevice device = lista.get(idx-1);

            // Cria a intent de resultado com o device selecionado
            Intent returnI = new Intent();
            returnI.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            setResult(RESULT_OK, returnI);

            // Finaliza a Activity
            finish();

        }
    }

    // Atualiza a lista de Devices e a lista de Nomes
    protected void updateList(){

        // Cria um arraylist de strings
        List<String> nomes = new ArrayList<String>();

        // O primeiro item é a busca
        nomes.add(0,getString(R.string.lvHeader));

        boolean pareado;

        for(BluetoothDevice device : lista){

            // Armazena o estado de pareamento do device corrente
            pareado = device.getBondState() == BluetoothDevice.BOND_BONDED;

            // Adiciona à lista o nome o enderesso e se o device está pareado ou não
            nomes.add(device.getName()+" - "+device.getAddress()+(pareado ? " *pareado" : ""));

        }

        // Configurações da listview (layout, arraylist, listener)
        int layout = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,layout,nomes);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    // Função de busca de dispositivos
    private void search(){

        // Previnindo iniciar busca com outra ocorrendo
        if(btfAdapter.isDiscovering()){
            btfAdapter.cancelDiscovery();
        }

        //Inicia a busca
        btfAdapter.startDiscovery();
        dialog = ProgressDialog.show(this, getString(R.string.pdTitle), getString(R.string.pdContent), false, true);

    }


    // Receiver para receber os broadcasts do bluetooth
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

        // Quantidade de dispositivos encontrados
        private int count;

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            // Se um dispositivo for encontrado
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {

                // Recupera o device da intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // Apenas insere na lista os devicesque ainda não estão pareadas
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {

                    lista.add(device);

                    Toast.makeText(context, getString(R.string.ttFoundD) + device.getName() + ":" + device.getAddress(), Toast.LENGTH_SHORT).show();

                    count++;

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

                // Iniciou a busca
                count = 0;
                Toast.makeText(context, getString(R.string.ttSearchS), Toast.LENGTH_SHORT).show();

            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                // Fim da Busca
                Toast.makeText(context, getString(R.string.ttSearchE1) + count + getString(R.string.ttSearchE2), Toast.LENGTH_LONG).show();
                dialog.dismiss();

                // Atualiza o listview, e agora todos os devices pareados estarão la
                updateList();

            }


        }
    };


}
