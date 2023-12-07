package com.dagnerchuman.miaplicativonegociomicroservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.ComprarActivity;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.EntradaActivity;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.CarritoItem;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;

import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class CategoriaAdapter extends RecyclerView.Adapter<CategoriaAdapter.ProductoViewHolder> {
    private Context context;
    private List<Producto> productList;
    private EntradaActivity entradaActivity;
    private List<CarritoItem> carrito;  // Agrega esta lista para llevar un registro del carrito.
    private List<Producto> productosEnCarrito = new ArrayList<>();


    public CategoriaAdapter(Context context, List<Producto> productList) {
        this.context = context;
        this.productList = productList;
        this.carrito = new ArrayList<>();
        this.productosEnCarrito = new ArrayList<>(); // Inicializa la lista de productos en el carrito

    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_categoria, parent, false);
        return new ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productList.get(position);

        holder.txtNombre.setText(producto.getNombre());
        //holder.txtCategoria.setText(String.valueOf(producto.getCategoriaId()));
        holder.txtPrecio.setText("$" + producto.getPrecio());
        holder.txtStock.setText(String.valueOf(producto.getStock()));

        RequestOptions requestOptions = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.ic_documento)
                .error(R.drawable.ic_documento);

        Glide.with(context)
                .load(producto.getPicture())
                .apply(requestOptions)
                .into(holder.imgProducto);


        holder.btnComprar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Aquí debes manejar la acción de "Comprar" para el producto actual.
                handleCompra(producto);
            }
        });
        holder.btnAnadirCarrito.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (producto.getStock() > 0) {
                    // Verifica si el stock del producto es mayor que 0
                    // Si el stock es 0, no se permite añadirlo al carrito
                    // Cambia el estado de selección del producto
                    producto.setSelected(!producto.isSelected());

                    // Notifica al adaptador que los datos han cambiado
                    notifyDataSetChanged();
                    // Declaración de la variable sweetAlertDialog
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("Producto añadido al carrito")
                            .setContentText("El producto se ha añadido con éxito")
                            .setConfirmText("OK");

                    sweetAlertDialog.show();

                    // Cierra el SweetAlert después de un tiempo específico
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            sweetAlertDialog.dismissWithAnimation();
                        }
                    }, 1800); // Cambia 2000 a la duración deseada en milisegundos

                } else {
                    // Muestra una SweetAlert de advertencia si el producto está agotado
                    new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Producto agotado")
                            .setContentText("Este producto está fuera de stock.")
                            .show();
                }
            }
        });




    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public class ProductoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProducto;
        TextView txtNombre, txtCategoria, txtPrecio, txtStock;
        Button btnComprar;
        Button btnAnadirCarrito; // Nombre actualizado del botón

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProducto = itemView.findViewById(R.id.imgProducto);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            //txtCategoria = itemView.findViewById(R.id.txtCategoria);
            txtPrecio = itemView.findViewById(R.id.txtPrecio);
            txtStock = itemView.findViewById(R.id.txtStock);
            btnComprar = itemView.findViewById(R.id.btnComprar);
            btnAnadirCarrito = itemView.findViewById(R.id.btnAnadirCarrito); // Nombre actualizado del botón
        }
    }

    private void handleCompra(Producto producto) {
        if (producto.getStock() > 0) {

            SharedPreferences sharedPreferences = context.getSharedPreferences("UserDataUser", Context.MODE_PRIVATE);
            long userId = sharedPreferences.getLong("userId", -1);

            if (userId != -1) {
                Intent intent = new Intent(context, ComprarActivity.class);
                intent.putExtra("userId", userId);
                intent.putExtra("productoId", producto.getId());
                intent.putExtra("nombreProducto", producto.getNombre());
                intent.putExtra("categoriaProducto", producto.getCategoriaId());
                intent.putExtra("precioProducto", producto.getPrecio());
                intent.putExtra("imagenProducto", producto.getPicture());
                intent.putExtra("stockProducto", producto.getStock());
                context.startActivity(intent);
            } else {
                // Si no estás autenticado, muestra una SweetAlert de error
                new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("Error")
                        .setContentText("Usuario no autenticado")
                        .show();        }
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Error")
                    .setContentText("Producto esta agotado")
                    .show();
        }
    }


    private void agregarAlCarrito(int productoId, String nombre, int cantidad, double precio) {
        // Verificar si el producto ya está en el carrito.
        for (CarritoItem item : carrito) {
            if (item.getProductoId() == productoId) {
                // El producto ya está en el carrito, aumenta la cantidad.
                item.setCantidad(item.getCantidad() + 1);
                notifyDataSetChanged();  // Notificar cambios en el adaptador.
                return;
            }
        }

        // Si el producto no está en el carrito, agrégalo como nuevo.
        CarritoItem carritoItem = new CarritoItem(productoId, nombre, cantidad, precio);
        carrito.add(carritoItem);
        notifyDataSetChanged();  // Notificar cambios en el adaptador.
    }


    public List<CarritoItem> getCarrito() {
        return carrito;
    }

}
