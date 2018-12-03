package com.example.android.inventoryappstage2;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryappstage2.data.BookContract.BookEntry;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BOOK_LOADER = 0;
    private ListView bookListView;
    private BookCursorAdapter mCursorAdapter;
    private String categoryTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        bookListView = findViewById(R.id.book_list_view);
        View emptyView = findViewById(R.id.empty_view);
        bookListView.setEmptyView(emptyView);
        mCursorAdapter = new BookCursorAdapter(this, null);
        bookListView.setAdapter(mCursorAdapter);
        bookListView.setDivider(getResources().getDrawable(R.drawable.listitem_divider));
        bookListView.setDividerHeight(5);
        bookListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri currentBookUri = ContentUris.withAppendedId(BookEntry.CONTENT_URI, id);
                intent.setData(currentBookUri);
                startActivity(intent);
            }
        });
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString("category", categoryTitle);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        categoryTitle = savedInstanceState.getString("category");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(categoryTitle) || categoryTitle.equals("All")) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.actionbar_title);
            }
        } else {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(categoryTitle);
            }
        }
        getLoaderManager().initLoader(BOOK_LOADER, null, this);
    }

    private void displayData(String whereClause, String[] whereArgs) {
        String[] projection = new String[]{
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_CATEGORY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER
        };
        Cursor cursor = getContentResolver().query(
                BookEntry.CONTENT_URI,
                projection,
                whereClause,
                whereArgs,
                null);
        mCursorAdapter = new BookCursorAdapter(this, cursor);
        bookListView.setAdapter(mCursorAdapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        categoryTitle = (String) item.getTitle();
        String whereClause = "category=?";
        String[] whereArgs;
        if (id == R.id.category_all) {
            whereClause = null;
            whereArgs = null;
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.actionbar_title);
            }
        } else {
            whereArgs = new String[]{categoryTitle};
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(categoryTitle);
            }
        }
        displayData(whereClause, whereArgs);
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String whereClause;
                String[] whereArgs;
                if (TextUtils.isEmpty(categoryTitle) || categoryTitle.equals("All")) {
                    whereClause = "productName LIKE ?";
                    whereArgs = new String[]{newText + "%"};
                } else {
                    whereClause = "productName LIKE ? AND category=?";
                    whereArgs = new String[]{newText + "%", categoryTitle};
                }
                displayData(whereClause, whereArgs);
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_all_entries) {
            String whereClause;
            String[] whereArgs;
            if (TextUtils.isEmpty(categoryTitle) || categoryTitle.equals("All")) {
                whereClause = null;
                whereArgs = null;
            } else {
                whereClause = "category=?";
                whereArgs = new String[]{categoryTitle};
            }
            deleteAllBooks(whereClause, whereArgs);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Helper method to delete all books in the database.
    private void deleteAllBooks(String whereClause, String[] whereArgs) {
        int rowsDeleted = getContentResolver().delete(BookEntry.CONTENT_URI, whereClause, whereArgs);
        Log.v(this.getClass().getName(), rowsDeleted + " rows deleted from bookStore database");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                BookEntry._ID,
                BookEntry.COLUMN_BOOK_NAME,
                BookEntry.COLUMN_BOOK_CATEGORY,
                BookEntry.COLUMN_BOOK_PRICE,
                BookEntry.COLUMN_BOOK_QUANTITY,
                BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER};
        return new CursorLoader(this,
                BookEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
        String whereClause;
        String[] whereArgs;
        if (TextUtils.isEmpty(categoryTitle) || categoryTitle.equals("All")) {
            whereClause = null;
            whereArgs = null;
        } else {
            whereClause = "category=?";
            whereArgs = new String[]{categoryTitle};
        }
        displayData(whereClause, whereArgs);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}
