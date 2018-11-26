package br.ufpa.phmetromed;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class ConfigurarExame extends MainActivity{

    private long id;
    private String NomePaciente;
    private Date DataNascPaciente;
    private String Convenio;

    private EditText editNomePaciente;
    private EditText editDataNascPaciente;
    private EditText editConvenio;

    DateFormat formatter = new SimpleDateFormat("dd/MM/yyyy"); // Make sure user insert date into edittext in this format.

    private Button salvarCadastro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configurar_exame);

        editNomePaciente = (EditText) findViewById(R.id.editNomePaciente);
        editDataNascPaciente = (EditText) findViewById(R.id.editDataNascPaciente);
        editConvenio = (EditText) findViewById(R.id.editConvenio);

        this.salvarCadastro = (Button) findViewById(R.id.salvarCadastro);

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
                    String datanasc = editDataNascPaciente.getText().toString();
                    try {
                        DataNascPaciente = formatter.parse(datanasc);
                    } catch (ParseException e) {
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
                        Toast.makeText(getApplicationContext(), "Enviar para o Arduino", Toast.LENGTH_SHORT).show();
                    }catch (Exception e){
                        Log.e("Error", "exception: " + e.getMessage());
                        Log.e("Error", "exception: " + e.toString());
                        Toast.makeText(getApplicationContext(), "ERRO ao enviar para o Arduino", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });

    }

}
