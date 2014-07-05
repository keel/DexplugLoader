package cn.play.dserv;

import android.app.Activity;
import android.view.View;
import android.widget.Button;

public interface ExitInterface {

	public Button getBT1();
	public Button getBT2();
	public Button getGBT1();
	public Button getGBT2();
	public int getVer();
	public View getExitView(Activity cx);
}
