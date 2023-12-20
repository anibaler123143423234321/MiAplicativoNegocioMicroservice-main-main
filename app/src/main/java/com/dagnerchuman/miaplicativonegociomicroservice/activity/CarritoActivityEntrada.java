package com.dagnerchuman.miaplicativonegociomicroservice.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.adapter.CarritoAdapter;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceCompras;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceNegocio;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ApiServiceProductos;
import com.dagnerchuman.miaplicativonegociomicroservice.api.ConfigApi;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Compra;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Negocio;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.ArrayList;
import java.util.List;

public class CarritoActivityEntrada extends AppCompatActivity {
    private List<Producto> productosEnCarrito;
    private List<Integer> cantidadesDeseadas;
    private Button btnFinalizarCompra;
    private CarritoAdapter carritoAdapter;
    private RecyclerView recyclerView;
    private Spinner spinnerTipoEnvio;
    private Spinner spinnerTipoDePago;
    private ImageButton btnBackToLogin;
    private ApiServiceCompras apiServiceCompras;
    private ApiServiceProductos apiServiceProductos;
    private ApiServiceNegocio apiServiceNegocio;
    private boolean productosCargados = false; // Bandera para rastrear si los productos ya se han cargado
    private ProgressDialog progressDialog;
    private boolean seRealizoUnaCompra = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrito);

        if (!productosCargados) {
            // Solo carga los productos desde Intent la primera vez que se inicia la actividad
            productosEnCarrito = (List<Producto>) getIntent().getSerializableExtra("productosEnCarritoE");
            productosCargados = true; // Marca que los productos se han cargado
        }
        apiServiceCompras = ConfigApi.getInstanceCompra(this);
        apiServiceProductos = ConfigApi.getInstanceProducto(this);
        apiServiceNegocio = ConfigApi.getInstanceNegocio(this);

        recyclerView = findViewById(R.id.recyclerViewCarrito);
        carritoAdapter = new CarritoAdapter(this, productosEnCarrito);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(carritoAdapter);

        spinnerTipoEnvio = findViewById(R.id.spinnerTipoEnvio);
        ArrayAdapter<CharSequence> tipoEnvioAdapter = ArrayAdapter.createFromResource(this, R.array.tipo_envio_array, android.R.layout.simple_spinner_item);
        tipoEnvioAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoEnvio.setAdapter(tipoEnvioAdapter);

        spinnerTipoDePago = findViewById(R.id.spinnerTipoDePago);
        ArrayAdapter<CharSequence> tipoPagoAdapter = ArrayAdapter.createFromResource(this, R.array.tipo_pago_array, android.R.layout.simple_spinner_item);
        tipoPagoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipoDePago.setAdapter(tipoPagoAdapter);

        spinnerTipoEnvio.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedTipoEnvio = (String) adapterView.getItemAtPosition(position);
                if ("Delivery".equals(selectedTipoEnvio)) {
                    // Muestra el SweetAlert al seleccionar "Delivery"
                    mostrarSweetAlertDeliveryOnInit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No es necesario implementar esta parte si no se requiere
            }
        });

        btnBackToLogin = findViewById(R.id.btnBackToLogin);
        btnBackToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Modifica el Intent para enviar el 'categoriaId' de vuelta a la actividad 'CategoriaProductosActivity'
                long categoriaId = getIntent().getLongExtra("categoriaSeleccionada", -1);
                Intent loginIntent = new Intent(CarritoActivityEntrada.this, EntradaActivity.class);
                loginIntent.putExtra("categoriaSeleccionada", categoriaId);
                startActivity(loginIntent);
                finish();
            }
        });

        cantidadesDeseadas = new ArrayList<>();
        for (int i = 0; i < productosEnCarrito.size(); i++) {
            cantidadesDeseadas.add(1);
        }

        // Recupera el userId desde SharedPreferences (ajusta el nombre de la preferencia y el valor por defecto según tus necesidades)
        SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
        Long userId = sharedPreferences.getLong("userId", 0L);  // 0L es el valor por defecto si no se encuentra la preferencia

        btnFinalizarCompra = findViewById(R.id.btnConfirmarCompra);
        btnFinalizarCompra.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog = new ProgressDialog(CarritoActivityEntrada.this);
                progressDialog.setMessage("Realizando compras...");
                progressDialog.setCancelable(false);
                progressDialog.show();

                // Agrega una espera de 2.5 segundos antes de continuar con la compra
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        realizarCompras();
                    }
                }, 2500); // Espera de 2.5 segundos antes de realizar las compras
            }

            private void realizarCompras() {
                if (productosEnCarrito != null && !productosEnCarrito.isEmpty()) {
                    String tipoEnvio = spinnerTipoEnvio.getSelectedItem().toString();
                    String tipoDePago = spinnerTipoDePago.getSelectedItem().toString();
                    int totalCompras = productosEnCarrito.size();
                    final int[] comprasExitosas = {0}; // Variable final para contar compras exitosas

                    for (int i = 0; i < totalCompras; i++) {
                        Producto producto = productosEnCarrito.get(i);
                        int cantidadDeseada = cantidadesDeseadas.get(i);

                        Compra compra = new Compra();
                        compra.setUserId(userId);
                        compra.setProductoId(producto.getId());
                        compra.setTitulo(producto.getNombre());
                        compra.setPrecioCompra(producto.getPrecio());
                        compra.setTipoEnvio(tipoEnvio);
                        compra.setTipoDePago(tipoDePago);
                        compra.setCantidad(cantidadDeseada);

                        Call<Producto> stockCall = apiServiceProductos.getProductoById(producto.getId());

                        stockCall.enqueue(new Callback<Producto>() {
                            @Override
                            public void onResponse(Call<Producto> call, Response<Producto> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Producto productoActualizado = response.body();
                                    int stockDisponible = productoActualizado.getStock();

                                    // Verificar si la cantidad deseada es menor o igual al stock disponible
                                    if (cantidadDeseada <= stockDisponible) {
                                        Call<Compra> compraCall = apiServiceCompras.saveCompra(compra);

                                        compraCall.enqueue(new Callback<Compra>() {
                                            @Override
                                            public void onResponse(Call<Compra> call, Response<Compra> response) {
                                                if (response.isSuccessful()) {
                                                    // Compra exitosa
                                                    comprasExitosas[0]++;

                                                    // Actualizar el stock del producto
                                                    productoActualizado.setStock(stockDisponible - cantidadDeseada);
                                                    Call<Producto> updateCall = apiServiceProductos.actualizarProducto(producto.getId(), productoActualizado);

                                                    updateCall.enqueue(new Callback<Producto>() {
                                                        @Override
                                                        public void onResponse(Call<Producto> call, Response<Producto> response) {
                                                            if (response.isSuccessful()) {
                                                                // El stock del producto se ha actualizado con éxito
                                                            } else {
                                                                // Manejar errores en la actualización del producto
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Call<Producto> call, Throwable t) {
                                                            // Manejar errores en la llamada al actualizarProducto
                                                        }
                                                    });

                                                    // Si todas las compras son exitosas, redirige a la actividad de confirmación
                                                    if (comprasExitosas[0] == totalCompras) {
                                                        progressDialog.dismiss(); // Ocultar el diálogo de carga
                                                        mostrarSweetAlert();
                                                        // Establece la bandera seRealizoUnaCompra en true
                                                        seRealizoUnaCompra = true;
                                                    }

                                                } else {
                                                    progressDialog.dismiss(); // Ocultar el diálogo de carga
                                                    // Manejar errores en la compra
                                                    Toast.makeText(CarritoActivityEntrada.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<Compra> call, Throwable t) {
                                                // Manejar errores en la llamada de compra
                                                progressDialog.dismiss(); // Ocultar el diálogo de carga
                                                Toast.makeText(CarritoActivityEntrada.this, "Error al realizar la compra", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    } else {
                                        // Manejar errores cuando la cantidad deseada supera el stock
                                        progressDialog.dismiss(); // Ocultar el diálogo de carga
                                        Toast.makeText(CarritoActivityEntrada.this, "La cantidad deseada para '" + producto.getNombre() + "' supera el stock actual (" + stockDisponible + ")", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Manejar errores al obtener el stock del producto
                                    progressDialog.dismiss(); // Ocultar el diálogo de carga
                                    Toast.makeText(CarritoActivityEntrada.this, "Error al obtener el stock del producto", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Producto> call, Throwable t) {
                                // Manejar errores en la llamada para obtener el stock del producto
                                progressDialog.dismiss(); // Ocultar el diálogo de carga
                                Toast.makeText(CarritoActivityEntrada.this, "Error al obtener el stock del producto", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    // Manejar errores cuando el carrito está vacío
                    progressDialog.dismiss(); // Ocultar el diálogo de carga
                    Toast.makeText(CarritoActivityEntrada.this, "El carrito está vacío", Toast.LENGTH_SHORT).show();
                }
            }


            private void mostrarSweetAlert() {
                new SweetAlertDialog(CarritoActivityEntrada.this, SweetAlertDialog.NORMAL_TYPE)
                        .setTitleText("¿Quieres avisar al negocio por WhatsApp?")
                        .setConfirmText("Sí")
                        .setCancelText("No")
                        .setConfirmClickListener(sweetAlertDialog -> {
                            // Obtener el número de WhatsApp del negocio
                            obtenerNumeroWhatsAppNegocio();
                            sweetAlertDialog.dismissWithAnimation();
                        })
                        .setCancelClickListener(sweetAlertDialog -> {
                            // Si el usuario elige "No", realizar la compra pero no vaciar el carrito
                            sweetAlertDialog.dismissWithAnimation();
                            seRealizoUnaCompra = true;
                        })
                        .show();
            }


            private void obtenerNumeroWhatsAppNegocio() {
                // Recuperar el userId desde SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
                Long userNegocioId = sharedPreferences.getLong("userNegocioId", 0L);

                // Llamar al endpoint para obtener el negocio por ID
                Call<Negocio> negocioCall = apiServiceNegocio.getNegocioById(userNegocioId);

                negocioCall.enqueue(new Callback<Negocio>() {
                    @Override
                    public void onResponse(Call<Negocio> call, Response<Negocio> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Negocio negocio = response.body();

                            // Agrega un log para imprimir el número de WhatsApp
                            Log.d("CarritoActivity", "Número de WhatsApp del negocio: " + negocio.getTelefono());

                            enviarMensajeWhatsApp(negocio.getTelefono());
                        } else {
                            // Manejar errores al obtener el negocio
                            Toast.makeText(CarritoActivityEntrada.this, "Error al obtener información del negocio", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Negocio> call, Throwable t) {
                        // Manejar errores en la llamada para obtener el negocio
                        Toast.makeText(CarritoActivityEntrada.this, "Error al obtener información del negocio", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            private void enviarMensajeWhatsApp(String numeroWhatsApp) {
                // Agrega un log para imprimir que se está enviando el mensaje
                Log.d("CarritoActivity", "Enviando mensaje a WhatsApp...");

                // Construye el mensaje con la lista de productos
                StringBuilder mensaje = new StringBuilder("¡Hola! Acabo de hacer un pedido en tu negocio. Mis productos son:\n");

                for (int i = 0; i < productosEnCarrito.size(); i++) {
                    Producto producto = productosEnCarrito.get(i);
                    int cantidad = cantidadesDeseadas.get(i);
                    mensaje.append(producto.getNombre()).append(" - Cantidad: ").append(cantidad).append("\n");
                }

                // Agrega el resto del mensaje
                mensaje.append("\n¿Puedes confirmar mi pedido? Gracias.");

                // Construye la URL de WhatsApp con el número y el mensaje predefinido
                String url = "https://wa.me/" + numeroWhatsApp + "?text=" + Uri.encode(mensaje.toString());

                // Abre WhatsApp con el número y el mensaje predefinido
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);

                // Agrega un log para imprimir que el mensaje se envió
                Log.d("CarritoActivity", "Mensaje enviado a WhatsApp.");
            }




        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Verifica si se realizó una compra y muestra el SweetAlert
        if (seRealizoUnaCompra) {
            // Llama a vaciarCarrito() aquí
            vaciarCarrito();
            seRealizoUnaCompra = false; // Restablece la bandera
        }
    }
    private void vaciarCarrito() {
        // Limpia las listas o datos que representan el carrito
        productosEnCarrito.clear();
        cantidadesDeseadas.clear();

        // Actualiza el adaptador del RecyclerView
        carritoAdapter.notifyDataSetChanged();
    }

    private void mostrarSweetAlertDeliveryOnInit() {
        new SweetAlertDialog(CarritoActivityEntrada.this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Costo adicional por Delivery")
                .setContentText("El envío por Delivery tiene un costo adicional de 2 soles. ¿Deseas continuar?")
                .setConfirmText("Sí")
                .setCancelText("No")
                .setConfirmClickListener(sweetAlertDialog -> {
                    // Agrega aquí la lógica para continuar con la compra y aplicar el costo adicional
                    sweetAlertDialog.dismissWithAnimation();
                })
                .setCancelClickListener(sweetAlertDialog -> {
                    // Agrega aquí la lógica para cancelar la compra o realizar alguna acción
                    sweetAlertDialog.dismissWithAnimation();
                })
                .show();
    }

}