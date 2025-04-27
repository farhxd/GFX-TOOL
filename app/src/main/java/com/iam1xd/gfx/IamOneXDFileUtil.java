package com.iam1xd.gfx;

import static android.os.Build.VERSION.SDK_INT;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.DocumentsContract;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.documentfile.provider.DocumentFile;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;



public class IamOneXDFileUtil {

    private SharedPreferences sharedPreferences;
    private String[] array;
    private ArrayList<HashMap<String, Object>> listMap;
    public String pathSelected = "";
    public static String SHARED_KEY = "uri_tree_paths";
    public static String KEY_URI = "uri";
    public static String KEY_PATH = "path";
    public static String KEY_NAME = "lastSegment";
    private final Activity activity;

    public IamOneXDFileUtil(/*SharedPreferences sharedPreferences, */Activity activity){
       // this.sharedPreferences = sharedPreferences;
        this.activity = activity;
    }




   /* public IamOneXDFileUtil addUri(Uri uri, String path) {
        HashMap hashMap = new HashMap();
        hashMap.put(KEY_PATH, path.replaceAll("/", ""));
        hashMap.put(KEY_URI, uri.toString());
        hashMap.put(KEY_NAME, Uri.parse(path).getLastPathSegment());
            listMap.add(hashMap);
            saveData();
        return this;
    } */

    public Uri uriFromPathDir(String path){
        path = path.substring(0, path.lastIndexOf("/"));
        return new MainUri(path).getTreeUriToFolder();
    }
    
     public Uri uriFromPathDirXD(String path){
        path = path.substring(0, path.lastIndexOf("/"));
        return new MainUri(path).getUriToFile();
    }

    public Uri uriFromPathFull(String path){
        return new MainUri(path).getTreeUriToFolder();
    }
    
        public Uri uriFromPathXD(String path){
        return new MainUri(path).getUriToFile();
    }

    public String getPath(){
        return pathSelected;
    }


  /*  public IamOneXDFileUtil saveData(){
        if (listMap!=null && listMap.size()>0){
            String dataJson = new Gson().toJson(listMap);
            sharedPreferences.edit().putString(SHARED_KEY, dataJson).apply();
        }
        return this;
    } */

    private Uri getUriFromList(String path){
        path = path.replaceAll("/", "");
        if (listMap!=null && listMap.size()>0){
            for (HashMap hashMap : listMap){
                if (hashMap.get(KEY_PATH).toString().equals(path)){
                    return Uri.parse(hashMap.get(KEY_URI).toString());
                }
            }
        }
        return null;
    }


   /* public IamOneXDFileUtil loadData(){
        if (sharedPreferences!=null&&sharedPreferences.contains(SHARED_KEY)&&
                !sharedPreferences.getString(SHARED_KEY, "").isEmpty()) {
            String data = sharedPreferences.getString(SHARED_KEY, "");
            listMap = new Gson().fromJson(data, new TypeToken<ArrayList<HashMap<String, Object>>>(){}.getType());
        } else {
            listMap = new ArrayList<>();
        }
        return this;
    } */

