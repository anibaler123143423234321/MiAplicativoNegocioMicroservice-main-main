package com.dagnerchuman.miaplicativonegociomicroservice.entity;


public class DniResponse {
        boolean success;
        String dni;
        String nombres;
        String apellidoPaterno;
        String apellidoMaterno;
        String codVerifica;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDni() {
        return dni;
    }

    public void setDni(String dni) {
        this.dni = dni;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getApellidoPaterno() {
        return apellidoPaterno;
    }

    public void setApellidoPaterno(String apellidoPaterno) {
        this.apellidoPaterno = apellidoPaterno;
    }

    public String getApellidoMaterno() {
        return apellidoMaterno;
    }

    public void setApellidoMaterno(String apellidoMaterno) {
        this.apellidoMaterno = apellidoMaterno;
    }

    public String getCodVerifica() {
        return codVerifica;
    }

    public void setCodVerifica(String codVerifica) {
        this.codVerifica = codVerifica;
    }
}
