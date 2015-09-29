package alysson.br.graficosarduino;


import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import java.util.ArrayList;
import java.util.List;


public class BluetoothCheckActivity extends MainActivity implements OnItemClickListener {

    protected List<BluetoothDevice> lista;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_check);

        listView = (ListView) findViewById(R.id.listview);

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
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,int idx,long id){

        if(idx==0){

            // Se o primeiro item foi clicado chama a activity de busca de dispositivos
            Intent intent = new Intent(BluetoothCheckActivity.this,BluetoothSearchDevices.class);
            startActivity(intent);

        }else{

            //Recupera o device selecionado
            BluetoothDevice device = lista.get(idx);

            // Cria a intent de resultado com o device selecionado
            Intent returnI = new Intent();
            returnI.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            setResult(RESULT_OK, returnI);

            // Finaliza a Activity
            finish();

        }
    }


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


}
