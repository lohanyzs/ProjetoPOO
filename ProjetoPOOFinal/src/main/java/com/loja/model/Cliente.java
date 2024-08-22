package com.loja.model;

public class Cliente {
    private int id;
    private String nome;
    private String email;
    private String celular;
    private String clienteDesde;
    private String endereco;

    public Cliente(int id, String nome, String email, String celular, String clienteDesde, String endereco) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.celular = celular;
        this.clienteDesde = clienteDesde;
        this.endereco = endereco;
    }

    public Cliente() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCelular() {
        return celular;
    }

    public void setCelular(String celular) {
        this.celular = celular;
    }

    public String getClienteDesde() {
        return clienteDesde;
    }

    public void setClienteDesde(String clienteDesde) {
        this.clienteDesde = clienteDesde;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }
}