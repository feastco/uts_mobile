package com.example.uts_a22202303006.product;

import com.google.gson.annotations.SerializedName;

// Model untuk merepresentasikan data produk
public class Product {
    private int qty = 1; // Jumlah produk dalam keranjang

    // Getter dan setter untuk jumlah produk
    public int getQty() { return qty; }
    public void setQty(int qty) { this.qty = qty; }

    @SerializedName("kode") // Kode produk
    private String kode;

    @SerializedName("merk") // Merk produk
    private String merk;

    @SerializedName("kategori") // Kategori produk
    private String kategori;

    @SerializedName("hargabeli") // Harga beli produk
    private Double hargaBeli;

    @SerializedName("hargajual") // Harga jual produk
    private Double hargaJual;

    @SerializedName("stok") // Stok produk
    private Integer stok;

    @SerializedName("foto") // URL foto produk
    private String foto;

    @SerializedName("deskripsi") // Deskripsi produk
    private String deskripsi;

    @SerializedName("diskonjual") // Diskon pada harga jual
    private Integer diskonJual;

    @SerializedName("diskonbeli") // Diskon pada harga beli
    private Integer diskonBeli;

    @SerializedName("hargapokok") // Harga pokok produk
    private Double hargaPokok;

    @SerializedName("visit") // Match the field name from your database
    private Integer visit;

    // Getter untuk properti produk
    public String getKode() { return kode; }
    public String getMerk() { return merk; }
    public String getKategori() { return kategori; }
    public Double getHargaBeli() { return hargaBeli != null ? hargaBeli : 0; }
    public Double getHargaJual() { return hargaJual != null ? hargaJual : 0; }
    public int getStok() { return stok; }
    public String getFoto() { return foto; }
    public String getDeskripsi() { return deskripsi; }
    public int getDiskonJual() { return diskonJual != null ? diskonJual : 0; }
    public int getDiskonBeli() { return diskonBeli != null ? diskonBeli : 0; }
    public Double getHargapokok() { return hargaPokok != null ? hargaPokok : 0; }

    // Getter method
    public int getVisitCount() {
        return visit != null ? visit : 0;
    }

    // Setter method
    public void setVisitCount(int visitCount) {
        this.visit = visitCount;
    }
}