    private ArrayList<HashMap<String, Object>> stringToListMap(String string){
        ArrayList<HashMap<String, Object>> listMap = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            if (jsonArray.length()> 0){
                for (int i =0; i < jsonArray.length(); i++){
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    HashMap hashMap = new HashMap();
                    hashMap.put(KEY_NAME, jsonObject.getString(KEY_NAME));
                    hashMap.put(KEY_PATH, jsonObject.getString(KEY_PATH));
                    hashMap.put(KEY_URI, jsonObject.getString(KEY_URI));
                    listMap.add(hashMap);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return listMap;
    }

    public boolean isPathExist(String uri){
        if (listMap!=null&& listMap.size()>0){
            for (HashMap hashMap : listMap){
                String temp = hashMap.get(KEY_PATH).toString();
                String target = uri.replaceAll("/", "");
                if (temp.equals(target)){
                    return true;
                }
            }
        }
        return false;
    }
    

    public void copyFileUtil(String source, String targetPath, String fileName){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (canReadAndWrite(uriFromPathXD(targetPath))){ //(canReadAndWrite(uriFromPathDirXD(source)) && canReadAndWrite(uriFromPathDirXD(targetPath)))
                    boolean copy = copyFile(source, targetPath, fileName);
                    if(copy){
                    //    showMessage("Success ✅");
                    } else {
                   //     showMessage("Error, Copying Failed");
                    }
                } else {
                    pathSelected = targetPath;
                    askPermission(100, uriFromPathFull(targetPath));
                    showMessage("Please give folder permission");
          
                /*pathSelected = source;
                askPermission(100, uriFromPathDir(source));*/
            }
        } else {
            FileUtil.copyFile(source, targetPath +"/" + fileName);
          //  showMessage("Success ✅");
        }
    }


    public static class MainUri {

        private final String path;
        public MainUri(String filePath){
            this.path = filePath;
        }


        Uri getTreeUriToFolder(){
            // content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb
            String uriPrincipal  = "content://com.android.externalstorage.documents/tree/primary%3A";
            String cleanPath = cleanPath(path); // = Alarms/folder/test.zip
            String parentFolder = getParent(cleanPath); // = Alarms
            String step2 = parentFolder+"/document/primary%3A"; // = Alarms/document/primary%3A
            if (cleanPath.endsWith("/")){
                cleanPath = cleanPath.substring(0, cleanPath.lastIndexOf("/"));
            }
            String step3 = cleanPath.replace("/", "%2F");
            String lastStep = uriPrincipal + step2 + step3;
            return Uri.parse(lastStep);
        }

        Uri getUriToFile(){
            // content://com.android.externalstorage.documents/tree/primary%3AAndroid%2Fobb
            String uriPrincipal  = "content://com.android.externalstorage.documents/tree/primary%3A";
            String cleanPath = cleanPath(path); // = Alarms/folder/test.zip
            String parentFolder = getParent(cleanPath); // = Alarms
            String step2 = parentFolder+"/document/primary%3A"; // = Alarms/document/primary%3A
            if (cleanPath.endsWith("/")){
                cleanPath = cleanPath.substring(0, cleanPath.lastIndexOf("/"));
            }
            String step3 = cleanPath.replace("/", "%2F");
            String lastStep = uriPrincipal + step3;
            return Uri.parse(lastStep);
            }

        String cleanPath(String path){
            String result = path;
            if (path.contains("sdcard")){
                if (path.startsWith("/")){
                    result = path.replace("/sdcard/", "");
                } else {
                    result = path.replace("sdcard/", "");
                }
            }
            if (path.contains("emulated")&&path.contains("storage")){
                if (path.startsWith("/")){
                    result = path.replace("/storage/emulated/0/", "");
                } else {
                    result = path.replace("storage/emulated/0/", "");
                }
            }
            return result;
        }

        String getParent(String path){
            String parent = path;
            if (parent.contains("/")){
                if (parent.contains("/data")){
                    parent = parent.substring(0, parent.indexOf("/"))+"%2Fdata";
                }
                else
                if (parent.contains("/obb")){
                    parent = parent.substring(0, parent.indexOf("/"))+"%2Fobb";
                }
                else {
                    parent = parent.substring(0, parent.indexOf("/"));
                }
            }
            return parent;
        }

    }

    public  void askPermission(int requestCode, Uri uri) {
        Intent i = new Intent();
        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        i.setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);
        //android.net.Uri muri = android.net.Uri.parse(new UtilsQ.Uri(path).getTreeUri());
        //muri = Uri.parse("content://com.android.externalstorage.documents/tree/primary%3AAndroid/document/primary%3AAndroid%2Fdata");
        i.putExtra(DocumentsContract.EXTRA_INITIAL_URI, uri);
        activity.startActivityForResult(i, requestCode);
    }

    public boolean canReadAndWrite(Uri uri){
        DocumentFile file = DocumentFile.fromTreeUri(activity, uri);
        if (file!=null) {
            return file.canRead() | file.canWrite();
        }
        return false;
    }

