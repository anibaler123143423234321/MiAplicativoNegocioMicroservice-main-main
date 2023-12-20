package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CategoriaAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoriaProductosActivity extends AppCompatActivity {
    private List<Producto> productList = new ArrayList();
    private RecyclerView recyclerView;
    private CategoriaAdapter adapter;
    private ImageButton btnBackToLogin, btnCarrito;
    private ConstraintLayout constraintLayout;
    private SwipeRefreshLayout swipeRefreshLayout; // Agrega esta variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_categoria_productos);

        recyclerView = findViewById(R.id.recyclerViewProductos);
        btnCarrito = findViewById(R.id.btnCarrito);
        constraintLayout = findViewById(R.id.constraintLayout);
        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);

        setupRecyclerView();
        setupBackToLoginButton();
        setupCarritoButton();
        obtainAndFilterProductos();

        // Configura el listener para el gesto de deslizar hacia abajo (refresh)
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Llama a la función para obtener y filtrar productos
                obtainAndFilterProductos();
            }
        });

    }

    private void setupRecyclerView() {
        adapter = new CategoriaAdapter(this, productList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void setupBackToLoginButton() {
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToLoginActivity();
            }
        });
    }

    private void setupCarritoButton() {
        btnCarrito.setOnTouchListener(new View.OnTouchListener() {
            private float dX, dY;
            private float startX, startY;
            private boolean isMoving = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        dY = v.getY() - event.getRawY();
                        startX = event.getRawX();
                        startY = event.getRawY();
                        isMoving = false;
                        break;

                    case MotionEvent.ACTION_MOVE:
                        float newX = event.getRawX() + dX;
                        float newY = event.getRawY() + dY;

                        newX = Math.max(0, Math.min(newX, constraintLayout.getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, constraintLayout.getHeight() - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);

                        if (Math.abs(event.getRawX() - startX) > 320 || Math.abs(event.getRawY() - startY) > 320) {
                            isMoving = true;
                        }
                        break;

                    case MotionEvent.ACTION_UP:
                        if (!isMoving) {
                            navigateToCarritoActivity();
                        }
                        break;
                }
                return true;
            }
        });

        btnCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToCarritoActivity();
            }
        });
    }

    private void navigateToLoginActivity() {
        Intent loginIntent = new Intent(CategoriaProductosActivity.this, EntradaActivity.class);
        startActivity(loginIntent);
        finish();
    }

    private void navigateToCarritoActivity() {
        ArrayList<Producto> productosSeleccionados = new ArrayList<>();
        for (Producto producto : productList) {
            if (producto.isSelected()) {
                productosSeleccionados.add(producto);
            }
        }

        Intent carritoIntent = new Intent(CategoriaProductosActivity.this, CarritoActivity.class);
        carritoIntent.putExtra("productosEnCarrito", productosSeleccionados);
        startActivity(carritoIntent);
    }

    private void obtainAndFilterProductos() {
        SharedPreferences sharedPreferences = getSharedPreferences("CategoriaPrefs", Context.MODE_PRIVATE);
        long categoriaId = sharedPreferences.getLong("categoriaId", -1);

        ApiServiceProductos apiServiceProductos = ConfigApi.getInstanceProducto(this);
        Call<List<Producto>> call = apiServiceProductos.getProductosPorCategoria(categoriaId);

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    List<Producto> productos = response.body();
                    productList.clear();
                    productList.addAll(productos);
                    adapter.notifyDataSetChanged();
                } else {
                    Log.e("API Response", "Respuesta no exitosa: " + response.code());
                }

                // Indica que la acción de refresh ha terminado
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);

                // Indica que la acción de refresh ha terminado
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_categoria, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_cart) {
            navigateToCarritoActivity();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
