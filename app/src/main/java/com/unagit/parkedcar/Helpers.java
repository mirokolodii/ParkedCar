package com.unagit.parkedcar;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by a264889 on 27.01.2018.
 */

public class Helpers {
    public static void showToast(String text, Context context) {
        int duration = Toast.LENGTH_SHORT;
        Toast.makeText(context, text, duration).show();
    }
}
