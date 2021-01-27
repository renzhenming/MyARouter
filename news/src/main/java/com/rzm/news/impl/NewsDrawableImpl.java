package com.rzm.news.impl;

import com.rzm.arouter_annotations.ARouter;
import com.rzm.library.news.NewsDrawable;
import com.rzm.news.R;

@ARouter(path = "/news/NewsDrawableImpl")
public class NewsDrawableImpl implements NewsDrawable {
    @Override
    public int getDrawable() {
        return R.mipmap.cat;
    }
}
