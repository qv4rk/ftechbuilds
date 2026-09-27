package com.feisttech.trendsevidence;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.UUID;

/** App-private persistent captures. The original image and manifest bytes are immutable. */
public final class CaptureStore {
    private final File root;
    public CaptureStore(Context context){root=new File(context.getFilesDir(),"captures");if(!root.exists()&&!root.mkdirs())throw new IllegalStateException("Cannot create capture directory");}
    public static byte[] read(File f)throws Exception{try(FileInputStream in=new FileInputStream(f);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);return out.toByteArray();}}
    public static String sha(byte[] bytes)throws Exception{byte[] d=MessageDigest.getInstance("SHA-256").digest(bytes);StringBuilder s=new StringBuilder();for(byte b:d)s.append(String.format(Locale.US,"%02x",b&255));return s.toString();}
    public synchronized String save(byte[] png,byte[] stamped,byte[] csv,JSONObject manifest)throws Exception{
        String id=UUID.randomUUID().toString();File directory=new File(root,id);if(!directory.mkdir())throw new IllegalStateException("Cannot create capture");
        try{
            write(new File(directory,"screenshot.png"),png);
            write(new File(directory,"stamped.png"),stamped);
            if(csv!=null)write(new File(directory,"data.csv"),csv);
            byte[] metadata=(manifest.toString(2)+"\n").getBytes("UTF-8");write(new File(directory,"manifest.json"),metadata);
            write(new File(directory,"manifest.sha256"),(sha(metadata)+"  manifest.json\n").getBytes("UTF-8"));
            return id;
        }catch(Exception e){File[] files=directory.listFiles();if(files!=null)for(File f:files)f.delete();directory.delete();throw e;}
    }
    private static void write(File file,byte[] bytes)throws Exception{try(FileOutputStream out=new FileOutputStream(file)){out.write(bytes);out.getFD().sync();}}
    public File file(String id,String name){if(!id.matches("[a-f0-9-]{36}")||!name.matches("screenshot\\.png|stamped\\.png|data\\.csv|manifest\\.json|manifest\\.sha256"))throw new IllegalArgumentException("Invalid capture path");return new File(new File(root,id),name);}
    public JSONArray list()throws Exception{
        JSONArray items=new JSONArray();File[] dirs=root.listFiles();if(dirs==null)return items;
        java.util.Arrays.sort(dirs,(a,b)->Long.compare(b.lastModified(),a.lastModified()));
        for(File d:dirs)if(d.isDirectory()&&d.getName().matches("[a-f0-9-]{36}")){
            File metadata=new File(d,"manifest.json");if(metadata.isFile())try{JSONObject m=new JSONObject(new String(read(metadata),"UTF-8"));items.put(new JSONObject().put("id",d.getName()).put("capturedAt",m.optString("capturedAt")).put("source",m.optJSONObject("source")));}catch(Exception ignored){}
        }
        return items;
    }
    public byte[] zip(String id)throws Exception{
        ByteArrayOutputStream out=new ByteArrayOutputStream();try(java.util.zip.ZipOutputStream zip=new java.util.zip.ZipOutputStream(out)){
            for(String name:new String[]{"screenshot.png","stamped.png","data.csv","manifest.json","manifest.sha256"}){File f=file(id,name);if(!f.isFile())continue;zip.putNextEntry(new java.util.zip.ZipEntry(name));zip.write(read(f));zip.closeEntry();}
        }return out.toByteArray();
    }
    public void delete(String id){for(String name:new String[]{"screenshot.png","stamped.png","data.csv","manifest.json","manifest.sha256"})file(id,name).delete();File dir=new File(root,id);dir.delete();}
}
