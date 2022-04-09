package com.droidev.levantamento;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.documentfile.provider.DocumentFile;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private TextView relacao, foraDaRelacaoTV, relacaoTV;
    private EditText foraDaRelacao;

    private Utilities utilities;

    private Boolean ultimoItem = false, voltarItem = false;

    private String ultimo, atual, nomeArquivo;

    private static final int LER_ARQUIVO = 1;
    private static final int CRIAR_JSON = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utilities = new Utilities();

        AppCompatDelegate.setDefaultNightMode(
                AppCompatDelegate.MODE_NIGHT_YES);

        foraDaRelacaoTV = findViewById(R.id.foraDaRelacaoTV);
        foraDaRelacao = findViewById(R.id.foraDaRelacao);
        relacaoTV = findViewById(R.id.relacaoTV);
        relacao = findViewById(R.id.relacao);

        relacao.setMovementMethod(new ScrollingMovementMethod());

        nomeArquivo = utilities.recuperarDaMemoria(MainActivity.this, "nome_arquivo.txt");
        if (nomeArquivo.equals("")) {

            setTitle("Levantamento");
        } else {

            setTitle(nomeArquivo);
        }

        String content_bens = utilities.recuperarDaMemoria(MainActivity.this, "fora_da_relacao.txt");
        foraDaRelacao.setText(content_bens);

        String content_relacao = utilities.recuperarDaMemoria(MainActivity.this, "relacao.txt");
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

        AlertDialog dialogo = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Sair")
                .setMessage("Deseja sair da aplicação?")
                .setPositiveButton("Sim", null)
                .setNegativeButton("Não", null)
                .show();

        Button positiveButton = dialogo.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                dialogo.dismiss();

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

                utilities.dialogoSimplesComView(MainActivity.this, "Modo manual", "Insira o número patrimonial abaixo:", "Exemplo: 012345", "Ok", "Cancelar", InputType.TYPE_CLASS_NUMBER, false, new Utilities.onButtonPressed() {
                    @Override
                    public void buttonPressed(String i) {

                        if (relacao.getText().toString().contains(i + " [OK]") || foraDaRelacao.getText().toString().contains(i)) {

                            Toast.makeText(getBaseContext(), i + " já foi escaneado", Toast.LENGTH_SHORT).show();

                        } else if (relacao.getText().toString().contains(i)) {

                            String relacao_check = relacao.getText().toString().replace(i, i + " [OK]");

                            relacao.setText(relacao_check);

                            manterNaMemoria();

                            Toast.makeText(getBaseContext(), i + " consta na relação", Toast.LENGTH_SHORT).show();

                        } else {

                            naoEncontrado(i);
                        }
                    }
                });

                return true;

            case R.id.escanear:

                utilities.scanner(MainActivity.this);

                return true;

            case R.id.copiarForaRelacao:

                utilities.copiarTexto(MainActivity.this, "LOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO:\n\n" + foraDaRelacao.getText().toString());

                return true;

            case R.id.copiarRelacao:

                utilities.copiarTexto(MainActivity.this, "\nRELAÇÃO:\n\n" + relacao.getText().toString());

                return true;

            case R.id.copiarAmbos:

                utilities.copiarTexto(MainActivity.this, "LOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO:\n\n" + foraDaRelacao.getText() + "\nRELAÇÃO:\n\n" + relacao.getText());

                return true;

            case R.id.gerarRelatorioCompleto:

                utilities.relatorioCompleto(MainActivity.this, relacao, foraDaRelacao);

                return true;

            case R.id.gerarForaRelacaoCSV:

                utilities.relatorioForaDaRelacaoCSV(MainActivity.this, foraDaRelacao);

                return true;

            case R.id.abrirArquivo:

                utilities.dialogoSimples(MainActivity.this, "Abrir novo arquivo", "Abrir um novo arquivo irá apagar tudo da relação atual no App. Deseja continuar?", "Sim", "Não", new Utilities.onButtonPressed() {
                    @Override
                    public void buttonPressed(String i) {
                        if (i.equals("true")) {

                            utilities.abrirArquivo(MainActivity.this);
                        }
                    }
                });

                return true;

            case R.id.naoLocalizados:

                utilities.separarNaoAchados(MainActivity.this, relacao);

                return true;

            case R.id.procurar:

                utilities.dialogoSimplesComView(MainActivity.this, "Procurar", "Digite uma palavra abaixo para realçar.", "Exemplo: estabilizador", "Procurar", "Cancelar", InputType.TYPE_CLASS_TEXT, true, new Utilities.onButtonPressed() {
                    @Override
                    public void buttonPressed(String i) {

                        utilities.realcarTexto(relacao, i.toUpperCase(), relacaoTV);
                        utilities.realcarTexto(foraDaRelacao, i.toUpperCase(), foraDaRelacaoTV);
                    }
                });

                return true;

            case R.id.editavel:

                utilities.campoEditavel(MainActivity.this, foraDaRelacao);

                return true;

            case R.id.contarLinhas:

                contadorLinhas();

                return true;

            case R.id.forcarSalvar:

                utilities.dialogoSimples(MainActivity.this, "Salvar", "Salvar todas as alterações na relação atual?", "Sim", "Não", new Utilities.onButtonPressed() {
                    @Override
                    public void buttonPressed(String i) {
                        if (i.equals("true")) {

                            manterNaMemoria();

                            Toast.makeText(getBaseContext(), "Salvo", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                return true;

            case R.id.criarJson:

                utilities.criarESalvarJson(MainActivity.this, nomeArquivo);

                return true;

            case R.id.teste:

                utilities.connectPastebin();

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

                    Toast.makeText(getBaseContext(), intentResult.getContents() + " já foi escaneado", Toast.LENGTH_SHORT).show();


                } else if (intentResult.getContents().contains("pastebin")) {

                    utilities.dialogoSimples(MainActivity.this, "Carregar nova relação", "Carregar uma nova relação do pastebin?", "Sim", "Cancelar", new Utilities.onButtonPressed() {
                        @Override
                        public void buttonPressed(String i) {

                            if (i.equals("true")) {

                                pastebin(intentResult.getContents());
                            }
                        }
                    });

                } else if (relacao.getText().toString().contains(intentResult.getContents())) {

                    String relacao_check = relacao.getText().toString().replace(intentResult.getContents(), intentResult.getContents() + " [OK]");

                    relacao.setText(relacao_check);

                    Toast.makeText(getBaseContext(), intentResult.getContents() + " consta na relação", Toast.LENGTH_SHORT).show();

                } else {

                    naoEncontrado(intentResult.getContents());
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }

        if (requestCode == LER_ARQUIVO
                && resultCode == Activity.RESULT_OK) {
            Uri uri = null;
            if (data != null) {
                uri = data.getData();

                DocumentFile file = DocumentFile.fromSingleUri(this, uri);
                nomeArquivo = file.getName();

                if (nomeArquivo.contains(".csv")) {

                    nomeArquivo = nomeArquivo.replace(".csv", "").replace(" ", "_");

                    setTitle(nomeArquivo);

                    try {
                        InputStream inputStream = getContentResolver().openInputStream(uri);
                        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));

                        relacao.setText("");
                        foraDaRelacao.setText("");

                        String mLine;
                        while ((mLine = r.readLine()) != null) {
                            relacao.append(mLine.toUpperCase().replace(",", ": ").replace("  ", " ") + "\n");
                        }
                    } catch (FileNotFoundException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }

                } else if (nomeArquivo.contains(".json")) {

                    nomeArquivo = nomeArquivo.replace(".json", "").replace(" ", "_");

                    setTitle(nomeArquivo);

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

                    } catch (FileNotFoundException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        if (requestCode == CRIAR_JSON) {
            if (resultCode == RESULT_OK) {
                try {

                    JSONObject jsonObject = utilities.criarJson(MainActivity.this, nomeArquivo, foraDaRelacao.getText().toString(), relacao.getText().toString());

                    Uri uri = data.getData();

                    OutputStream outputStream = getContentResolver().openOutputStream(uri);

                    outputStream.write(jsonObject.toString().getBytes());

                    outputStream.close();

                    Toast.makeText(getBaseContext(), "Arquivo JSON salvo", Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "Não foi possível salvar o arquivo JSON", Toast.LENGTH_SHORT).show();
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

    private void pastebin(String url) {

        utilities.dialogoSimplesComView(MainActivity.this, "Nome da relação", "Insira o nome da relação abaixo:", "Exemplo: SEPAT", "Ok", "Cancelar", InputType.TYPE_CLASS_TEXT, false, new Utilities.onButtonPressed() {
            @Override
            public void buttonPressed(String i) {

                nomeArquivo = i.replace(" ", "_").toUpperCase();

                relacao.setText("Buscando no pastebin...");

                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        final StringBuilder sb = new StringBuilder();

                        try {

                            Document doc = Jsoup.connect(url).get();

                            String text = doc.select("textarea[class=textarea]").text().replace(",", ": ") + "\n";

                            sb.append(text);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    relacao.setText(sb);

                                    setTitle(nomeArquivo);

                                    contadorLinhas();

                                    manterNaMemoria();
                                }
                            });

                        } catch (Exception e) {
                            e.printStackTrace();

                            relacao.setText(e.toString());
                        }
                    }
                }).start();
            }
        });
    }

    private void contadorLinhas() {

        int contadorForaDaRelacao, contadorRelacao;

        contadorForaDaRelacao = foraDaRelacao.getLineCount() - 1;
        contadorRelacao = relacao.getLineCount() - 1;

        foraDaRelacaoTV.setText("LOC. FIS. FORA DA RELAÇÃO: " + contadorForaDaRelacao + " ITENS");
        relacaoTV.setText("RELAÇÃO: " + contadorRelacao + " ITENS");
    }

    private void esperar() {

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                contadorLinhas();
            }
        }, 3000);
    }

    private void naoEncontrado(String patrimonio) {

        utilities.naoEncontrado(MainActivity.this, patrimonio, new Utilities.onButtonPressed() {
            @Override
            public void buttonPressed(String i) {

                ultimoRelacao();

                foraDaRelacao.append(i);

                manterNaMemoria();

                atualRelacao();

                ultimoItem = true;

                voltarItem = true;
            }
        });
    }

    private void manterNaMemoria() {

        utilities.manterNaMemoria(MainActivity.this, nomeArquivo, "nome_arquivo.txt");

        utilities.manterNaMemoria(MainActivity.this, foraDaRelacao.getText().toString(), "fora_da_relacao.txt");

        utilities.manterNaMemoria(MainActivity.this, relacao.getText().toString(), "relacao.txt");

        contadorLinhas();
    }
}