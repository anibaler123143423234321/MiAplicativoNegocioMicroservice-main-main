package com.dagnerchuman.miaplicativonegociomicroservice.activity.ActivityMiPerfil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.EntradaActivity;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.LoginActivity;
import com.google.android.material.button.MaterialButton;

public class InvitarAmigos extends AppCompatActivity {
    private TextView toolbarTitle;
    private ImageButton btnBack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.invitaramigos);

        // Configurar el Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        btnBack = findViewById(R.id.btnBack);

        // Configurar el botón de regreso
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // Configurar el botón para invitar amigos
        MaterialButton buttonInvitar = findViewById(R.id.buttonInvitar);

        buttonInvitar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                invitarAmigos();
            }
        });


        // Configuración del botón de retroceso
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(InvitarAmigos.this, EntradaActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

    }

    private void invitarAmigos() {
        // Crear un mensaje para compartir
        String mensaje = "¡Descarga Altoque. 'Te la recomiendo!\n" +
                "https://play.google.com/store/apps/details?id=com.dagnerchuman.miaplicativonegociomicroservice&pcampaignid=web_share";

        // Crear un intent para compartir
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, mensaje);

        // Mostrar el selector de aplicaciones para compartir
        startActivity(Intent.createChooser(intent, "Invitar amigos con..."));
    }
}
