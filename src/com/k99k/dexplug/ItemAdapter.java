package com.k99k.dexplug;

import java.util.ArrayList;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

public class ItemAdapter extends BaseAdapter {
	

    ArrayList<ItemData> list = new ArrayList<ItemData>();

    private Context context;

	public ItemAdapter(Context context) {
		this.context = context;
		
	}
	
	public void addItem(ItemData it){
		this.list.add(it);
	}
	

	@Override
	public int getCount() {
		return this.list.size();
	}

	@Override
	public Object getItem(int position) {
		return this.list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return this.list.get(position).getId();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemData it = this.list.get(position);
		ItemView iv = new ItemView(this.context, it);
		AbsListView.LayoutParams lp = new AbsListView.LayoutParams(AbsListView.LayoutParams.FILL_PARENT,  
                AbsListView.LayoutParams.WRAP_CONTENT);  
		FrameLayout wrapLayout = new FrameLayout(this.context);
		wrapLayout.addView(iv,lp);
        return wrapLayout;
	}

}
