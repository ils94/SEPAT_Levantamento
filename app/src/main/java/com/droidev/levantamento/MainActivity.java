package com.droidev.levantamento;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.documentfile.provider.DocumentFile;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private TextView relacao, foraDaRelacaoTV, relacaoTV;
    private EditText foraDaRelacao;

    private Utils utils;
    private Pastebin pastebin;
    private JSON json;
    private Arquivos arquivos;
    private CaixaDialogo caixaDialogo;

    private Boolean ultimoItem = false, voltarItem = false;

    private String ultimo, atual;
    public String nomeArquivo;

    private static final int LER_ARQUIVO = 1;
    private static final int CRIAR_JSON = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utils = new Utils();
        pastebin = new Pastebin();
        json = new JSON();
        arquivos = new Arquivos();
        caixaDialogo = new CaixaDialogo();

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        foraDaRelacaoTV = findViewById(R.id.foraDaRelacaoTV);
        foraDaRelacao = findViewById(R.id.foraDaRelacao);
        relacaoTV = findViewById(R.id.relacaoTV);
        relacao = findViewById(R.id.relacao);

        relacao.setMovementMethod(new ScrollingMovementMethod());

        nomeArquivo = utils.recuperarDaMemoria(MainActivity.this, "nome_arquivo.txt");

        if (!nomeArquivo.equals("")) {

            setTitle(nomeArquivo.toUpperCase());
        }

        String content_bens = utils.recuperarDaMemoria(MainActivity.this, "fora_da_relacao.txt");
        foraDaRelacao.setText(content_bens);

        String content_relacao = utils.recuperarDaMemoria(MainActivity.this, "relacao.txt");
        relacao.setText(content_relacao);

        foraDaRelacao.setFocusableInTouchMode(false);
        foraDaRelacao.clearFocus();
        foraDaRelacao.setCursorVisible(false);
    }

    @Override
    protected void onStart() {
        super.onStart();

        esperar();
    }

    @Override
    public void onBackPressed() {

        caixaDialogo.simples(MainActivity.this, "Sair", "Deseja sair da aplica????o?", "Sim", "N??o", i -> {

            if (i.equals("true")) {

                manterNaMemoria();

                MainActivity.this.finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.desfazer:

                if (ultimoItem) {

                    foraDaRelacao.setText(ultimo);

                    contadorLinhas();
                }

                return true;

            case R.id.refazer:

                if (voltarItem) {

                    foraDaRelacao.setText(atual);

                    contadorLinhas();
                }

                return true;

            case R.id.manual:

                caixaDialogo.inserirManualmente(MainActivity.this, i -> {

                    if (relacao.getText().toString().contains(i + " [OK]") || foraDaRelacao.getText().toString().contains(i)) {

                        Toast.makeText(getBaseContext(), i + " j?? foi escaneado", Toast.LENGTH_SHORT).show();

                    } else if (relacao.getText().toString().contains(i)) {

                        String relacao_check = relacao.getText().toString().replace(i, i + " [OK]");

                        relacao.setText(relacao_check);

                        manterNaMemoria();

                        Toast.makeText(getBaseContext(), i + " consta na rela????o", Toast.LENGTH_SHORT).show();

                    } else {

                        naoEncontrado(i);
                    }
                });

                return true;

            case R.id.escanear:

                utils.scanner(MainActivity.this);

                return true;

            case R.id.copiarForaRelacao:

                utils.copiarTexto(MainActivity.this, "LOCALIZADOS FISICAMENTE QUE N??O CONSTA NA RELA????O:\n\n" + foraDaRelacao.getText().toString());

                return true;

            case R.id.copiarRelacao:

                utils.copiarTexto(MainActivity.this, "\nRELA????O:\n\n" + relacao.getText().toString());

                return true;

            case R.id.copiarAmbos:

                utils.copiarTexto(MainActivity.this, "LOCALIZADOS FISICAMENTE QUE N??O CONSTA NA RELA????O:\n\n" + foraDaRelacao.getText() + "\nRELA????O:\n\n" + relacao.getText());

                return true;

            case R.id.gerarRelatorioCompleto:

                arquivos.relatorioCompleto(MainActivity.this, relacao, foraDaRelacao);

                return true;

            case R.id.gerarForaRelacaoCSV:

                arquivos.relatorioForaDaRelacaoCSV(MainActivity.this, foraDaRelacao);

                return true;

            case R.id.abrirArquivo:

                caixaDialogo.simples(MainActivity.this, "Abrir novo arquivo", "Abrir um novo arquivo ir?? apagar tudo da rela????o atual no App. Deseja continuar?", "Sim", "N??o", i -> {
                    if (i.equals("true")) {

                        arquivos.abrirArquivo(MainActivity.this);
                    }
                });

                return true;

            case R.id.naoLocalizados:

                utils.separarNaoAchados(MainActivity.this, relacao);

                return true;

            case R.id.procurar:

                caixaDialogo.simplesComView(MainActivity.this,
                        "Procurar",
                        "Digite uma palavra abaixo para real??ar.",
                        "Exemplo: estabilizador",
                        "Procurar",
                        "Cancelar",
                        true,
                        i -> {

                            utils.realcarTexto(relacao, i.toUpperCase(), relacaoTV);
                            utils.realcarTexto(foraDaRelacao, i.toUpperCase(), foraDaRelacaoTV);
                        });

                return true;

            case R.id.editavel:

                utils.campoEditavel(MainActivity.this, foraDaRelacao);

                return true;

            case R.id.contarLinhas:

                contadorLinhas();

                return true;

            case R.id.forcarSalvar:

                caixaDialogo.simples(MainActivity.this, "Salvar", "Salvar todas as altera????es na rela????o atual?", "Sim", "N??o", i -> {
                    if (i.equals("true")) {

                        manterNaMemoria();

                        Toast.makeText(getBaseContext(), "Salvo", Toast.LENGTH_SHORT).show();
                    }
                });

                return true;

            case R.id.enviarJson:

                arquivos.enviarArquivo(MainActivity.this,
                        getTitle().toString(), json.criarJson(MainActivity.this,
                                getTitle().toString(), foraDaRelacao.getText().toString(),
                                relacao.getText().toString()).toString(),
                        ".json");

                return true;

            case R.id.criarJson:

                json.criarESalvarJson(MainActivity.this, getTitle().toString());

                return true;

            case R.id.gerarQrCode:

                pastebin.checarQrCode(MainActivity.this, json.criarJson(MainActivity.this, getTitle().toString(), foraDaRelacao.getText().toString(), relacao.getText().toString()).toString());

                return true;

            case R.id.salvarConta:

                pastebin.salvarPastebinLogin(MainActivity.this);

                return true;

            case R.id.criarConta:

                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://pastebin.com/signup"));
                startActivity(i);

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (intentResult != null) {
            if (intentResult.getContents() == null) {
                Toast.makeText(getBaseContext(), "Cancelado", Toast.LENGTH_SHORT).show();
            } else {

                if (relacao.getText().toString().contains(intentResult.getContents() + " [OK]") || foraDaRelacao.getText().toString().contains(intentResult.getContents())) {

                    Toast.makeText(getBaseContext(), intentResult.getContents() + " j?? foi escaneado", Toast.LENGTH_SHORT).show();

                } else if (intentResult.getContents().contains("pastebin")) {

                    caixaDialogo.simples(MainActivity.this, "Carregar nova rela????o", "Carregar uma nova rela????o do pastebin?", "Sim", "Cancelar", i -> {

                        if (i.equals("true")) {

                            pastebin.pastebin(MainActivity.this, intentResult.getContents(), foraDaRelacao, relacao, foraDaRelacaoTV, relacaoTV);
                        }
                    });

                } else if (relacao.getText().toString().contains(intentResult.getContents())) {

                    String relacao_check = relacao.getText().toString().replace(intentResult.getContents(), intentResult.getContents() + " [OK]");

                    relacao.setText(relacao_check);

                    Toast.makeText(getBaseContext(), intentResult.getContents() + " consta na rela????o", Toast.LENGTH_SHORT).show();

                } else {

                    naoEncontrado(intentResult.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == LER_ARQUIVO
                && resultCode == Activity.RESULT_OK) {
            Uri uri;
            if (data != null) {
                uri = data.getData();

                DocumentFile file = DocumentFile.fromSingleUri(this, uri);
                assert file != null;
                nomeArquivo = file.getName();

                assert nomeArquivo != null;
                if (nomeArquivo.contains(".csv")) {

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

                        relacao.setText("");
                        foraDaRelacao.setText("");

                        String mLine;
                        while ((mLine = r.readLine()) != null) {
                            relacao.append(mLine.toUpperCase().replace(",", ": ").replace("  ", " ") + "\n");
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                } else if (nomeArquivo.contains(".json")) {

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
                        StringBuilder stringBuilder = new StringBuilder();

                        String mLine;
                        while ((mLine = r.readLine()) != null) {
                            stringBuilder.append(mLine);
                        }

                        JSONObject jsonObject = new JSONObject(String.valueOf(stringBuilder));

                        nomeArquivo = jsonObject.getString("nomeArquivo");

                        foraDaRelacao.setText(jsonObject.getString("foraRelacao"));

                        relacao.setText(jsonObject.getString("relacao"));

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {

                    Toast.makeText(getBaseContext(), "Arquivo inv??lido", Toast.LENGTH_SHORT).show();
                }
            }

            nomeArquivo = nomeArquivo.replace(".json", "").replace(".csv", "").toUpperCase();

            setTitle(nomeArquivo);
        }

        if (requestCode == CRIAR_JSON) {
            if (resultCode == RESULT_OK) {
                try {

                    JSONObject jsonObject = json.criarJson(MainActivity.this, getTitle().toString(), foraDaRelacao.getText().toString(), relacao.getText().toString());

                    assert data != null;
                    Uri uri = data.getData();

                    OutputStream outputStream = getContentResolver().openOutputStream(uri);

                    outputStream.write(jsonObject.toString().getBytes());

                    outputStream.close();

                    Toast.makeText(getBaseContext(), "Arquivo JSON salvo", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {

                Toast.makeText(getBaseContext(), "N??o foi poss??vel salvar o arquivo JSON", Toast.LENGTH_SHORT).show();
            }
        }

        manterNaMemoria();
    }

    private void ultimoRelacao() {

        ultimo = foraDaRelacao.getText().toString();

        contadorLinhas();
    }

    private void atualRelacao() {

        atual = foraDaRelacao.getText().toString();

        contadorLinhas();
    }

    public void contadorLinhas() {

        utils.contadorLinhas(foraDaRelacao, relacao, foraDaRelacaoTV, relacaoTV);
    }

    private void esperar() {

        Handler handler = new Handler();
        handler.postDelayed(this::contadorLinhas, 3000);
    }

    private void naoEncontrado(String patrimonio) {

        caixaDialogo.naoEncontrado(MainActivity.this, patrimonio, i -> {

            ultimoRelacao();

            foraDaRelacao.append(i);

            manterNaMemoria();

            atualRelacao();

            ultimoItem = true;

            voltarItem = true;
        });
    }

    private void manterNaMemoria() {

        utils.manterNaMemoria(MainActivity.this, getTitle().toString(), "nome_arquivo.txt");

        utils.manterNaMemoria(MainActivity.this, foraDaRelacao.getText().toString(), "fora_da_relacao.txt");

        utils.manterNaMemoria(MainActivity.this, relacao.getText().toString(), "relacao.txt");

        contadorLinhas();
    }
}