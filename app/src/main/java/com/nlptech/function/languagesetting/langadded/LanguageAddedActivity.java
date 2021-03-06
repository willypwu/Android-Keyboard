package com.nlptech.function.languagesetting.langadded;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.inputmethod.latin.R;
import com.android.inputmethod.latin.databinding.ActivityLanguageAddedBinding;
import com.nlptech.Agent;
import com.nlptech.common.utils.PrefUtil;
import com.nlptech.function.languagesetting.LanguageListener;
import com.nlptech.function.languagesetting.LanguageSettingAdapter;
import com.nlptech.function.languagesetting.LanguageSettingViewModel;
import com.nlptech.function.languagesetting.langchooser.LanguageChooserActivity;
import com.nlptech.language.CharsetTable;
import com.nlptech.language.IMELanguage;
import com.nlptech.language.IMELanguageWrapper;
import com.nlptech.language.VertexInputMethodManager;

import java.util.List;

import static com.nlptech.language.VertexInputMethodManager.PREF_CHARSET_TO_LAYOUT_PREFIX;

public class LanguageAddedActivity extends AppCompatActivity implements LanguageListener,
        SelectLayoutSetDialogFragment.SelectLayoutSetListener, MultiLocaleRemoveDialogFragment.MultiLocaleRemoveListener {
    public static final int LAYOUT = R.layout.activity_language_added;

    private ActivityLanguageAddedBinding binding;

    private LanguageSettingAdapter adapter;

    private LanguageSettingViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);

        binding = DataBindingUtil.setContentView(this,LAYOUT);
        viewModel = ViewModelProviders.of(this).get(LanguageSettingViewModel.class);
        viewModel.getAddedResult().observe(this, this::handleResponse);
        adapter = new LanguageSettingAdapter(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        binding.recyclerView.setAdapter(adapter);

        ItemTouchHelperCallback itemTouchHelperCallback = new ItemTouchHelperCallback(adapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(itemTouchHelperCallback);
        touchHelper.attachToRecyclerView(binding.recyclerView);

        binding.addLanguage.setOnClickListener(v -> {
            Intent intent = new Intent(this, LanguageChooserActivity.class);
            startActivity(intent);
        });

        Agent.getInstance().downloadDictionary();
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.obtainList();
    }

    private void handleResponse(List<IMELanguageWrapper> result) {
        adapter.setDataSet(result);
    }

    @Override
    public void onClickAdd(IMELanguageWrapper item) {
        viewModel.addSubtype(item.getIMELanguage());
        Agent.getInstance().downloadDictionary();
    }

    @Override
    public void onClickRemove(IMELanguageWrapper item) {
        viewModel.removeSubtype(item);
        Agent.getInstance().downloadDictionary();
    }

    @Override
    public void onLanguagePositionChanged(int from, int to) {
        viewModel.moveSubtype(from, to);
    }

    @Override
    public void onClickChangeLayoutSet(String charset) {
        String[] layoutSets = CharsetTable.getInstance().obtainLayoutSetFromCharset(charset);
        if (layoutSets == null) {
            return;
        }

        String currentLayoutSet = PrefUtil.getString(this, PREF_CHARSET_TO_LAYOUT_PREFIX + charset, "");
        if (TextUtils.isEmpty(currentLayoutSet)) {
            return;
        }

        showSelectLayoutSetDialogFragment(charset, currentLayoutSet, layoutSets);
    }

    private void showSelectLayoutSetDialogFragment(String charset, String currentLayoutSet, String[] layoutSets) {
        SelectLayoutSetDialogFragment f = SelectLayoutSetDialogFragment.newInstance();
        f.setCharset(charset);
        f.setCurrentLayoutSet(currentLayoutSet);
        f.setLayoutSets(layoutSets);
        f.setSelectLayoutSetListener(this);
        f.show(getSupportFragmentManager(), SelectLayoutSetDialogFragment.DIALOG_FRAGMENT);
    }


    @Override
    public void onLayoutSetChanged(String charset, String newLayout) {
        VertexInputMethodManager.getInstance().onLayoutChangedAndUpdate(charset, newLayout);
        viewModel.obtainList();
    }

    @Override
    public void onLocaleRemoved(IMELanguage subtypeIME) {
        viewModel.removeSubtype(subtypeIME);
        Agent.getInstance().downloadDictionary();
    }
}
