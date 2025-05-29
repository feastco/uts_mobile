package com.example.uts_a22202303006.api;

import com.example.uts_a22202303006.product.ProductImageResponse;
import com.example.uts_a22202303006.product.ProductResponse;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface RegisterAPI {
    @FormUrlEncoded
    @POST("get_login.php")
    Call<ResponseBody> login(
            @Field("identifier") String identifier,
            @Field("password") String password
    );

    @FormUrlEncoded
    @POST("post_register.php")
    Call<ResponseBody> register(
            @Field("email") String email,
            @Field("nama") String nama,
            @Field("username") String username,
            @Field("password") String password
    );

    @GET("get_profile.php")
    Call<ResponseBody> getProfile(
            @Query("username") String username
    );

    @FormUrlEncoded
    @POST("post_profile.php")
    Call<ResponseBody> updateProfile(
            @Field("nama") String nama,
            @Field("alamat") String alamat,
            @Field("kota") String kota,
            @Field("provinsi") String provinsi,
            @Field("telp") String telp,
            @Field("kodepos") String kodepos,
            @Field("foto") String foto,
            @Field("email") String email,
            @Field("username") String username
    );

    @GET("get_products.php")
    Call<ProductResponse> getProducts(@Query("kategori") String kategori,
                                      @Query("search") String search);

    @GET("update_visit.php")  // Change from POST to GET
    Call<ResponseBody> updateVisitCount(@Query("kode") String kode);  // Use Query instead of Field


    @Multipart
    @POST("upload_image.php")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part foto,
            @Part("username") RequestBody username
    );

    @GET("get_product_images.php")
    Call<ProductImageResponse> getProductImages(@Query("product_code") String productCode);

    @GET("get_provinces.php")
    Call<ResponseBody> getProvinces();

    @GET("get_cities.php")
    Call<ResponseBody> getCities(@Query("province_id") int provinceId);

    @FormUrlEncoded
    @POST("get_shipping_cost.php")
    Call<ResponseBody> getShippingCost(
            @Field("origin") int originCityId,
            @Field("destination") int destinationCityId,
            @Field("weight") int weightInGrams,
            @Field("courier") String courier
    );

    @FormUrlEncoded
    @POST("checkout.php")
    Call<ResponseBody> processCheckout(
            @Field("user_id") int userId,
            @Field("shipping_address_id") int addressId,
            @Field("total_product_amount") double totalProductAmount,
            @Field("shipping_cost") double shippingCost,
            @Field("grand_total") double grandTotal,
            @Field("courier") String courier,
            @Field("courier_service") String courierService,
            @Field("products") String cartJson
    );

    @GET("get_shipping_address.php")
    Call<ResponseBody> getShippingAddresses(@Query("user_id") int userId);

    @FormUrlEncoded
    @POST("post_shipping_address.php")
    Call<ResponseBody> addShippingAddress(
            @Field("user_id") int userId,
            @Field("nama_penerima") String recipientName,
            @Field("nomor_telepon") String phoneNumber,
            @Field("alamat_lengkap") String address,
            @Field("province_id") int provinceId,
            @Field("province") String provinceName,
            @Field("city_id") int cityId,
            @Field("city") String cityName,
            @Field("kode_pos") String postalCode
    );

    @FormUrlEncoded
    @POST("delete_shipping_address.php")
    Call<ResponseBody> deleteShippingAddress(
            @Field("id") int addressId,
            @Field("user_id") int userId
    );

    @FormUrlEncoded
    @POST("set_default_shipping_address.php")
    Call<ResponseBody> setDefaultShippingAddress(
            @Field("id") int addressId,
            @Field("user_id") int userId
    );

    @FormUrlEncoded
    @POST("update_shipping_address.php")
    Call<ResponseBody> updateShippingAddress(
            @Field("id") int id,
            @Field("user_id") int userId,
            @Field("nama_penerima") String namaPenerima,
            @Field("nomor_telepon") String nomorTelepon,
            @Field("alamat_lengkap") String alamatLengkap,
            @Field("province_id") int provinceId,
            @Field("province") String province,
            @Field("city_id") int cityId,
            @Field("city") String city,
            @Field("kode_pos") String kodePos,
            @Field("is_default") int isDefault
    );
}
