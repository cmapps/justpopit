package com.charlesmass.justpopit;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by charlesmass on 29/06/2015.
 */
public class TVAgentOrange extends TextView {

    public TVAgentOrange(Context c, AttributeSet as){
        super(c, as);
        this.setTypeface(Typeface.createFromAsset(c.getAssets(), "fonts/AGENTORANGE.TTF"), Typeface.BOLD);
    }
}
