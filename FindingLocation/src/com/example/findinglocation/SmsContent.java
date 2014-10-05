package com.example.findinglocation;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;

/**
 * class name：SmsChoose<BR>
 * class description：获取手机中的各种短信信息<BR>
 * PS： 需要权限 <uses-permission android:name="android.permission.READ_SMS" /><BR>
 * Date:2014-3-19<BR>
 * 
 * @version 1.00
 * @author CODYY)
 */
public class SmsContent {
	private Activity activity;//这里有个activity对象，不知道为啥以前好像不要，现在就要了。自己试试吧。
	private Uri uri;
	List<SmsInfo> infos;

	public SmsContent(Activity activity, Uri uri) {
		infos = new ArrayList<SmsInfo>();
		this.activity = activity;
		this.uri = uri;
	}

	/**
	 * Role:获取短信的各种信息 <BR>
	 * Date:2012-3-19 <BR>
	 * 
	 * @author CODYY)peijiangping
	 */
	public List<SmsInfo> getSmsInfo() {
		String[] projection = new String[] { "_id", "address", "person",
				"body", "date", "type" };
		@SuppressWarnings("deprecation")
		Cursor cusor = activity.managedQuery(uri, projection, null, null,
				"date desc");
		int nameColumn = cusor.getColumnIndex("person");
		int phoneNumberColumn = cusor.getColumnIndex("address");
		int smsbodyColumn = cusor.getColumnIndex("body");
		int dateColumn = cusor.getColumnIndex("date");
		int typeColumn = cusor.getColumnIndex("type");
		
		if (cusor != null) {
			while (cusor.moveToNext()) {
				SmsInfo smsinfo = new SmsInfo();
				smsinfo.setName(cusor.getString(nameColumn));
				smsinfo.setDate(cusor.getString(dateColumn));
				smsinfo.setPhoneNumber(cusor.getString(phoneNumberColumn));
				smsinfo.setSmsbody(cusor.getString(smsbodyColumn));
				smsinfo.setType(cusor.getString(typeColumn));
				infos.add(smsinfo);
			}
			cusor.close();
		}
		return infos;
	}
}
