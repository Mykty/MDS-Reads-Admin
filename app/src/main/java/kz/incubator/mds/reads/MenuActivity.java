package kz.incubator.mds.reads;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.dk.view.folder.ResideMenu;
import com.dk.view.folder.ResideMenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import kz.incubator.mds.reads.about_us_menu.AboutUsFragment;
import kz.incubator.mds.reads.authentications.LoginByEmailPage;
import kz.incubator.mds.reads.book_list_menu.BookListFragment;
import kz.incubator.mds.reads.database.StoreDatabase;
import kz.incubator.mds.reads.groups_menu.GroupsFragment;
import kz.incubator.mds.reads.rules_menu.RuleFragment;
import kz.incubator.mds.reads.settings_menu.SettingsFragment;

public class MenuActivity extends AppCompatActivity implements View.OnClickListener {

    public ResideMenu resideMenu;
    private ResideMenuItem users, userProfile, book_list, groupListMenu;
    private ResideMenuItem rules, about_us, settings, log_out;
    public static Toolbar actionToolbar;
    DatabaseReference mDatabaseRef, booksRef, usersRef;
    StoreDatabase storeDb;
    SQLiteDatabase sqdb;
    BookListFragment bookListFragment;
    RuleFragment ruleFragment;
    SettingsFragment settingsFragment;
    FirebaseUser currentUser;
    static String currentUserEmail = "empty";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_layout2);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            currentUserEmail = currentUser.getEmail();
        }

        setUpMenu();
        setupViews(savedInstanceState);
    }

    public void setupViews(Bundle savedInstanceState) {

        actionToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(actionToolbar);
        actionToolbar.setNavigationIcon(R.drawable.ic_home_black);
        actionToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });

        actionToolbar.inflateMenu(R.menu.search_menu);

        bookListFragment = new BookListFragment();
        ruleFragment = new RuleFragment();
        settingsFragment = new SettingsFragment();


        if (savedInstanceState == null) {
            changeFragment(bookListFragment);
            setTitle(getString(R.string.menu_books));
        }

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        usersRef = mDatabaseRef.child("user_list");
        booksRef = mDatabaseRef.child("book_list");
        storeDb = new StoreDatabase(this);
        sqdb = storeDb.getWritableDatabase();
        checkInternetConnection();
    }

    public static void setTitle(String title) {
        actionToolbar.setTitle(title);
    }

    @Override
    public void onBackPressed() {

    }

    private void setUpMenu() {
        resideMenu = new ResideMenu(this);
        resideMenu.setUse3D(true);
        resideMenu.setBackground(R.drawable.back_menu);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);

        resideMenu.setScaleValue(0.6f);

        book_list = new ResideMenuItem(this, R.drawable.ic_list_black_24dp, getString(R.string.menu_books));
        groupListMenu = new ResideMenuItem(this, R.drawable.ic_groups, getString(R.string.menu_groups));

        rules = new ResideMenuItem(this, R.drawable.ic_assignment_black_24dp, getString(R.string.menu_rules));
        about_us = new ResideMenuItem(this, R.drawable.ic_info_outline_black_24dp, getString(R.string.menu_about_us));
        settings = new ResideMenuItem(this, R.drawable.ic_language_white, getString(R.string.menu_change_language));
        log_out = new ResideMenuItem(this, R.drawable.ic_exit_to_app_black_24dp, getString(R.string.menu_sing_out));

        book_list.setOnClickListener(this);
        groupListMenu.setOnClickListener(this);
        rules.setOnClickListener(this);
        about_us.setOnClickListener(this);
        settings.setOnClickListener(this);
        log_out.setOnClickListener(this);

        resideMenu.addMenuItem(groupListMenu, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(book_list, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(rules, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(about_us, ResideMenu.DIRECTION_LEFT);

        resideMenu.addMenuItem(settings, ResideMenu.DIRECTION_LEFT);
        resideMenu.addMenuItem(log_out, ResideMenu.DIRECTION_LEFT);

        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

    @Override
    public void onClick(View view) {

        if (view == book_list) {
            changeFragment(bookListFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_books));
            actionToolbar.setNavigationIcon(R.drawable.ic_list_black_24dp);

        }else if (view == groupListMenu) {
            changeFragment(new GroupsFragment());
            getSupportActionBar().setTitle(getString(R.string.menu_groups));
            actionToolbar.setNavigationIcon(R.drawable.ic_groups);

        } else if (view == about_us) {
            changeFragment(new AboutUsFragment());
            getSupportActionBar().setTitle(getString(R.string.menu_about_us));
            actionToolbar.setNavigationIcon(R.drawable.ic_info_outline_black_24dp);

        } else if (view == rules) {
            changeFragment(ruleFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_rules));
            actionToolbar.setNavigationIcon(R.drawable.ic_assignment_black_24dp);

        } else if (view == settings) {
            changeFragment(settingsFragment);
            getSupportActionBar().setTitle(getString(R.string.menu_change_language));
            actionToolbar.setNavigationIcon(R.drawable.ic_assignment_black_24dp);

        } else if (view == log_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
            currentUserEmail = "empty";

            startActivity(new Intent(MenuActivity.this, LoginByEmailPage.class));
        }

        resideMenu.closeMenu();
    }

    private boolean checkInternetConnection() {
        if (isNetworkAvailable()) {
            return true;

        } else {
            Toast.makeText(this, getResources().getString(R.string.inetConnection), Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        }

        @Override
        public void closeMenu() {
        }
    };

    private void changeFragment(Fragment targetFragment) {
        resideMenu.clearIgnoredViewList();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content, targetFragment, "fragment")
                .setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();
    }

    public ResideMenu getResideMenu() {
        return resideMenu;
    }
}
