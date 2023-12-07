package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CompraAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCompras;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Compra;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import android.app.AlertDialog;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ComprarActivity extends AppCompatActivity implements CompraAdapter.BoletaDownloadListener {

    private TextView txtUserId;
    private TextView txtProductoId;
    private TextView txtNombreProducto;
    private TextView txtPrecioProducto;
    private TextView txtStockProducto;
    private ImageView imgProducto;
    private EditText edtCantidadDeseada;
    private Button btnConfirmarCompra;
    private RadioGroup radioGroupEnvio;
    private RadioGroup radioGroupPago;
    private ApiServiceCompras apiServiceCompras;
    private ApiServiceProductos apiServiceProductos;

    private Long userId;
    private Long productoId;
    private String titulo;
    private Double precioCompra;
    private int stockProducto;
    private Integer cantidad;
    private String tipoEnvio;
    private String tipoDePago;
    private List<Producto> listaCarrito; // Lista de productos en el carrito
    private CompraAdapter compraAdapter; // Crea un adapter personalizado para las compras
    // Añade una variable para el RecyclerView y su adaptador
    private RecyclerView recyclerView;
    private List<Compra> comprasList = new ArrayList<>();
    private Object compraLock = new Object(); // Declaración de compraLock como objeto para bloquear
    private boolean compraConfirmada = false; // Declaración de compraConfirmada como variable booleana

    private boolean isCompraConfirmada = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comprar);

        // Inicializa las vistas
        initView();

