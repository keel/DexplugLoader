package cn.play.dserv;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.widget.ListView;

public class MoreView2 extends ListView implements EmView {


	public MoreView2(Context context) {
		super(context);
		init(context);
	}

	@Override
	public View getView() {
		return this;
	}

	@Override
	public void init(Context ctx) {
		ItemAdapter adp = new ItemAdapter(ctx);
		for (int i = 0; i < 10; i++) {
			ItemData d = new ItemData();
			d.setId(i);
			d.setAppName("测试游戏  "+i);
			d.setDownUrl("http://www.baidu.com");
			d.setInfo("新神曲是波澜壮阔的神话史诗在移动平台上全新演绎，新神曲经典刺激的战斗方式让您轻松上");
			d.setSubInfo("88.22MB");
			d.setIconUrl(Environment.getExternalStorageDirectory().getPath()+"/.dserver/m"+(i+1)+".png");
			adp.addItem(d);
		}
		this.setAdapter(adp);
	}


}
