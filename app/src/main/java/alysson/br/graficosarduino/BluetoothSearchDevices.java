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
import android.widget.Toast;


public class BluetoothSearchDevices extends BluetoothCheckActivity {

    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Registra o receiver para receber as mensagens de dispositivos pareados
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));

        // Registra o receiver para reveber as mensagens de quando a busca terminar
        this.registerReceiver(mReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));

        // Inicia a Busca
        search();

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


    private void search(){

        // Previnindo iniciar busca com outra ocorrendo
        if(btfAdapter.isDiscovering()){
            btfAdapter.cancelDiscovery();
        }

        //Inicia a busca
        btfAdapter.startDiscovery();
        dialog = ProgressDialog.show(this, getString(R.string.pdTitle), getString(R.string.pdContent),false,true);

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

                finish();


            }


        }
    };


}
