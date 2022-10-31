package com.droidev.levantamento;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class AnotacoesActivity extends AppCompatActivity {

    EditText anotacao;
    Utils utils;
    TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_anotacoes);

        setTitle("Anotações");

        utils = new Utils();
        tinyDB = new TinyDB(AnotacoesActivity.this);

        anotacao = findViewById(R.id.anotacao_edittext);

        if (!tinyDB.getString("Fonte").isEmpty()) {

            anotacao.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("Fonte")));
        }

        anotacao.setText(utils.recuperarDaMemoria(AnotacoesActivity.this, "anotacoes.txt"));
    }

    @Override
    public void onBackPressed() {

        utils.manterNaMemoria(AnotacoesActivity.this, anotacao.getText().toString(), "anotacoes.txt");

        AnotacoesActivity.this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_anotacoes_activity, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.apagar_anotacao:

                anotacao.setText("");

                return true;

            case R.id.copiar_anotacao:

                utils.copiarTexto(AnotacoesActivity.this, anotacao.getText().toString());

                return true;

            case R.id.salvar_anotacao:

                utils.manterNaMemoria(AnotacoesActivity.this, anotacao.getText().toString(), "anotacoes .txt");

                Toast.makeText(AnotacoesActivity.this, "Salvo", Toast.LENGTH_SHORT).show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}