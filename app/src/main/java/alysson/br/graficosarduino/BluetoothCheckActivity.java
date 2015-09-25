package alysson.br.graficosarduino;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;


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

            lista = new ArrayList<BluetoothDevice>(btfAdapter.getBondedDevices());
            updatelista();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    protected void updatelista(){

        List<String> nomes = new ArrayList<String>();
        nomes.add(0,getString(R.string.lvHeader));
        for(BluetoothDevice device : lista){
            boolean pareado = device.getBondState() == BluetoothDevice.BOND_BONDED;
            nomes.add(device.getName()+" - "+device.getAddress()+(pareado ? " *pareado" : ""));
        }

        int layout = android.R.layout.simple_list_item_1;
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,layout,nomes);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,int idx,long id){
        //Reupera o device selecionado
        if(idx==0){
            Intent intent = new Intent(BluetoothCheckActivity.this,BluetoothSearchDevices.class);
            startActivity(intent);

        }else{
            BluetoothDevice device = lista.get(idx);
            //String msg = device.getName()+" - "+device.getAddress();
            //Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

            //Vai para a tela de envio de mensagens
        /*
        Intent intent = new Intent(this,ConexaoBluetooth.class);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE,device);
        startActivity(intent);
        */

            Intent returnI = new Intent();
            returnI.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
            setResult(RESULT_OK, returnI);
            finish();

        }
    }


}
