package com.example.android.inventoryappstage2;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryappstage2.data.BookContract.BookEntry;

public class BookCursorAdapter extends CursorAdapter {

    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        TextView nameTextView = view.findViewById(R.id.book_name);
        TextView categoryTextView = view.findViewById(R.id.book_category);
        TextView priceTextView = view.findViewById(R.id.book_price);
        TextView quantityTextView = view.findViewById(R.id.book_quantity);
        Button orderButton = view.findViewById(R.id.btn_order_book);
        Button saleButton = view.findViewById(R.id.btn_book_sale);
        // Find the columns of book attributes
        int nameColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_NAME);
        int categoryColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_CATEGORY);
        int priceColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY);
        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        String bookCategory = cursor.getString(categoryColumnIndex);
        String bookPrice = cursor.getString(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);
        if (TextUtils.isEmpty(bookCategory)) {
            bookCategory = context.getString(R.string.category_unknown);
        }
        nameTextView.setText(bookName);
        categoryTextView.setText(bookCategory);
        priceTextView.setText(bookPrice);
        quantityTextView.setText(bookQuantity);
        final int position = cursor.getPosition() + 1;
        orderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cursor.moveToPosition(position - 1);
                String contactNumber = cursor.getString(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER));
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + contactNumber));
                context.startActivity(callIntent);
            }
        });
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cursor.moveToPosition(position - 1);
                int currentQuantity = cursor.getInt(cursor.getColumnIndex(BookEntry.COLUMN_BOOK_QUANTITY));
                if (currentQuantity > 0) {
                    currentQuantity = currentQuantity - 1;
                    ContentValues values = new ContentValues();
                    values.put(BookEntry.COLUMN_BOOK_QUANTITY, currentQuantity);
                    context.getContentResolver().update(BookEntry.CONTENT_URI, values,
                            BookEntry._ID + "=" + cursor.getInt(cursor.getColumnIndex(BookEntry._ID)),
                            null);
                } else {
                    Toast.makeText(context, R.string.btn_sale_out_of_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
