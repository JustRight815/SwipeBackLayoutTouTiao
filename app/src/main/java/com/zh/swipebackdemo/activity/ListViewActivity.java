package com.zh.swipebackdemo.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.zh.swipebackdemo.BaseSwipeActivity;
import com.zh.swipebackdemo.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by GongWen on 17/8/24.
 */

public class ListViewActivity extends BaseSwipeActivity {
    private ListView mListView;
    private List<String> dataList;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_listview;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListView = (ListView) findViewById(R.id.listView);
        dataList = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            dataList.add("Test" + i);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter(this,
                android.R.layout.simple_expandable_list_item_1,
                dataList);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        dataList.get(position), Toast.LENGTH_LONG)
                        .show();
            }
        });
    }
}
