package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiService;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceDispositivo;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceDni;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Dispositivo;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.DniResponse;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.User;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import org.json.JSONObject;

import cn.pedant.SweetAlert.SweetAlertDialog;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public class RegisterActivity extends AppCompatActivity {

    // Declaraciones de las vistas y elementos
    private TextInputLayout textInputLayoutNombre;
    private TextInputEditText editTextNombre;
    private TextInputLayout textInputLayoutApellido;
    private TextInputEditText editTextApellido;
    private TextInputLayout textInputLayoutTelefono;
    private TextInputLayout textInputLayoutDNI;
    private TextInputEditText editTextTelefono;
    private TextInputEditText editTextDNI;
    private TextInputLayout textInputLayoutEmail;
    private TextInputEditText editTextEmail;
    private TextInputLayout textInputLayoutUsername;
    private TextInputEditText editTextUsername;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText editTextPassword;
    private Spinner spinnerNegocios;
    private Spinner spinnerTipoDocumento;
    private Spinner spinnerDepartamento;
    private Spinner spinnerProvincia;
    private Spinner spinnerDistrito;

    private Button buttonSignUp;
    private List<Negocio> listaNegocios;
    private Long selectedNegocioId = null;
    private ImageButton btnBackToLogin;
    private Button buttonBuscarDNI; // Agrega este botón en tu XML y configúralo
    private static final int GALLERY_REQUEST_CODE = 1;

    private static final int REQUEST_INTERNET_PERMISSION = 123;
    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView imageViewSelected;

    // Declaración de variables globales
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri selectedImageUri; // Variable para almacenar la URI de la imagen seleccionada
    private boolean isRegistering = false;
    private Dispositivo dispositivoSaved;

    private String dispositivoId;



    // Define una interfaz para la API de consulta de DNI
    public interface ApiServiceDni {
        @GET("dni/{dni}")
        Call<DniResponse> getDniData(
                @Path("dni") String dni,
                @Query("token") String token
        );
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Inicializa las vistas y otros elementos
        spinnerNegocios = findViewById(R.id.spinnerNegocios);
        textInputLayoutNombre = findViewById(R.id.textInputLayoutNombre);
        textInputLayoutApellido = findViewById(R.id.textInputLayoutApellido);
        textInputLayoutTelefono = findViewById(R.id.textInputLayoutTelefono);
        textInputLayoutDNI = findViewById(R.id.textInputLayoutDNI);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutUsername = findViewById(R.id.textInputLayoutUsername);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);


        editTextNombre = findViewById(R.id.editTextNombre);
        editTextApellido = findViewById(R.id.editTextApellido);
        editTextTelefono = findViewById(R.id.editTextTelefono);
        editTextDNI = findViewById(R.id.editTextDNI);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonSignUp = findViewById(R.id.buttonSignUp);
        spinnerTipoDocumento = findViewById(R.id.spinnerTipoDocumento);
        spinnerDepartamento = findViewById(R.id.spinnerDepartamento);
        spinnerProvincia = findViewById(R.id.spinnerProvincia);
        spinnerDistrito = findViewById(R.id.spinnerDistrito);
        imageViewSelected = findViewById(R.id.imageViewSelected);



        // Configura el evento click para el botón "Seleccionar Imagen"
        Button buttonSelectImage = findViewById(R.id.buttonSelectImage);
        buttonSelectImage.setOnClickListener(view -> {
            // Verifica si tienes permiso de acceso a la galería
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_INTERNET_PERMISSION);
            } else {
                openImagePicker();
            }
        });

        // Inicializa Firebase Storage en onCreate o donde sea apropiado
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Crea adaptadores personalizados para los Spinners
        ArrayAdapter<CharSequence> tipoDocumentoAdapter = ArrayAdapter.createFromResource(this, R.array.document_types, R.layout.custom_spinner_item);
        tipoDocumentoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDocumento.setAdapter(tipoDocumentoAdapter);

        ArrayAdapter<CharSequence> departamentoAdapter = ArrayAdapter.createFromResource(this, R.array.departamentos, R.layout.custom_spinner_item);
        departamentoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDepartamento.setAdapter(departamentoAdapter);

        ArrayAdapter<CharSequence> provinciaAdapter = ArrayAdapter.createFromResource(this, R.array.provincias, R.layout.custom_spinner_item);
        provinciaAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvincia.setAdapter(provinciaAdapter);

        ArrayAdapter<CharSequence> distritoAdapter = ArrayAdapter.createFromResource(this, R.array.distritos, R.layout.custom_spinner_item);
        distritoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDistrito.setAdapter(distritoAdapter);


        // Verifica si tienes permiso de acceso a Internet
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, REQUEST_INTERNET_PERMISSION);
        }
        // Configura el evento click para el botón "Registrarse"
