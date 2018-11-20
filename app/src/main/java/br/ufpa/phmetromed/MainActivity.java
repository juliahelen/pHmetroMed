package br.ufpa.phmetromed;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    @SuppressLint("StaticFieldLeak")
    static TextView statusMessage;
    @SuppressLint("StaticFieldLeak")
    static TextView viewPH;

    public String data_completa, dia, hora;
    public Date data_atual;

    public ConnectionThread connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        statusMessage = (TextView) findViewById(R.id.statusMessage);

        viewPH = (TextView) findViewById(R.id.viewPH);

        /* Teste rápido. O hardware Bluetooth do dispositivo Android
            está funcionando ou está bugado
         */
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusMessage.setText("Que pena! Hardware Bluetooth não está funcionando :(");
        } else {
            statusMessage.setText("Ótimo! Hardware Bluetooth está funcionando :D");
        }

        if(!btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
            statusMessage.setText("Solicitando ativação do Bluetooth...");
        } else {
            statusMessage.setText("Bluetooth já ativado :)");
        }

        /* Um descanso rápido, para evitar bugs.
         */
        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }

    public void configExame(View view){
        Intent Intent = new Intent(this, ConfigurarExame.class);
        startActivity(Intent);
    }

    public void tempo(){
        try{
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
            SimpleDateFormat dateFormat_dia = new SimpleDateFormat("dd-MM-yyyy");

            Date data = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            data_atual = cal.getTime();

            data_completa = dateFormat.format(data_atual);
            dia = dateFormat_dia.format(data_atual);
            hora = dateFormat_hora.format(data_atual);

            Log.i("data_completa", data_completa);
        }catch (Exception e){

        }

    }

    public void salvar(String texto){
        tempo();
        try{
            if (true){ //função de salvar
                Toast.makeText(MainActivity.this,"Salvo com sucesso!",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(MainActivity.this,"Erro ao salvar!",Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e) {
            Toast.makeText(MainActivity.this, "Erro ao salvar!", Toast.LENGTH_LONG).show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        //int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
        //    return true;
        //}
        return super.onOptionsItemSelected(item);
    }


    @SuppressLint("HandlerLeak")
    public static Handler handler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            /* Esse método é invocado na Activity principal
                sempre que a thread de conexão Bluetooth recebe
                uma mensagem.
             */
            Bundle bundle = msg.getData();
            byte[] data = null;
            data = bundle.getByteArray("data");
            String dataString= null;
            if (data != null) {
                dataString = new String(data);
            }
            /* Aqui ocorre a decisão de ação, baseada na string
                recebida. Caso a string corresponda à uma das
                mensagens de status de conexão (iniciadas com --),
                atualizamos o status da conexão conforme o código.
             */
            if (dataString != null) {
                if(dataString.equals("---N"))
                    statusMessage.setText("Ocorreu um erro durante a conexão D:");
                else if(dataString.equals("---S"))
                    statusMessage.setText("Conectado :D");
                else {
                    /* Se a mensagem não for um código de status,
                        então ela deve ser tratada pelo aplicativo
                        como uma mensagem vinda diretamente do outro
                        lado da conexão. Nesse caso, simplesmente
                        atualizamos o valor contido no TextView do
                        contador.
                     */
                    if(dataString.contains("PH")){
                        viewPH.setText(dataString);
                    }
                }
            }
        }
    };

    public void searchPairedDevices(View view) {
        Intent searchPairedDevicesIntent = new Intent(this, PairedDevices.class);
        startActivityForResult(searchPairedDevicesIntent, SELECT_PAIRED_DEVICE);
    }

    @SuppressLint("SetTextI18n")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == ENABLE_BLUETOOTH) {
            if(resultCode == RESULT_OK) {
                statusMessage.setText("Bluetooth ativado :D");
            }
            else {
                statusMessage.setText("Bluetooth não ativado :(");
            }
        }
        else if(requestCode == SELECT_PAIRED_DEVICE || requestCode == SELECT_DISCOVERED_DEVICE) {
            if (resultCode == RESULT_OK) {
                statusMessage.setText("Você selecionou " + data.getStringExtra("btDevName") + "\n"
                        + data.getStringExtra("btDevAddress"));

                connect = new ConnectionThread(data.getStringExtra("btDevAddress"));
                connect.start();
            } else {
                statusMessage.setText("Nenhum dispositivo selecionado :(");
            }
        }
    }
}

