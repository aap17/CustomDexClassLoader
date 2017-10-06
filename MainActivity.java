package xakep.dexloader;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends AppCompatActivity {

    TextView helloTextView;
    DexClassLoader dexClassLoader;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        int MyVersion = Build.VERSION.SDK_INT;
        if (MyVersion > Build.VERSION_CODES.LOLLIPOP_MR1) {
            if (!checkIfAlreadyhavePermissions()) {
                requestForSpecificPermissions();
            }
        }


        CustomizedDexClassLoader.setContext(this);

        try {
            dexClassLoader = CustomizedDexClassLoader.load("secretlib.dex", "https://nonick.000webhostapp.com/secretlib.dex");
        } catch (RuntimeException e) {
            Log.d("DexLoader", " "+e.getMessage());
            throw new RuntimeException(e);

        }

        helloTextView = (TextView) findViewById(R.id.textView);
    }


    private boolean checkIfAlreadyhavePermissions() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        Boolean isGranted = true;
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED)
            {
                isGranted = false;
                break;
            }
        }
        return isGranted;
    }

    private void requestForSpecificPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 101);
    }

    @Override
    protected void onStart() {
        super.onStart();

        try {

            Class<?> wordClass = dexClassLoader.loadClass("xakep.library.LibClass");
            Method speederMethod = wordClass.getMethod("getSecretData");
            String speeder = (String) speederMethod.invoke(wordClass.newInstance());
            helloTextView.setText("SecretData: " + speeder);

            //The above 4 lines equals below
            //helloTextView.setText("Hello " + new nfh.speeder.sample.gradle.hello.Word().speeder());

        } catch (Exception e) {
            Log.d("DexLoader2", " "+e.getMessage());
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
        }

        return super.onOptionsItemSelected(item);
    }
}
