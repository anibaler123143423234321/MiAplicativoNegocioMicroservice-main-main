package com.dagnerchuman.miaplicativonegociomicroservice.activity.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.SearchView;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.CarritoActivityEntrada;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.CategoriaProductosActivity;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CategoriaAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CategoriasPagerAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.ProductoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCategorias;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Categoria;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EntradaFragment extends Fragment implements ProductoAdapter.OnProductSelectedListener, CategoriasPagerAdapter.OnCategoriaClickListener {

    private RecyclerView recyclerView;
    private SearchView searchView;
    private ProductoAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Producto> productosList;  // Inicializa la lista aquí
    private List<Producto> productList = new ArrayList();
    private FloatingActionButton btnCarrito;
    private List<Producto> productosSeleccionados = new ArrayList<>();
    private CategoriasPagerAdapter categoriasPagerAdapter; // Cambio de nombre del adaptador
    private Handler autoScrollHandler;
    private final long AUTO_SCROLL_INTERVAL = 5000; // Intervalo en milisegundos (2 segundos)

    private ViewPager2 viewPagerCategorias;

    public EntradaFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_entrada, container, false);

        viewPagerCategorias = view.findViewById(R.id.viewPagerCategorias);
        recyclerView = view.findViewById(R.id.recyclerView);
        searchView = view.findViewById(R.id.searchView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        btnCarrito = view.findViewById(R.id.btnCarritoE);

        productosList = new ArrayList<>();  // Inicializa la lista aquí
        setupSearchView();
        setupRecyclerView();
        setupSwipeRefreshLayout();
        setupCarritoButton();

        // Inicia la automatización del desplazamiento
        startAutoScroll();

        // Aquí, obtén los productos del negocio cuando se crea el fragmento
        obtenerProductosDelNegocio();
        obtenerCategorias();


        return view;
    }

    @Override
    public void onProductSelected(Producto producto) {
        if (producto.isSelected()) {
            productosSeleccionados.add(producto);
            Toast.makeText(requireContext(), "Producto seleccionado: " + producto.getNombre(), Toast.LENGTH_SHORT).show();
        } else {
            productosSeleccionados.remove(producto);
        }
    }
    @Override
    public void onCategoriaClick(Categoria categoria) {
        navigateToCategoriaProductosActivity(categoria.getId());
    }

    private void setupSearchView() {
        searchView.setIconified(true);
        searchView.setQuery("", true);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filterProductos(newText);
                return true;
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new ProductoAdapter(requireContext(), productosList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(requireContext(), 2));
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            obtenerProductosDelNegocio();
            swipeRefreshLayout.setRefreshing(false);
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

                        newX = Math.max(0, Math.min(newX, requireActivity().findViewById(R.id.constraintLayoutE).getWidth() - v.getWidth()));
                        newY = Math.max(0, Math.min(newY, requireActivity().findViewById(R.id.constraintLayoutE).getHeight() - v.getHeight()));

                        v.setX(newX);
                        v.setY(newY);

                        if (Math.abs(event.getRawX() - startX) < 5 || Math.abs(event.getRawY() - startY) < 5 ) {
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

        btnCarrito.setOnClickListener(view -> navigateToCarritoActivity());
    }

    private void obtenerProductosDelNegocio() {
        ApiServiceProductos apiService = ConfigApi.getInstanceProducto(requireContext());
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);
        Call<List<Producto>> call = apiService.getProductosPorNegocio(userNegocioId);

        call.enqueue(new Callback<List<Producto>>() {
            @Override
            public void onResponse(Call<List<Producto>> call, Response<List<Producto>> response) {
                if (response.isSuccessful()) {
                    List<Producto> productos = response.body();
                    productosList.clear();

                    productList.addAll(productos);
                    adapter.notifyDataSetChanged();
                    int maxProductos = Math.min(productos.size(), 10);
                    for (int i = 0; i < maxProductos; i++) {
                        productosList.add(productos.get(i));
                    }
                    adapter = new ProductoAdapter(requireActivity(), productosList);
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.e("API Response ProductosDelNegocio", "Respuesta no exitosa: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Producto>> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }

    private void obtenerCategorias() {
        ApiServiceCategorias apiServiceCategorias = ConfigApi.getInstanceCategorias(requireContext());
        Call<List<Categoria>> call = apiServiceCategorias.getAllCategorias();

        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
        Long userNegocioId = sharedPreferences.getLong("userNegocioId", -1);

        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                if (response.isSuccessful()) {
                    List<Categoria> categorias = response.body();

                    // Crear una lista de categorías que pertenecen al negocio
                    List<Categoria> categoriasDelNegocio = new ArrayList<>();
                    for (Categoria categoria : categorias) {
                        if (categoria.getNegocioId().equals(userNegocioId)) {
                            categoriasDelNegocio.add(categoria);
                        }
                    }

                    // Configura el adaptador para el ViewPager2
                    categoriasPagerAdapter = new CategoriasPagerAdapter(requireContext(), categoriasDelNegocio);
                    viewPagerCategorias.setAdapter(categoriasPagerAdapter);

// Agregar la acción de hacer clic en una categoría
                    categoriasPagerAdapter.setOnCategoriaClickListener(new CategoriasPagerAdapter.OnCategoriaClickListener() {
                        @Override
                        public void onCategoriaClick(Categoria categoria) {
                            long categoriaId = categoria.getId();
                            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("CategoriaPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putLong("categoriaId", categoriaId);
                            editor.apply();
                            Intent categoriaIntent = new Intent(requireActivity(), CategoriaProductosActivity.class);
                            startActivity(categoriaIntent);
                        }
                    });

                } else {
                    Log.e("API Response CATEGORIAS", "Respuesta no exitosa: " + response.code());
                    Log.e("API Response", "Estado del servidor: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {
                Log.e("API Failure", "Fallo en la solicitud a la API", t);
            }
        });
    }



    private void navigateToCarritoActivity() {
        ArrayList<Producto> productosSeleccionados = new ArrayList<>();
        for (Producto producto : productList) {
            if (producto.isSelected()) {
                productosSeleccionados.add(producto);
            }
        }

        Intent carritoIntent = new Intent(requireActivity(), CarritoActivityEntrada.class);
        carritoIntent.putExtra("productosEnCarritoE", productosSeleccionados);
        startActivity(carritoIntent);
    }
    private void navigateToCategoriaProductosActivity(long categoriaId) {
        Intent categoriaIntent = new Intent(requireActivity(), CategoriaProductosActivity.class);
        categoriaIntent.putExtra("categoriaId", categoriaId);
        startActivity(categoriaIntent);
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

    private void startAutoScroll() {
        autoScrollHandler = new Handler();
        autoScrollHandler.postDelayed(autoScrollRunnable, AUTO_SCROLL_INTERVAL);
    }

    private Runnable autoScrollRunnable = new Runnable() {
        @Override
        public void run() {
            // Realiza el desplazamiento automático
            int currentItem = viewPagerCategorias.getCurrentItem();
            int itemCount = categoriasPagerAdapter.getItemCount();
            if (currentItem < itemCount - 1) {
                viewPagerCategorias.setCurrentItem(currentItem + 1);
            } else {
                viewPagerCategorias.setCurrentItem(0);
            }

            // Programa el siguiente desplazamiento automático
            autoScrollHandler.postDelayed(this, AUTO_SCROLL_INTERVAL);
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Detiene el desplazamiento automático
        autoScrollHandler.removeCallbacks(autoScrollRunnable);
    }


}