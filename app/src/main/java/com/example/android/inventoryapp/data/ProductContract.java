package com.example.android.inventoryapp.data;


import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class ProductContract {

    // Name of the Content Provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    // Base Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    // Possible path
    public static final String PATH_PRODUCTS = "products";

    // To prevent someone from accidentally instantiating the contract class
    private ProductContract() {
    }

    public static final class ProductEntry implements BaseColumns {

        //The content URI to access the product data in the provider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);

        // The MIME type of the CONTENT URI for a list of product
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // The MIME type of the CONTENT URI for a single product
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        // Name of database table
        public final static String TABLE_NAME = "products";

        // unique ID number for product
        public final static String _ID = BaseColumns._ID;

        // Product's fields
        public final static String COLUMN_PRODUCT_NAME = "name";
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        public final static String COLUMN_PRODUCT_PRICE = "price";
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier";
        public final static String COLUMN_PRODUCT_SUPPLIER_EMAIL = "email";
        public final static String COLUMN_PRODUCT_IMAGE = "image";
    }
}
