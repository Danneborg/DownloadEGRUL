import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DownloadEGRUL {
    private static String targetURL = "https://egrul.nalog.ru/";

    //Возвращаем урл для скачивания файла
    private static String GetStringUrlRequestLoadFile(String secondToken) {

        return targetURL + "vyp-download/" + secondToken;

    }
    /*
    * Just a commit to check it is working*/
    private static void LoadFile(String ogrn, String name) {
        //Получаем токен для огрн
        JSONObject token = new JSONObject(doPostToken(ogrn));
//            System.out.println(token.getString("t"));
        String url = getStringGetQuery(token.getString("t"));
        //Получаем 2ой токен, для дальнейшего доступа к выписке
        JSONObject token2 = new JSONObject(new JSONArray(new JSONObject(doGetToken(url)).get("rows").toString()).get(0).toString());
//            System.out.println(token2.getString("t"));
        //Перед скачиванием нужно дождаться готовности файла с выпиской
        boolean status = Pause(token2.getString("t"));
        if (status) {
            String fileName = name + ".pdf";
            try {
                FileUtils.copyURLToFile(new URL(GetStringUrlRequestLoadFile(token2.getString("t"))), new File(fileName));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


    }

    private static boolean Pause(String secondToken) {
        doGetToken(GetStringUrlRequestSt(secondToken));
        try {
            int count = 10;
            while (count-- > 0) {
                Thread.sleep(200);
                String str = doGetToken(GetStringUrlRequestStatus(secondToken));
                JSONObject statusObject = new JSONObject(str);
                if (statusObject.getString("status").equals("ready")) {
                    return true;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Получаем урл по второму токену для создания статуса
    private static String GetStringUrlRequestSt(String token) {
        String t = "672";
        String time = String.valueOf(java.time.Instant.now().getEpochSecond());
        return targetURL + "vyp-request/" + token + "?r=" + time + t + "&_" + time + t;
    }

    //Получаем урл по второму токену для проверки статуса готовности файла
    private static String GetStringUrlRequestStatus(String token) {
        String t = "672";
        String time = String.valueOf(java.time.Instant.now().getEpochSecond());
        return targetURL + "vyp-status/" + token + "?r=" + time + t + "&_" + time + t;
    }

    //Создаем ссылку с токеном и меткой времени
    private static String getStringGetQuery(String token) {
        String stub = "672";
        String time = String.valueOf(java.time.Instant.now().getEpochSecond());
//        return $"{_urlService}search-result/{token}?r={time}{t}&_{time}{t}";
        return targetURL + "search-result/" + token + "?r=" + time + stub + "&_" + time + stub;
    }

    //Получаем токен гетом
    private static String doGetToken(String url) {
        String result = "";
        try {
            URL urlLink = new URL(url);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) urlLink.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);

            //Get Response
            connection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            in.close();
            JSONObject j = new JSONObject(stringBuilder.toString());
//            System.out.println(stringBuilder.toString());
//            result = j.getString("t");
            connection.disconnect();
            result = stringBuilder.toString();
        } catch (Exception e) {
        }
        return result;
    }

    //Получаем токен постом, передаем сюда огрн
    private static String doPostToken(String ogrn) {
        String result = "";
        try {
            String urlParameters =
                    "$vyp3CaptchaToken=" +
                            "&page=" +
                            "&query=" + ogrn +
                            "&region=" +
                            "&PreventChromeAutocomplete=;";
            URL urlLink = new URL(targetURL);
            HttpURLConnection connection = null;
            connection = (HttpURLConnection) urlLink.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            //Send request
            DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
            wr.writeBytes(urlParameters);
            wr.flush();
            wr.close();

            //Get Response
            connection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            String inputLine;
            StringBuilder stringBuilder = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                stringBuilder.append(inputLine);
            }
            connection.disconnect();
            in.close();
            result = stringBuilder.toString();
        } catch (Exception e) {
        }
        return result;
    }

    public static void main(String[] args) {
        try {
            LoadFile("1027700198767", "test1");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}