package com.teamltt.carcare.activity;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.teamltt.carcare.R;
import com.teamltt.carcare.database.contract.ResponseContract;
import com.teamltt.carcare.fragment.MyObdResponseRecyclerViewAdapter;
import com.teamltt.carcare.fragment.ObdResponseFragment;
import com.teamltt.carcare.fragment.SimpleDividerItemDecoration;
import com.teamltt.carcare.model.ObdContent;

public class HomeActivity extends AppCompatActivity  implements ObdResponseFragment.OnListFragmentInteractionListener {

    /**
     * A writable database connection
     */
    SQLiteDatabase db;
    // Used to keep track of the items in the RecyclerView
    private RecyclerView.Adapter responseListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        Intent intent = new Intent(this, ObdBluetoothService.class);
        // Stop any existing services, we don't need more than one running
        stopService(intent); // is this immediate?
        // Now start the new service
        startService(intent);

        // Set up the list for responses
        responseListAdapter = new MyObdResponseRecyclerViewAdapter(ObdContent.ITEMS, this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.obd_reponse_list);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(true);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
            recyclerView.setAdapter(responseListAdapter);
        }
    }

    // This should be called somewhere so we can read the data into a graph
    AsyncTask<Void, String, Void> readTask = new AsyncTask<Void, String, Void>() {
        @Override
        protected Void doInBackground(Void... voids) {
            // Read from the database
            String[] projection = {
                    ResponseContract.ResponseEntry.COLUMN_NAME_NAME,
                    ResponseContract.ResponseEntry.COLUMN_NAME_VALUE,
            };
            String sortOrder =
                    ResponseContract.ResponseEntry.COLUMN_NAME_TIMESTAMP + " ASC";

            Cursor cursor = db.query(
                    ResponseContract.ResponseEntry.TABLE_NAME,                     // The table to query
                    projection,                               // The columns to return
                    null,                                // The columns for the WHERE clause
                    null,                            // The values for the WHERE clause
                    null,                                     // don't group the rows
                    null,                                     // don't filter by row groups
                    sortOrder                                 // The sort order
            );

            while(cursor.moveToNext()) {
                byte[] commandNameBytes = cursor.getBlob(
                        cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_NAME));
                String commandName = new String(commandNameBytes);
                byte[] valueBytes = cursor.getBlob(cursor.getColumnIndexOrThrow(ResponseContract.ResponseEntry.COLUMN_NAME_VALUE));
                String value = new String(valueBytes);
                publishProgress(commandName, value);

            }
            cursor.close();
            return null;
        }

        @Override
        public void onProgressUpdate(String... values) {
            int nextId = responseListAdapter.getItemCount() + 1;
            ObdContent.addItem(ObdContent.createItemWithResponse(nextId, values[0], values[1]));
            responseListAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    };

    @Override
    public void onListFragmentInteraction(ObdContent.ObdResponse item) {
        Log.i("ObdResponse Card", item.toString());
    }
}
