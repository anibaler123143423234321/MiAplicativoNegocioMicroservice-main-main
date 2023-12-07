package com.dagnerchuman.miaplicativonegociomicroservice.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dagnerchuman.miaplicativonegociomicroservice.R;
import com.dagnerchuman.miaplicativonegociomicroservice.entity.Categoria;

import java.util.List;

public class CategoriasPagerAdapter extends RecyclerView.Adapter<CategoriasPagerAdapter.CategoriaViewHolder> {

    private List<Categoria> categorias;
    private Context context;
    private OnCategoriaClickListener onCategoriaClickListener; // Agregar esta instancia

    public CategoriasPagerAdapter(Context context, List<Categoria> categorias) {
        this.context = context;
        this.categorias = categorias;
    }
    // Agregar un método para configurar el listener
    public void setOnCategoriaClickListener(OnCategoriaClickListener listener) {
        this.onCategoriaClickListener = listener;
    }
    @NonNull
    @Override
    public CategoriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.categoria_pages_item_xml, parent, false);
        return new CategoriaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriaViewHolder holder, int position) {
        Categoria categoria = categorias.get(position);

        holder.bindCategoria(categoria);
    }

    public interface OnCategoriaClickListener {
        void onCategoriaClick(Categoria categoria);
    }

    @Override
    public int getItemCount() {
        return categorias.size();
    }

    public class CategoriaViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageView;
        private TextView textView;

        public CategoriaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.categoriaImageView);
            textView = itemView.findViewById(R.id.categoriaTextView);

            // Agregar un OnClickListener para el itemView
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onCategoriaClickListener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            Categoria categoria = categorias.get(position);
                            onCategoriaClickListener.onCategoriaClick(categoria);
                        }
                    }
                }
            });

        }

        public void bindCategoria(Categoria categoria) {
            // Cargar la imagen de fondo de la categoría
            Glide.with(itemView)
                    .load(categoria.getPicture())
                    .transform(new CenterCrop())
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            imageView.setImageDrawable(resource);
                        }
                    });

            // Establecer el nombre de la categoría como texto
            textView.setText(categoria.getNombre());
        }
    }
}
