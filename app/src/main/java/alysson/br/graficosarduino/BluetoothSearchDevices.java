package alysson.br.graficosarduino;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;


public class BluetoothSearchDevices extends BluetoothCheckActivity {

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.activity_bluetooth_search_devices);

        //Registra o receiver para receber as mensagens de dispositivos pareados
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
        //Register for broadcasts when discovery has finished
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        buscar();
    }

    private void buscar(){
        //previnindo de iniciar com outra busca ocorrendo
        if(btfAdapter.isDiscovering()){
            btfAdapter.cancelDiscovery();
        }

        //Inicia a busca
        btfAdapter.startDiscovery();
        dialog = ProgressDialog.show(this,"Aguarde", "Buscando aparelhos bluetooth",false,true);


    }

    //Receiver para receber os broadcasts do bluetooth
    private  final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        //Quantidade de dispositivos encontrados
        private int count;

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            //Se um dispositivo for encontrado
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //Recupera o device da intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //Apenas insere na lista os devicesque ainda não estão pareadas
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    lista.add(device);

                    Toast.makeText(context, "Encontrou: " + device.getName() + ":" + device.getAddress(), Toast.LENGTH_SHORT).show();

                    count++;

                }

            } else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //Iniciou a busca
                count = 0;
                Toast.makeText(context, "Busca Iniciada", Toast.LENGTH_SHORT).show();
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //Fim da Busca
                Toast.makeText(context, "Busca Finalizada. " + count + " devices encontrados", Toast.LENGTH_LONG).show();
                dialog.dismiss();

                //Atualiza o listview, e agora todos os devices pareados estarão la
                updateList();


            }


        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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



}
