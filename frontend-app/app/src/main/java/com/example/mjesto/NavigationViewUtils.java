package com.example.mjesto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.widget.Toast;

public class NavigationViewUtils {

    public static boolean handleOnNavigationItemSelected(@NonNull MenuItem menuItem, Context context) {
        DrawerLayout drawerLayout = ((Activity) context).findViewById(R.id.drawer);
        drawerLayout.closeDrawers();
        switch (menuItem.getItemId()) {
            case R.id.nav_search:
                Toast.makeText(context, "Profile Clicked", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(context, ProfileActivity.class);
//                ((Activity) context).startActivity(intent);
                return true;
            default:
                return false;
        }
    }
}
