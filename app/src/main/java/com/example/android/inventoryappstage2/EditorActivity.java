package com.example.android.inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.BookContract.BookEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final int EXISTING_BOOK_LOADER = 0;
    private final long REPEAT_DELAY = 50;
    private Uri mCurrentBookUri;
    private EditText mNameEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private EditText mSupplierNameEditText;
    private EditText mSupplierPhoneNumberEditText;
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private Handler mRepeatUpdateHandler = new Handler();
    private Spinner mCategorySpinner;
    private String mCategory;
    private boolean mBookHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mBookHasChanged = true;
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        Intent intent = getIntent();
        mCurrentBookUri = intent.getData();
        if (mCurrentBookUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_book));
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_book));
            getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);
        }
        mNameEditText = findViewById(R.id.edit_book_name);
        mCategorySpinner = findViewById(R.id.spinner_category);
        mPriceEditText = findViewById(R.id.edit_book_price);
        mQuantityEditText = findViewById(R.id.edit_book_quantity);
        mSupplierNameEditText = findViewById(R.id.edit_book_supplier_name);
        mSupplierPhoneNumberEditText = findViewById(R.id.edit_book_supplier_phone_number);
        ImageButton mIncreaseQuantity = findViewById(R.id.increase_quantity);
        ImageButton mDecreaseQuantity = findViewById(R.id.decrease_quantity);
        // Method to Setup the dropdown spinner for book category.
        setupSpinner();
        // OnTouchListeners to determine if the user has entered of modified the data.
        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierNameEditText.setOnTouchListener(mTouchListener);
        mSupplierPhoneNumberEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);
        // Implementing functionality to increase and decrease quantity on button clicks.
        class RepetitiveUpdater implements Runnable {
            @Override
            public void run() {
                if (mAutoIncrement) {
                    increment();
                    mRepeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                } else if (mAutoDecrement) {
                    decrement();
                    mRepeatUpdateHandler.postDelayed(new RepetitiveUpdater(), REPEAT_DELAY);
                }
            }
        }
        mIncreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                increment();
            }
        });
        mIncreaseQuantity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoIncrement = true;
                mRepeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        mIncreaseQuantity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });
        mDecreaseQuantity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                decrement();
            }
        });
        mDecreaseQuantity.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                mAutoDecrement = true;
                mRepeatUpdateHandler.post(new RepetitiveUpdater());
                return false;
            }
        });
        mDecreaseQuantity.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP || motionEvent.getAction() == MotionEvent.ACTION_CANCEL && mAutoDecrement) {
                    mAutoDecrement = false;
                }
                return false;
            }
        });
    }

    private void increment() {
        if (isQuantityEntered()) {
            int quantity = Integer.parseInt(mQuantityEditText.getText().toString());
            if (quantity < 100) {
                quantity++;
                mQuantityEditText.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(EditorActivity.this, getString(R.string.maximum_quantity_limit), Toast.LENGTH_SHORT).show();
            }
        } else {
            String alertMessage = getString(R.string.quantity_numeric_value);
            buttonAlerts(alertMessage);
        }
    }

    private void decrement() {
        if (isQuantityEntered()) {
            int quantity = Integer.parseInt(mQuantityEditText.getText().toString());
            if (quantity > 0) {
                quantity--;
                mQuantityEditText.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(EditorActivity.this, getString(R.string.negative_quantity), Toast.LENGTH_SHORT).show();
            }
        } else {
            String alertMessage = getString(R.string.quantity_numeric_value);
            buttonAlerts(alertMessage);
        }
    }

    public void phoneCall(View v) {
        if (isPhoneNumber()) {
            String contactNumber = mSupplierPhoneNumberEditText.getText().toString();
            Intent callIntent = new Intent(Intent.ACTION_DIAL);
            callIntent.setData(Uri.parse("tel:" + contactNumber));
            startActivity(callIntent);
        }
    }

    private boolean isPhoneNumber() {
        String supplierPhoneNumber = mSupplierPhoneNumberEditText.getText().toString();
        if (!TextUtils.isEmpty(supplierPhoneNumber)) {
            try {
                Long.parseLong(supplierPhoneNumber);
                if (supplierPhoneNumber.length() != 10) {
                    buttonAlerts("Please enter valid supplier phone number");
                    return false;
                } else {
                    return true;
                }
            } catch (NumberFormatException e) {
                buttonAlerts("Please enter valid supplier phone number");
                return false;
            }
        } else {
            buttonAlerts("Please enter supplier phone number");
            return false;
        }
    }

    private void buttonAlerts(String alert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alert);
        builder.setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean isQuantityEntered() {
        String quantityString = mQuantityEditText.getText().toString();
        if (TextUtils.isEmpty(quantityString)) {
            mQuantityEditText.setText("0");
            return true;
        } else {
            try {
                Integer.parseInt(quantityString);
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
    }

    private void showAlertDialog(String alert) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(alert);
        builder.setPositiveButton(R.string.alert_dialog_discard, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                NavUtils.navigateUpFromSameTask(EditorActivity.this);
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private boolean saveBook() {
        String nameString = mNameEditText.getText().toString().trim();
        String priceString = mPriceEditText.getText().toString().trim();
        String quantityString = mQuantityEditText.getText().toString().trim();
        String supplierNameString = mSupplierNameEditText.getText().toString().trim();
        String supplierPhoneNumberString = mSupplierPhoneNumberEditText.getText().toString().trim();
        if (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(priceString) || TextUtils.isEmpty(quantityString) ||
                TextUtils.isEmpty(supplierNameString) || TextUtils.isEmpty(supplierPhoneNumberString)) {
            Toast.makeText(this, R.string.empty_data_toast_message, Toast.LENGTH_LONG).show();
            return false;
        } else if (inspectUserInputs(priceString, quantityString, supplierPhoneNumberString)) {
            showAlertDialog(getString(R.string.alert_dialog_invalid_data));
            return false;
        } else if (Integer.parseInt(priceString) < 0 || Integer.parseInt(quantityString) < 0 ||
                Integer.parseInt(quantityString) > 100 || supplierPhoneNumberString.length() != 10) {
            String alertMessage = setAlertMessage(priceString, quantityString, supplierPhoneNumberString);
            showAlertDialog(alertMessage);
            return false;
        } else {
            int bookPrice = Integer.parseInt(priceString);
            long phoneNumber = Long.parseLong(supplierPhoneNumberString);
            ContentValues values = new ContentValues();
            values.put(BookEntry.COLUMN_BOOK_NAME, nameString);
            values.put(BookEntry.COLUMN_BOOK_CATEGORY, mCategory);
            values.put(BookEntry.COLUMN_BOOK_PRICE, bookPrice);
            // If the quantity is not entered by the user, use 0 by default.
            int bookQuantity = 0;
            if (!TextUtils.isEmpty(quantityString)) {
                bookQuantity = Integer.parseInt(quantityString);
            }
            values.put(BookEntry.COLUMN_BOOK_QUANTITY, bookQuantity);
            values.put(BookEntry.COLUMN_BOOK_SUPPLIER_NAME, supplierNameString);
            values.put(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER, phoneNumber);
            // Determine if this is a new or existing book by checking if mCurrentBookUri is null or not
            if (mCurrentBookUri == null) {
                Uri newUri = getContentResolver().insert(BookEntry.CONTENT_URI, values);
                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                int rowsAffected = getContentResolver().update(mCurrentBookUri, values, null, null);
                if (rowsAffected == 0) {
                    Toast.makeText(this, getString(R.string.editor_update_book_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_book_successful),
                            Toast.LENGTH_SHORT).show();
                }
            }
            return true;
        }
    }

    private boolean inspectUserInputs(String priceString, String quantityString, String supplierPhoneNumberString) {
        try {
            Integer.parseInt(priceString);
            Integer.parseInt(quantityString);
            Long.parseLong(supplierPhoneNumberString);
            return false;
        } catch (NumberFormatException e) {
            return true;
        }
    }

    private String setAlertMessage(String price, String quantity, String phoneNumber) {
        String alertMessage;
        boolean negativePrice = Integer.parseInt(price) < 0;
        boolean negativeQuantity = Integer.parseInt(quantity) < 0;
        boolean higherQuantity = Integer.parseInt(quantity) > 100;
        boolean wrongPhoneNumber = phoneNumber.length() != 10;
        String negativePriceAlert = getString(R.string.negative_price);
        String negativeQuantityAlert = getString(R.string.negative_quantity);
        String higherQuantityAlert = getString(R.string.maximum_quantity_limit);
        String invalidPhoneNumberAlert = getString(R.string.invalid_phone_number);
        if (higherQuantity) {
            if (negativePrice && wrongPhoneNumber) {
                alertMessage = negativePriceAlert + "\n" + higherQuantityAlert + "\n" + invalidPhoneNumberAlert;
            } else if (negativePrice) {
                alertMessage = negativePriceAlert + "\n" + higherQuantityAlert;
            } else if (wrongPhoneNumber) {
                alertMessage = higherQuantityAlert + "\n" + invalidPhoneNumberAlert;
            } else {
                alertMessage = higherQuantityAlert;
            }
        } else if (negativeQuantity) {
            if (negativePrice && wrongPhoneNumber) {
                alertMessage = negativePriceAlert + "\n" + negativeQuantityAlert + "\n" + invalidPhoneNumberAlert;
            } else if (negativePrice) {
                alertMessage = negativePriceAlert + "\n" + negativeQuantityAlert;
            } else if (wrongPhoneNumber) {
                alertMessage = negativeQuantityAlert + "\n" + invalidPhoneNumberAlert;
            } else {
                alertMessage = negativeQuantityAlert;
            }
        } else {
            if (negativePrice && wrongPhoneNumber) {
                alertMessage = negativePriceAlert + "\n" + invalidPhoneNumberAlert;
            } else if (negativePrice) {
                alertMessage = negativePriceAlert;
            } else {
                alertMessage = invalidPhoneNumberAlert;
            }
        }
        return alertMessage;
    }

    private void setupSpinner() {
        mCategory = getString(R.string.category_unknown);
        ArrayAdapter genderSpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_gender_options, android.R.layout.simple_spinner_item);
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        mCategorySpinner.setAdapter(genderSpinnerAdapter);
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCategory = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = getString(R.string.category_unknown);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If user is inserting a book, hide the Delete menu item.
        if (mCurrentBookUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                if (saveBook()) {
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mBookHasChanged) {
                    finish();
                    return true;
                }
                // If there are unsaved changes, show alert dialog.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (!mBookHasChanged) {
            super.onBackPressed();
            return;
        }
        // If there are unsaved changes, show alert dialog.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteBook() {
        if (mCurrentBookUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentBookUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private void setCategorySpinnerSelection(String category) {
        switch (category) {
            case "Novel":
                mCategorySpinner.setSelection(1);
                break;
            case "Biography":
                mCategorySpinner.setSelection(2);
                break;
            case "History":
                mCategorySpinner.setSelection(3);
                break;
            case "Health":
                mCategorySpinner.setSelection(4);
                break;
            case "Academics":
                mCategorySpinner.setSelection(5);
                break;
            case "Science":
                mCategorySpinner.setSelection(6);
                break;
            case "Technology":
                mCategorySpinner.setSelection(7);
                break;
            case "Politics":
                mCategorySpinner.setSelection(8);
                break;
            case "Sports":
                mCategorySpinner.setSelection(9);
                break;
            default:
                mCategorySpinner.setSelection(0);
                break;
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_CATEGORY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER
        };
        return new CursorLoader(this,
                mCurrentBookUri,
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
            int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
            int categoryColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_CATEGORY);
            int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
            int supplierNameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int supplierPhoneNumberColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);
            String name = cursor.getString(nameColumnIndex);
            String category = cursor.getString(categoryColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);
            String supplierName = cursor.getString(supplierNameColumnIndex);
            String supplierPhoneNumber = cursor.getString(supplierPhoneNumberColumnIndex);
            mNameEditText.setText(name);
            mPriceEditText.setText(price);
            mQuantityEditText.setText(quantity);
            mSupplierNameEditText.setText(supplierName);
            mSupplierPhoneNumberEditText.setText(supplierPhoneNumber);
            // method to select the category from the spinner.
            setCategorySpinnerSelection(category);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mSupplierNameEditText.setText("");
        mSupplierPhoneNumberEditText.setText("");
        mCategorySpinner.setSelection(0); // Select "Unknown" category
    }
}