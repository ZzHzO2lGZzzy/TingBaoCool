package com.example.tingbaoweather.Bean;

public class Daily_forecast {
    public String date;
    public cond cond;
    public tmp tmp;

    public class cond {
        public String txt_d;
    }

    public class tmp {
        public String max;
        public String min;
    }
}
