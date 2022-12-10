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
    private static final int SALVAR_ARQUIVO = 3;

    private void exportarDados(Activity activity, String dados, String extensao) {

        caixaDialogo.simplesComView(activity,
                "Enviar relatório",
                "Nome do arquivo:",
                "Exemplo: SEPAT Sala 06",
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

        exportarDados(activity, utils.organizarDadosRelatorio(activity, textView, editText), ".txt");

    }

    public void relatorioForaDaRelacaoCSV(Activity activity, EditText editText) {

        String foraDaRelacaoReplace = editText.getText().toString().replace("-", ",").replace(":", ",");

        exportarDados(activity, foraDaRelacaoReplace, ".csv");
    }

    public void salvarTXT(Activity activity) {

        caixaDialogo.simplesComView(activity,
                "Enviar relatório",
                "Nome do arquivo:",
                "Exemplo: SEPAT Sala 06",
                "Salvar",
                "Cancelar",
                InputType.TYPE_CLASS_TEXT,
                false,
                false,
                i -> {

                    Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TITLE, i + ".txt");

                    activity.startActivityForResult(intent, SALVAR_ARQUIVO);

                });
    }
}
