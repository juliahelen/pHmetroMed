package br.ufpa.phmetromed;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConfigurarExame extends AppCompatActivity {

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
        });

    }

    public void Salvar(View view) throws ParseException {

        int test = 0;

        if (this.editNomePaciente.getText().toString().isEmpty() == false) {
            NomePaciente = this.editNomePaciente.getText().toString();
        } else{
            Toast.makeText(getApplicationContext(), "Preencha o Nome do Paciente...", Toast.LENGTH_LONG).show();
            test = 1;
        }
        if(this.editDataNascPaciente.getText().toString().isEmpty() == false) {
            String datanasc = this.editDataNascPaciente.getText().toString();
            try {
                DataNascPaciente = formatter.parse(datanasc);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else{
            Toast.makeText(getApplicationContext(), "Preencha a Data de Nascimento...", Toast.LENGTH_LONG).show();
            test = 1;
        }
        if (this.editConvenio.getText().toString().isEmpty() == false) {
            Convenio = this.editConvenio.getText().toString();
        } else{
            Toast.makeText(getApplicationContext(), "Preencha o Convênio...", Toast.LENGTH_LONG).show();
            test = 1;
        }

        if(test == 0){
            // Emvia para o arduino
            Toast.makeText(getApplicationContext(), "Enviar para o Arduino", Toast.LENGTH_SHORT).show();
        }
    }

}
