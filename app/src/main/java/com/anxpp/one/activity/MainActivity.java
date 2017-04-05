package com.anxpp.one.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.anxpp.one.R;
import com.anxpp.one.fragment.HomeFragment;
import com.anxpp.one.fragment.PersonFragment;
import com.anxpp.one.fragment.BlogFragment;
import com.anxpp.one.service.ImService;
import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    BottomNavigationBar bottomNavigationBar;
    Fragment[] fragments = {new HomeFragment(),new BlogFragment(),new PersonFragment(),new PersonFragment(),new PersonFragment()};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //侧拉栏
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //头像点击事件
        navigationView.getHeaderView(0).findViewById(R.id.imageViewHeader).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, FullscreenActivity.class));
            }
        });

        getSupportFragmentManager().beginTransaction().add(R.id.container,fragments[0]).commit();
        //开启服务
        startService(new Intent(this, ImService.class));
        startActivity(new Intent(this, StartActivity.class));
        initNav();
    }

    private void initNav() {

        bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar_container);
        bottomNavigationBar.setAutoHideEnabled(true);//自动隐藏

        //BottomNavigationBar.MODE_SHIFTING;
        //BottomNavigationBar.MODE_FIXED;
        //BottomNavigationBar.MODE_DEFAULT;
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);

        // BottomNavigationBar.BACKGROUND_STYLE_DEFAULT;
        // BottomNavigationBar.BACKGROUND_STYLE_RIPPLE
        // BottomNavigationBar.BACKGROUND_STYLE_STATIC
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_STATIC);

        bottomNavigationBar.setBarBackgroundColor(R.color.white);//背景颜色
        bottomNavigationBar.setInActiveColor(R.color.gray);//未选中时的颜色
        bottomNavigationBar.setActiveColor(R.color.colorPrimaryDark);//选中时的颜色
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_info_black_24dp, "Home"))
                .addItem(new BottomNavigationItem(R.drawable.ic_info_black_24dp, "Books"))
                .addItem(new BottomNavigationItem(R.drawable.ic_info_black_24dp, "Music"))
                .addItem(new BottomNavigationItem(R.drawable.ic_sync_black_24dp, "Movies & TV"))
                .addItem(new BottomNavigationItem(R.drawable.ic_notifications_black_24dp, "Games"))
                .initialise();
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener() {
            @Override
            public void onTabSelected(int position) {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                for(Fragment fragment : fragments){
                    transaction.hide(fragment);
                }
                if(fragments[position].isAdded()){
                    transaction.show(fragments[position]);
                }else{
                    transaction.add(R.id.container,fragments[position]).show(fragments[position]);
                }
                transaction.commit();
            }
            @Override
            public void onTabUnselected(int position) {
            }
            @Override
            public void onTabReselected(int position) {
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Integer id = item.getItemId();
        Log.i("onClick", id.toString());
        switch (id) {
            case R.id.nav_todolist:
                startActivity(new Intent(this, ToDoListActivity.class));
                break;
            case R.id.nav_camera:
                startActivity(new Intent(this, ArticleDetailsActivity.class));
                break;
            case R.id.nav_gallery:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.nav_slideshow:
                startActivity(new Intent(this, EmptyActivity.class));
                break;
            case R.id.nav_manage:
                startActivity(new Intent(this, BaseActivity.class));
                break;
            case R.id.nav_share:
                startActivity(new Intent(this, LoginActivity.class));
                break;
            case R.id.nav_send:
//                startActivity(new Intent(this, TabbedActivity.class));
                startActivity(new Intent(this, StartActivity.class));
                break;
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
