package com.droidev.levantamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class Arquivos {

    CaixaDialogo caixaDialogo = new CaixaDialogo();
    Utils utils = new Utils();

    private static final int LER_ARQUIVO = 1;

    private void exportarDados(Activity activity, String dados, String extensao) {

        caixaDialogo.simplesComView(activity,
                "Enviar relatório",
                "Nome do arquivo:",
                "Exemplo: Deposito 2 / SAMS UL 580",
                "Enviar",
                "Cancelar",
                InputType.TYPE_CLASS_TEXT,
                false,
                false,
                i -> {

                    String[] informacao;

                    informacao = dados.split("\n");

                    StringBuilder data = new StringBuilder();

                    for (String dado : informacao) {

                        data.append(dado).append("\n");
                    }

                    enviarArquivo(activity, i, data.toString(), extensao);
                });
    }

    public void enviarArquivo(Context context, String file, String content, String extension) {

        try {

            FileOutputStream out = context.openFileOutput(file + extension, Context.MODE_PRIVATE);
            out.write((content.getBytes()));
            out.close();

            File fileLocation = new File(context.getFilesDir(), file + extension);
            Uri path = FileProvider.getUriForFile(context, "com.droidev.levantamento.fileprovider", fileLocation);
            Intent fileIntent = new Intent(Intent.ACTION_SEND);
            fileIntent.setType("text/csv|application/json");
            fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            fileIntent.putExtra(Intent.EXTRA_STREAM, path);
            context.startActivity(Intent.createChooser(fileIntent, "Enviar"));

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void abrirArquivo(Activity activity) {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("text/comma-separated-values|application/json");
            String[] mimetypes = {"text/comma-separated-values", "application/json"};
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            activity.startActivityForResult(Intent.createChooser(intent, "Abrir relação"), LER_ARQUIVO);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(activity, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    public void relatorioCompleto(Activity activity, TextView textView, EditText editText) {

        String anotacoes = utils.recuperarDaMemoria(activity, "anotacoes.txt");

        String[] separado = textView.getText().toString().split("\n");

        StringBuilder listaNaoAchados = new StringBuilder();
        int quantidadeForaRelacao;

        String[] listaForaRelacao = editText.getText().toString().split("\n");
        String[] listaRelacao = textView.getText().toString().split("\n");

        if (!editText.getText().toString().equals("")) {

            quantidadeForaRelacao = listaForaRelacao.length;
        } else {

            quantidadeForaRelacao = 0;
        }

        int j = 0;

        for (String s : separado) {
            if (!s.contains("[OK]")) {

                j = j + 1;

                listaNaoAchados.append(s).append("\n");
            }
        }

        String documento = utils.dataHora()
                + "\n\nTOTAL DE BENS NA RELAÇÃO: "
                + listaRelacao.length
                + " ITENS\nTOTAL DE BENS LOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: "
                + quantidadeForaRelacao
                + " ITENS\nTOTAL DE BENS NÃO LOCALIZADOS FISICAMENTE: "
                + j + " ITENS\nSOMA TOTAL (RELAÇÃO + FISICAMENTE LOCALIZADOS QUE NÃO CONSTA NA RELAÇÃO): "
                + (listaRelacao.length + quantidadeForaRelacao)
                + " ITENS\n\nLOCALIZADOS FISICAMENTE QUE NÃO CONSTA NA RELAÇÃO: "
                + quantidadeForaRelacao + " ITENS\n\n"
                + editText.getText().toString() + "\n"
                + "BENS NÃO LOCALIZADOS FISICAMENTE: "
                + j + " ITENS\n\n" + listaNaoAchados
                + "\n" + "RELAÇÃO: "
                + listaRelacao.length
                + " ITENS\n\n" + textView.getText().toString()
                + "\nANOTAÇÕES\n\n" + anotacoes;

        exportarDados(activity, documento, ".txt");

    }

    public void relatorioForaDaRelacaoCSV(Activity activity, EditText editText) {

        String foraDaRelacaoReplace = editText.getText().toString().replace("-", ",").replace(":", ",");

        exportarDados(activity, foraDaRelacaoReplace, ".csv");
    }
}
