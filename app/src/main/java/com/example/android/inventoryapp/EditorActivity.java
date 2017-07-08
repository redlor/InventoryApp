package com.example.android.inventoryapp;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ProductContract.ProductEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int EXISTING_PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;
    private Uri mImageUri;
    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private TextView mImageText;
    private ImageView mProductImage;
    private Button mPlusButton;
    private Button mMinusButton;

    private boolean mProductHasChanged;

    int quantity;

private View.OnTouchListener mTouchListener =  new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mProductHasChanged = true;
        return false;
    }
};

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_editor);

    // Check the intent used to reach the activity
    final Intent intent = getIntent();
    mCurrentProductUri = intent.getData();

    mNameEditText = (EditText) findViewById(R.id.edit_product_name);
    mQuantityEditText = (EditText) findViewById(R.id.edit_product_quantity);
    mPriceEditText = (EditText) findViewById(R.id.edit_product_price);
    mImageText = (TextView) findViewById(R.id.add_image_text);
    mProductImage = (ImageView) findViewById(R.id.product_image);
    mPlusButton = (Button) findViewById(R.id.plus_button);
    mMinusButton = (Button) findViewById(R.id.minus_button);

    mPlusButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            quantity++;
            mQuantityEditText.setText(String.valueOf(quantity));
        }
    });

    mMinusButton.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (quantity < 1) {
                Toast.makeText(getApplicationContext(), "Can't set a negative quantity", Toast.LENGTH_SHORT).show();
            } else {
                quantity--;
                mQuantityEditText.setText(String.valueOf(quantity));
            }
        }
    });

    // If the intent does not have data, then it is a new product insertion
    if (mCurrentProductUri == null) {
        setTitle(getString(R.string.editor_activity_title_new_product));
        mImageText.setText(getString(R.string.add_image_text));
        // Hide the option menu
        invalidateOptionsMenu();
    } else {
        setTitle(getString(R.string.editor_activity_title_edit_product));
        mImageText.setText(getString(R.string.change_image_text));
        // Initialize the loader to read product data from the database
        getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
    }

    // Setup OnTouchListeners on all the input fields
    mNameEditText.setOnTouchListener(mTouchListener);
    mQuantityEditText.setOnTouchListener(mTouchListener);
    mPriceEditText.setOnTouchListener(mTouchListener);
    mPlusButton.setOnTouchListener(mTouchListener);
    mMinusButton.setOnTouchListener(mTouchListener);
    mProductImage.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            attemptOpenSelector();
            mProductHasChanged = true;
        }
    });

}

    public void attemptOpenSelector() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            return;
        }
        openImageSelector();
    }

    public void openImageSelector() {
        Intent selectionIntent;
        if (Build.VERSION.SDK_INT <19) {
            selectionIntent =  new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            selectionIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            selectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        selectionIntent.setType(getString(R.string.intentType));
        startActivityForResult(Intent.createChooser(selectionIntent, getString(R.string.selectImage)), 0);
    }

     @Override
     public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResult) {
         switch (requestCode) {
             case 1:
                 if (grantResult.length > 0 && grantResult[0] == PackageManager.PERMISSION_GRANTED) {
                     openImageSelector();
                 }
         }
     }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                mImageUri = data.getData();
                mProductImage.setImageURI(mImageUri);
                mProductImage.invalidate();
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClicklistener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes);
        builder.setPositiveButton(R.string.cancel_button, discardButtonClicklistener);
        builder.setNegativeButton(R.string.back_to_edit_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder =  new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Delete product
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem cancelItem = menu.findItem(R.id.action_delete);
            cancelItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                // Insert product in the database
                saveProduct();
                finish();
                return true;
            case R.id.action_delete:
                // Call delete dialog
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_order:
                orderProduct();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                // If there are changes, show a Dialog
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void orderProduct() {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Order " + mNameEditText.getText().toString().trim());
        startActivity(emailIntent);
    }

    private void saveProduct() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        String nameString = mNameEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();


        // Check id it is a new product
        if (mCurrentProductUri == null &&
            TextUtils.isEmpty(nameString) && TextUtils.isEmpty(quantityString) &&
            TextUtils.isEmpty(priceString)) {
            return;
        }
        // Create a ContentValues object
        ContentValues values =  new ContentValues();
        values.put(ProductEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(ProductEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
        values.put(ProductEntry.COLUMN_PRODUCT_PRICE, priceString);

        if (mImageUri == null) {
            Toast.makeText(this, getString(R.string.picture_check), Toast.LENGTH_SHORT).show();
        }
        values.put(ProductEntry.COLUMN_PRODUCT_IMAGE, mImageUri.toString());

        // New product
        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(ProductEntry.CONTENT_URI, values);

            if (newUri == null) {
                Toast.makeText(this, getString(R.string.editor_insert_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_insert_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // Existing product
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_product_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_product_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

public void deleteProduct() {
    if (mCurrentProductUri != null) {
        int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.editor_delete_product_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the delete was successful and we can display a toast.
            Toast.makeText(this, getString(R.string.editor_delete_product_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }
    finish();
}

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = {
                ProductEntry._ID,
                ProductEntry.COLUMN_PRODUCT_NAME,
                ProductEntry.COLUMN_PRODUCT_QUANTITY,
                ProductEntry.COLUMN_PRODUCT_PRICE,
                ProductEntry.COLUMN_PRODUCT_IMAGE };

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            // Find the columns of product attributes
            int nameColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_NAME);
            int quantityColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_PRICE);
            int imageColumnIndex = cursor.getColumnIndex(ProductEntry.COLUMN_PRODUCT_IMAGE);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            quantity = cursor.getInt(quantityColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String imageUriString = cursor.getString(imageColumnIndex);

            // Update the views on the screen with the values from the database
            mNameEditText.setText(name);
            mQuantityEditText.setText(Integer.toString(quantity));
            mPriceEditText.setText(price);
            Uri imageUri = Uri.parse(imageUriString);
            mProductImage.setImageURI(imageUri);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");

    }
}
