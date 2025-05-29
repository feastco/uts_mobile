package com.example.uts_a22202303006.profile;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class ShippingAddress implements Serializable {

    private int id;

    @SerializedName("user_id")
    private int userId;

    @SerializedName("nama_penerima")
    private String namaPenerima;

    @SerializedName("nomor_telepon")
    private String nomorTelepon;

    @SerializedName("alamat_lengkap")
    private String alamatLengkap;

    @SerializedName("province_id")
    private int provinceId;

    @SerializedName("province")
    private String province;

    @SerializedName("city_id")
    private int cityId;

    @SerializedName("city")
    private String city;

    @SerializedName("kode_pos")
    private String kodePos;

    @SerializedName("is_default")
    private int isDefault;

    // Getters
    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getNamaPenerima() {
        return namaPenerima;
    }

    public String getNomorTelepon() {
        return nomorTelepon;
    }

    public String getAlamatLengkap() {
        return alamatLengkap;
    }

    public int getProvinceId() {
        return provinceId;
    }

    public String getProvince() {
        return province;
    }

    public int getCityId() {
        return cityId;
    }

    public String getCity() {
        return city;
    }

    public String getKodePos() {
        return kodePos;
    }

    public int getIsDefault() {
        return isDefault;
    }

    // Setters
    public void setId(int id) {
        this.id = id;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setNamaPenerima(String namaPenerima) {
        this.namaPenerima = namaPenerima;
    }

    public void setNomorTelepon(String nomorTelepon) {
        this.nomorTelepon = nomorTelepon;
    }

    public void setAlamatLengkap(String alamatLengkap) {
        this.alamatLengkap = alamatLengkap;
    }

    public void setProvinceId(int provinceId) {
        this.provinceId = provinceId;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setKodePos(String kodePos) {
        this.kodePos = kodePos;
    }

    public void setIsDefault(int isDefault) {
        this.isDefault = isDefault;
    }

    // Utility methods
    public boolean isDefault() {
        return isDefault == 1;
    }

    public int getIsDefaultAsInt() {
        return isDefault;
    }

    public String getFullAddress() {
        StringBuilder address = new StringBuilder();

        if (alamatLengkap != null && !alamatLengkap.isEmpty()) {
            address.append(alamatLengkap);
        }

        if (city != null && !city.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(city);
        }

        if (province != null && !province.isEmpty()) {
            if (address.length() > 0) address.append(", ");
            address.append(province);
        }

        if (kodePos != null && !kodePos.isEmpty()) {
            if (address.length() > 0) address.append(" ");
            address.append(kodePos);
        }

        return address.toString();
    }
}