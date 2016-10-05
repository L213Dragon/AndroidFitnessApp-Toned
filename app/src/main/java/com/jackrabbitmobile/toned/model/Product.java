package com.jackrabbitmobile.toned.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Wrapper for Google Billing v3 products (subscriptions in this case)
 * {
 * "productId":"unlimited_access.yearly",
 * "type":"subs",
 * "price":"$39.99",
 * "price_amount_micros":39990000,
 * "price_currency_code":"USD",
 * "title":"Unlimited Access Yearly Subscription (Toned Home Workouts)",
 * "description":"Switch it up! New workouts and exercise videos added weekly!"
 * }
 */
public class Product implements Parcelable {

    public static final Creator<Product> CREATOR = new Creator<Product>() {
        @Override
        public Product createFromParcel(Parcel in) {
            return new Product(in);
        }

        @Override
        public Product[] newArray(int size) {
            return new Product[size];
        }
    };
    public String productId;
    public String type;
    public String price;
    public long priceAmountMicros;
    public String priceCurrencyCode;
    public String title;

    protected Product(Parcel in) {
        this.productId = in.readString();
        this.type = in.readString();
        this.price = in.readString();
        this.priceAmountMicros = in.readLong();
        this.priceCurrencyCode = in.readString();
        this.title = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(productId);
        dest.writeString(type);
        dest.writeString(price);
        dest.writeLong(priceAmountMicros);
        dest.writeString(priceCurrencyCode);
        dest.writeString(title);
    }
}
