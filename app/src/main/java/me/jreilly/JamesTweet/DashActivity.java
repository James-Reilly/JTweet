package me.jreilly.JamesTweet;

import android.content.pm.PackageInstaller;
import android.provider.ContactsContract;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.Session;
import com.twitter.sdk.android.core.models.Tweet;

import java.util.ArrayList;
import java.util.List;

import me.jreilly.JamesTweet.TweetParsers.ProfileLink;
import me.jreilly.JamesTweet.TweetParsers.ProfileSwitch;


public class DashActivity extends ActionBarActivity{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.

    private Toolbar toolbar;
    RecyclerView mDrawerView;
    RecyclerView.Adapter mAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    DrawerLayout mDrawer;

    ActionBarDrawerToggle mDrawerToggle;

    String[] navItems = {
            "Profile",
            "Timeline",
            "Mentions",
            "Favorites",
            "Settings"

    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Setting the navigation drawer


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDrawerView = (RecyclerView) findViewById(R.id.left_drawer);
        mDrawerView.setHasFixedSize(true);

        mAdapter = new NavAdapter(navItems);

        mDrawerView.setAdapter(mAdapter);

        final GestureDetector mGestureDetector = new GestureDetector(DashActivity.this, new GestureDetector.SimpleOnGestureListener(){
           @Override
           public boolean onSingleTapUp(MotionEvent e){
               return true;
           }
        });

        mDrawerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {

            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                View child = rv.findChildViewUnder(e.getX(),e.getY());

                if (child != null && mGestureDetector.onTouchEvent(e)){
                    mDrawer.closeDrawers();
                    if(navItems[rv.getChildPosition(child)].equals("Profile")){
                        String uId = Twitter.getSessionManager().getActiveSession().getUserName();
                        /*
                        Bundle bundle = new Bundle();
                        bundle.putLong("param1", uId);
                        ProfileFragment fragPro = new ProfileFragment()
                        */
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, ProfileFragment.newInstance(uId)).addToBackStack(null).commit();
                    } else if (navItems[rv.getChildPosition(child)].equals("Timeline")){
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.content_frame, new DashFragment()).commit();
                    }

                    return true;
                }



                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {

            }
        });

        mLayoutManager = new LinearLayoutManager(this);
        mDrawerView.setLayoutManager(mLayoutManager);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this,mDrawer,this.toolbar, R.string.openDrawer, R.string.closeDrawer){
            @Override
            public void onDrawerOpened(View drawerView){
                super.onDrawerOpened(drawerView);
            }
            @Override
            public void onDrawerClosed(View drawerView){
                super.onDrawerClosed(drawerView);
            }
        };

        mDrawer.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content_frame, new DashFragment())
                    .commit();
        }

    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home){

            if(mDrawer.isDrawerOpen(mDrawerView)){
                mDrawer.closeDrawer(mDrawerView);
            }else {
                mDrawer.openDrawer(mDrawerView);
            }
        }

        return super.onOptionsItemSelected(item);
    }

}
