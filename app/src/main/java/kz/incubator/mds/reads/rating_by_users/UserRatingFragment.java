package kz.incubator.mds.reads.rating_by_users;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import kz.incubator.mds.reads.R;
import kz.incubator.mds.reads.database.StoreDatabase;
import kz.incubator.mds.reads.groups_menu.GetUsersAsyncTask;
import kz.incubator.mds.reads.groups_menu.adapters.UserListAdapter;
import kz.incubator.mds.reads.groups_menu.module.User;

public class UserRatingFragment extends Fragment implements View.OnClickListener {
    DatabaseReference mDatabaseRef, ratingUserRef;
    RecyclerView recyclerView;
    SwipeRefreshLayout mSwipeRefreshLayout;
    RecyclerView.LayoutManager linearLayoutManager;
    ArrayList<User> userListCopy;
    ArrayList<User> userList;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    SearchView searchView;
    View view;
    ProgressBar progressBar;
    UserListAdapter listAdapter;
    String TABLE_USER = "user_store";
    AlertDialog sortDialog;
    View progressLoading;
    ArrayList<String> goldUsers, silverUsers, bronzeUsers, otherUsers;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.activity_user_list, container, false);

        initView();
        checkVersion();
        getRatingUsers();

        return view;
    }

    public void initView() {

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        storeDb = new StoreDatabase(getActivity());
        sqdb = storeDb.getWritableDatabase();

        ratingUserRef = mDatabaseRef.child("rating_users");
        userList = new ArrayList<>();
        goldUsers = new ArrayList<>();
        silverUsers = new ArrayList<>();
        bronzeUsers = new ArrayList<>();
        otherUsers = new ArrayList<>();

        progressBar = view.findViewById(R.id.ProgressBar);
        progressLoading = view.findViewById(R.id.llProgressBar);

        recyclerView = view.findViewById(R.id.rv);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        int resId = R.anim.layout_anim;
        LayoutAnimationController animation = AnimationUtils.loadLayoutAnimation(getActivity(), resId);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutAnimation(animation);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));


        searchView = view.findViewById(R.id.searchView);
        userListCopy = new ArrayList<>();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filter(s);
                return false;
            }
        });

        setupSwipeRefresh();
    }

    private void getRatingUsers() {

        userList.clear();
        goldUsers.clear();
        silverUsers.clear();
        bronzeUsers.clear();
        otherUsers.clear();

        Query goldQuery = ratingUserRef.child("a_gold").orderByChild("point");
        Log.d("M_UserRatingFragment", "a_gold");
        goldQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot userData: dataSnapshot.getChildren()){
                    String userPhone = userData.getKey();
                    Log.d("M_UserRatingFragment", "userPhone: " + userPhone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Query silverQuery = ratingUserRef.child("b_silver").orderByChild("point");
        Log.d("M_UserRatingFragment", "silver");
        silverQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot userData: dataSnapshot.getChildren()){
                    String userPhone = userData.getKey();
                    Log.d("M_UserRatingFragment", "userPhone: " + userPhone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Query bronzeQuery = ratingUserRef.child("c_bronze").orderByChild("point");
        Log.d("M_UserRatingFragment", "silver");
        bronzeQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot userData: dataSnapshot.getChildren()){
                    String userPhone = userData.getKey();
                    Log.d("M_UserRatingFragment", "userPhone: " + userPhone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        Query otherQuery = ratingUserRef.child("other").orderByChild("point");
        Log.d("M_UserRatingFragment", "other");
        otherQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for(DataSnapshot userData: dataSnapshot.getChildren()){
                    String userPhone = userData.getKey();
                    Log.d("M_UserRatingFragment", "userPhone: " + userPhone);
                }

                progressLoading.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
        /*
        ratingUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot types : dataSnapshot.getChildren()) {
                        String key = types.getKey();
                        Log.d("M_UserRatingFragment", "types: " + key);

                        for (DataSnapshot user : types.getChildren()) {
                            String userPhone = user.getKey();
                            Long userPoint = (Long) user.getValue();
                            Log.d("M_UserRatingFragment", "userPhone: " + userPhone);
                            Log.d("M_UserRatingFragment", "userPoint: " + userPoint);

                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        */

        /*
        Cursor userCursor = sqdb.rawQuery("SELECT * FROM " + TABLE_USER, null);
        if (((userCursor != null) && (userCursor.getCount() > 0))) {
            userList.clear();
            while (userCursor.moveToNext()) {
                userList.add(new User(
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_INFO)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_EMAIL)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_PHONE)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP_ID)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_GROUP)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_PHOTO)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_ENTER_DATE)),
                        userCursor.getString(userCursor.getColumnIndex(COLUMN_IMG_STORAGE_NAME)),
                        userCursor.getInt(userCursor.getColumnIndex(COLUMN_BCOUNT)),
                        userCursor.getInt(userCursor.getColumnIndex(COLUMN_POINT)),
                        userCursor.getInt(userCursor.getColumnIndex(COLUMN_REVIEW_SUM)),
                        userCursor.getInt(userCursor.getColumnIndex(COLUMN_RAINTING_IN_GROUPS))
                ));

            }

            userListCopy = (ArrayList<User>) userList.clone();
            Collections.reverse(userListCopy);

        }
        */

    }

    @Override
    public void onClick(View v) {

    }

    public void filter(String text) {
        userList.clear();
        if (text.isEmpty()) {
            userList.addAll(userListCopy);
        } else {
            text = text.toLowerCase();
            for (User item : userListCopy) {
                if (item.getInfo().toLowerCase().contains(text) || item.getInfo().toLowerCase().contains(text) ||
                        item.getPhoneNumber().toUpperCase().contains(text)) {
                    userList.add(item);
                }
            }
        }
        recyclerView.setAdapter(listAdapter);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();

        inflater.inflate(R.menu.menu_card_read, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.login_page:

                break;

            case R.id.filter_user:

                sortDialog.show();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setupSwipeRefresh() {
        mSwipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshItems();
            }
        });
    }

    public void refreshItems() {
        if (isOnline()) {
            getRatingUsers();
        }
    }

    private boolean isOnline() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void checkVersion() {
        Query myTopPostsQuery = mDatabaseRef.child("user_ver");

        myTopPostsQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    String newVersion = dataSnapshot.getValue().toString();
                    if (!getDayCurrentVersion().equals(newVersion)) {
                        refreshUsersFromFirebase(newVersion);
                    }
                } else {
                    Toast.makeText(getActivity(), "Can not find user_ver firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void refreshUsersFromFirebase(String version) {
        new GetUsersAsyncTask(getActivity(), version, mSwipeRefreshLayout, progressLoading).execute();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    public String getDayCurrentVersion() {
        Cursor res = sqdb.rawQuery("SELECT user_ver FROM versions ", null);
        res.moveToNext();
        return res.getString(0);
    }

    public class BackgroundTaskForUserFill extends AsyncTask<Void, Void, Void> {
        RecyclerView recyclerView;
        ProgressBar progressBar;
        StoreDatabase storeDb;
        SQLiteDatabase sqdb;
        Context context;

        public BackgroundTaskForUserFill(Context context, RecyclerView recyclerView, ProgressBar progressBar) {
            this.recyclerView = recyclerView;
            this.progressBar = progressBar;
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDatabaseRef = FirebaseDatabase.getInstance().getReference();
            progressBar.setVisibility(View.VISIBLE);
            storeDb = new StoreDatabase(context);
            sqdb = storeDb.getWritableDatabase();
        }

        @Override
        protected Void doInBackground(Void... voids) {


            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Collections.reverse(userList);
            recyclerView.setAdapter(listAdapter);
            progressBar.setVisibility(View.GONE);
        }
    }
}