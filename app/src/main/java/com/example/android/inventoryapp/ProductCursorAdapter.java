package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.R.attr.id;
import static com.example.android.inventoryapp.R.id.price;

public class ProductCursorAdapter extends CursorAdapter {

    private InventoryActivity invAct = new InventoryActivity();

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);

    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);
        TextView priceTextView = (TextView) view.findViewById(price);
        ImageView productImage = (ImageView) view.findViewById(R.id.product_image);
        Button sellProduct = (Button) view.findViewById(R.id.soldProduct);

        // Find the columns of products attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        String productQuantity = String.valueOf(quantity);
        String price = cursor.getString(priceColumnIndex);
        Uri imageUri = Uri.parse(cursor.getString(imageColumnIndex));


        // Update the TextViews with the attributes for the current product
        nameTextView.setText(productName);
        quantityTextView.setText(productQuantity);
        priceTextView.setText(price);
        productImage.setImageURI(imageUri);
        productImage.invalidate();

        sellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quantity > 0) {
                    invAct.sellProduct(id, quantity);
                } else {
                    Toast.makeText(invAct, "Quantity wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