// Configura el evento click para el botón "Registrarse"
        buttonSignUp.setOnClickListener(view -> {
            if (!isRegistering) {
                // Mostrar el diálogo de espera
                SweetAlertDialog progressDialog = new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.PROGRESS_TYPE);
                progressDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
                progressDialog.setTitleText("Registrando...");
                progressDialog.setCancelable(false); // Evita que el usuario cierre el diálogo
                progressDialog.show();

                // Obtiene los valores de los campos de entrada
                String nombre = editTextNombre.getText().toString();
                String apellido = editTextApellido.getText().toString();
                String telefono = editTextTelefono.getText().toString();
                String email = editTextEmail.getText().toString();
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String dni = editTextDNI.getText().toString();
                String tipoDoc = spinnerTipoDocumento.getSelectedItem().toString();
                String departamento = spinnerDepartamento.getSelectedItem().toString();
                String provincia = spinnerProvincia.getSelectedItem().toString();
                String distrito = spinnerDistrito.getSelectedItem().toString();


                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String tokenSaved = preferences.getString("DEVICE_ID", "");


                // Obtén la imagen en forma de URL (usando el contenido de selectedImageUri)
                String picture = (selectedImageUri != null) ? selectedImageUri.toString() : ""; // Reemplaza "" con el valor predeterminado adecuado

                // Realiza la solicitud de registro
                performSignUp(nombre, apellido, telefono, dni, email, username, password, picture, tipoDoc, departamento, provincia, distrito, tokenSaved);
                registerDevice();
                // Cierra el diálogo de espera después de un tiempo específico (por ejemplo, 2 segundos)
                new Handler().postDelayed(() -> progressDialog.dismissWithAnimation(), 1500);
            }
        });


        // Configura el evento click para el botón "Buscar DNI"
        buttonBuscarDNI = findViewById(R.id.buttonBuscarDNI); // Reemplaza con el ID correcto de tu botón

        buttonBuscarDNI.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dni = editTextDNI.getText().toString();
                String token = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJlbWFpbCI6Imtpa2UuamVwZkBnbWFpbC5jb20ifQ.NRMVtJiFKRXYBXuJPLwNsKeRK5hPWIALRxbqNSWloXU";

                // Registra información de depuración para ver los datos antes de la solicitud
                Log.d("MiApp", "Solicitando datos para el DNI: " + dni);

                // Crear una instancia de Retrofit para la API de consulta de DNI
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("https://dniruc.apisperu.com/api/v1/")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();

                // Crear una instancia de la interfaz de la API
                ApiServiceDni apiServiceDni = retrofit.create(ApiServiceDni.class);

                // Realizar la solicitud para obtener los datos del DNI
                Call<DniResponse> call = apiServiceDni.getDniData(dni, token);

                call.enqueue(new Callback<DniResponse>() {
                    @Override
                    public void onResponse(Call<DniResponse> call, Response<DniResponse> response) {
                        if (response.isSuccessful()) {
                            DniResponse dniResponse = response.body();
                            if (dniResponse != null && dniResponse.isSuccess()) {
                                String nombreCompleto = dniResponse.getNombres();
                                String apellidosCompletos = dniResponse.getApellidoPaterno() + " " + dniResponse.getApellidoMaterno();
                                // Completa los campos de nombre y apellido
                                editTextNombre.setText(nombreCompleto);
                                editTextApellido.setText(apellidosCompletos);

                                // Deshabilita los campos de nombre y apellido

                                Log.d("MiApp", "Datos del DNI exitosos. Nombre: " + nombreCompleto + " Apellidos: " + apellidosCompletos);

                            } else {
                                // Manejar el caso en que la API no devuelva datos válidos
                                new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                                        .setTitleText("Error")
                                        .setContentText("No se encontraron datos para el DNI proporcionado. Completa los campos manualmente.")
                                        .show();
                                editTextNombre.setEnabled(true);
                                editTextApellido.setEnabled(true);

                                Log.d("MiApp", "Datos del DNI no válidos.");


                            }
                        } else {
                            // Manejar el caso en que la solicitud a la API no sea exitosa
                            new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.ERROR_TYPE)
                                    .setTitleText("Error")
                                    .setContentText("Error al obtener datos del DNI. Brinda un DNI activo.")
                                    .show();
                            Log.d("MiApp", "URL de solicitud: " + call.request().url());
                        }
                    }

                    @Override
                    public void onFailure(Call<DniResponse> call, Throwable t) {
                        // Manejar el error de la solicitud de red aquí
                        Log.e("MiApp", "Error en la solicitud: " + t.getMessage());
                        Toast.makeText(RegisterActivity.this, "Error en la solicitud: " + t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });




        // Configura el evento de selección del Spinner de negocios
        spinnerNegocios.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtén el negocio seleccionado y su ID
                selectedNegocioId = listaNegocios.get(position).getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Maneja el caso en que no se haya seleccionado nada en el Spinner
                selectedNegocioId = null;
            }
        });

        // Configura el evento de selección del Spinner de tipo de documento
        spinnerTipoDocumento.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Obtén el tipo de documento seleccionado
                String selectedDocumentType = spinnerTipoDocumento.getSelectedItem().toString();
                // Puedes hacer algo con la selección, como guardarla en una variable.
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Maneja el caso en que no se haya seleccionado nada
            }
        });

        // Obtén la lista de negocios y configura el Spinner
        getNegociosAndSetupSpinner();

        // Obtén una referencia al botón "Regresar a Login"
        btnBackToLogin = findViewById(R.id.btnBackToLogin);

        // Configura el evento click para el botón "Regresar a Login"
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("MiApp", "Clic en el botón 'Regresar a Login'");

                // Resto del código para iniciar LoginActivity y cerrar la actividad actual
                Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }


    // Método para realizar la solicitud de registro
    private void performSignUp(String nombre, String apellido, String telefono, String dni, String email, String username, String password, String picture, String tipoDoc, String departamento, String provincia, String distrito, String dispositivoId) {
        // Verifica que se haya seleccionado un negocio
        if (selectedNegocioId == null) {
            Toast.makeText(this, "Selecciona un negocio válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica que se haya seleccionado una imagen
        if (selectedImageUri == null) {
            Toast.makeText(this, "Selecciona una imagen primero", Toast.LENGTH_SHORT).show();
            return;
        }

        // Verifica si ya se está registrando
        if (isRegistering) {
            return;
        }

        // Marca que se está registrando
        isRegistering = true;

        // Genera un nombre único para la imagen en Firebase Storage
        String imageName = UUID.randomUUID().toString();

        // Obtén una referencia al lugar donde se guardará la imagen en Firebase Storage
        StorageReference imageRef = storageReference.child("images/" + imageName);

        // Sube la imagen a Firebase Storage
        imageRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // La imagen se ha subido exitosamente, obtén la URL de descarga
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();

                        // Aquí puedes guardar imageUrl en tu base de datos o hacer lo que necesites con la URL de la imagen.
                        // Ahora puedes configurar la solicitud de registro con la URL de la imagen
                        // Obtén la instancia de ApiService de ConfigApi
                        ApiService apiService = ConfigApi.getInstance(this);

                        // Crea un objeto Usuario para la solicitud
                        User user = new User();
                        user.setNombre(nombre);
                        user.setApellido(apellido);
                        user.setTelefono(telefono);
                        user.setDni(dni);
                        user.setEmail(email);
                        user.setUsername(username);
                        user.setPassword(password);
                        user.setPicture(imageUrl); // Establece la URL de la imagen
                        user.setNegocioId(selectedNegocioId);
                        user.setTipoDoc(tipoDoc);
                        user.setDepartamento(departamento);
                        user.setProvincia(provincia);
                        user.setDistrito(distrito);
                        user.setDispositivoId(dispositivoId);
                        // Realiza la solicitud de registro
                        Call<User> call = apiService.signUp(user);

                        Log.d("MiApp", "Antes de la solicitud de registro");

                        call.enqueue(new Callback<User>() {
                            @Override
                            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                                isRegistering = false; // Restablece el estado
                                if (response.isSuccessful()) {

                                    // El registro fue exitoso
                                    Log.d("MiApp", "Registro exitoso");
                                    User user = response.body();

                                    if (user != null) {
                                        // Aquí puedes realizar las acciones necesarias después del registro exitoso
                                        new SweetAlertDialog(RegisterActivity.this, SweetAlertDialog.SUCCESS_TYPE)
                                                .setTitleText("Registro Exitoso")
                                                .setContentText("¡Tu registro se ha completado exitosamente!")
                                                .setConfirmClickListener(sDialog -> {
                                                    // Redirige al usuario a la actividad de inicio de sesión
                                                    Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
                                                    startActivity(loginIntent);
                                                    finish(); // Cierra esta actividad para que el usuario no pueda volver atrás
                                                    sDialog.dismissWithAnimation();
                                                })
                                                .show();
                                    } else {
                                        // Maneja el caso en que el usuario sea nulo
                                    }
                                } else {
                                    // El registro falló
                                    Log.d("MiApp", "Registro fallido");
                                    Toast.makeText(RegisterActivity.this, "Registro fallido", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                                isRegistering = false; // Restablece el estado
                                // Maneja el error de la solicitud de red aquí
                                Log.e("MiApp", "Error en la solicitud: " + t.getMessage());
                                Toast.makeText(RegisterActivity.this, "Error en la solicitud: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        Log.d("MiApp", "Después de la solicitud de registro");
                    });
                })
                .addOnFailureListener(e -> {
                    // Ocurrió un error al subir la imagen
                    Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                });
    }

    // Método para obtener la lista de negocios y configurar el Spinner
    private void getNegociosAndSetupSpinner() {
        ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);

        Call<List<Negocio>> call = apiServiceNegocio.getAllNegocios();

        call.enqueue(new Callback<List<Negocio>>() {
            @Override
            public void onResponse(Call<List<Negocio>> call, Response<List<Negocio>> response) {
                if (response.isSuccessful()) {
                    listaNegocios = response.body();

                    // Filtra el negocio con id=1
                    for (Negocio negocio : listaNegocios) {
                        if (negocio.getId() == 1) {
                            listaNegocios.remove(negocio);
                            break; // Termina el bucle después de encontrar el negocio con id=1
                        }
                    }

                    // Configura el adaptador para el Spinner
                    ArrayAdapter<Negocio> adapter = new ArrayAdapter<>(RegisterActivity.this, android.R.layout.simple_spinner_item, listaNegocios);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinnerNegocios.setAdapter(adapter);
                } else {
                    // Maneja el caso en que no se pudo obtener la lista de negocios
                    Toast.makeText(RegisterActivity.this, "Error al obtener la lista de negocios", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Negocio>> call, Throwable t) {
                // Maneja el error de la solicitud de red aquí
                Log.e("MiApp", "Error en la solicitud: " + t.getMessage());
                Toast.makeText(RegisterActivity.this, "Error en la solicitud: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Implementación de openImagePicker
    private void openImagePicker() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    // En onActivityResult, maneja la selección de la imagen
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                selectedImageUri = data.getData();
                imageViewSelected.setImageURI(selectedImageUri); // Muestra la imagen seleccionada en tu ImageView
            }
        }
    }
    private void registerDevice() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w(ContentValues.TAG, "Fetching FCM registration token failed", task.getException());
                        return;
                    }
                    // Get new FCM registration token
                    String token = task.getResult();

                    // El token de registro puede cambiar en las siguientes situaciones:
                    // La app se restablece en un dispositivo nuevo.
                    // El usuario desinstala y vuelve a instalar la app.
                    // El usuario borra los datos de la app.
                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                    String tokenSaved = preferences.getString("DEVICE_ID", "");

                    // Si el código recibido es distinto al último que tenía, lo envío al servidor
                    if (token != null && !token.equals(tokenSaved)) {
                        // Registra el token en el servidor
                        Dispositivo dispositivo = new Dispositivo();
                        dispositivo.setDeviceId(token);
                        dispositivo.setNegocioId(selectedNegocioId);
                        // Asigna el token al atributo dispositivoId
                        dispositivoId = token;


                        // Agrega un registro para mostrar los datos que se están enviando al servidor
                        Log.d("MiApp", "Enviando solicitud de registro del dispositivo al servidor. Datos: " + dispositivo.toString());

                        // Realiza la solicitud al servidor
                        ApiServiceDispositivo apiServiceDispositivo = ConfigApi.getInstanceDispositivo(this);
                        Call<Dispositivo> call = apiServiceDispositivo.registerDevice(dispositivo);

                        call.enqueue(new Callback<Dispositivo>() {
                            @Override
                            public void onResponse(@NonNull Call<Dispositivo> call, @NonNull Response<Dispositivo> response) {
                                if (response.isSuccessful()) {
                                    // Registro exitoso, guarda el token
                                    dispositivoSaved = response.body();
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putString("DEVICE_ID", dispositivoSaved.getDeviceId());
                                    editor.apply();
                                    // Agrega un registro para indicar que el registro del dispositivo fue exitoso
                                    Log.d("MiApp", "Registro del dispositivo exitoso. Token: " + dispositivoSaved.getDeviceId());
                                } else {
                                    // Agrega un registro para indicar que la solicitud al servidor no fue exitosa
                                    Log.d("MiApp", "Error en la solicitud de registro del dispositivo al servidor. Código: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<Dispositivo> call, @NonNull Throwable t) {
                                // Manejar el error de la solicitud de red aquí
                                Log.e("MiApp", "Error en la solicitud: " + t.getMessage());
                                Toast.makeText(RegisterActivity.this, "Error en la solicitud: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        // Agrega un registro para indicar que no se envió la solicitud al servidor porque el token no cambió
                        Log.d("MiApp", "No se envió la solicitud de registro del dispositivo al servidor porque el token no cambió.");
                    }
                    // Agrega un registro para mostrar el token
                    Log.d("MiApp", "Token del dispositivo: " + token);
         });
}
}
