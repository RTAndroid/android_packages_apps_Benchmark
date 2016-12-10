/*
 * Copyright (C) 2017 RTAndroid Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rtandroid.benchmark.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtils
{
    public static final int REQUEST_ASK_PERMISSIONS = 17;

    private Fragment mFragment = null;

    public void setup(Fragment fragment)
    {
        mFragment = fragment;
        requestPermissions();
    }

    private void requestPermissions()
    {
        // on older devices, there is no need for checking runtime permissions
        int version = Build.VERSION.SDK_INT;
        if (version <= Build.VERSION_CODES.LOLLIPOP_MR1) { return; }

        List<String> permissionsList = new ArrayList<>();
        addRequiredPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE);
        addRequiredPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        // we may already have all the permissions, skip this step in this case
        if (permissionsList.isEmpty()) { return; }

        // send out the request
        int size = permissionsList.size();
        String[] permissionsArray = new String[size];
        permissionsList.toArray(permissionsArray);
        mFragment.requestPermissions(permissionsArray, REQUEST_ASK_PERMISSIONS);
    }

    private void addRequiredPermission(List<String> permissionsList, String permission)
    {
        Context context = mFragment.getContext();
        boolean granted = (ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED);
        if (granted) { return; }

        permissionsList.add(permission);
    }

    public void onPermissionResult(int[] grantResults)
    {
        // analyze returned results
        boolean granted = true;
        for (int grantResult : grantResults)
          if (grantResult != PackageManager.PERMISSION_GRANTED) { granted = false; }

        // and repeat if needed
        if (!granted) { requestPermissions(); }
    }
}
