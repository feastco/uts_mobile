package com.example.uts_a22202303006.product;

import com.google.gson.annotations.SerializedName;

public class Product {
    private int qty = 1;

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    @SerializedName("kode")
    private String kode;

    @SerializedName("merk")
    private String merk;

    @SerializedName("kategori")
    private String kategori;

    @SerializedName("hargabeli")
    private Double hargaBeli;

    @SerializedName("hargajual")
    private Double hargaJual;

    @SerializedName("stok")
    private Integer stok;

    @SerializedName("foto")
    private String foto;

    @SerializedName("deskripsi")
    private String deskripsi;

    @SerializedName("diskonjual")
    private Integer diskonJual;

    @SerializedName("diskonbeli")
    private Integer diskonBeli;

    @SerializedName("hargapokok")
    private Double hargaPokok;

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
}
