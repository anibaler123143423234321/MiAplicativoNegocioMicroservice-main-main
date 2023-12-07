package com.dagnerchuman.miaplicativonegociomicroservice.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Producto implements Serializable {

    private Long id;
    private String nombre;
    private Long categoriaId;
    private String picture;
    private Double precio;
    private String fechaCreacion;
    private Long negocioId;
    private int stock;
    private static final Map<Long, ReentrantLock> productoLocks = new HashMap<>();
    private boolean isSelected;

    public Producto(Long id, String nombre, Long categoriaId, String picture, Double precio, String fechaCreacion, Long negocioId, int stock) {
        this.id = id;
        this.nombre = nombre;
        this.categoriaId = categoriaId;
        this.picture = picture;
        this.precio = precio;
        this.fechaCreacion = fechaCreacion;
        this.negocioId = negocioId;
        this.stock = stock;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Double getPrecio() {
        return precio;
    }

    public void setPrecio(Double precio) {
        this.precio = precio;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Long getNegocioId() {
        return negocioId;
    }

    public void setNegocioId(Long negocioId) {
        this.negocioId = negocioId;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Producto producto = (Producto) o;
        return Objects.equals(id, producto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", categoriaId=" + categoriaId +
                ", picture='" + picture + '\'' +
                ", precio=" + precio +
                ", fechaCreacion='" + fechaCreacion + '\'' +
                ", negocioId=" + negocioId +
                ", stock=" + stock +
                '}';
    }

    // Otros métodos...

    // Método para intentar bloquear la compra
    public boolean intentarBloquearCompra() {
        synchronized (productoLocks) {
            ReentrantLock productoLock = productoLocks.computeIfAbsent(id, k -> new ReentrantLock());
            return productoLock.tryLock();
        }
    }

    // Método para desbloquear la compra
    public void desbloquearCompra() {
        synchronized (productoLocks) {
            ReentrantLock productoLock = productoLocks.get(id);
            if (productoLock != null) {
                productoLock.unlock();
            }
        }
    }

}


