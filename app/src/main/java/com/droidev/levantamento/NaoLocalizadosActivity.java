package com.droidev.levantamento;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class NaoLocalizadosActivity extends AppCompatActivity {

    TextView naoAchados, naoAchadosTV;
    ScrollView naoAchadosScrollView;
    ArrayList arrayList;
    String s = "";
    int j = 0;
    Utils utils;
    CaixaDialogo caixaDialogo;
    TinyDB tinyDB;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nao_achados);

        setTitle("NÃO LOCALIZADOS");

        utils = new Utils();
        caixaDialogo = new CaixaDialogo();

        tinyDB = new TinyDB(NaoLocalizadosActivity.this);

        arrayList = new ArrayList<>();

        naoAchados = findViewById(R.id.naoAchados);
        naoAchadosTV = findViewById(R.id.naoAchadosTV);

        naoAchadosScrollView = findViewById(R.id.naoAchadosScrollView);

        Intent intent = getIntent();
        arrayList = intent.getStringArrayListExtra("arraylist");

        for (int i = 0; i < arrayList.size(); i++) {

            j = i + 1;

            s += j + "- " + arrayList.get(i) + "\n";
        }

        naoAchadosTV.setText(j + " ITENS");

        naoAchados.setText(s);

        if (!tinyDB.getString("Fonte").isEmpty()) {

            naoAchados.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("Fonte")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_nao_achados_activity, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copiarNaoAchados:

                utils.copiarTexto(NaoLocalizadosActivity.this,
                        "Bens que faltam checar - "
                                + j + "\n\n"
                                + naoAchados.getText().toString());

                return true;

            case R.id.procurarNaoAchados:

                caixaDialogo.simplesComView(NaoLocalizadosActivity.this,
                        "Procurar",
                        "Digite uma palavra abaixo para realçar.",
                        "Exemplo: estabilizador",
                        "Procurar",
                        "Cancelar",
                        InputType.TYPE_CLASS_TEXT,
                        true,
                        false,
                        i -> utils.procurarTexto(naoAchados, i.toUpperCase(), naoAchadosTV));

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
}