// Configura el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewCompras);
        recyclerView.setLayoutManager(new LinearLayoutManager(this)); // This line causes NullPointerException
        compraAdapter = new CompraAdapter(this);
        recyclerView.setAdapter(compraAdapter);


        // Configura el adaptador con las compras
        compraAdapter.setCompras(comprasList);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Intent intent = getIntent();
        List<Producto> productosSeleccionados = (List<Producto>) intent.getSerializableExtra("productosSeleccionados");


        Intent intent2 = getIntent();
        if (intent2 != null) {
            userId = intent2.getLongExtra("userId", -1);
            productoId = intent2.getLongExtra("productoId", -1);
            String nombreProducto = intent2.getStringExtra("nombreProducto");
            double precioProducto = intent2.getDoubleExtra("precioProducto", 0.0);
            stockProducto = intent2.getIntExtra("stockProducto", 0);
            String imagenProducto = intent2.getStringExtra("imagenProducto");

            mostrarDetallesDelProducto(userId, productoId, nombreProducto, precioProducto, stockProducto, imagenProducto);
        }

        btnConfirmarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                confirmarCompra();
            }
        });


    }

    private void initView() {
        //txtUserId = findViewById(R.id.txtUserId);
        txtProductoId = findViewById(R.id.txtProductoId);
        txtNombreProducto = findViewById(R.id.txtNombreProducto);
        txtPrecioProducto = findViewById(R.id.txtPrecioProducto);
        txtStockProducto = findViewById(R.id.txtStockProducto);
        imgProducto = findViewById(R.id.imgProducto);
        edtCantidadDeseada = findViewById(R.id.edtCantidadDeseada);
        btnConfirmarCompra = findViewById(R.id.btnConfirmarCompra);
        radioGroupEnvio = findViewById(R.id.radioGroupEnvio);
        radioGroupPago = findViewById(R.id.radioGroupPago);
        apiServiceCompras = ConfigApi.getInstanceCompra(this);
        apiServiceProductos = ConfigApi.getInstanceProducto(this);
    }

    private void mostrarDetallesDelProducto(Long userId, Long productoId, String nombreProducto, double precioProducto, int stockProducto, String imagenProducto) {
        //txtUserId.setText("ID del Usuario: " + userId);
        txtProductoId.setText("ID del Producto: " + productoId);
        txtNombreProducto.setText("Nombre del Producto: " + nombreProducto);
        txtPrecioProducto.setText("Precio: $" + precioProducto);
        txtStockProducto.setText("Stock: " + stockProducto);
        titulo = nombreProducto;
        precioCompra = precioProducto;

        if (imagenProducto != null && !imagenProducto.isEmpty()) {
            Glide.with(this)
                    .load(imagenProducto)
                    .into(imgProducto);
        } else {
            imgProducto.setImageResource(R.drawable.image_not_found);
        }
    }

    private void confirmarCompra() {
        if (compraConfirmada) {
            // La compra ya ha sido confirmada, no hagas nada.
            return;
        }

        String cantidadDeseadaStr = edtCantidadDeseada.getText().toString().trim();

        if (!cantidadDeseadaStr.isEmpty()) {
            cantidad = Integer.parseInt(cantidadDeseadaStr);
            progressDialog = new ProgressDialog(ComprarActivity.this);
            progressDialog.setMessage("Esperando...");
            progressDialog.setCancelable(false); // Evita que el usuario pueda cancelar el diálogo
            progressDialog.show();

            // Agrega una espera de 3 segundos antes de continuar con la compra
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();

                    // Consulta el stock actual del producto
                    Call<Producto> stockCall = apiServiceProductos.getProductoById(productoId);
                    stockCall.enqueue(new Callback<Producto>() {
                        @Override
                        public void onResponse(Call<Producto> stockCall, Response<Producto> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                Producto producto = response.body();
                                int stockDisponible = producto.getStock();

                                // Realiza una comprobación atómica y bloquea otros hilos si la cantidad deseada supera el stock
                                if (producto != null && producto.intentarBloquearCompra()) {
                                    try {
                                        if (cantidad <= stockDisponible) {
                                            tipoEnvio = obtenerValorRadioSeleccionado(radioGroupEnvio);
                                            tipoDePago = obtenerValorRadioSeleccionado(radioGroupPago);

                                            // Añade una variable para mantener los datos originales del producto
                                            int stockOriginal = producto.getStock();

                                            // Realiza una segunda llamada a getProductoById para obtener los datos actualizados
                                            Call<Producto> secondStockCall = apiServiceProductos.getProductoById(productoId);
                                            secondStockCall.enqueue(new Callback<Producto>() {
                                                @Override
                                                public void onResponse(Call<Producto> secondStockCall, Response<Producto> secondResponse) {
                                                    if (secondResponse.isSuccessful() && secondResponse.body() != null) {
                                                        Producto updatedProducto = secondResponse.body();

                                                        // Mostrar los datos originales y actualizados en la consola
                                                        Log.i("ComprarActivity", "Datos originales del producto: Stock = " + stockOriginal);
                                                        Log.i("ComprarActivity", "Datos actualizados del producto: Stock = " + updatedProducto.getStock());

                                                        if (updatedProducto.getStock() == stockOriginal) {
                                                            Compra compra = new Compra();
                                                            compra.setUserId(userId);
                                                            compra.setProductoId(productoId);
                                                            compra.setTitulo(titulo);
                                                            compra.setPrecioCompra(precioCompra);
                                                            compra.setCantidad(cantidad);
                                                            compra.setTipoEnvio(tipoEnvio);
                                                            compra.setTipoDePago(tipoDePago);

                                                            // Realiza la compra
                                                            Call<Compra> compraCall = apiServiceCompras.saveCompra(compra);
                                                            compraCall.enqueue(new Callback<Compra>() {
                                                                @Override
                                                                public void onResponse(Call<Compra> compraCall, Response<Compra> response) {
                                                                    if (response.isSuccessful() && response.body() != null) {
                                                                        Compra compraConfirmada = response.body();
                                                                        Log.i("ComprarActivity", "Compra confirmada con éxito. ID: " + compraConfirmada.getId());

                                                                        // Actualiza el stock del producto
                                                                        int nuevoStock = stockDisponible - cantidad;
                                                                        actualizarStockProducto(productoId, nuevoStock); // Agregar esta línea

                                                                        // Realiza una segunda verificación del stock antes de confirmar la compra
                                                                        Call<Producto> thirdStockCall = apiServiceProductos.getProductoById(productoId);
                                                                        thirdStockCall.enqueue(new Callback<Producto>() {
                                                                            @Override
                                                                            public void onResponse(Call<Producto> thirdStockCall, Response<Producto> thirdResponse) {
                                                                                if (thirdResponse.isSuccessful() && thirdResponse.body() != null) {
                                                                                    Producto finalUpdatedProducto = thirdResponse.body();
                                                                                    int finalUpdatedStock = finalUpdatedProducto.getStock();

                                                                                    if (finalUpdatedStock == nuevoStock) {
                                                                                        // El stock se mantiene igual, lo que significa que la compra se realizó con éxito
                                                                                        mostrarAlertaCompraExitosa();
                                                                                    } else {
                                                                                        // El stock ha cambiado, lo que significa que otro usuario compró el producto
                                                                                        Toast.makeText(ComprarActivity.this, "El stock ha cambiado, Aviso: Este producto es popular y otros usuarios pueden estar comprándolo.", Toast.LENGTH_SHORT).show();
                                                                                    }
                                                                                }
                                                                            }

                                                                            @Override
                                                                            public void onFailure(Call<Producto> thirdStockCall, Throwable t) {
                                                                                Toast.makeText(ComprarActivity.this, "Error al verificar el stock del producto", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    } else {
                                                                        mostrarAlertaError("No se pudo confirmar la compra");
                                                                        Log.e("ComprarActivity", "Error en la respuesta: " + response.code());
                                                                    }
                                                                }

                                                                @Override
                                                                public void onFailure(Call<Compra> compraCall, Throwable t) {
                                                                    mostrarAlertaError("Error en la conexión");
                                                                    Log.e("ComprarActivity", "Error de conexión", t);
                                                                }
                                                            });
                                                        } else {
                                                            // Los datos han cambiado, muestra un mensaje de error
                                                            Toast.makeText(ComprarActivity.this, "El stock ha cambiado desde que iniciaste la compra, inténtalo de nuevo", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<Producto> secondStockCall, Throwable t) {
                                                    Toast.makeText(ComprarActivity.this, "Error al verificar el stock del producto", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        } else {
                                            mostrarAlertaError("La cantidad deseada supera el stock actual (" + stockDisponible + ")");
                                        }
                                    } finally {
                                        producto.desbloquearCompra(); // Asegúrate de desbloquear el producto, incluso si ocurre una excepción
                                    }
                                } else {
                                    Toast.makeText(ComprarActivity.this, "El producto está siendo comprado por otro usuario, por favor, inténtalo más tarde.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Producto> stockCall, Throwable t) {
                            Toast.makeText(ComprarActivity.this, "Error al obtener el stock del producto", Toast.LENGTH_SHORT).show();
                            Log.e("ComprarActivity", "Error al obtener el stock del producto", t);
                        }
                    });
                }}, 2500); // Espera de 3 segundos (3000 milisegundos)
        } else {
            mostrarAlertaError("Ingresa la cantidad deseada");
        }
    }

    // Función para mostrar una alerta de compra exitosa
    private void mostrarAlertaCompraExitosa() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Compra Exitosa");
        builder.setMessage("Tu compra se realizó con éxito.");
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Lógica a ejecutar después de hacer clic en "Aceptar" (si es necesario)
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Función para mostrar una alerta de error
    private void mostrarAlertaError(String mensaje) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(mensaje);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            // Lógica a ejecutar después de hacer clic en "Aceptar" (si es necesario)
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void actualizarStockProducto(Long productoId, int nuevoStock) {
        Call<Producto> call = apiServiceProductos.getProductoById(productoId);
        call.enqueue(new Callback<Producto>() {
            @Override
            public void onResponse(Call<Producto> call, Response<Producto> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Producto producto = response.body();
                    producto.setStock(nuevoStock);

                    Call<Producto> updateCall = apiServiceProductos.actualizarProducto(productoId, producto);
                    updateCall.enqueue(new Callback<Producto>() {
                        @Override
                        public void onResponse(Call<Producto> call, Response<Producto> response) {
                            if (response.isSuccessful()) {
                                Log.i("ComprarActivity", "Stock del producto actualizado con éxito.");
                                mostrarMensajeCompraExitosa();
                            } else {
                                Toast.makeText(ComprarActivity.this, "No se pudo actualizar el stock del producto", Toast.LENGTH_SHORT).show();
                                Log.e("ComprarActivity", "Error en la respuesta al actualizar el stock del producto: " + response.code());
                            }
                        }

                        @Override
                        public void onFailure(Call<Producto> call, Throwable t) {
                            Toast.makeText(ComprarActivity.this, "Error de conexión al actualizar el stock del producto", Toast.LENGTH_SHORT).show();
                            Log.e("ComprarActivity", "Error de conexión al actualizar el stock del producto", t);
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<Producto> call, Throwable t) {
                Toast.makeText(ComprarActivity.this, "Error de conexión al obtener el detalle del producto", Toast.LENGTH_SHORT).show();
                Log.e("ComprarActivity", "Error de conexión al obtener el detalle del producto", t);
            }
        });
    }

    private void mostrarMensajeCompraExitosa() {
        compraConfirmada = true;
        Toast.makeText(ComprarActivity.this, "Compra realizada con éxito", Toast.LENGTH_SHORT).show();
        Intent entradaIntent = new Intent(ComprarActivity.this, EntradaActivity.class);
        startActivity(entradaIntent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    private String obtenerValorRadioSeleccionado(RadioGroup radioGroup) {
        int selectedId = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectedId);
        return radioButton != null ? radioButton.getText().toString() : "";
    }



    // Implementa el método onBoletaDownload para manejar la descarga de boletas
    @Override
    public void onBoletaDownload(Compra compra) {
        // Aquí puedes implementar la lógica para descargar la boleta de la compra.
        // Por ejemplo, mostrar un diálogo de descarga o iniciar una actividad de descarga.
    }

    // Este método simula cargar las compras desde una fuente de datos.
    private void cargarComprasDesdeFuenteDeDatos() {
        // Simula cargar una lista de compras desde una fuente de datos.
        // Debes obtener las compras reales de tu aplicación y agregarlas a la lista.

        // Agrega más compras según tus datos reales.
    }

}