package org.example.Client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.figure.Circle;
import org.example.figure.Line;
import org.example.figure.Rectangle;

import java.io.*;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class ClientLogic {
    private final String host;
    private final int port;
    private final Gson gson=new Gson();

    public ClientLogic(String host,int port){
        this.host=host;this.port=port;
    }

    public String sendCircle(Circle c){
        return sendObject("Circle",c);
    }

    public String sendRectangle(Rectangle r){
        return sendObject("Rectangle",r);
    }

    public String sendLine(Line l){
        return sendObject("Line",l);
    }

    private String sendObject(String type, Object obj) {
        Map<String, Object> map = new HashMap<>();
        map.put("type", type);
        map.put("data", gson.fromJson(gson.toJson(obj), Map.class));
        String json = gson.toJson(map);
        return postJson("/objects", json);
    }


    private Map<String,Object> objectToMap(String type,Object obj){
        @SuppressWarnings("unchecked")
        Map<String,Object> map = gson.fromJson(gson.toJson(obj), Map.class);
        map.put("type",type);
        return map;
    }

    private String postJson(String path,String json){
        try{
            URL url=new URL("http://"+host+":"+port+path);
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");
            conn.setDoOutput(true);
            try(OutputStream os=conn.getOutputStream()){
                os.write(json.getBytes());
            }

            int code=conn.getResponseCode();
            if(code==201 || code==200){
                try(InputStream in=conn.getInputStream();
                    InputStreamReader isr=new InputStreamReader(in);
                    BufferedReader br=new BufferedReader(isr)){
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while((line=br.readLine())!=null) sb.append(line);
                    return sb.toString();
                }
            } else {
                try(InputStream err=conn.getErrorStream();
                    InputStreamReader isr=new InputStreamReader(err);
                    BufferedReader br=new BufferedReader(isr)){
                    StringBuilder sb=new StringBuilder("Error code "+code+": ");
                    String line;
                    while((line=br.readLine())!=null) sb.append(line);
                    return sb.toString();
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            return "IO Error: "+e.getMessage();
        }
    }

    public List<Object> getAllObjects(){
        try{
            URL url=new URL("http://"+host+":"+port+"/objects");
            HttpURLConnection conn=(HttpURLConnection)url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept","application/json");
            int code=conn.getResponseCode();
            if(code==200){
                try(InputStream in=conn.getInputStream();
                    InputStreamReader isr=new InputStreamReader(in);
                    BufferedReader br=new BufferedReader(isr)){
                    StringBuilder sb=new StringBuilder();
                    String line;
                    while((line=br.readLine())!=null) sb.append(line);
                    String json=sb.toString();

                    Type listType=new TypeToken<List<Map<String,Object>>>(){}.getType();
                    List<Map<String,Object>> list=gson.fromJson(json,listType);
                    return mapListToObjects(list);
                }
            } else {
                return Collections.emptyList();
            }
        }catch(IOException e){
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    private List<Object> mapListToObjects(List<Map<String, Object>> list) {
        List<Object> result = new ArrayList<>();
        for (Map<String, Object> m : list) {
            Object obj = mapToObject(m);
            if (obj != null) {
                result.add(obj);
            }
        }
        return result;
    }

    private Object mapToObject(Map<String, Object> m) {
        String type = (String) m.get("type");
        if (type == null) return null;

        Object data = m.get("data");
        if (!(data instanceof Map)) return null;

        String jsonData = gson.toJson(data);

        switch (type) {
            case "Circle":
                return gson.fromJson(jsonData, Circle.class);
            case "Rectangle":
                return gson.fromJson(jsonData, Rectangle.class);
            case "Line":
                return gson.fromJson(jsonData, Line.class);
            default:
                return null;
        }
    }
}
