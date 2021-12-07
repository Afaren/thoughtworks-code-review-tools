// Copyright 2000-2020 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
// lihui change the file start 2021

package net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.controller;

import com.intellij.openapi.options.Configurable;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloBoardLabel;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloBoardLabelState;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloBoardMember;
import com.julienvey.trello.TrelloBadRequestException;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloBoardMemberState;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloConfiguration;
import net.lihui.app.plugin.thoughtworkscodereviewtools.intellij.store.TrelloState;
import net.lihui.app.plugin.thoughtworkscodereviewtools.service.CodeReviewBoardService;
import net.lihui.app.plugin.thoughtworkscodereviewtools.ui.settingView.TwCodeReviewSettingsComponent;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * Provides controller functionality for application settings.
 */
public class TwCodeReviewSettingsConfigurable implements Configurable {

    private TwCodeReviewSettingsComponent twCodeReviewSettingsComponent;

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "Tw Code Review Tools";
    }

    @Override
    public JComponent getPreferredFocusedComponent() {
        return twCodeReviewSettingsComponent.getPreferredFocusedComponent();
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        twCodeReviewSettingsComponent = new TwCodeReviewSettingsComponent();
        return twCodeReviewSettingsComponent.getPanel();
    }

    @Override
    public boolean isModified() {
        TrelloConfiguration trelloConfiguration = TrelloState.getInstance().getState();

        return !trelloConfiguration.equals(twCodeReviewSettingsComponent.getCurrentTrelloConfiguration());
    }

    @Override
    public void apply() {
        TrelloState trelloState = TrelloState.getInstance();
        trelloState.setState(twCodeReviewSettingsComponent.getCurrentTrelloConfiguration());

        try {
            updateBoard();
            twCodeReviewSettingsComponent.setTrelloSettingStatusLabel("OK");
        } catch (TrelloBadRequestException trelloBadRequestException) {
            if (trelloBadRequestException.getMessage().equals("invalid id")) {
                twCodeReviewSettingsComponent.setTrelloSettingStatusLabel("You board id is invalid id, please check it.");
            }
        }
    }

    private void updateBoard() {
        TrelloConfiguration trelloConfiguration = TrelloState.getInstance().getState();
        CodeReviewBoardService codeReviewBoardService = new CodeReviewBoardService(trelloConfiguration);

        List<TrelloBoardMember> trelloBoardMembers = codeReviewBoardService.getTrelloBoardMembers();
        TrelloBoardMemberState boardMemberState = TrelloBoardMemberState.getInstance();
        boardMemberState.updateTrelloBoardMemberList(trelloBoardMembers);

        List<TrelloBoardLabel> trelloBoardLabels = codeReviewBoardService.getTrelloBoardLabels();
        TrelloBoardLabelState boardLabelState = TrelloBoardLabelState.getInstance();
        boardLabelState.updateTrelloBoardLabelList(trelloBoardLabels);
    }

    @Override
    public void reset() {
        TrelloState settings = TrelloState.getInstance();
        TrelloConfiguration trelloConfiguration = settings.getState();

        twCodeReviewSettingsComponent.setTrelloApiKey(trelloConfiguration.getTrelloApiKey());
        twCodeReviewSettingsComponent.setTrelloApiToken(trelloConfiguration.getTrelloApiToken());
        twCodeReviewSettingsComponent.setTrelloBoardId(trelloConfiguration.getTrelloBoardId());
    }

    @Override
    public void disposeUIResources() {
        twCodeReviewSettingsComponent = null;
    }

}
