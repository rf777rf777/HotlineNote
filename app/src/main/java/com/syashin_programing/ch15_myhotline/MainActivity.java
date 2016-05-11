package com.syashin_programing.ch15_myhotline;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
//import android.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter;

public class MainActivity extends AppCompatActivity
                    implements AdapterView.OnItemClickListener{

    static final String DB_NAME = "HotlineDB";  //資料庫名稱
    static final String TB_NAME = "hotlist"; //資料表名稱
    static final int MAX=8;  //程式紀錄的通訊資料筆數上限
    static final String[] FROM = new String[]{"name","phone","email"};  //資料表欄位名稱字串陣列

    SQLiteDatabase db;
    Cursor cur;  //存放查詢結果的Cursor物件
    SimpleCursorAdapter adapter;
    EditText etName,etPhone,etEmail;  //用於輸入姓名 電話 EMAIL的EditText欄位
    Button btInsert,btUpdate,btDelete;  //新增 修改 刪除按鈕
    ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etName = (EditText)findViewById(R.id.etName);
        etPhone = (EditText)findViewById(R.id.etPhone);
        etEmail = (EditText)findViewById(R.id.etEmail);
        btInsert = (Button)findViewById(R.id.btInsert);
        btUpdate = (Button)findViewById(R.id.btUpdate);
        btDelete = (Button)findViewById(R.id.btDelete);

        //開啟或建立資料庫
        db = openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE,null);

        //建立資料表
        String createTable = "CREATE TABLE IF NOT EXISTS "+ TB_NAME +
                "(_id INTEGER PRIMARY KEY AUTOINCREMENT, "+  //索引欄位
                "name VARCHAR(32), "+
                "phone VARCHAR(16), "+
                "email VARCHAR(64))";

        db.execSQL(createTable);
        cur=db.rawQuery("SELECT * FROM "+TB_NAME,null);  //查詢資料

        //若是空的則寫入測試資料
        if (cur.getCount()==0)
        {
            addData("聯絡人1","01-2345678","ONE@TestMail.com");
            addData("聯絡人2","09-8765432","ONE@TestMail.com");
        }

        //建立Adapter物件
        adapter = new SimpleCursorAdapter(this,R.layout.item,cur,  //自訂的Layout,Cursor物件
                                            FROM,  //欄位名稱陣列
                                            new int[] {R.id.name,R.id.phone,R.id.email}, //TextView資源id陣列
                                            0);

        lv = (ListView)findViewById(R.id.lv);
        lv.setAdapter(adapter);   //設定Adapter
        lv.setOnItemClickListener(this);  //設定按下事件的監聽器

        //內部類別寫法
        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                onItemClick(parent,view,position,id); //呼叫選取項目的事件處理方法
                call(view);   //呼叫撥號的方法
                return true;  //表示已經處理好了 不需要再引發後續事件
            }
        });

        requery();  //呼叫自訂方法重新查詢及設定按鈕狀態
    }

    private void addData(String name,String phone,String email)
    {
        ContentValues cv = new ContentValues(3);
        cv.put(FROM[0],name);
        cv.put(FROM[1],phone);
        cv.put(FROM[2],email);

        db.insert(TB_NAME,null,cv);
    }

    private void update(String name,String phone,String email,int id)
    {
        ContentValues cv =new ContentValues(3);
        cv.put(FROM[0],name);
        cv.put(FROM[1],phone);
        cv.put(FROM[2],email);

        db.update(TB_NAME, cv, "_id=" + id, null);  //更新_id所指的紀錄
    }

    private void requery()  //重新查詢的自訂方法
    {
        cur=db.rawQuery("SELECT * FROM "+TB_NAME,null);
        adapter.changeCursor(cur);  //更改Adapter的Cursor
        if (cur.getCount()==MAX)
        {
            btInsert.setEnabled(false);
        }
        else
        {
            btInsert.setEnabled(true);
        }

        btUpdate.setEnabled(false);  //停用更新鈕 待使用者選取項目後再啟用
        btDelete.setEnabled(false);  //停用刪除鈕 待使用者選取項目後再啟用
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        cur.moveToPosition(position);  //移動Cursor至使用者選取的項目

        //讀出姓名 電話 EMAIL資料並顯示
        etName.setText(cur.getString(cur.getColumnIndex(FROM[0])));
        etPhone.setText(cur.getString(cur.getColumnIndex(FROM[1])));
        etEmail.setText(cur.getString(cur.getColumnIndex(FROM[2])));

        btUpdate.setEnabled(true);  //啟用更新鈕
        btDelete.setEnabled(true);  //啟用刪除鈕
    }

    public void onInsertUpdate(View v)
    {
        String nameStr = etName.getText().toString().trim();
        String phoneStr=etPhone.getText().toString().trim();
        String emailStr=etEmail.getText().toString().trim();

        if (nameStr.length()==0||phoneStr.length()==0||emailStr.length()==0)  //任一欄位沒有內容即返回
        {
            return;
        }

        if (v.getId()==R.id.btUpdate)  //按更新鈕
        {
            update(nameStr,phoneStr,emailStr,cur.getInt(0));  //cur.getInt(0)為 取得_id值 更新含此_id的紀錄
        }
        else //按新增鈕
        {
            addData(nameStr,phoneStr,emailStr);
        }

        requery();//更新Cursor內容
    }

    public void onDelete(View v)  //刪除鈕的onClick事件方法
    {
        db.delete(TB_NAME,"_id="+cur.getInt(0),null);
        requery();  //更新Cursor內容
    }

    public void call(View v)  //打電話
    {
        String uri = "tel:"+cur.getString(cur.getColumnIndex(FROM[1]));  //"phone"欄位
        Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(it);
    }

    public void mail(View v)  //寄送電子郵件
    {
        String uri="mailto:"+cur.getString(cur.getColumnIndex(FROM[2]));  //"email"欄位
        Intent it = new Intent(Intent.ACTION_SENDTO,Uri.parse(uri));
        startActivity(it);
    }
}
