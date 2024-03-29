package com.android.timlin.ivedioplayer;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.timlin.ivedioplayer.business.list.file.FileListFragment;
import com.meituan.android.walle.WalleChannelReader;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final FileListFragment fileListFragment = new FileListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.content_main, fileListFragment)
                .commit();
        WalleChannelReader.getChannel(this.getApplicationContext());
    }
}
