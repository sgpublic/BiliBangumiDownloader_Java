package com.sgpublic.bilidownload.BangumiAPI;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;

public class APIHelper {
    private static final String TAG = "APIHelper";

    private static final String build = "5442100";
    private static final String android_key = "4409e2ce8ffd12b8";
    private static final String platform = "android";

    private static final int METHOD_GET = 0;
    private static final int METHOD_POST = 1;

    private String access_token;

    public APIHelper() {
    }

    APIHelper(String access_token) {
        this.access_token = access_token;
    }

    Call getKeyRequest() {
        String url = "https://passport.bilibili.com/api/oauth2/getKey";
        String[][] arg_array = new String[][]{
                {"appkey", android_key},
                {"mobi_app", platform},
                {"platform", platform},
                {"ts", getTS()}
        };
        return onReturn(url, METHOD_POST, arg_array, true);
    }

    Call getLoginRequest(String username, String password_encrypted) {
        String url = "https://passport.bilibili.com/api/v3/oauth2/login";
        String[][] arg_array = {
                {"appkey", android_key},
                {"build", build},
                {"gee_type", "10"},
                {"mobi_app", platform},
                {"password", password_encrypted},
                {"platform", platform},
                {"ts", getTS()},
                {"username", username}
        };
        String[][] header_array = {
                {"User-Agent", "Mozilla/5.0 (bbcallen@gmail.com)"}
        };
        return onReturn(url, METHOD_POST, arg_array, header_array, true);
    }

    Call getLoginWebRequest(String cookie, String user_agent) {
        String url = "https://passport.bilibili.com/login/app/third";
        String[][] arg_array = {
                {"appkey", "27eb53fc9058f8c3"},
                {"api", "http://link.acg.tv/forum.php"},
                {"sign", "67ec798004373253d60114caaad89a8c"}
        };
        String[][] header_array = {
                {"Cookie", cookie},
                {"User-Agent", user_agent}
        };
        return onReturn(url, METHOD_GET, arg_array, header_array, false);
    }

    Call getRefreshTokenRequest(String refresh_token){
        String url = "https://passport.bilibili.com/api/oauth2/refreshToken";
        String[][] arg_array = {
                {"access_token", access_token},
                {"appkey", android_key},
                {"refresh_token", refresh_token},
                {"ts", getTS()}
        };
        return onReturn(url, METHOD_POST, arg_array, true);
    }

    Call getLoginConfirmRequest(String url, String cookie, String user_agent) {
        String[][] header_array = {
                {"Connection", "keep-alive"},
                {"Upgrade-Insecure-Requests", "1"},
                {"User-Agent", user_agent},
                {"Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*,q=0.8"},
                {"Accept-Encoding", "gzip, deflate"},
                {"Accept-Language", "zh-CH,en-US;q=0.8"},
                {"Cookie", cookie},
                {"X-Requested-With", "com.sgpublic.bilidownload"}
        };
        return onReturn(url, METHOD_GET, null, header_array, false);
    }

    Call getUserInfoRequest(String mid) {
        String url = "https://api.bilibili.com/x/space/acc/info";
        String[][] arg_array = new String[][]{
                {"mid", mid}
        };
        return onReturn(url, METHOD_GET, arg_array, false);
    }

