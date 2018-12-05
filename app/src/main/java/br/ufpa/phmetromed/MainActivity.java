package br.ufpa.phmetromed;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vicmikhailau.maskededittext.MaskedFormatter;
import com.vicmikhailau.maskededittext.MaskedWatcher;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private long id_exame;
    private String NomePaciente;
    private String DataNascPaciente;
    private String Convenio;

    public static int ENABLE_BLUETOOTH = 1;
    public static int SELECT_PAIRED_DEVICE = 2;
    public static int SELECT_DISCOVERED_DEVICE = 3;

    @SuppressLint("StaticFieldLeak")
    static TextView statusMessage;
    @SuppressLint("StaticFieldLeak")
    static TextView viewPH;

    public Button configExame;

    public String data_completa, dia, hora;
    public Date data_atual;

    public ConnectionThread connect;
    public String btDevAddress = null;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        configExame = findViewById(R.id.configurarExame);
        configExame.setOnClickListener(new Button.OnClickListener()
            {
                @SuppressLint("SetTextI18n")
                public void onClick(View v){
                    showInputDialog();
                }
            }
        );

        statusMessage = findViewById(R.id.statusMessage);
        viewPH = findViewById(R.id.viewPH);

        /* Teste rápido. O hardware Bluetooth do dispositivo Android
            está funcionando ou está bugado
         */
        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            statusMessage.setText("Que pena! Hardware Bluetooth não está funcionando :(");
        } else {
            statusMessage.setText("Ótimo! Hardware Bluetooth está funcionando :D");
        }

        if (btAdapter != null) {
            if(!btAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH);
                statusMessage.setText("Solicitando ativação do Bluetooth...");
            } else {
                statusMessage.setText("Bluetooth já ativado :)");
            }
        }

        /* Um descanso rápido, para evitar bugs.
         */
        try {
            Thread.sleep(1000);
        } catch (Exception E) {
            E.printStackTrace();
        }
    }


    public void tempo(){
        try{
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat_hora = new SimpleDateFormat("HH:mm:ss");
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateFormat_dia = new SimpleDateFormat("dd-MM-yyyy");

            Date data = new Date();
            Calendar cal = Calendar.getInstance();
            cal.setTime(data);
            data_atual = cal.getTime();

            data_completa = dateFormat.format(data_atual);
            dia = dateFormat_dia.format(data_atual);
            hora = dateFormat_hora.format(data_atual);

            Log.i("data_completa", data_completa);
        }catch (Exception ignored){

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
            byte[] data;
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
                switch (dataString) {
                    case "---N":
                        statusMessage.setText("Ocorreu um erro durante a conexão D:");
                        break;
                    case "---S":
                        statusMessage.setText("Conectado :D");
                        break;
                    default:
                    /* Se a mensagem não for um código de status,
                        então ela deve ser tratada pelo aplicativo
                        como uma mensagem vinda diretamente do outro
                        lado da conexão. Nesse caso, simplesmente
                        atualizamos o valor contido no TextView do
                        contador.
                     */
                        if (dataString.contains("PH")) {
                            viewPH.setText(dataString);
                        }
                        break;
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
                statusMessage.setText("Bluetooth ativado");
            }
            else {
                statusMessage.setText("Bluetooth não ativado");
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
                statusMessage.setText("Nenhum dispositivo selecionado");
            }
        }
    }

    protected void showInputDialog() {

        // get dialog_configurarexame.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        @SuppressLint("InflateParams") View promptView = layoutInflater.inflate(R.layout.dialog_configurarexame, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        //Set campo
        final EditText editDataNascPaciente = (EditText) promptView.findViewById(R.id.data_nascimento);
        MaskedFormatter formatter = new MaskedFormatter("##/##/####");
        editDataNascPaciente.addTextChangedListener(new MaskedWatcher(formatter, editDataNascPaciente));

        final EditText editNomePaciente = (EditText) promptView.findViewById(R.id.nome_paciente);
        final EditText editConvenio = (EditText) promptView.findViewById(R.id.convenio);

        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("Registrar", new DialogInterface.OnClickListener() {
                    @SuppressLint("SetTextI18n")
                    public void onClick(DialogInterface dialog, int id) {

                        //executado ao clicar no botao
                        int test = 0;

                        if (!editNomePaciente.getText().toString().isEmpty()) {
                            NomePaciente = editNomePaciente.getText().toString();

                            if(!editDataNascPaciente.getText().toString().isEmpty()) {
                                DataNascPaciente = editDataNascPaciente.getText().toString();

                                if (!editConvenio.getText().toString().isEmpty()) {
                                    Convenio = editConvenio.getText().toString();
                                } else{
                                    Toast.makeText(getApplicationContext(), "Preencha o Convênio...", Toast.LENGTH_SHORT).show();
                                    test = 1;
                                }

                            } else{
                                Toast.makeText(getApplicationContext(), "Preencha a Data de Nascimento...", Toast.LENGTH_SHORT).show();
                                test = 1;
                            }

                        } else{
                            Toast.makeText(getApplicationContext(), "Preencha o Nome do Paciente...", Toast.LENGTH_SHORT).show();
                            test = 1;
                        }

                        if(test == 0){  //enviar
                            Random r = new Random();
                            id_exame = r.nextInt(999999999);
                            tempo();

                            String cadastro = "ID: "+ id_exame +"\nNome: " + NomePaciente + "\nData de Nascimento: "
                                    + DataNascPaciente + "\nConvênio: " + Convenio + "\n"+ "Início do Exame:" + data_completa + "\n";

                            Log.i("cadastro", cadastro);
                            Log.i("btDevAddress: ", btDevAddress);
                            try {
                                // Envia para o arduino
                                connect.write(cadastro.getBytes());
                                Toast.makeText(getApplicationContext(), "Enviar para o Arduino", Toast.LENGTH_SHORT).show();
                            }catch (Exception e){
                                Log.e("Error", "exception: " + e.getMessage());
                                Log.e("Error", "exception: " + e.toString());
                                Toast.makeText(getApplicationContext(), "Erro ao configurar, tente novamente.", Toast.LENGTH_SHORT).show();
                            }
                        }

                    }
                })
                .setNegativeButton("Cancelar",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }
}

