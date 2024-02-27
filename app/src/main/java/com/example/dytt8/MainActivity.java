package com.example.dytt8;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.SyncFailedException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;


public class MainActivity extends AppCompatActivity {

    List<MovieCtx> movieCtxes = new ArrayList<>();

    static String BASE_CODEC = "GB2312";
    static String base_url = "https://dytt.dytt8.net";
    String index_url = "https://dytt.dytt8.net/html/gndy/dyzz/index.html";
    ListView listView_movies = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(MainActivity.this, "更新列表中...", Toast.LENGTH_LONG).show();
        update_movies_list();
    }

    public void getMovieInfo(String url_detail, MovieCtx ctx) {
        try {
            String url = base_url + url_detail;
            URL obj = new URL(url);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestProperty("User-Agent", "Mozilla/5.0");

            // 设置请求的编码为GB2312
            con.setRequestProperty("Accept-Charset", BASE_CODEC);

            int responseCode = con.getResponseCode();
//            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), BASE_CODEC));

            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            String content = response.toString();

            // 使用Jsoup解析HTML内容
            org.jsoup.nodes.Document soup = Jsoup.parse(content);

//            System.out.println(soup);
            // 查找所有的td标签
            Elements tds = soup.select("div[id=Zoom]");
//            System.out.println(tds.toString());
            for (org.jsoup.nodes.Element td : tds) {
//                System.out.println(td.text());
                if (td.text().contains("片　　名")) {

                    // 查找img标签
                    Elements imgs = td.select("img");
                    for (org.jsoup.nodes.Element img : imgs) {
                        String src = img.attr("src");
                        ctx.url_img = img.toString();
                        ctx.webctx = tds.toString();
//                        System.out.println(td.text());
                        System.out.println(img.toString());
                        break; // 如果只需要第一个匹配的img标签，可以在这里break
                    }
                    break; // 如果只处理第一个匹配的td，可以在这里break
                }
            }

        } catch (Exception e) {
//            e.printStackTrace();
//            Toast.makeText(e.toString(), )
//            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }


    void update_movies_list() {

        Handler handler = new Handler(Looper.getMainLooper());
        new Thread() {
            @Override
            public void run() {
                try {
                    URL obj = new URL(index_url);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestProperty("User-Agent", "Mozilla/5.0");

                    // 设置请求的编码，这里使用UTF-8，因为Jsoup默认使用UTF-8
                    //            con.setRequestProperty("Accept-Charset", "UTF-8");
                    con.setRequestProperty("Accept-Charset", BASE_CODEC);

                    String charset = con.getContentEncoding();
                    if (charset == null || !charset.equalsIgnoreCase(BASE_CODEC)) {
                        // 如果服务器没有指定编码，或者编码不是GB2312，手动设置为GB2312
                        charset = BASE_CODEC;
                    }

                    int responseCode = con.getResponseCode();
                    System.out.println("Response Code : " + responseCode);

                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));

                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String content = response.toString();

                    // 使用Jsoup解析HTML内容
                    org.jsoup.nodes.Document soup = Jsoup.parse(content);

                    // 查找所有的option标签
                    //            Elements options = soup.select("option");
                    //            for (org.jsoup.nodes.Element option : options) {
                    //                System.out.println(option.attr("value") + " " + option.text());
                    //            }

                    // 查找所有的a标签，其中包含class "ulink"
                    movieCtxes.clear();
                    Elements links = soup.select("a[class=ulink]");
                    for (org.jsoup.nodes.Element link : links) {
                        String href = link.attr("href");
                        String text = link.text();
                        System.out.println(href + " " + text);
                        MovieCtx ctx = new MovieCtx(text, href);

                        movieCtxes.add(ctx);
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            listView_movies = findViewById(R.id.list_movies);

                            CtxAdapter lll = new CtxAdapter(MainActivity.this, R.layout.list_ctx_view, movieCtxes);
                            listView_movies.setAdapter(lll);
                        }
                    });
                } catch (Exception e) {
//                    e.printStackTrace();

                    handler.post(new Runnable() {
                        @Override
                        public void run() {

                            Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_LONG).show();

                        }
                    });
                }


            }

        }.start();


    }


    class CtxAdapter extends ArrayAdapter<MovieCtx> {
        private final int RESOURCE_ID;
        Map<Integer, View> mDic = new TreeMap<Integer, View>();
        Map<Integer, Intent> mDic_in = new TreeMap<Integer, Intent>();

        @Override
        public View getView(int position, View view, ViewGroup parent) {

//            if (mDic.containsKey(position)) {
//                return mDic.get(position);
//            } else {
            @SuppressLint("ViewHolder") View view1 = LayoutInflater.from(parent.getContext()).inflate(RESOURCE_ID, parent, false);
            MovieCtx data = getItem(position);
            TextView txt = view1.findViewById(R.id.movie_name);
            txt.setText(data.name);

            WebView img = view1.findViewById(R.id.movie_img);
//                img.setImageURL(data.url_img);
            mDic.put(position, view1);

            Handler handler = new Handler(Looper.getMainLooper());
            new Thread() {
                @Override
                public void run() {
                    getMovieInfo(data.url_detail, data);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            img.setInitialScale(85);
                            img.loadData(data.url_img, "text/html", base_url);
                        }
                    });
                }

            }.start();

            view1.setOnClickListener(view2 -> {
                System.out.println("here");
                if (mDic_in.containsKey(position)) {
                    startActivity(mDic_in.get(position));
                    return;
                }

                Intent in = new Intent();
                in.setClass(MainActivity.this, webview_movie.class);
                in.putExtra("web_txt", data.webctx);
                mDic_in.put(position, in);
                startActivity(in);
            });

            return view1;
//            }


        }

        public CtxAdapter(@NonNull Context context, int resource, @NonNull List<MovieCtx> objects) {
            super(context, resource, objects);
            RESOURCE_ID = resource;
        }
    }
}