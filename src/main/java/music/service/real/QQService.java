package music.service.real;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import music.pojo.MainFormPojo;
import music.pojo.qq.QQPojo;
import music.pojo.qq.Singer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tool.HttpTool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

@Service
public class QQService implements MusicServices {

    @Value("#{config['search.pagesize']}")
    private String pagesize;

    @Autowired
    private HttpTool httpTool;

    public JSONArray getFormatJson(MainFormPojo form) {
        String pristineJson = getSearchResult(form);
        JSONArray jsonList = JSON.parseObject(pristineJson).getJSONObject("data").getJSONObject("song").getJSONArray("list");
        List<QQPojo> list = jsonList.toJavaList(QQPojo.class);
        JSONArray array = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            JSONObject json = new JSONObject();
            QQPojo single = list.get(i);
            List<Singer> singerList = single.getSinger();
            StringBuilder singer = new StringBuilder();
            for (int a = 0; a < singerList.size(); a++) {
                singer.append(singerList.get(a).getName()).append(" ");
            }
            json.put("singer", singer);
            json.put("name", single.getSongname());
            json.put("album", single.getAlbumname());
            json.put("s128", single.getSize128() == 0 ? 0 : single.getSongmid());
            json.put("s320", single.getSize320() == 0 ? 0 : single.getSongmid());
            json.put("sogg", single.getSizeogg() == 0 ? 0 : single.getSongmid());
            json.put("SQ", 0);
            //把小于10的秒数前面加个“0”
            int seconds = single.getInterval() % 60;
            String secStr = seconds < 10 ? "0" + String.valueOf(seconds) : String.valueOf(seconds);
            json.put("time", single.getInterval() / 60 + ":" + secStr);
            json.put("mv", single.getVid().equals("") ? 0 : single.getVid());
            array.add(json);
        }
        return array;
    }

    @Override
    public String getSearchResult(MainFormPojo form) {

        String keyword = null;
        try {
            keyword = URLEncoder.encode(form.getKeyword(), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String url = "http://soso.music.qq.com/fcgi-bin/search_cp?aggr=0&catZhida=0&lossless=1&sem=1&w="
                + keyword + "&n=" + pagesize + "&t=0&p=" + form.getPage() + "&remoteplace=sizer.yqqlist.song&g_tk=5381&loginUin=0&" +
                "hostUin=0&format=jsonp&inCharset=GB2312&outCharset=utf-8&notice=0&platform=yqq&needNewCode=0";
        String result = this.httpTool.getJsonResultWithGet(url);
        result = result.substring(result.indexOf("(") + 1, result.lastIndexOf(")"));
        return result;
    }


}
