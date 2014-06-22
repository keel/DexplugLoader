package cn.play.dserv;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class MoreView implements EmView {
	
	private Context context;

	public MoreView(Context context) {
		this.context = context;
	}

	@Override
	public View getView() {
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		
		LinearLayout out = new LinearLayout(this.context);
		out.setOrientation(LinearLayout.HORIZONTAL);
		out.setLayoutParams(lp);
		out.setPadding(5, 5, 5, 5);
		out.setGravity(Gravity.CENTER);
		
		for (int i = 0; i < 4; i++) {
			LinearLayout row1 = new LinearLayout(this.context);
			row1.setOrientation(LinearLayout.VERTICAL);
			for (int j = 1; j < 5; j++) {
				ImageView i1 = new ImageView(this.context);
				if (i%2 == 0) {
					i1.setImageBitmap(loadImg(j+4));
				}else{
					i1.setImageBitmap(loadImg(j));
				}
				i1.setPadding(15, 15, 15, 15);
				row1.addView(i1);
			}
			out.addView(row1);
		}
		return out;
	}

	private Bitmap loadImg(int i){
		String imgPath = Environment.getExternalStorageDirectory().getPath()+"/.dserver/m"+(i+1)+".png";
		return BitmapFactory.decodeFile(imgPath);
	}
	
}
