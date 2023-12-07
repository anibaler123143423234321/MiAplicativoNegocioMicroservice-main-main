package com.dagnerchuman.miaplicativonegociomicroservice.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.activity.CarritoActivity;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Producto;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CarritoAdapter extends RecyclerView.Adapter<CarritoAdapter.CarritoViewHolder> {
    private Context context;
    private List<Producto> productosEnCarrito;
    private List<Integer> cantidadesDeseadas;

    public CarritoAdapter(Context context, List<Producto> productosEnCarrito) {
        this.context = context;
        this.productosEnCarrito = productosEnCarrito;
        this.cantidadesDeseadas = new ArrayList<>();
        for (int i = 0; i < productosEnCarrito.size(); i++) {
            cantidadesDeseadas.add(0);
        }
    }

    @NonNull
    @Override
    public CarritoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrito, parent, false);
        return new CarritoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CarritoViewHolder holder, int position) {
        Producto producto = productosEnCarrito.get(position);
        holder.txtNombreProducto.setText(producto.getNombre());
        holder.txtPrecioProducto.setText("$" + producto.getPrecio());
        Picasso.get().load(producto.getPicture()).into(holder.imgProducto);

        int cantidadDeseada = cantidadesDeseadas.get(position);
        holder.txtCantidad.setText(String.valueOf(cantidadDeseada));

        // Manejar el botón de aumentar cantidad
        holder.btnAumentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = holder.getAdapterPosition();
                if (newPosition != RecyclerView.NO_POSITION) {
                    int nuevaCantidad = cantidadesDeseadas.get(newPosition) + 1;
                    cantidadesDeseadas.set(newPosition, nuevaCantidad);
                    holder.txtCantidad.setText(String.valueOf(nuevaCantidad));
                    // Actualiza el total del producto si es necesario
                    double nuevoTotal = producto.getPrecio() * nuevaCantidad;
                    holder.txtTotalProducto.setText("Total: $" + nuevoTotal);
                }
            }
        });

        // Manejar el botón de disminuir cantidad
        holder.btnDisminuir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = holder.getAdapterPosition();
                if (newPosition != RecyclerView.NO_POSITION) {
                    int cantidadDeseada = cantidadesDeseadas.get(newPosition);
                    if (cantidadDeseada > 0) {
                        int nuevaCantidad = cantidadDeseada - 1;
                        cantidadesDeseadas.set(newPosition, nuevaCantidad);
                        holder.txtCantidad.setText(String.valueOf(nuevaCantidad));
                        // Actualiza el total del producto si es necesario
                        double nuevoTotal = producto.getPrecio() * nuevaCantidad;
                        holder.txtTotalProducto.setText("Total: $" + nuevoTotal);
                    }
                }
            }
        });

        // Manejar el botón de eliminar
        holder.imgEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int newPosition = holder.getAdapterPosition();
                if (newPosition != RecyclerView.NO_POSITION) {
                    // Elimina el producto y la cantidad deseada de las listas
                    productosEnCarrito.remove(newPosition);
                    cantidadesDeseadas.remove(newPosition);
                    // Notifica al adaptador que los datos han cambiado
                    notifyItemRemoved(newPosition);

                    // Actualiza el intent con la lista de productos actualizada
                    actualizarIntentCarrito(productosEnCarrito);
                }
            }
        });
    }
    // Agregar esta función a la clase CarritoAdapter
    private void actualizarIntentCarrito(List<Producto> productos) {
        Intent intent = new Intent(context, CarritoActivity.class);
        intent.putExtra("productosEnCarrito", new ArrayList<>(productos));
        context.startActivity(intent);
    }
    public int getCantidadDeseada(int position) {
        return cantidadesDeseadas.get(position);
    }

    @Override
    public int getItemCount() {
        return productosEnCarrito.size();
    }

    public void setProductos(List<Producto> productos) {
        this.productosEnCarrito = productos;
        notifyDataSetChanged(); // Notificar al adaptador que los datos han cambiado
    }

    public class CarritoViewHolder extends RecyclerView.ViewHolder {
        public ImageView imgProducto;
        public TextView txtNombreProducto;
        public TextView txtPrecioProducto;
        public TextView txtCantidad;
        public TextView txtTotalProducto;
        public Button btnAumentar;
        public Button btnDisminuir;
        public ImageView imgEliminar; // ImageView para el botón de eliminación


        public CarritoViewHolder(View itemView) {
            super(itemView);
            imgProducto = itemView.findViewById(R.id.imgProducto);
            txtNombreProducto = itemView.findViewById(R.id.txtNombreProducto);
            txtPrecioProducto = itemView.findViewById(R.id.txtPrecioProducto);
            txtCantidad = itemView.findViewById(R.id.txtCantidad);
            txtTotalProducto = itemView.findViewById(R.id.txtTotalProducto);
            btnAumentar = itemView.findViewById(R.id.btnAumentar);
            btnDisminuir = itemView.findViewById(R.id.btnDisminuir);
            imgEliminar = itemView.findViewById(R.id.imgEliminar); // Asociar ImageView al botón de eliminación

        }
    }
}

