package com.droidev.levantamento;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import org.json.JSONObject;

public class JSON {

    private static final int CRIAR_JSON = 2;

    public JSONObject criarJson(Context context, String nomeArquivo, String foraRelacao, String relacao) {

        JSONObject jsonObject = new JSONObject();

        try {

            jsonObject.put("nomeArquivo", nomeArquivo);
            jsonObject.put("foraRelacao", foraRelacao);
            jsonObject.put("relacao", relacao);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.toString(), Toast.LENGTH_SHORT).show();
        }

        return jsonObject;
    }

    public void criarESalvarJson(Activity activity, String nomeArquivo) {

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);

        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, nomeArquivo + ".json");

        activity.startActivityForResult(intent, CRIAR_JSON);
    }
}
