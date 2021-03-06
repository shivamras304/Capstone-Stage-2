package com.shivam.pillbox.ui;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.transition.Fade;
import android.transition.Transition;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shivam.pillbox.R;
import com.shivam.pillbox.data.MedicineColumns;
import com.shivam.pillbox.extras.ContextAndId;
import com.shivam.pillbox.extras.Utility;
import com.shivam.pillbox.recyclerViewHelpers.MedicineDetailsCursorAdapter;
import com.shivam.pillbox.recyclerViewHelpers.RecyclerViewClickListener;
import com.shivam.pillbox.tasks.DeleteCurrentMedicineTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    @BindView(R.id.detail_activity_container)
    LinearLayout detailsActivityContainer;

    @BindView(R.id.details_name)
    TextView nameTextView;

    @BindView(R.id.details_frequency)
    TextView frequencyTextView;

    @BindView(R.id.details_instructions_title)
    TextView instructionsTitleTextView;

    @BindView(R.id.details_instructions)
    TextView instructionsTextView;

    @BindView(R.id.details_upcoming_title)
    TextView upcomingTextView;

    @BindView(R.id.recyler_view_details)
    RecyclerView recyclerView;

    @BindView(R.id.floatingActionButtonDelete)
    FloatingActionButton fab;

    private Context mContext;
    private String medId;
    private static final int CURSOR_LOADER_ID = 1;
    private MedicineDetailsCursorAdapter adapter;
    private Uri medicineUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);

        mContext = this;

        Intent intent = getIntent();
        medicineUri = intent.getData();

        getLoaderManager().initLoader(CURSOR_LOADER_ID, null, this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new AlertDialog.Builder(mContext)
                        .setPositiveButton(
                                getString(R.string.delete_medicine_alert_dialog_positive_button),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        new DeleteCurrentMedicineTask().execute(new ContextAndId(
                                                mContext, medId));

                                        finish();
                                    }
                                })
                        .setNegativeButton(getString(R.string.dialog_time_picker_negative_button),
                                null)
                        .setMessage(getString(R.string
                                .delete_medicine_alert_dialog))
                        .show();
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        adapter = new MedicineDetailsCursorAdapter(mContext, null);
        recyclerView.setAdapter(adapter);
        recyclerView.addOnItemTouchListener(new RecyclerViewClickListener(this, new
                RecyclerViewClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View v, int position) {
                        //v.setFocusable(true);
                        v.setSelected(true);
                    }
                }));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Transition detailsEnter = new Fade(Fade.IN);
            Transition detailsReturn = new Fade(Fade.OUT);

            detailsEnter.setDuration(250);
            detailsReturn.setDuration(250);

            detailsEnter.excludeTarget(android.R.id.statusBarBackground, true);
            detailsEnter.excludeTarget(android.R.id.navigationBarBackground, true);
            detailsEnter.excludeTarget(R.id.floatingActionButtonDelete, true);
            detailsEnter.excludeTarget(R.id.toolbar_details, true);

            detailsReturn.excludeTarget(android.R.id.statusBarBackground, true);
            detailsReturn.excludeTarget(android.R.id.navigationBarBackground, true);
            detailsReturn.excludeTarget(R.id.floatingActionButtonDelete, true);
            detailsReturn.excludeTarget(R.id.toolbar_details, true);

            getWindow().setEnterTransition(detailsEnter);
            getWindow().setReturnTransition(detailsReturn);
        }
    }

    public void onClickBackButton(View view) {
        finish();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(
//                mContext,
//                MedicineProvider.Medicines.CONTENT_URI,
//                MedicineColumns.ALL_COLUMNS,
//                MedicineColumns.MEDICINE_ID + " = \"" + medId + "\"",
//                null,
//                null
//        );
        return new CursorLoader(
                mContext,
                medicineUri,
                MedicineColumns.ALL_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        bindViews(cursor);
        adapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }


    private void bindViews(Cursor cursor) {
        cursor.moveToFirst();
  //      medId = intent.getStringExtra("medId");

        //TODO : App crashes on deleting a medicine at this point
        medId = cursor.getString(MedicineColumns.MEDICINE_ID_INDEX);

 //       final int colorIndex = intent.getIntExtra("medColor", -1);
        final int colorIndex = cursor.getInt(MedicineColumns.COLOR_INDEX);

        detailsActivityContainer.setBackground(getResources().getDrawable(
                Utility.getColorBackground(colorIndex)));

//        nameTextView.setText(intent.getStringExtra("medName"));
        nameTextView.setText(cursor.getString(MedicineColumns.NAME_INDEX));
        nameTextView.setTextColor(getResources().getColor(Utility.getColorText(colorIndex)));

        instructionsTitleTextView.setTextColor(getResources().getColor(Utility.getColorText(colorIndex)));
        upcomingTextView.setTextColor(getResources().getColor(Utility.getColorText(colorIndex)));

//        int freq = intent.getIntExtra("medFreq", 1);
        int freq = cursor.getInt(MedicineColumns.DAY_FREQUENCY_INDEX);
        if (freq > 1)
            frequencyTextView.setText(getString(R.string.details_frequency_text, freq));
        else
            frequencyTextView.setText(getString(R.string.details_frequency_text_one));

        String descString = "";
//        String foodMsg = intent.getStringExtra("medFoodMessage");
        String foodMsg = cursor.getString(MedicineColumns.MESSAGE_FOOD_INDEX);
        if (!foodMsg.equals("")) {
            descString = getString(R.string.details_instructions_starting_text);
            descString += " " + foodMsg;
        }
//        String freeMsg = intent.getStringExtra("medFreeMessage");
        String freeMsg = cursor.getString(MedicineColumns.MESSAGE_FREE_INDEX);
        if (foodMsg.equals("") && freeMsg.equals(""))
            descString = getString(R.string.no_specific_instructions);
        else {
            if (!foodMsg.equals(""))
                descString += ". " + freeMsg;
            else
                descString = freeMsg;
        }

        instructionsTextView.setText(descString);

    }
}
