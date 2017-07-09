package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

import static android.content.ContentValues.TAG;
import static com.example.android.inventoryapp.R.id.price;

public class ProductCursorAdapter extends CursorRecyclerAdapter<ProductCursorAdapter.ViewHolder> {

    private InventoryActivity activity = new InventoryActivity();

    public ProductCursorAdapter(InventoryActivity context, Cursor c) {
        super(context, c);
        this.activity = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final Cursor cursor) {
        final Uri currentUri = ProductEntry.CONTENT_URI;

        // Find the columns of products attributes that we're interested in
        final int productId = cursor.getInt(cursor.getColumnIndex(ProductEntry._ID));
        int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
        int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
        int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
        int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

        // Read the product attributes from the Cursor for the current product
        String productName = cursor.getString(nameColumnIndex);
        final int quantity = cursor.getInt(quantityColumnIndex);
        String productQuantity = String.valueOf(quantity);
        String price = cursor.getString(priceColumnIndex);
        final Uri imageUri = Uri.parse(cursor.getString(imageColumnIndex));

        if (quantity == 0) {
            viewHolder.inStockTextView.setTextColor(Color.RED);
        } else {
            viewHolder.inStockTextView.setTextColor(Color.GREEN);
        }

        // Update the TextViews with the attributes for the current product
        viewHolder.nameTextView.setText(productName);
        viewHolder.quantityTextView.setText(productQuantity);
        viewHolder.priceTextView.setText("€ " + price);
        viewHolder.productImage.setImageURI(imageUri);
        Log.e("ProductCursorAdapter", "Message: " + imageUri);
        viewHolder.productImage.invalidate();
        viewHolder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.onItemClick(productId);
            }
        });
        viewHolder.sellProduct.setTag(cursor.getPosition());
        viewHolder.sellProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri itemUri = ContentUris.withAppendedId(currentUri, productId);
                buyProduct(activity, itemUri, quantity);
            }
        });
    }

    // Decrease product quantity by 1
    private void buyProduct(Context context, Uri itemUri, int currentCount) {
        int newCount = (currentCount >= 1) ? currentCount - 1 : 0;
        ContentValues values = new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, newCount);
        int numRowsUpdated = context.getContentResolver().update(itemUri, values, null, null);

        if (numRowsUpdated > 0) {
            Log.i(TAG, "Buy product successful");
        } else {
            Log.i(TAG, "Could not update buy product");
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        protected CardView cardView;
        protected TextView nameTextView;
        protected TextView inStockTextView;
        protected TextView quantityTextView;
        protected TextView priceTextView;
        protected ImageView productImage;
        protected Button sellProduct;

        public ViewHolder(View itemView) {
            super(itemView);
            cardView = (CardView) itemView.findViewById(R.id.card_view);
            nameTextView = (TextView) itemView.findViewById(R.id.name);
            inStockTextView = (TextView) itemView.findViewById(R.id.in_stock);
            quantityTextView = (TextView) itemView.findViewById(R.id.quantity);
            priceTextView = (TextView) itemView.findViewById(price);
            productImage = (ImageView) itemView.findViewById(R.id.image);
            sellProduct = (Button) itemView.findViewById(R.id.soldProduct);
        }
    }

}
