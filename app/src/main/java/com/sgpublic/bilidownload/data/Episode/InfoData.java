package com.sgpublic.bilidownload.data.Episode;

import java.util.Objects;

public class InfoData {
    public static final int PAYMENT_NORMAL = 2;
    public static final int PAYMENT_VIP = 13;

    public String index;

    public long aid;
    public long cid;
    public long ep_id;
    public String cover;
    public String pub_real_time;
    public String title;
    public int payment;
    public String bvid;
    public int area_limit;

    public String badge;
    public int badge_color;
    public int badge_color_night;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InfoData infoData = (InfoData) o;
        return ep_id == infoData.ep_id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ep_id);
    }
}
