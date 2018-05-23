package com.example.ngocthai.a1512502_miniproject1;

import java.util.List;

/**
 * Created by Ngoc Thai on 5/15/2018.
 */

public interface DirectionFinderListener {
    void onDirectionFinderStart();
    void onDirectionFinderSuccess(List<Route> route);
}
