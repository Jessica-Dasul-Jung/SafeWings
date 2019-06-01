package com.travelous.socialcomponents.main.search.users;

import com.travelous.socialcomponents.main.base.BaseFragmentView;
import com.travelous.socialcomponents.model.Profile;

import java.util.List;

/**
 * Created by Alexey on 08.06.18.
 */
public interface SearchUsersView extends BaseFragmentView {
    void onSearchResultsReady(List<Profile> profiles);

    void showLocalProgress();

    void hideLocalProgress();

    void showEmptyListLayout();

    void updateSelectedItem();
}