    public boolean canRead(Uri uri){
        DocumentFile file = DocumentFile.fromTreeUri(activity, uri);
        if (file!=null) {
            return file.canRead();
        }
        return false;
    }
    public boolean canWrite(Uri uri){
        DocumentFile file = DocumentFile.fromTreeUri(activity, uri);
        if (file!=null) {
            return file.canWrite();
        }
        return false;
    }
    public String removeTrailingSlash(String filePath) {
      if (filePath.endsWith("/")) {
        filePath = filePath.substring(0, filePath.length() - 1);
      }
      return filePath;
    }

    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    public void persistFolder(Intent _data){
        Uri muri = _data.getData();
        if (muri.getPath().endsWith(":")) {
        }
        else {
            DocumentFile docPath = DocumentFile.fromTreeUri(activity, _data.getData());
            if (docPath != null) {
                final ContentResolver resolver = activity.getContentResolver();
                resolver.takePersistableUriPermission(_data.getData(),
                        Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                if (!isPathExist(pathSelected)) {
                   // addUri(_data.getData(), pathSelected);
                   // saveData();
                } else {
                }
            }
        }
    }

    // resuest permission to manage all files access
    public void requestPermissionAllFilesAccess(){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                } catch (Exception ex){
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                    activity.startActivity(intent);
                }
            }
        } else {
            //below android 11=======
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                ActivityCompat.requestPermissions(activity, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1000);
            }
        }
    }

    private StringBuilder stringBuilder;
      private boolean copyFile(final String source, final String target, final String fileName) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        stringBuilder = new StringBuilder();

        try {
            inStream = new FileInputStream(source);
            Uri treeUri = uriFromPathXD(target);
                DocumentFile targetDocument = DocumentFile.fromTreeUri(activity, treeUri);

                delete(treeUri, fileName);

                DocumentFile  copy1 = targetDocument.createFile("*/*", fileName);
                
                if (copy1 != null) {
                    outStream = activity.getContentResolver().openOutputStream(copy1.getUri());
                }

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }

        }
        catch (Exception e) {
            stringBuilder.append(e);
            showMessage(stringBuilder.toString());
            return false;
        }
        finally {
    try {
        if (inStream != null) {
            inStream.close();
        }
    }
    catch (Exception e) {
        // ignore exception
    }
    try {
        if (outStream != null) {
            outStream.close();
        }
    }
    catch (Exception e) {
        // ignore exception
    }
}

        return true;
    }

    public boolean copySingleDir(String source, String target) {
        stringBuilder = new StringBuilder();
        if (SDK_INT >= 30) {
            FileInputStream fis = null;
            OutputStream os = null;
            File file = new File(source);
            File[] filez  = file.listFiles();
            Uri uri = uriFromPathXD(target);
            if (canReadAndWrite(uri)) {
                DocumentFile dir = DocumentFile.fromTreeUri(activity, uri);
                DocumentFile dir2 = dir.findFile(file.getName());
                if (dir2 != null) {
                    dir2.delete();
                }

                dir2 = dir.createDirectory(file.getName());


                for (File file1 : filez) {
                    try {
                        DocumentFile dFile = dir2.createFile("*/*", file1.getName());
                        fis = new FileInputStream(file1.getAbsolutePath());
                        os = activity.getContentResolver().openOutputStream(dFile.getUri());
                        if (os != null) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = fis.read(buffer)) != -1) {
                                os.write(buffer, 0, bytesRead);
                            }
                        }


                    } catch (Exception e) {
                        stringBuilder.append(e);
                      //  showText(stringBuilder.toString());
                        return false;
                    }
                    finally {

                        try {
                            fis.close();
                        } catch (Exception e) {
                            // ignore exception
                        }
                        try {
                            os.close();
                        } catch (Exception e) {
                            // ignore exception
                        }
                    }

                }
                return true;
            }
            else {
                askPermission(100, uri);
            }
        } else {
            FileUtil.deleteFile(target);
            FileUtil.copyDir(source, target);
            return true;
        }
        return false;
    }

    public boolean copyUri(Uri fileUri, String targetPath) {
        FileInputStream inStream = null;
        OutputStream outStream = null;
        stringBuilder = new StringBuilder();
        try {
            inStream = new FileInputStream(activity.getContentResolver().openFileDescriptor(fileUri, "r").getFileDescriptor());

             
         
        File file = new File(targetPath + fileNameFromUri(activity.getContentResolver(), fileUri));
        if(file.exists()) {
        file.delete();
        } 
        file.createNewFile();
         
                   outStream = new FileOutputStream(file.getAbsolutePath());
                

                if (outStream != null) {
                    // Both for SAF and for Kitkat, write to output stream.
                    byte[] buffer = new byte[4096]; // MAGIC_NUMBER
                    int bytesRead;
                    while ((bytesRead = inStream.read(buffer)) != -1) {
                        outStream.write(buffer, 0, bytesRead);
                    }
                }
                return true;
        } catch (Exception e) {
            stringBuilder.append(e);
        } finally {
            try {
                inStream.close();
            } catch (Exception e) {
                // ignore exception
            }
            try {
                outStream.close();
            } catch (Exception e) {
                // ignore exception
            }
        }
        return false;
    }

    private String fileNameFromUri(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }


    private static final String PRIMARY_VOLUME_NAME = "primary";

    @Nullable
    public String getFullPathFromTreeUri(@Nullable final Uri treeUri) {
        if (treeUri == null) return null;
        String volumePath = getVolumePath(getVolumeIdFromTreeUri(treeUri),activity);
        if (volumePath == null) return File.separator;
        if (volumePath.endsWith(File.separator))
            volumePath = volumePath.substring(0, volumePath.length() - 1);

        String documentPath = getDocumentPathFromTreeUri(treeUri);
        if (documentPath.endsWith(File.separator))
            documentPath = documentPath.substring(0, documentPath.length() - 1);

        if (documentPath.length() > 0) {
            if (documentPath.startsWith(File.separator))
                return volumePath + documentPath;
            else
                return volumePath + File.separator + documentPath;
        }
        else return volumePath;
    }


    private static String getVolumePath(final String volumeId, Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            return getVolumePathForAndroid11AndAbove(volumeId, context);
        else
            return getVolumePathBeforeAndroid11(volumeId, context);
    }


    private static String getVolumePathBeforeAndroid11(final String volumeId, Context context){
        try {
            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            Class<?> storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getUuid = storageVolumeClazz.getMethod("getUuid");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isPrimary = storageVolumeClazz.getMethod("isPrimary");
            Object result = getVolumeList.invoke(mStorageManager);

            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String uuid = (String) getUuid.invoke(storageVolumeElement);
                Boolean primary = (Boolean) isPrimary.invoke(storageVolumeElement);

                if (primary && PRIMARY_VOLUME_NAME.equals(volumeId))    // primary volume?
                    return (String) getPath.invoke(storageVolumeElement);

                if (uuid != null && uuid.equals(volumeId))    // other volumes?
                    return (String) getPath.invoke(storageVolumeElement);
            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.R)
    private static String getVolumePathForAndroid11AndAbove(final String volumeId, Context context) {
        try {
            StorageManager mStorageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            List<StorageVolume> storageVolumes = mStorageManager.getStorageVolumes();
            for (StorageVolume storageVolume : storageVolumes) {
                // primary volume?
                if (storageVolume.isPrimary() && PRIMARY_VOLUME_NAME.equals(volumeId))
                    return storageVolume.getDirectory().getPath();

                // other volumes?
                String uuid = storageVolume.getUuid();
                if (uuid != null && uuid.equals(volumeId))
                    return storageVolume.getDirectory().getPath();

            }
            // not found.
            return null;
        } catch (Exception ex) {
            return null;
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getVolumeIdFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if (split.length > 0) return split[0];
        else return null;
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String getDocumentPathFromTreeUri(final Uri treeUri) {
        final String docId = DocumentsContract.getTreeDocumentId(treeUri);
        final String[] split = docId.split(":");
        if ((split.length >= 2) && (split[1] != null)) return split[1];
        else return File.separator;
    }


    // CODES SPECIAL FOR COPY ASSETS FILE OR FOLDER FOR ALL ANDROID VERSIONS

    /// ---- copy assets for android 11 and above

    public void copyAssetsFolder(String  folder, String targetPath){
        if (SDK_INT >= Build.VERSION_CODES.R) {
                if (isPathExist(targetPath)){
                    boolean copy = copyAssetFolder(folder, getUriFromList(targetPath));
                    if(copy){
                        Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    pathSelected = targetPath;
                    askPermission(100, uriFromPathFull(targetPath));
                }
        } else {
            copyAllFilesAssets(folder, targetPath);
        }
    }
    
    // iAM1XDFILEUTILS2.0

    public void copyAssetFile(String  file, String targetPath){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (isPathExist(targetPath)){
                try {
                    Uri mUri = getUriFromList(targetPath);

                    String fileName = Uri.parse(file).getLastPathSegment();

                    DocumentFile dFile = DocumentFile.fromTreeUri(activity, mUri);

                    delete(mUri, fileName);

                    DocumentFile dFile2 = dFile.createFile("*/*", fileName);
                    mUri = dFile2.getUri();

                    if (copyAsset(file, mUri)) {
                        showMessage("Done");
                    } else {
                        showMessage(stringBuilder.toString());
                    }

                } catch (Exception re) {
                }

            } else {
                pathSelected = targetPath;
                askPermission(100, uriFromPathFull(targetPath));
            }
        } else {
            boolean copy = copyOneFileFromAssets(file, targetPath);
            if(copy){
                Toast.makeText(activity, "Done", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, stringBuilder.toString(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    //lib by - iAM1XD

    private boolean copyAssetFolder(String  _folder, Uri _out) {
        AssetManager assetManager = activity.getAssets();
        stringBuilder = new StringBuilder();
        int sizeList = 0;
        String[] files = null;
        try {
            files = assetManager.list(_folder);

        } catch (IOException e) {

        }
        final ArrayList<String> str = new ArrayList<>(Arrays.asList(files));
        try {
            int nn = 0;
            for (int _repeat12 = 0; _repeat12 < str.size(); _repeat12++) {

                try {
                    Uri mUri = _out;

                    String fileName = str.get(nn);
                    sizeList = str.size() - 1;

                    DocumentFile dFile = DocumentFile.fromTreeUri(activity, mUri);
                    Uri mUri2 = Uri.parse(mUri + "%2" + fileName);
                    DocumentFile dFile2 = DocumentFile.fromTreeUri(activity, mUri2);

                    try {

                        DocumentFile file = dFile.findFile(fileName);
                        DocumentsContract.deleteDocument(activity.getContentResolver(), file.getUri());
                        DocumentsContract.deleteDocument(activity.getContentResolver(), mUri2);
                    } catch (FileNotFoundException e) {
                    } catch (Exception e2) {
                    }


                    dFile2 = dFile.createFile("*/*", fileName);
                    mUri = dFile2.getUri();

                    if (copyAsset(_folder + "/" + fileName, mUri)) {
                    }

                } catch (Exception re) {
                }

                nn++;
            }
            return true;
        } catch (Exception tt){
            stringBuilder.append(tt);
            return false;
        }
    }
    
    //iAM1XD

    private boolean copyAsset(final String assetFilename, final Uri targetUri) {
        try{
            stringBuilder = new StringBuilder();
            int count;
            InputStream input = null;
            OutputStream output = null;

            ContentResolver content = activity.getContentResolver();

            input = activity.getAssets().open(assetFilename);

            output = content.openOutputStream(targetUri);


            byte[] data = new byte[1024];
            while ((count = input.read(data))>0) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
        }catch(Exception e){
            stringBuilder.append(e);
            return false;
        }
        return true;
    }

    // ---- COPY ASSETS FOR ANDROID 10 and bellow -----
    
    private boolean copyAllFilesAssets(String assetsFolder, String outPath) {
        AssetManager assetManager = activity.getAssets();
        String[] files = null;
        stringBuilder = new StringBuilder();
        try {
            files = assetManager.list(assetsFolder);
        } catch (IOException e) {

        }
        try {
            if (files != null) for (String filename : files) {
                InputStream in = null;
                OutputStream out = null;
                try {
                    in = assetManager.open(assetsFolder + "/" + filename);
                    File outFile = new File(outPath, filename);
                    if (!(outFile.exists())) {// File does not exist...
                        out = new FileOutputStream(outFile);
                        copyFile(in, out);
                        showMessage("success !");
                    }
                } catch (IOException e) {
                    showMessage(e.toString());
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                    if (out != null) {
                        try {
                            out.close();
                        } catch (IOException e) {
                            // NOOP
                        }
                    }
                }
            }
            return true;
        } catch (Exception u){
            stringBuilder.append(u);
            return false;
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
    
    //iAM1XD_YT

    private boolean copyOneFileFromAssets(final String assetFilename, final String assetSavePath) {
        stringBuilder = new StringBuilder();
        try{
            int count;
            InputStream input = activity.getAssets().open(assetFilename);
            OutputStream output = new  FileOutputStream(assetSavePath+"/"+assetFilename);
            byte[] data = new byte[1024];
            while ((count = input.read(data))>0) {
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            return true;
        }catch(Exception e){
            stringBuilder.append(e);
            return false;
        }
    }


    ///////////// COPY ASSETS ENDS ///////////

    private String getMimeType(Uri uri) {
        String mimeType = null;
        if (ContentResolver.SCHEME_CONTENT.equals(uri.getScheme())) {
            ContentResolver cr = activity.getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }
    
    //dev-iam1xd

    void showText(String message){
        AlertDialog dialog = new AlertDialog.Builder(activity).create();
        TextView text = new TextView(activity);
        text.setTextIsSelectable(true);
        text.setText(message);
        dialog.setView(text);
        dialog.show();
    }

    public void showMessage(String _s) {
        Toast.makeText(activity, _s, Toast.LENGTH_SHORT).show();
    }

    public boolean isExistFile(Uri uri){
            try {
                    InputStream inputStream = activity.getContentResolver()
                            .openInputStream(uri);
                    inputStream.close();
                    return true;
            } catch (Exception e) {
              return false;
            
            }
        
    }
    
    //iAM1XD

    public boolean isExistFile(String path){
        if (SDK_INT >= Build.VERSION_CODES.R){
            DocumentFile documentFile = DocumentFile.fromTreeUri(activity,
                    uriFromPathDir(path));
            DocumentFile fullUriFile = documentFile.findFile("jadx.sh");
            return fullUriFile != null;
        } else {
            return FileUtil.isExistFile(path);
        }
    }

    private boolean makedir(Uri treeUri, String dirName){
        try {
            DocumentFile documentFile = DocumentFile.fromTreeUri(activity, treeUri);
            DocumentFile directory = documentFile.createDirectory(dirName);
            return true;
        } catch (Exception ty){
            return false;
        }
    }

    public void makeDir(String directoryPath){
        if (SDK_INT >= Build.VERSION_CODES.R) {
          String fp = removeTrailingSlash(directoryPath);
          Uri treeUri = uriFromPathDirXD(fp);
            if (canReadAndWrite(treeUri)) {
                String directoryName = new File(fp).getName();
                
                boolean done = makedir(treeUri, directoryName);
                if (done){
                    //showMessage("Done");
                } else {
                  //showMessage("Error making folder")
                }
            } else {
                pathSelected = directoryPath;
                askPermission(100, uriFromPathDir(directoryPath));
            }
            
        } else {
            FileUtil.makeDir(directoryPath);
        }
    }

    public DocumentFile newDir(Uri treeUri, String dirName){
        DocumentFile documentFile = DocumentFile.fromTreeUri(activity, treeUri);
        return documentFile.createDirectory(dirName);
    }

    private boolean makefile(Uri treeUri, String fileName){
      
    
        try {
            DocumentFile documentFile = DocumentFile.fromTreeUri(activity, treeUri);
            DocumentFile file = documentFile.createFile("", fileName);
            return true;
        } catch (Exception tt){
            return false;
        }
    }
    public void makeFile(String filePath){
        if (SDK_INT >= Build.VERSION_CODES.R) {
          String fp = removeTrailingSlash(filePath);
            if (isDirectory2(fp)) {
              Uri treeUri = uriFromPathDirXD(fp);
              if(canReadAndWrite(treeUri)) {
               String fileName = new File(fp).getName();
               boolean done = makefile(treeUri, fileName);
               if (done) {
                   //showMessage("Done");
               } else {
                   showMessage(stringBuilder.toString());
               }
            } else {
                pathSelected = filePath;
                askPermission(100, uriFromPathDir(fp));
            }
         } else {
            FileUtil.writeFile("", filePath);
        }
       }
    }
    
    //iAM1XD
    
    public Boolean rename(String filePath, /*String oldName, */String newName) {
      File filez = new File(filePath);
     Boolean complete = false;
      if (isPath(filePath)) {
        if (SDK_INT <= 29) {
            complete = new File(filePath).renameTo(new File(filePath.replaceAll(filez.getName(), newName)));

      } else {
      
   DocumentFile fromTreeUri = DocumentFile.fromTreeUri(activity, uriFromPathDirXD(filePath));
   if (fromTreeUri.canRead() && fromTreeUri.canWrite()) {
     
   if (SDK_INT <= 32) {
    fromTreeUri = fromTreeUri.findFile(filez.getName());
   }
   if (fromTreeUri != null) {
   
    complete = fromTreeUri.renameTo(newName);

    }
   
  
       } else {
        pathSelected = filePath;
        askPermission(100, uriFromPathDir(filePath));
     }
     }
    
       } else {showMessage("No Directory");
      }
        return complete;
    }
    
    //iAM1XD
    
/*public static long getDirectorySize(Context context, Uri uri) {
    ContentResolver contentResolver = context.getContentResolver();

    String[] projection = new String[]{DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE};
    long totalSize = 0;

    try (Cursor cursor = contentResolver.query(uri, projection, null, null, null)) {
        while (cursor.moveToNext()) {
            String mimeType = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
            if (DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType)) {
                int sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);
                Long fileSize = cursor.getLong(sizeIndex);
                if (fileSize != null && fileSize > 0) {
                    totalSize += fileSize;
                }

                String documentId = cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(uri, documentId);

                totalSize += getDirectorySize(context, childrenUri);
            } else {
                int sizeIndex = cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE);
                Long fileSize = cursor.getLong(sizeIndex);
                if (fileSize != null && fileSize > 0) {
                    totalSize += fileSize;
                }
            }
        }
    } catch (Exception ex) {
        //showMessage("Error calculating directory size");
    }

    return totalSize;

}

//Under construction



      public long fileSize(String path) {
      
      Uri fileP = uriFromPathFull(path);
      DocumentFile fromTreeUri = DocumentFile.fromTreeUri(activity, fileP);
      return getDirectorySize(activity, fileP); }
    
    
    /*public double fileSize(String filePath) {
      long bytes = 0;
     Uri fileP = uriFromPathXD(filePath);
    DocumentFile fromTreeUri = DocumentFile.fromTreeUri(activity, fileP);
        for (DocumentFile file : fromTreeUri.listFiles()) {
                if (file.isFile()) {
                    bytes += file.length();
                } else {
                    bytes += fileSize(file.getUri().getPath().replace("/tree/primary:Android/data/com.pubg.imobile/document/primary:", "/storage/emulated/0/"));
                    showMessage("3" + file.getUri().getPath().replace("/tree/primary:Android/data/com.pubg.imobile/document/primary:", "/storage/emulated/0/"));
                }
                }
    
    return bytes;
    }*/
    private boolean deleteDir(Uri treeUri) {
    DocumentFile dfile = DocumentFile.fromTreeUri(activity, treeUri);
    if (dfile != null) {
      dfile.delete();
      return true;
    } else {
      return false;
    } 
    }

    private boolean delete(Uri treeUri, String fileName) {
            DocumentFile dfile = DocumentFile.fromTreeUri(activity, treeUri);
            DocumentFile file = dfile.findFile(fileName);
            if (file != null) {
              file.delete();
              return true;
            } else {
            return false;
            }
            }
            
    public void deleteFile(String fileP){
      
      if (SDK_INT >= Build.VERSION_CODES.R) {
    if (canReadAndWrite(uriFromPathDirXD(fileP))) {
      
        String fileName = new File(fileP).getName();
        
          Uri treeUri = uriFromPathDirXD(fileP);
          if (fileP.endsWith("/")) {
            if (deleteDir(treeUri)) {
              //   showMessage("directory deleted");
                } else {
              //     showMessage("directory not found");
                 }   
          } else {
               if (delete(treeUri, fileName)) {
              //   showMessage("deleted");
                } else {
                //   showMessage("not found");
                 }
                }
            } else {
                pathSelected = fileP;
                askPermission(100, uriFromPathDir(fileP));
            }
           
        } else {
            FileUtil.deleteFile(fileP);
        }

    }
    
    public  boolean isPath(String dirPath) {
      if (SDK_INT <= 33) {
        Uri treeUri = uriFromPathFull(dirPath);
        String mimeType = getMimeType(treeUri);
        return mimeType != null;
        } else {
          return true;
        }
    }
    

    public  boolean isDirectory(String dirPath) {
      if (SDK_INT <= 33) {
        Uri treeUri = uriFromPathFull(dirPath);
        String mimeType = getMimeType(treeUri);
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
         } else {
          return true;
        }
    }
    
    public  boolean isDirectory2(String dirPath) {
      if (SDK_INT <= 33) {    
        Uri treeUri = uriFromPathDir(removeTrailingSlash(dirPath));
        String mimeType = getMimeType(treeUri);
        return DocumentsContract.Document.MIME_TYPE_DIR.equals(mimeType);
         } else {
          return true;
        }
    }

    public void listDir(String dirPath, ArrayList<String> list){
        if (SDK_INT >= Build.VERSION_CODES.R) {
            String cleanDirPath = dirPath;
            if (cleanDirPath.endsWith("/")){
                cleanDirPath = cleanDirPath.substring(0, cleanDirPath.length()-1);
            }
            if (isPathExist(dirPath)) {
                Uri treeUri = getUriFromList(dirPath);
                DocumentFile directory = DocumentFile.fromTreeUri(activity, treeUri);
                DocumentFile[] listFiles = directory.listFiles();
                if (listFiles != null){
                    for (DocumentFile documentFile : listFiles){
                      //  String fullPath = cleanDirPath+"/"+documentFile.getName();
                        String fullPath = cleanDirPath+"/"+documentFile.getName();

                        list.add(documentFile.getName());
                    }
                }
            } else {
                pathSelected = dirPath;
                askPermission(100, uriFromPathFull(dirPath));
            }
        } else {
            FileUtil.listDir(dirPath, list);
        }
    }

    public void batchDelete(String dirPath, String name){
        if (SDK_INT >= Build.VERSION_CODES.R) {

            if (isPath(dirPath)) {
                Uri treeUri = uriFromPathXD(dirPath);
                DocumentFile directory = DocumentFile.fromTreeUri(activity, treeUri);
                if (directory != null) {
                    if (directory.canRead() && directory.canWrite()) {

                        DocumentFile[] listFiles = directory.listFiles();
                        if (listFiles != null){
                            for (DocumentFile documentFile : listFiles){
                                if (documentFile.getName().startsWith(name)) {
                                    documentFile.delete();
                                }
                            }
                        }

                    } else {
                        pathSelected = dirPath;
                        askPermission(100, uriFromPathFull(dirPath));
                    }
                }
            }
        } else {
            File dir = new File(dirPath);
            File[] files = dir.listFiles();
            for (File file : files) {
                if (file.getName().startsWith(name)) {
                    file.delete();
                }
            }
        }
    }

    private String reafFileUri(Uri fileUri) {
        StringBuffer datax = new StringBuffer();
        try {
            FileInputStream fIn =
                    new FileInputStream(activity.getContentResolver().openFileDescriptor(fileUri, "r").getFileDescriptor());

            InputStreamReader isr = new InputStreamReader ( fIn ) ;
            BufferedReader buffreader = new BufferedReader ( isr ) ;

            String readString = buffreader.readLine ( ) ;
            while ( readString != null ) {
                datax.append(readString);
                datax.append("\n");
                readString = buffreader.readLine ( ) ;
            }

            isr.close ( ) ;
        } catch ( IOException ioe ) {
            ioe.printStackTrace ( ) ;
        }
        return datax.toString() ;
    }

    public String readFile(String pathFile){
        File file = new File(pathFile);
        String content = "";
        if (SDK_INT >= Build.VERSION_CODES.R){
          
        if (isPath(pathFile)) {
          String fp = removeTrailingSlash(pathFile);
        String fileName = new File(fp).getName();
        
                DocumentFile directory = DocumentFile.fromTreeUri(activity, uriFromPathDirXD(fp));
                if(directory.canRead() && directory.canWrite()) {
                DocumentFile targetFile = directory.findFile(fileName);
                if (targetFile!=null){
                    content = reafFileUri(targetFile.getUri());
                }
                
            } else {
            
                pathSelected = pathFile;
                askPermission(100, uriFromPathDir(fp));
                showMessage("Please give folder permission");
                }
                } else {
                  showMessage("Error read, File not found");
                }
            
        } else {
            content = FileUtil.readFile(pathFile);
        }
        return content;
    }
  


    private void writeFileUri(Uri uri, String data) {
        try {
            OutputStream fOut =  activity.getContentResolver().openOutputStream(uri, "w");
            OutputStreamWriter osw = new OutputStreamWriter (fOut) ;
            osw.write (data);
            osw.flush ();
            osw.close ();
        } catch ( Exception e ) {
            e.printStackTrace ( ) ;
        }
    }

    public void writeFile(String pathFile, String content){
        
        if (SDK_INT >= Build.VERSION_CODES.R){
          String fp = removeTrailingSlash(pathFile);
            if (isDirectory2(fp)){
        String fileName = new File(fp).getName();
                Uri treeUri = uriFromPathDirXD(fp);
                DocumentFile directory = DocumentFile.fromTreeUri(activity, treeUri);
                if (directory.canRead() && directory.canWrite()) {
                DocumentFile targetFile = directory.findFile(fileName);
                if (targetFile==null){
                    DocumentFile targetF = directory.createFile("", fileName);
                    writeFileUri(targetF.getUri(), content);
                //    showMessage("Done ✅");
                } else {
                    targetFile.delete();
                    DocumentFile targetF = directory.createFile("", fileName);
                    writeFileUri(targetF.getUri(), content);
               //     showMessage("Done ✅");
                }
            } else {
                pathSelected = pathFile;
                askPermission(100, uriFromPathDir(fp));
                showMessage("Please give folder permission");
            }
        } else {
          showMessage("Error write, File not found");
        
        }
    } else {
      FileUtil.writeFile(pathFile, content);
     //  showMessage("Done ✅");
      }
     }

    public void unZip(InputStream zipFile, String outputFolder) {
        byte[] buffer = new byte[1024];
        try {
        //    java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream(new FileInputStream(zipFile));
            java.util.zip.ZipInputStream zis = new java.util.zip.ZipInputStream((zipFile));
            java.util.zip.ZipEntry ze = zis.getNextEntry();
            while (ze != null) {
                String fileName = ze.getName();
                File newFile = new File(outputFolder + File.separator + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }
                ze = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public static String formatDate(long milliseconds) {
        Instant instant = Instant.ofEpochMilli(milliseconds);
        LocalDateTime dateTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm");
        return dateTime.format(formatter);
    }



public static void listDirMap(String path, ArrayList<HashMap<String, Object>> fileList) {
    File dir = new File(path);
    if (!dir.exists() || dir.isFile()) return;

    File[] listFiles = dir.listFiles();
    if (listFiles == null || listFiles.length <= 0) return;

    if (fileList == null) return;
    fileList.clear();
    for (File file : listFiles) {
        HashMap<String, Object> fileInfo = new HashMap<>();
        fileInfo.put("file_path", file.getAbsolutePath());
        fileInfo.put("file_name", file.getName());
       // fileInfo.put("file_size", FileUtil.fileSize(file.getAbsolutePath()));
        fileInfo.put("file_last_modified", formatDate(file.lastModified()));
        fileList.add(fileInfo);

  }
 }
 
}

