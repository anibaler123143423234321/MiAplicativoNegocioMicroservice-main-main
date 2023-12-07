package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import androidx.appcompat.widget.SearchView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments.ComprasListFragment;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments.EntradaFragment;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments.UsuarioFragment;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.ProductoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntradaActivity extends AppCompatActivity   {


    private BottomNavigationView bottomNavigationView;
    private final Fragment entradaFragment = new EntradaFragment();

    private final Fragment usuarioFragment = new UsuarioFragment();
    private final Fragment comprasListFragment = new ComprasListFragment();
    private FrameLayout fragmentContainer;

    private ImageButton btnBackToLogin, btnCarrito;
    private RecyclerView recyclerView;
    private SearchView searchView;
    private ProductoAdapter adapter;
    private List<Producto> productosList;
    private List<Producto> productList = new ArrayList();
    private ConstraintLayout constraintLayout;
    private AlertDialog popupDialog;
    private int categoriaCount = 0;
    private String nombreNegocio;
    private View customTitle;
    private TextView toolbarTitle;
    private LinearLayout categoryButtonContainer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrada);
        // Cargar el fragmento de entrada por defecto al iniciar la actividad
        loadFragment(entradaFragment);
        initViews();
        obtenerNombreNegocio();

        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        String userName = sharedPreferences.getString("userName", "");
        updateWelcomeMessage(userName);

    }

    private void initViews() {
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        constraintLayout = findViewById(R.id.constraintLayoutE);
        customTitle = getLayoutInflater().inflate(R.layout.custom_toolbar_title, null);
        toolbarTitle = customTitle.findViewById(R.id.toolbar_title);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.page_1) {
                // Recarga la actividad actual
                loadFragment(entradaFragment);
                return true;
            } else if (item.getItemId() == R.id.page_2) {
                loadFragment(usuarioFragment);
                hideWelcomeMessage(); // Oculta el mensaje de bienvenida al seleccionar page_2
                return true;
            } else if (item.getItemId() == R.id.page_3) {
                loadFragment(comprasListFragment);
                hideWelcomeMessage(); // Oculta el mensaje de bienvenida al seleccionar page_3
                return true;
            }
            return false;
        });


        // Configuración del botón de retroceso
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent loginIntent = new Intent(EntradaActivity.this, LoginActivity.class);
                startActivity(loginIntent);
                finish();
            }
        });
    }


    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragmentContainer, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



    private void updateWelcomeMessage(String userName) {
        TextView welcomeText = findViewById(R.id.userText);
        if (!userName.isEmpty()) {
            String welcomeMessage = "¡Hola, " + userName + "!";
            welcomeText.setText(welcomeMessage);
            welcomeText.setVisibility(View.VISIBLE);
        }
    }

    private void hideWelcomeMessage() {
        TextView welcomeText = findViewById(R.id.userText);
        welcomeText.setVisibility(View.GONE);
    }


    private void obtenerNombreNegocio() {
        // Configura el servicio API y obtiene el ID del negocio del SharedPreferences
        ApiServiceNegocio apiServiceNegocio = ConfigApi.getInstanceNegocio(this);
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        // Crea la llamada para obtener los datos del negocio
        Call<Negocio> call = apiServiceNegocio.getNegocioById(userNegocioId);

        // Obtiene el contexto de la actividad
        final Context context = this;

        // Realiza la llamada asíncrona a la API
        call.enqueue(new Callback<Negocio>() {
            @Override
            public void onResponse(Call<Negocio> call, Response<Negocio> response) {
                if (response.isSuccessful()) {
                    Negocio negocio = response.body();
                    if (negocio != null) {
                        nombreNegocio = negocio.getNombre();

                        // Configura el Toolbar con el nombre del negocio
                        Toolbar toolbar = findViewById(R.id.toolbar);
                        setSupportActionBar(toolbar);
                        ActionBar actionBar = getSupportActionBar();
                        if (actionBar != null) {
                            actionBar.setDisplayShowTitleEnabled(false); // Oculta el título predeterminado
                            actionBar.setDisplayShowCustomEnabled(true); // Habilita la vista personalizada del título
                            actionBar.setCustomView(customTitle); // Establece la vista personalizada como título
                            actionBar.setTitle(""); // Limpia cualquier título existente
                            toolbarTitle = customTitle.findViewById(R.id.toolbar_title);

                            if (nombreNegocio != null) {
                                toolbarTitle.setText("Bienvenido a " + nombreNegocio);
                            }
                        }
                        Log.d("EntradaActivity", "Nombre del negocio: " + nombreNegocio);
                    }
                }
            }

            @Override
            public void onFailure(Call<Negocio> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }

}
