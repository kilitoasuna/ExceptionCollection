package com.example.songxh.exceptioncollection;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Songxh on 2017/9/10.
 */

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private Context mContext;
    private Thread.UncaughtExceptionHandler defaultHandler;
    private static CrashHandler mCrashHandler;
    //存储设备信息和异常信息
    private Map<String, String> mInfor = new HashMap<>();
    //文件日期格式
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private CrashHandler(){

    }

    /**
     * 单例模式
     * @return
     */
    public static CrashHandler getInstance() {
        if(mCrashHandler == null){
            synchronized (CrashHandler.class){//给该类加上同步锁
                if(mCrashHandler == null){
                    mCrashHandler = new CrashHandler();
                }
            }
        }
        return mCrashHandler;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        //1.收集错误信息
        //2.保存错误信息（保存在本地的文件）
        //3.上传到服务器（暂不实现）
        if(!handleException(ex)){//如果没有进行人为的处理
            if(defaultHandler != null) {
                defaultHandler.uncaughtException(thread, ex);//调用系统默认的处理器来处理
            }
        }else {//已经人为的处理了
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Process.killProcess(Process.myPid());//强制杀掉进程
            System.exit(1);
        }

    }

    /**
     * 初始化
     */
    public void init(Context mContext){
        this.mContext = mContext;
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }
    /**
     * 是否已经人为处理异常
     */
    private boolean handleException(Throwable throwable){
        if(throwable == null){
            return false;
        }
        //toast提示
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(mContext,"UncaughtException", Toast.LENGTH_SHORT);
                Looper.loop();
            }
        }).start();

        //收集错误信息
        collectErrorInfo();
        //保存错误信息
        saveErrorInfo(throwable);
        return true;
    }

    /**
     * 收集版本信息和属性信息
     */
    private void collectErrorInfo(){
        PackageManager pm = mContext.getPackageManager();
        try {
            //版本信息
            PackageInfo pi = pm.getPackageInfo(mContext.getPackageName(), PackageManager.GET_ACTIVITIES);
            if(pi != null){
                String versionName = TextUtils.isEmpty(pi.versionName) ? "未设置版本名称" : pi.versionName;
                String versionCode = pi.versionCode + "";
                mInfor.put("versionName", versionName);
                mInfor.put("versionCode", versionCode);
            }
            //属性信息
            Field[] fields = Build.class.getFields();
            for(Field field : fields){
                field.setAccessible(true);
                try {
                    mInfor.put(field.getName(), field.get(null).toString());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * 保存错误信息到本地文件
     * @param throwable
     */
    private void saveErrorInfo(Throwable throwable){
        StringBuffer stringBuffer = new StringBuffer();
        for(Map.Entry<String, String> entry : mInfor.entrySet()){
            String key = entry.getKey();
            String value = entry.getValue();
            stringBuffer.append(key + "=" + value + "\n");

        }
        stringBuffer.append("下面是捕获到的异常信息---------->"+ throwable + "\n");
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while(cause != null){
            cause.printStackTrace(printWriter);
            cause = throwable.getCause();
        }
        stringBuffer.append(printWriter.toString());
        printWriter.close();

        long curTime = System.currentTimeMillis();
        String time = dateFormat.format(new Date());
        String fileName = "crash-" + time + "-" + curTime + ".log";

        //判断有没有SD卡
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/crash_kilito/";
            File file = new File(path);
            if(!file.exists()){
                file.mkdir();
            }
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(path + fileName);
                try {
                    fos.write(stringBuffer.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }finally {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
