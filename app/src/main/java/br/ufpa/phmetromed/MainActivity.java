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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private long id;
    private String NomePaciente;
    private String DataNascPaciente;
    //private Date DataNascPaciente;
    private String Convenio;

    private EditText editNomePaciente;
    private EditText editDataNascPaciente;
    private EditText editConvenio;

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // Make sure user insert date into edittext in this format.

    private Button salvarCadastro;

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
    public String btDevAddress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editNomePaciente = (EditText) findViewById(R.id.editNomePaciente2);
        editDataNascPaciente = (EditText) findViewById(R.id.editDataNascPaciente2);
        editConvenio = (EditText) findViewById(R.id.editConvenio2);

        this.salvarCadastro = (Button) findViewById(R.id.configurarExame);

        this.salvarCadastro.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //executado ao clicar no botao
                int test = 0;
                Random r = new Random();
                id = r.nextInt(999999999);

                if (!editNomePaciente.getText().toString().isEmpty()) {
                    NomePaciente = editNomePaciente.getText().toString();
                } else{
                    Toast.makeText(getApplicationContext(), "Preencha o Nome do Paciente...", Toast.LENGTH_LONG).show();
                    test = 1;
                }
                if(!editDataNascPaciente.getText().toString().isEmpty()) {
                    //String datanasc = editDataNascPaciente.getText().toString();
                    try {
                        DataNascPaciente = editDataNascPaciente.getText().toString();//formatter.parse(datanasc);
                    } catch (/*Parse*/Exception e) {
                        e.printStackTrace();
                    }
                } else{
                    Toast.makeText(getApplicationContext(), "Preencha a Data de Nascimento...", Toast.LENGTH_LONG).show();
                    test = 1;
                }
                if (!editConvenio.getText().toString().isEmpty()) {
                    Convenio = editConvenio.getText().toString();
                } else{
                    Toast.makeText(getApplicationContext(), "Preencha o Convênio...", Toast.LENGTH_LONG).show();
                    test = 1;
                }

                if(test == 0){
                    String cadastro = "Id: "+ id +"\nNome: " + NomePaciente + "\nData de Nascimento: " + DataNascPaciente + "\nConvênio: " + Convenio + "\n";
                    Log.i("cadastro", cadastro);
                    Log.i("btDevAddress: ", btDevAddress);
                    try {
                        // Envia para o arduino
                        connect.write(cadastro.getBytes());
                        Toast.makeText(getApplicationContext(), "Enviar para o Arduino", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.e("Error", "exception: " + e.getMessage());
                        Log.e("Error", "exception: " + e.toString());
                        Toast.makeText(getApplicationContext(), "ERRO ao enviar para o Arduino", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

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
        //Intent Intent = new Intent(this, ConfigurarExame.class);
        //startActivity(Intent);
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

                btDevAddress = data.getStringExtra("btDevAddress");
                connect = new ConnectionThread(data.getStringExtra("btDevAddress"));
                connect.start();
            } else {
                statusMessage.setText("Nenhum dispositivo selecionado :(");
            }
        }
    }
}

