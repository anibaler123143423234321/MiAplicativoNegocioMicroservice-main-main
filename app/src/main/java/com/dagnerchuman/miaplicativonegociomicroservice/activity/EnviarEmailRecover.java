package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceEmailRecover;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.EmailValuesDto;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnviarEmailRecover extends AppCompatActivity {

    private EditText editTextEmail;
    private ImageButton btnBackToLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enviaremail_recuperarcontrasenia);

        // ... (inicializa tu toolbar aquí)

        editTextEmail = findViewById(R.id.editTextEmail);
        Button buttonEnviarCorreo = findViewById(R.id.buttonEnviarCorreo);

        buttonEnviarCorreo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enviarCorreo();
            }
        });

        // Obtén una referencia al botón "Regresar a Login"
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Configura el evento click para el botón "Regresar a Login"
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MiApp", "Clic en el botón 'Regresar a Login'");

                // Resto del código para iniciar LoginActivity y cerrar la actividad actual
                Intent loginIntent = new Intent(EnviarEmailRecover.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });

    }

    private void enviarCorreo() {
        String email = editTextEmail.getText().toString().trim();

        if (!email.isEmpty()) {
            EmailValuesDto emailValuesDto = new EmailValuesDto(email);
            ApiServiceEmailRecover apiServiceEmailRecover = ConfigApi.getInstanceEmailRecover(this);

            Call<Void> call = apiServiceEmailRecover.sendEmail(emailValuesDto);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        // Mostrar Sweet Alert de éxito
                        new SweetAlertDialog(EnviarEmailRecover.this, SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Éxito")
                                .setContentText("Correo enviado con éxito")
                                .setConfirmClickListener(sweetAlertDialog -> {
                                    sweetAlertDialog.dismissWithAnimation();
                                    // Navegar a LoginActivity
                                    navigateToLoginActivity();
                                })
                                .show();
                    } else {
                        // Mostrar Sweet Alert de error
                        new SweetAlertDialog(EnviarEmailRecover.this, SweetAlertDialog.ERROR_TYPE)
                                .setTitleText("Error")
                                .setContentText("Error al enviar el correo electrónico")
                                .show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    // Mostrar Sweet Alert de error en la llamada
                    new SweetAlertDialog(EnviarEmailRecover.this, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Error")
                            .setContentText("Error en la llamada")
                            .show();
                }
            });
        } else {
            // Mostrar Sweet Alert de advertencia si el campo de correo electrónico está vacío
            new SweetAlertDialog(EnviarEmailRecover.this, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Advertencia")
                    .setContentText("Por favor, ingrese una dirección de correo electrónico válida.")
                    .show();
        }
    }

    private void navigateToLoginActivity() {
        Intent loginIntent = new Intent(EnviarEmailRecover.this, LoginActivity.class);
        startActivity(loginIntent);
        finish();  // Opcional: Finalizar la actividad actual si no quieres volver atrás desde LoginActivity
    }

}