    Call getFollowsRequest(long mid, int page_index, int status) {
        String url = "https://api.bilibili.com/pgc/app/follow/v2/bangumi";
        String[][] arg_array = {
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"mid", String.valueOf(mid)},
                {"pn", String.valueOf(page_index)},
                {"ps", "18"},
                {"status", String.valueOf(status)},
                {"ts", getTS()},
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    Call getHotWordRequest() {
        String url = "https://s.search.bilibili.com/main/hotword";
        return onReturn(url, METHOD_GET, null, false);
    }

    Call getSearchResultRequest(String keyword) {
        String url = "https://api.bilibili.com/x/web-interface/search/type";
        String[][] arg_array = {
                {"search_type", "media_bangumi"},
                {"keyword", keyword}
        };
        String[][] header_array = {
                {"Referer", "https://search.bilibili.com"}
        };
        return onReturn(url, METHOD_GET, arg_array, header_array, false);
    }

    Call getSearchSuggestRequest(String keyword) {
        String url = "https://s.search.bilibili.com/main/suggest";
        String[][] arg_array = {
                {"main_ver", "v1"},
                {"special_acc_num", "1"},
                {"topic_acc_num", "1"},
                {"upuser_acc_num", "3"},
                {"tag_num", "10"},
                {"special_num", "10"},
                {"bangumi_num", "10"},
                {"upuser_num", "3"},
                {"term", keyword}
        };
        String[][] header_array = {
                {"Referer", "https://search.bilibili.com"}
        };
        return onReturn(url, METHOD_GET, arg_array, header_array, false);
    }

    Call getSeasonInfoAppRequest(long sid) {
        String url = "https://api.bilibili.com/pgc/view/app/season";
        String[][] arg_array = new String[][]{
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"platform", platform},
                {"season_id", String.valueOf(sid)},
                {"ts", getTS()},
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    Call getSeasonInfoWebRequest(long sid) {
        String url = "https://api.bilibili.com/pgc/view/web/season";
        String[][] arg_array = new String[][]{
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"c_locale", "hk_CN"},
                {"platform", platform},
                {"s_locale", "hk_CN"},
                {"season_id", String.valueOf(sid)},
                {"ts", getTS()},
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    @Deprecated
    Call getSeasonInfoOldRequest(long sid) {
        String url = "https://bangumi.bilibili.com/view/web_api/season";
        String[][] arg_array = new String[][]{
                {"season_id", String.valueOf(sid)}
        };
        return onReturn(url, METHOD_GET, arg_array, false);
    }

    Call getEpisodeOfficialRequest(long cid, int qn) {
        String url = "https://api.bilibili.com/pgc/player/api/playurl";
        String[][] arg_array = new String[][]{
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"cid", String.valueOf(cid)},
                {"fnval", "16"},
                {"fnver", "0"},
                {"fourk", "1"},
                {"module", "bangumi"},
                {"otype", "json"},
                {"platform", platform},
                {"qn", String.valueOf(qn)},
                {"season_type", "1"},
                {"ts", getTS()}
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    Call getEpisodeBiliplusRequest(long cid, int qn) {
        String url = "https://www.biliplus.com/BPplayurl.php";
        String[][] arg_array = new String[][]{
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"cid", String.valueOf(cid)},
                {"device", platform},
                {"fnval", "16"},
                {"fnver", "0"},
                {"fourk", "1"},
                {"module", "bangumi"},
                {"otype", "json"},
                {"platform", platform},
                {"qn", String.valueOf(qn)},
                {"season_type", "1"},
                {"ts", getTS()}
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    Call getEpisodeKghostRequest(long cid, int qn) {
        String url = "https://bilibili-tw-api.kghost.info/pgc/player/web/playurl";
        String[][] arg_array = new String[][]{
                {"access_key", access_token},
                {"appkey", android_key},
                {"build", build},
                {"cid", String.valueOf(cid)},
                {"device", platform},
                {"fnval", "16"},
                {"fnver", "0"},
                {"fourk", "1"},
                {"module", "bangumi"},
                {"otype", "json"},
                {"platform", platform},
                {"qn", String.valueOf(qn)},
                {"season_type", "1"},
                {"ts", getTS()}
        };
        return onReturn(url, METHOD_GET, arg_array, true);
    }

    Call getDanmakuRequest(long cid) {
        String url = "https://api.bilibili.com/x/v1/dm/list.so";
        String[][] arg_array = new String[][]{
                {"oid", String.valueOf(cid)}
        };
        return onReturn(url, METHOD_GET, arg_array, false);
    }

    public Call getUpdateRequest(String version) {
        String url = "https://sgpublic.xyz/bilidl/update/index.php";
        String[][] arg_array = new String[][]{
                {"version", version}
        };
        return onReturn(url, METHOD_POST, arg_array, false);
    }

    private Call onReturn(String url, int method, String[][] arg_array, boolean with_sign) {
        return onReturn(url, method, arg_array, null, with_sign);
    }

    private Call onReturn(String url, int method, String[][] arg_array, String[][] header_array, boolean with_sign) {
        OkHttpClient client = new OkHttpClient.Builder()
                .readTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(5, TimeUnit.SECONDS)
                .connectTimeout(5, TimeUnit.SECONDS)
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request.Builder requestBuilder = new Request.Builder();
        boolean give_user_agent = false;
        if (header_array != null) {
            for (String[] header_index : header_array) {
                requestBuilder.addHeader(header_index[0], header_index[1]);
                if (header_index[0].equals("User-Agent")) {
                    give_user_agent = true;
                }
            }
        }
        if (!give_user_agent) {
            requestBuilder.addHeader("User-Agent", "Bilibili Freedoooooom/Markll");
        }

        String url_final = url;
        if (arg_array != null) {
            if (method == METHOD_POST) {
                requestBuilder.post(new GetArgs(arg_array).getForm(with_sign));
            } else if (method == METHOD_GET) {
                url_final = url + "?" + new GetArgs(arg_array).getString(with_sign);
            }
        }

        Log.d(TAG, "onReturn: " + url_final);

        requestBuilder.url(url_final);
        return client.newCall(requestBuilder.build());
    }

    static class GetArgs {
        String[][] arg_array;

        String arg_string;
        FormBody.Builder arg_form = new FormBody.Builder();

        GetArgs(String[][] arg_array) {
            this.arg_array = arg_array;

            StringBuilder arg_builder = new StringBuilder();
            for (String[] arg_info : arg_array) {
                if (arg_info != arg_array[0]) {
                    arg_builder.append("&");
                }
                arg_builder.append(arg_info[0]).append("=").append(arg_info[1]);

                String arg_post;
                try {
                    arg_post = URLDecoder.decode(arg_info[1], "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    arg_post = arg_info[1];
                }
                arg_form.add(arg_info[0], arg_post);
            }
            arg_string = arg_builder.toString();
        }

        String getString(boolean out_sign) {
            String result = arg_string;
            if (out_sign) {
                result = result + "&sign=" + getSign(arg_string);
            }
            return result;
        }

        FormBody getForm(boolean out_sign) {
            FormBody.Builder result = arg_form;
            if (out_sign) {
                result.add("sign", getSign(arg_string));
            }
            return result.build();
        }

        private String getSign(String content) {
            content = content + "59b43e04ad6965f34319062b478f83dd";
            byte[] hash;
            try {
                hash = MessageDigest.getInstance("MD5").digest(content.getBytes());
            } catch (NoSuchAlgorithmException e) {
                return "";
            }
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                if ((b & 0xFF) < 0x10) {
                    hex.append("0");
                }
                hex.append(Integer.toHexString(b & 0xFF));
            }
            return hex.toString();
        }
    }

    public static String getTS() {
        return String.valueOf(System.currentTimeMillis());
    }
}
