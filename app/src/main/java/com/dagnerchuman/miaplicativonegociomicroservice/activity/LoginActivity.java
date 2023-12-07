    package com.dagnerchuman.miaplicativonegociomicroservice.activity;

    import android.Manifest;
    import android.content.ContentValues;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.content.pm.PackageManager;
    import android.os.Bundle;
    import android.os.Handler;
    import android.text.InputType;
    import android.util.Log;
    import android.view.View;
    import android.widget.Button;
    import android.widget.ImageView;
    import android.widget.Toast;
    import androidx.annotation.NonNull;
    import androidx.appcompat.app.AppCompatActivity;
    import androidx.core.app.ActivityCompat;
    import androidx.core.content.ContextCompat;
    import com.dagnerchuman.miaplicativonegociomicroservice.R;
    import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiService;
    import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceDispositivo;
    import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
    import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
    import com.dagnerchuman.miaplicativonegociomicroservice.entity.Dispositivo;
    import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
    import com.dagnerchuman.miaplicativonegociomicroservice.entity.User;
    import com.google.android.material.textfield.TextInputLayout;
    import com.google.android.material.textfield.TextInputEditText;
    import com.google.firebase.messaging.FirebaseMessaging;

    import java.io.IOException;

    import retrofit2.Call;
    import retrofit2.Callback;
    import retrofit2.Response;

    public class LoginActivity extends AppCompatActivity {

        private TextInputLayout textInputLayoutEmail;
        private TextInputLayout textInputLayoutPassword;
        private TextInputEditText editTextEmail;
        private TextInputEditText editTextPassword;
        private Button buttonSignIn;

        private static final int REQUEST_INTERNET_PERMISSION = 123;
        private Dispositivo dispositivoSaved;
        private long userNegocioId;  // Declarar userNegocioId como variable global

        private String dispositivoId;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_login);

            // Inicializa las vistas
            textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
            textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
            editTextEmail = findViewById(R.id.editTextEmail);
            editTextPassword = findViewById(R.id.editTextPassword);
            buttonSignIn = findViewById(R.id.buttonSignIn);

            // Verifica si tienes permiso de acceso a Internet
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
            }

            // Configura el evento click para el botón "Iniciar Sesión"
            buttonSignIn.setOnClickListener(view -> {
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();

                if (validateInput(email, password)) {
                    performSignIn(email, password);
                }
            });

            ImageView togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
            TextInputEditText editTextPassword = findViewById(R.id.editTextPassword);

    // Configura un listener para alternar la visibilidad de la contraseña
            togglePasswordVisibility.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editTextPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                        // Cambiar a texto claro
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    } else {
                        // Cambiar a contraseña oculta
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    }
                }
            });

        }

        private boolean validateInput(String email, String password) {
            if (email.isEmpty() || password.isEmpty()) {
                showToast("Por favor, completa todos los campos");
                return false;
            }
            return true;
        }

        private void performSignIn(String email, String password) {
            ApiService apiService = ConfigApi.getInstance(this);

            User user = new User();
            user.setEmail(email);
            user.setPassword(password);

            Call<User> call = apiService.signIn(user);

            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (response.isSuccessful()) {
                        User user = response.body();
                        Long userId = user.getId();
                        String authToken = user.getToken();
                        ConfigApi.setAuthToken(authToken);

                        Log.d("LoginActivity", "Valor del dispositivoId: " + user.getDispositivoId());

                        handleSuccessfulLogin(user);

                        // Verifica si el usuario tiene un dispositivo registrado
                        if (necesitaActualizar(user)) {
                            // Actualiza el dispositivoId del usuario
                            registerDeviceAndUpdateUser(user);
                        }

                    } else {
                        handleLoginFailure();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    handleNetworkError(t);
                }
            });
        }


        private void handleSuccessfulLogin(User user) {
            if (user != null && user.getId() != -1 && user.getNegocioId() != -1) {
                // Guarda los datos del usuario en SharedPreferences
                saveUserData(user);

                // Llama al método para establecer el token en ConfigApi
                ConfigApi.setAuthToken(user.getToken());

                // Configura Retrofit y obtén una instancia de ApiServiceNegocio
                ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);

                // Realiza las solicitudes utilizando apiServiceNegocio
                // ...

                Log.d("LoginActivity", "Token recibido: " + user.getToken());



                // Muestra un mensaje y navega a la siguiente actividad
                showToastAndNavigate("Inicio de sesión exitoso", EntradaActivity.class);


            } else {
                handleLoginFailure();
            }
        }


        private void handleLoginFailure() {
            showToast("Inicio de sesión fallido");
        }

        private void handleNetworkError(Throwable t) {
            showToast("Error en la solicitud: " + t.getMessage());
        }

        private void showToast(String message) {
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
        }

        private void showToastAndNavigate(String message, Class<?> targetActivity) {
            showToast(message);
            Intent intent = new Intent(LoginActivity.this, targetActivity);
            startActivity(intent);
            finish();
        }

        private void saveUserData(User user) {
            SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // Guarda todos los atributos del usuario
            editor.putString("userToken", user.getToken());
            editor.putString("userEmail", user.getEmail());
            editor.putString("userName", user.getNombre());
            editor.putString("picture", user.getPicture());
            editor.putString("dni", user.getDni());
            editor.putString("userApellido", user.getApellido());
            editor.putString("userTelefono", user.getTelefono());
            editor.putLong("userId", user.getId());
            editor.putLong("userNegocioId", user.getNegocioId());

            // Guarda el userNegocioId en una variable adicional (userNegocioId global)
            userNegocioId = user.getNegocioId();

            // Agrega más atributos según las propiedades de la clase User
            Log.d("LoginActivity", "Picture URL a guardar: " + user.getPicture());

            editor.apply();
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            if (requestCode == REQUEST_INTERNET_PERMISSION) {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // El permiso de Internet fue concedido, pero ya habíamos solicitado el inicio de sesión previamente.
                    // No es necesario realizar otra solicitud de inicio de sesión aquí.
                } else {
                    showToast("Permiso de Internet denegado. Por favor, concede el permiso para continuar.");
                }
            }
        }


        public void onClickSignUp(View view) {
            Intent registerIntent = new Intent(this, RegisterActivity.class);
            startActivity(registerIntent);
        }

        private void registerDeviceAndUpdateUser(User user) {
            // Registra el dispositivo y obtén el nuevo dispositivoId
            registerDeviceAndUpdateUserInApi(user);
        }

        private void registerDeviceAndUpdateUserInApi(User user) {
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                if (!task.isSuccessful()) {
                    Log.w("MiApp", "Fetching FCM registration token failed", task.getException());
                    return;
                }

                // Get new FCM registration token
                String nuevoDispositivoId = task.getResult();
                Log.d("MiApp", "Token del dispositivo: " + nuevoDispositivoId);

                // Verifica si el dispositivo ya está registrado
                if (!necesitaActualizar(user)) {
                    Log.d("MiApp", "El dispositivo ya está registrado para este usuario. No es necesario actualizar.");
                    return;
                }

                // Registra el dispositivo y actualiza el dispositivoId del usuario
                registerDeviceAndUpdateUser(user, nuevoDispositivoId);
            });
        }


        // Método para verificar si el usuario necesita ser actualizado
        private boolean necesitaActualizar(User user) {
            // Verifica si el dispositivo ya está registrado
            String dispositivoId = user != null ? user.getDispositivoId() : null;

            // Asegúrate de que el dispositivoId no sea "0" (cero)
            return dispositivoId == null || dispositivoId.isEmpty() || dispositivoId.equals("0");
        }


        private void registerDeviceAndUpdateUser(User user, String nuevoDispositivoId) {
            // Registra el dispositivo y actualiza el dispositivoId del usuario
            registerDevice(nuevoDispositivoId, user.getNegocioId(), user);
        }

        private void registerDevice(String dispositivoId, long negocioId, User user) {
            ApiServiceDispositivo apiServiceDispositivo = ConfigApi.getInstanceDispositivo(this);

            // Verifica si el dispositivoId está presente en la entidad Dispositivo
            if (!dispositivoIdExistsInDispositivoEntity(dispositivoId)) {
                // El dispositivoId no existe en la entidad Dispositivo, realiza el registro
                Dispositivo dispositivo = new Dispositivo();
                dispositivo.setDeviceId(dispositivoId);
                dispositivo.setNegocioId(negocioId);

                Call<Dispositivo> call = apiServiceDispositivo.registerDevice(dispositivo);

                call.enqueue(new Callback<Dispositivo>() {
                    @Override
                    public void onResponse(@NonNull Call<Dispositivo> call, @NonNull Response<Dispositivo> response) {
                        if (response.isSuccessful()) {
                            Dispositivo dispositivoResponse = response.body();
                            // Manejar la respuesta exitosa del registro del dispositivo si es necesario
                            Log.d("LoginActivity", "Dispositivo registrado exitosamente: " + dispositivoResponse.getDeviceId());

                            // Actualiza el dispositivoId del usuario
                            actualizarDispositivoId(user, dispositivoId);
                        } else {
                            // Manejar el fallo del registro del dispositivo si es necesario
                            Log.e("LoginActivity", "Error al registrar el dispositivo. Código: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Dispositivo> call, @NonNull Throwable t) {
                        // Manejar el error de la solicitud de red aquí
                        Log.e("LoginActivity", "Error en la solicitud de registro de dispositivo: " + t.getMessage());
                    }
                });
            } else {
                // El dispositivoId ya existe en la entidad Dispositivo
                Log.d("LoginActivity", "El dispositivoId ya está registrado en la entidad Dispositivo. No es necesario actualizar.");
                // Puedes agregar más lógica aquí si es necesario
            }
        }

        // Método para verificar si el dispositivoId existe en la entidad Dispositivo
        private boolean dispositivoIdExistsInDispositivoEntity(String dispositivoId) {
            // Implementa la lógica para verificar la existencia del dispositivoId en la entidad Dispositivo
            // Devuelve true si existe, false si no existe
            // Puedes realizar una solicitud a la API, consultar una base de datos local, etc.
            // Por ahora, retorna siempre false, por lo que siempre intentará registrar el dispositivo
            return false;
        }



        private void actualizarDispositivoId(User user, String nuevoDispositivoId) {
            ApiService apiService = ConfigApi.getInstance(this);

            // Actualiza el dispositivoId del usuario
            user.setDispositivoId(nuevoDispositivoId);

            // Log del cuerpo de la solicitud
            Log.d("LoginActivity", "Cuerpo de la solicitud: " + user.toString());

            // Llama al método específico para actualizar el dispositivoId
            Call<User> callActualizarDispositivoId = apiService.updateUser(user.getId(), user);
            Log.d("LoginActivity", "Enviando solicitud a: " + callActualizarDispositivoId.request().url());

            callActualizarDispositivoId.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (response.isSuccessful()) {



                        // Manejar la respuesta exitosa si es necesario
                        Log.d("LoginActivity", "DispositivoId actualizado exitosamente");
                    } else {
                        // Manejar el fallo de la solicitud si es necesario
                        Log.e("LoginActivity", "Error al actualizar el dispositivoId. Código: " + response.code());

                        // Imprimir detalles adicionales sobre la respuesta
                        try {
                            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Error body is null";
                            Log.e("LoginActivity", "Error body: " + errorBody);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    // Manejar el error de la solicitud de red aquí
                    Log.e("LoginActivity", "Error en la solicitud de actualización de dispositivoId: " + t.getMessage());
                }
            });
        }

        public void onClickForgotPassword(View view) {
            Intent intent = new Intent(this, EnviarEmailRecover.class);
            startActivity(intent);
        }



    }
